# Azure Front Door STANDARD : point d'entrée mondial redondé, TLS géré, routage et protection
# anti-DDoS de plateforme. Le pare-feu applicatif OWASP (Core Rule Set) est assuré à l'INGRESS
# (ModSecurity/Coraza devant les Container Apps), ce qui évite le coût fixe du WAF managé de
# Front Door Premium (~5 489 $/an, présence horaire) tout en conservant la protection OWASP.
# Voir docs/trois-riviere/AZURE-VS-AWS-PRIX-REELS-ET-DECISION.md (option C).
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

resource "azurerm_cdn_frontdoor_origin_group" "aca" {
  name                     = "aca-ingress"
  cdn_frontdoor_profile_id = azurerm_cdn_frontdoor_profile.this.id
  load_balancing {
    sample_size                 = 4
    successful_samples_required = 3
  }
  health_probe {
    path                = "/actuator/health"
    protocol            = "Https"
    interval_in_seconds = 30
    request_type        = "GET"
  }
}
