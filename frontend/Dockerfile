# Étape de construction (Vite)
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Étape d'exécution (Nginx sert la SPA + proxifie l'API). Image officielle non privilégiée : nginx s'exécute sans
# root (utilisateur 101) et écoute sur 8080 ; une faille du serveur web ne donne pas la main sur le conteneur.
FROM nginxinc/nginx-unprivileged:1.29-alpine
# Correctifs de sécurité des paquets Alpine publiés depuis la construction de l'image de base (porte Trivy du déploiement).
USER root
RUN apk upgrade --no-cache
# Template envsubst : ${BACKEND_URL} substitué au démarrage (Compose ou Azure).
COPY default.conf.template /etc/nginx/templates/default.conf.template
ENV BACKEND_URL=http://backend:8080
# Origine du fournisseur du défi anti-robot admise par la politique de contenu ; vide : aucune (ADR-026).
ENV CSP_SOURCES_CAPTCHA=""
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 8080
USER 101
