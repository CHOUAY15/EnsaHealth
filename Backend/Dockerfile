# Utiliser une image de base avec JDK 17
FROM openjdk:17-jdk-slim

# Répertoire de travail dans le conteneur
WORKDIR /app

# Copier le jar généré dans le conteneur
COPY target/*.jar /app/app.jar

# Exposer le port 8081 pour Spring Boot
EXPOSE 8081

# Lancer l'application Spring Boot sur le port 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]