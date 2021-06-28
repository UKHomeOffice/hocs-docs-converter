#!/usr/bin/env bash

NAME=${NAME:-docs}

JAR=$(find . -name ${NAME}*.jar|head -1)
exec /usr/local/java/bin/java ${JAVA_OPTS} -Dcom.sun.management.jmxremote.local.only=false -Djava.security.egd=file:/dev/./urandom -jar "${JAR}"
