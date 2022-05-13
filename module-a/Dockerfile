# syntax=docker/dockerfile:1
FROM openjdk:11-jdk-slim
ARG JAR_FILE
WORKDIR /app
EXPOSE 8081
# COPY ${JAR_FILE} app.jar
COPY . .
RUN find . -depth -name "*SNAPSHOT.jar" -exec sh -c 'f="{}"; mv -- "$f" "app.jar"' \;
RUN ["ls", "-lh"]
CMD ["java", "-jar", "-Dconfig_server_host=conf-server", "-Dsolace_host=solace", "app.jar"]
