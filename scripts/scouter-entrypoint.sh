#!/bin/sh

exec java -javaagent:/app/scouter/agent.java/scouter.agent.jar \
  -Dscouter.config=/app/scouter/conf/scouter.conf \
  -jar /app/app.jar
