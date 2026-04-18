#!/bin/sh
# Gradle wrapper script for PushGram
# APP_HOME = directory containing this script (the project root)
APP_HOME=$(cd "$(dirname "$0")" && pwd)
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

# Validate wrapper jar exists
if [ ! -f "$CLASSPATH" ]; then
  echo "ERROR: Gradle wrapper jar not found at $CLASSPATH" >&2
  exit 1
fi

# Find Java
if [ -n "$JAVA_HOME" ]; then
  JAVACMD="$JAVA_HOME/bin/java"
else
  JAVACMD="java"
fi

exec "$JAVACMD" \
  ${JAVA_OPTS:-} \
  -classpath "$CLASSPATH" \
  org.gradle.wrapper.GradleWrapperMain \
  "$@"
