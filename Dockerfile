FROM java:8

RUN mkdir /opt/vertx

EXPOSE 8080

COPY /build/installShadow/vertx-errors-panel/lib/vertx-errors-panel-1.0-SNAPSHOT-fat.jar /opt/vertx

CMD ["java", "-jar", "/opt/vertx/vertx-errors-panel-1.0-SNAPSHOT-fat.jar"]