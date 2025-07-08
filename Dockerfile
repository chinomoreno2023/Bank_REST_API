FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/bank-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
