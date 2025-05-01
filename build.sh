#!/bin/bash
source stenv.cfg

if [ -z ${JAVA_HOME} ]; then
  javac -cp "slf4j-nop.jar:selenium-java-${SELENIUM_VERSION}/*" -Xlint:unchecked -Xlint:deprecation SimpleTester.java
else
  ${JAVA_HOME}/bin/javac -cp "slf4j-nop.jar:selenium-java-${SELENIUM_VERSION}/*" -Xlint:unchecked -Xlint:deprecation SimpleTester.java
fi

jar cf SimpleTester.jar SimpleTester*.class
rm -f SimpleTester*.class

