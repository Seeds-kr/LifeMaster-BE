FROM bellsoft/liberica-openjdk-alpine:21
# or
# FROM openjdk:8-jdk-alpine
# FROM openjdk:11-jdk-alpine

CMD ["./gradlew", "clean", "build"]
# or Maven
# CMD ["./mvnw", "clean", "package"]

VOLUME /tmp

ARG JAR_FILE=build/libs/LifeMaster-BE-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} app.jar

EXPOSE 8081

ENTRYPOINT ["java","-jar","/app.jar"]