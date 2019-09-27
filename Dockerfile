FROM openjdk:8
ADD target/booking-microservice.jar booking-microservice.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "booking-microservice.jar"]