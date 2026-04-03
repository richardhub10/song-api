# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Package stage
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*-SNAPSHOT.jar app.jar
EXPOSE 8080
# Render environments may not have IPv6 routing; Supabase hostnames can resolve to IPv6.
# Prefer IPv4 to avoid "Network unreachable" during DB connection.
ENTRYPOINT ["java", "-Djava.net.preferIPv4Stack=true", "-Djava.net.preferIPv6Addresses=false", "-jar", "app.jar"]