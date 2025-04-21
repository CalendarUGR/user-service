FROM amazoncorretto:21-alpine-jdk
WORKDIR /app
EXPOSE 8081
COPY ./target/user-service-0.0.1-SNAPSHOT.jar user-service.jar
COPY .env .env

ENTRYPOINT ["java", "-jar", "user-service.jar"]