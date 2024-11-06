FROM gradle:8.10.0-jdk21-alpine

WORKDIR /app

COPY build/libs/user-service-0.0.1.jar ./app.jar

CMD ["java", "-jar", "app.jar"]
