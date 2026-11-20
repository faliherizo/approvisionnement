# Étape de construction
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

# Étape d'exécution
FROM eclipse-temurin:17-jre
# LibreOffice sans interface : conversion des contrats issus d'un gabarit Word en PDF/A, mise en page conservée
# (addenda 4, Q22). Polices Liberation et DejaVu : métriques compatibles avec Arial, Times et Calibri.
RUN apt-get update \
    && apt-get install -y --no-install-recommends libreoffice-writer-nogui fonts-liberation fonts-dejavu-core \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/target/contrat-backend-0.1.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
