# Etapa 1: Build
FROM eclipse-temurin:24-jdk AS build

RUN apt-get update && apt-get install -y maven
WORKDIR /app

COPY pom.xml ./
COPY src ./src

RUN mvn clean package -DskipTests

# Etapa 2: Runtime
FROM eclipse-temurin:24-jre

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]