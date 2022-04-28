# syntax=docker/dockerfile:1
## JDK from lpicanco/java11-alpine triggers problems with https access from container to out url's
# FROM lpicanco/java11-alpine
FROM openjdk:11-jdk-slim
ARG JAR_FILE
WORKDIR /app
EXPOSE 8888
COPY . .
RUN find . -depth -name "*SNAPSHOT.jar" -exec sh -c 'f="{}"; mv -- "$f" "app.jar"' \;
RUN ["ls", "-lh"]
CMD ["java", "-jar", "app.jar"]