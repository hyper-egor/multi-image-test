# syntax=docker/dockerfile:1
FROM lpicanco/java11-alpine
ARG JAR_FILE
WORKDIR /app
EXPOSE 8080
# COPY ${JAR_FILE} app.jar
COPY . .
RUN find . -depth -name "*SNAPSHOT.jar" -exec sh -c 'f="{}"; mv -- "$f" "app.jar"' \;
RUN ["ls", "-lh"]
CMD ["java", "-jar", "app.jar"]
