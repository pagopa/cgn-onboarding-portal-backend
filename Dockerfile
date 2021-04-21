FROM adoptopenjdk/openjdk11:alpine-jre as builder

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE}  application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM adoptopenjdk/openjdk11:alpine-jre

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ADD --chown=spring:spring https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.0.3/applicationinsights-agent-3.0.3.jar /applicationinsights-agent-3.0.3.jar

COPY --chown=spring:spring  --from=builder dependencies/ ./
COPY --chown=spring:spring  --from=builder snapshot-dependencies/ ./
# https://github.com/moby/moby/issues/37965#issuecomment-426853382
RUN true
COPY --chown=spring:spring  --from=builder spring-boot-loader/ ./
COPY --chown=spring:spring  --from=builder application/ ./

EXPOSE 8080

ENTRYPOINT ["java", "-javaagent:/applicationinsights-agent-3.0.3.jar", "org.springframework.boot.loader.JarLauncher"]