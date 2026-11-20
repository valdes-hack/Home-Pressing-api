# Étape 1 : Build Maven avec Java 21
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
# Téléchargement des dépendances en cache
RUN mvn dependency:go-offline -B || true
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : Image runtime allégée (optimisée pour les 512 Mo de Render gratuit)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/atlantic-palace-pressing-api-*.jar app.jar

ENV PORT=8080
EXPOSE 8080

# Options JVM pour faible consommation mémoire sur Render
ENTRYPOINT ["java", "-XX:+UseSerialGC", "-Xss512k", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
