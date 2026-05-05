FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/recompense-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8094
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
