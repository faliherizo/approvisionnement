# Étape de construction (Vite)
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Étape d'exécution (Nginx sert la SPA + proxifie l'API)
FROM nginx:1.27-alpine
# Template envsubst : ${BACKEND_URL} substitué au démarrage (Compose ou Azure).
COPY default.conf.template /etc/nginx/templates/default.conf.template
ENV BACKEND_URL=http://backend:8080
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
