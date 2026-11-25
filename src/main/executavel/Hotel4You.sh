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
exec "$JAVA" -jar Hotel4You.jar
