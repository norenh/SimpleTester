#!/bin/bash
source stenv.cfg

if [ -z ${JAVA_HOME} ]; then
  java -cp "SimpleTester.jar:slf4j-nop.jar:selenium-java-${SELENIUM_VERSION}/*" SimpleTester "$@"
else
  ${JAVA_HOME}/bin/java -cp "SimpleTester.jar:slf4j-nop.jar:selenium-java-${SELENIUM_VERSION}/*" SimpleTester "$@"
fi
