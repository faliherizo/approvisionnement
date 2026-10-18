// Infrastructure Azure pour la solution SaaS de rédaction contractuelle.
// Ressources : Log Analytics, ACR, Container Apps Environment, PostgreSQL Flexible,
// Container Apps backend (Spring Boot, profil prod) et frontend (Nginx).
// Déploiement : az deployment group create -g <rg> -f infra/main.bicep -p ...

@description('Préfixe des ressources')
param prefixe string = 'contrat'

@description('Région')
param location string = resourceGroup().location

@description('Nom du registre de conteneurs (ACR)')
param acrName string

@description('Étiquette d image à déployer')
param imageTag string = 'latest'

@secure()
@description('Mot de passe administrateur PostgreSQL')
param dbPassword string

@secure()
@description('Secret de signature JWT (>= 256 bits)')
param jwtSecret string

@description('Origine autorisée du frontend (CORS)')
param frontendOrigin string = 'https://${prefixe}-frontend.azurecontainerapps.io'

var dbAdmin = 'contrat'
var dbName = 'contrat'

resource logs 'Microsoft.OperationalInsights/workspaces@2023-09-01' = {
  name: '${prefixe}-logs'
  location: location
  properties: { sku: { name: 'PerGB2018' }, retentionInDays: 30 }
}

resource acr 'Microsoft.ContainerRegistry/registries@2023-11-01-preview' = {
  name: acrName
  location: location
  sku: { name: 'Basic' }
  properties: { adminUserEnabled: true }
}

resource postgres 'Microsoft.DBforPostgreSQL/flexibleServers@2023-06-01-preview' = {
  name: '${prefixe}-pg'
  location: location
  sku: { name: 'Standard_B1ms', tier: 'Burstable' }
  properties: {
    version: '15'
    administratorLogin: dbAdmin
    administratorLoginPassword: dbPassword
    storage: { storageSizeGB: 32 }
    highAvailability: { mode: 'Disabled' }
  }
}

resource pgDb 'Microsoft.DBforPostgreSQL/flexibleServers/databases@2023-06-01-preview' = {
  parent: postgres
  name: dbName
}

resource pgFirewall 'Microsoft.DBforPostgreSQL/flexibleServers/firewallRules@2023-06-01-preview' = {
  parent: postgres
  name: 'AllowAzureServices'
  properties: { startIpAddress: '0.0.0.0', endIpAddress: '0.0.0.0' }
}

resource caEnv 'Microsoft.App/managedEnvironments@2024-03-01' = {
  name: '${prefixe}-env'
  location: location
  properties: {
    appLogsConfiguration: {
      destination: 'log-analytics'
      logAnalyticsConfiguration: {
        customerId: logs.properties.customerId
        sharedKey: logs.listKeys().primarySharedKey
      }
    }
  }
}

// Backend Spring Boot (profil prod → PostgreSQL, secrets injectés)
resource backend 'Microsoft.App/containerApps@2024-03-01' = {
  name: '${prefixe}-backend'
  location: location
  properties: {
    managedEnvironmentId: caEnv.id
    configuration: {
      ingress: { external: true, targetPort: 8080 }
      registries: [ { server: '${acr.name}.azurecr.io', username: acr.name, passwordSecretRef: 'acr-pwd' } ]
      secrets: [
        { name: 'acr-pwd', value: acr.listCredentials().passwords[0].value }
        { name: 'db-password', value: dbPassword }
        { name: 'jwt-secret', value: jwtSecret }
      ]
    }
    template: {
      containers: [
        {
          name: 'backend'
          image: '${acr.name}.azurecr.io/${prefixe}-backend:${imageTag}'
          resources: { cpu: json('1.0'), memory: '2Gi' }
          env: [
            { name: 'SPRING_PROFILES_ACTIVE', value: 'prod' }
            { name: 'DB_URL', value: 'jdbc:postgresql://${postgres.properties.fullyQualifiedDomainName}:5432/${dbName}?sslmode=require' }
            { name: 'DB_USER', value: dbAdmin }
            { name: 'DB_PASSWORD', secretRef: 'db-password' }
            { name: 'JWT_SECRET', secretRef: 'jwt-secret' }
            { name: 'APP_CORS_ALLOWED_ORIGINS', value: frontendOrigin }
            { name: 'COOKIE_SECURE', value: 'true' }
            { name: 'COOKIE_SAMESITE', value: 'None' }
          ]
        }
      ]
      scale: { minReplicas: 1, maxReplicas: 5 }
    }
  }
}

// Frontend Nginx (SPA + proxy /api → backend)
resource frontend 'Microsoft.App/containerApps@2024-03-01' = {
  name: '${prefixe}-frontend'
  location: location
  properties: {
    managedEnvironmentId: caEnv.id
    configuration: {
      ingress: { external: true, targetPort: 80 }
      registries: [ { server: '${acr.name}.azurecr.io', username: acr.name, passwordSecretRef: 'acr-pwd' } ]
      secrets: [ { name: 'acr-pwd', value: acr.listCredentials().passwords[0].value } ]
    }
    template: {
      containers: [
        {
          name: 'frontend'
          image: '${acr.name}.azurecr.io/${prefixe}-frontend:${imageTag}'
          resources: { cpu: json('0.5'), memory: '1Gi' }
        }
      ]
      scale: { minReplicas: 1, maxReplicas: 3 }
    }
  }
}

output backendUrl string = 'https://${backend.properties.configuration.ingress.fqdn}'
output frontendUrl string = 'https://${frontend.properties.configuration.ingress.fqdn}'
