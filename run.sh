#!/bin/bash
source stenv.cfg

if [ -z "${JAVA_HOME}" ]; then
  JAVAJ=java
else
  JAVAJ="${JAVA_HOME}/bin/java"
fi

"${JAVAJ}" -cp "lib/SimpleTester.jar:lib/slf4j-nop.jar:lib/selenium-java-${SELENIUM_VERSION}/*" SimpleTester "$@"

