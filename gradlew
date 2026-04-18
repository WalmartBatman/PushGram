#!/bin/sh
set -e

# Resolve the absolute path of this script, following symlinks
PRG="$0"
while [ -h "$PRG" ]; do
  ls=$(ls -ld "$PRG")
  link=$(expr "$ls" : '.*-> \(.*\)')
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=$(dirname "$PRG")/"$link"
  fi
done
APP_HOME=$(cd "$(dirname "$PRG")" && pwd)

WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

# Debug: print resolved paths
echo "APP_HOME=$APP_HOME"
echo "WRAPPER_JAR=$WRAPPER_JAR"
echo "JAR exists: $(test -f "$WRAPPER_JAR" && echo YES || echo NO)"

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "ERROR: gradle-wrapper.jar not found at $WRAPPER_JAR" >&2
  exit 1
fi

# Find Java
if [ -n "$JAVA_HOME" ]; then
  JAVACMD="$JAVA_HOME/bin/java"
else
  JAVACMD="java"
fi

echo "JAVACMD=$JAVACMD"
echo "JAVA_HOME=$JAVA_HOME"

exec "$JAVACMD" \
  -classpath "$WRAPPER_JAR" \
  org.gradle.wrapper.GradleWrapperMain \
  "$@"
