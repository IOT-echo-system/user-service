FROM gradle:8.5.0-jdk21

WORKDIR /app

COPY build/libs/user-service-0.0.1.jar ./app.jar

CMD ["java", "-jar", "app.jar"]
