FROM gradle:7.6.0-jdk17 AS build
COPY . /app
WORKDIR /app
RUN gradle build -x test

FROM openjdk:17
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]