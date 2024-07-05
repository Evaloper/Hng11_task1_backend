FROM openjdk:21
COPY target/*.jar HNG-task-one-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/HNG-task-one-0.0.1-SNAPSHOT.jar"]