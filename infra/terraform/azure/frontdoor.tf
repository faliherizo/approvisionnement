# Azure Front Door STANDARD : point d'entrée mondial redondé, TLS géré, routage et protection
# anti-DDoS de plateforme. Le pare-feu applicatif OWASP (Core Rule Set) est assuré à l'INGRESS
# (ModSecurity/Coraza sur le contrôleur d'ingress Kubernetes), ce qui évite le coût fixe du WAF
# managé de Front Door Premium (~5 489 $/an, présence horaire) tout en conservant la protection
# OWASP. Voir docs/trois-riviere/AZURE-VS-AWS-PRIX-REELS-ET-DECISION.md (option C).
resource "azurerm_cdn_frontdoor_profile" "this" {
  name                = "${local.name}-afd"
  resource_group_name = azurerm_resource_group.this.name
  sku_name            = "Standard_AzureFrontDoor"
  tags                = local.common_tags
}

resource "azurerm_cdn_frontdoor_endpoint" "this" {
  name                     = "${local.name}-ep"
  cdn_frontdoor_profile_id = azurerm_cdn_frontdoor_profile.this.id
}

# Groupe d'origines actif-passif (ADR-039) : la sonde lit la santé publique du serveur (/api/health, sans donnée d'IQ).
# Trois échecs sur quatre sondes retirent une origine ; le trafic passe alors à l'origine de priorité 2 dès qu'elle
# répond, c'est-à-dire une fois la région de relève promue et agrandie.
resource "azurerm_cdn_frontdoor_origin_group" "aks" {
  name                     = "aks-ingress"
  cdn_frontdoor_profile_id = azurerm_cdn_frontdoor_profile.this.id
  session_affinity_enabled = false
  load_balancing {
    sample_size                        = 4
    successful_samples_required        = 3
    additional_latency_in_milliseconds = 0
  }
  health_probe {
    path                = "/api/health"
    protocol            = "Https"
    interval_in_seconds = 30
    request_type        = "GET"
  }
}

resource "azurerm_cdn_frontdoor_origin" "primaire" {
  name                           = "aks-primaire"
  cdn_frontdoor_origin_group_id  = azurerm_cdn_frontdoor_origin_group.aks.id
  enabled                        = true
  host_name                      = var.origine_primaire_hote
  origin_host_header             = var.origine_primaire_hote
  priority                       = 1
  weight                         = 1000
  certificate_name_check_enabled = true
}

resource "azurerm_cdn_frontdoor_origin" "secours" {
  count                          = var.origine_secours_hote == null ? 0 : 1
  name                           = "aks-secours"
  cdn_frontdoor_origin_group_id  = azurerm_cdn_frontdoor_origin_group.aks.id
  enabled                        = true
  host_name                      = var.origine_secours_hote
  origin_host_header             = var.origine_secours_hote
  priority                       = 2
  weight                         = 1000
  certificate_name_check_enabled = true
}

# Nom d'hôte public, certificat géré par Front Door : il ne change pas à la bascule (seule l'origine change).
resource "azurerm_cdn_frontdoor_custom_domain" "app" {
  name                     = replace(var.app_hostname, ".", "-")
  cdn_frontdoor_profile_id = azurerm_cdn_frontdoor_profile.this.id
  host_name                = var.app_hostname
  tls {
    certificate_type = "ManagedCertificate"
    minimum_version  = "TLS12"
  }
}

resource "azurerm_cdn_frontdoor_route" "app" {
  name                            = "app"
  cdn_frontdoor_endpoint_id       = azurerm_cdn_frontdoor_endpoint.this.id
  cdn_frontdoor_origin_group_id   = azurerm_cdn_frontdoor_origin_group.aks.id
  cdn_frontdoor_origin_ids        = concat([azurerm_cdn_frontdoor_origin.primaire.id], azurerm_cdn_frontdoor_origin.secours[*].id)
  cdn_frontdoor_custom_domain_ids = [azurerm_cdn_frontdoor_custom_domain.app.id]
  supported_protocols             = ["Http", "Https"]
  patterns_to_match               = ["/*"]
  forwarding_protocol             = "HttpsOnly"
  https_redirect_enabled          = true
  link_to_default_domain          = true
}
