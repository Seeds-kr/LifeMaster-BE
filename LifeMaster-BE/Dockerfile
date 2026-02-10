# =========================
# 1. Build stage
# =========================
FROM gradle:8.10.2-jdk21 AS builder
WORKDIR /app

COPY build.gradle* settings.gradle* gradlew gradlew.bat ./
COPY gradle ./gradle
RUN chmod +x ./gradlew

# (있으면) gradle.properties 같은 것도 캐시 효율에 도움
COPY gradle.properties* ./

# 소스
COPY src ./src

# bootJar만 생성 (plain.jar 문제 줄임)
RUN ./gradlew clean bootJar -x test


# =========================
# 2. Run stage
# =========================
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/build/libs/*-SNAPSHOT.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
