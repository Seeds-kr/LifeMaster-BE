# =========================
# 1. Build stage
# =========================
FROM gradle:8.10.2-jdk21 AS builder

WORKDIR /app

# Gradle 캐시 활용
COPY build.gradle settings.gradle gradlew gradlew.bat ./
COPY gradle ./gradle
RUN chmod +x ./gradlew

# 소스 복사
COPY src ./src

# 빌드 (테스트 제외)
RUN ./gradlew clean build -x test


# =========================
# 2. Run stage
# =========================
FROM eclipse-temurin:21-jre

WORKDIR /app

# 빌드된 jar 복사
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
