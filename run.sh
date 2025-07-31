#!/bin/bash
source stenv.cfg

if [ -z "${JAVA_HOME}" ]; then
  JAVAJ=java
else
  JAVAJ="${JAVA_HOME}/bin/java"
fi

"${JAVAJ}" -cp "SimpleTester.jar:slf4j-nop.jar:selenium-java-${SELENIUM_VERSION}/*" SimpleTester "$@"

