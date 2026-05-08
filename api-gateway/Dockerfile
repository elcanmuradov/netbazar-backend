FROM gradle:8.12.1-jdk21-alpine AS build
WORKDIR /app

# Copy build files and source code
COPY build.gradle .
COPY settings.gradle .
COPY src src

# Build the executable jar using the Gradle image
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache wget
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
