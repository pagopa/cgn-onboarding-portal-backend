FROM eclipse-temurin:17.0.9_9-jre-alpine as builder

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE}  application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM eclipse-temurin:17.0.9_9-jre-alpine

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ADD --chown=spring:spring https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.5.2/applicationinsights-agent-3.5.2.jar /applicationinsights-agent.jar
COPY --chown=spring:spring docker/applicationinsights.json ./applicationinsights.json

COPY --chown=spring:spring  --from=builder dependencies/ ./
COPY --chown=spring:spring  --from=builder snapshot-dependencies/ ./
# https://github.com/moby/moby/issues/37965#issuecomment-426853382
RUN true
COPY --chown=spring:spring  --from=builder spring-boot-loader/ ./
COPY --chown=spring:spring  --from=builder application/ ./

EXPOSE 8080

COPY --chown=spring:spring  docker/run.sh ./run.sh
ENTRYPOINT ["./run.sh"]
