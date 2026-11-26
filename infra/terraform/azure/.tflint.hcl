# Analyse statique Terraform (workflow releve.yml, job « infrastructure ») : règles du langage et règles azurerm
# (SKU, tailles de VM, versions inexistantes).
config {
  call_module_type = "local"
}

plugin "terraform" {
  enabled = true
  preset  = "recommended"
}

plugin "azurerm" {
  enabled = true
  version = "0.27.0"
  source  = "github.com/terraform-linters/tflint-ruleset-azurerm"
}
