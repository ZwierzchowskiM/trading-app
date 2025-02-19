# Stage 1: Build the application
FROM openjdk:21-jdk AS build
WORKDIR /app
COPY pom.xml .
COPY src src
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

# Stage 2: Create the final Docker image
FROM openjdk:21-jdk-slim
WORKDIR /app
VOLUME /tmp


# Kopiowanie skompilowanej aplikacji
COPY --from=build /app/target/price-tracker-0.0.1-SNAPSHOT.jar app.jar

# Resetowanie ENTRYPOINT i CMD
ENTRYPOINT []
CMD ["java", "-jar", "app.jar"]

# Eksponowanie portu
EXPOSE 8080