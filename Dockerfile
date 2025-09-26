FROM openjdk:17-ea-33-jdk-buster

COPY target/*.jar app.jar
COPY imgs/ /imgs
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]