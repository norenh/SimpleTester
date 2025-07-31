#!/bin/bash
source stenv.cfg

if [ -z "${JAVA_HOME}" ]; then
  JAVAC="javac"
  JAVAP="jar"
else
  JAVAC="${JAVA_HOME}/bin/javac"
  JAVAP="${JAVA_HOME}/bin/jar"
fi

"${JAVAC}" -cp "slf4j-nop.jar:selenium-java-${SELENIUM_VERSION}/*" -Xlint:unchecked -Xlint:deprecation SimpleTester.java

"${JAVAP}" cf SimpleTester.jar SimpleTester*.class
rm -f SimpleTester*.class

