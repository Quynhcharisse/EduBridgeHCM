FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom first to leverage Docker layer caching
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Copy source and build jar
COPY src ./src
RUN mvn -q -DskipTests clean package

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Render provides PORT env var; default to 8080 for local runs
ENV PORT=8080

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]
