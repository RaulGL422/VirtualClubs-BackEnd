# Etapa 1: Compilar con Maven
FROM maven:3.9.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true

# Etapa 2: Ejecutar el JAR
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render usará la variable PORT automáticamente
ENV PORT=4584
EXPOSE 4584

ENTRYPOINT ["java", "-jar", "app.jar"]