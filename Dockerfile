# Wybór obrazu JDK 21 jako podstawy
FROM openjdk:21-jdk

# Ustawienie katalogu roboczego w kontenerze
WORKDIR /app

# Skopiowanie pliku JAR do kontenera
COPY target/complaint-api-0.0.1-SNAPSHOT.jar /app/complaint-api-0.0.1-SNAPSHOT.jar

# Uruchomienie aplikacji
ENTRYPOINT ["java", "-jar", "/app/complaint-api-0.0.1-SNAPSHOT.jar"]