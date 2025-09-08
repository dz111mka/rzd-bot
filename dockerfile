FROM eclipse-temurin:21-jdk-jammy

COPY ./build/libs/rzdbot-1.0.0.jar ./rzdbot-1.0.0.jar

ENV SPRING_PROFILES_ACTIVE=docker
ENV TZ=Europe/Moscow

EXPOSE 8080
EXPOSE 9010

ENTRYPOINT ["java", \
    "-Dcom.sun.management.jmxremote=true", \
    "-Dcom.sun.management.jmxremote.port=9010", \
    "-Dcom.sun.management.jmxremote.local.only=false", \
    "-Dcom.sun.management.jmxremote.authenticate=false", \
    "-Dcom.sun.management.jmxremote.ssl=false", \
    "-Dcom.sun.management.jmxremote.rmi.port=9010", \
    "-Djava.rmi.server.hostname=localhost", \
    "-XX:+HeapDumpOnOutOfMemoryError", \
    "-XX:MinRAMPercentage=40", \
    "-XX:MaxRAMPercentage=40", \
    "-noverify", \
    "-jar", \
    "./rzdbot-1.0.0.jar"]