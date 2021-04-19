FROM adoptopenjdk/openjdk11:alpine-jre
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
ADD --chown=spring:spring https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.0.3/applicationinsights-agent-3.0.3.jar /applicationinsights-agent-3.0.3.jar
ARG JAR_FILE=target/*.jar
COPY --chown=spring:spring ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-javaagent:/applicationinsights-agent-3.0.3.jar", "-jar","/app.jar"]