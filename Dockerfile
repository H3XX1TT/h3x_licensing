FROM eclipse-temurin:21-jre

WORKDIR /app
COPY target/h3x_licensing-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]

