#!/bin/sh
cd "$(dirname "$0")" || exit 1
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    JAVA="$JAVA_HOME/bin/java"
elif command -v java >/dev/null 2>&1; then
    JAVA=java
else
    echo "Nao foi encontrado o Java. Instale o Java 17 ou superior e volte a tentar."
    exit 1
fi
JAR=Hotel4You.jar
if "$JAVA" -XshowSettings:properties -version 2>&1 | grep -q "os.arch = aarch64"; then
    JAR=Hotel4You-mac-arm.jar
fi
exec "$JAVA" -jar "$JAR"
