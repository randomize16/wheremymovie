FROM openjdk:8-jdk-alpine

COPY target/videosearcher-1.0-SNAPSHOT.jar app.jar
COPY files/google-project.json /google-project.json
ENV GOOGLE_APPLICATION_CREDENTIALS /google-project.json
ENV TELEGRAM_TOKEN 480509063:AAHdawE1WKJo5b0W8s9-jr2TZfot0iwTUvo
ENTRYPOINT ["/usr/bin/java"]
CMD ["-jar", "/app.jar"]
