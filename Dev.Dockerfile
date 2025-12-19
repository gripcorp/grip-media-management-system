FROM eclipse-temurin:21-jre

RUN adduser --disabled-password --gecos '' grip

ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# JMX & OpenTelemetry
COPY addons/jmx_prometheus_javaagent-0.17.2.jar /home/grip/
COPY addons/jmx_exporter_config.yaml /home/grip/
COPY addons/opentelemetry-javaagent.jar /home/grip/

# Datadog APM
RUN mkdir /home/grip/datadog
ADD 'https://dtdg.co/latest-java-tracer' /home/grip/datadog/dd-java-agent.jar

# Scripts & Application
COPY scripts /home/grip/scripts
COPY build/libs/grip-media-system-*-SNAPSHOT.jar /home/grip/deploy/grip-media-system.jar
RUN chmod +x /home/grip/scripts/startup.sh

EXPOSE 8090

ARG phase
ENV SPRING_PROFILES_ACTIVE=$phase
ENTRYPOINT ["/home/grip/scripts/startup.sh", "grip-media-system.jar"]
