# syntax=docker/dockerfile:1
FROM openjdk:11-jdk-slim
ARG JAR_FILE
WORKDIR /app
#EXPOSE 8080
COPY ${JAR_FILE} module-a.jar
RUN ["ls", "-lh"]
CMD ["java", "-jar", "-Dconfig_server_host=conf-server", "-Dsolace_host=solace", "module-a.jar"]
