# Pre-built JAR stage — build locally with:
#   JAVA_HOME=/home/ubuntu/.jdk/jdk-25.0.2 ./mvnw clean package -Pproduction -DskipTests
# Then rebuild the image: docker compose build app

FROM mcr.microsoft.com/openjdk/jdk:25-ubuntu

WORKDIR /app

# Copy the pre-built production JAR
COPY target/qr-seat-reservation-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
