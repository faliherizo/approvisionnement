# Étape de construction
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

# Étape d'exécution
FROM eclipse-temurin:17-jre
# La conversion Word → PDF/A est confiée au service « conversion » (Gotenberg, LibreOffice isolé ; ADR-018).
# Le serveur s'exécute sous un compte dédié sans privilège : seuls l'application et son journal lui appartiennent.
RUN groupadd --system --gid 10001 contrat && useradd --system --uid 10001 --gid contrat --no-create-home contrat \
    && mkdir -p /app /var/log/contrat && chown -R contrat:contrat /app /var/log/contrat
WORKDIR /app
COPY --from=build --chown=contrat:contrat /app/target/contrat-backend-0.1.0.jar app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
