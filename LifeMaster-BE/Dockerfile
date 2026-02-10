# ---- Build stage ----
FROM gradle:8.10.2-jdk21 AS builder
WORKDIR /app

COPY build.gradle settings.gradle gradlew gradlew.bat /app/
COPY gradle /app/gradle
RUN chmod +x /app/gradlew

COPY src /app/src

RUN ./gradlew clean build -x test

# ---- Run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]