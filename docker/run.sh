#!/bin/sh

exec java -javaagent:/applicationinsights-agent-3.0.3.jar ${JAVA_OPTS} org.springframework.boot.loader.JarLauncher "$@"
