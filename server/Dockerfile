FROM openjdk:8
VOLUME /tmp
ADD target/server-0.0.1-SNAPSHOT.jar server-0.0.1-SNAPSHOT.jar
ADD src/main/resources/application.properties application.properties
EXPOSE 8080
ENTRYPOINT ["java","-jar","server-0.0.1-SNAPSHOT.jar", "--spring.config.location=\"file:application.properties\""]
