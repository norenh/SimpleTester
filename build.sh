. stenv.sh

if [ -z ${JAVA_HOME} ]; then
  javac -cp "slf4j-nop.jar:selenium-java-${SELENIUM_VERSION}/*:selenium-java-${SELENIUM_VERSION}/lib/*" -Xlint:unchecked -Xlint:deprecation SimpleTester.java
else
  ${JAVA_HOME}/bin/javac -cp "slf4j-nop.jar:selenium-java-${SELENIUM_VERSION}/*:selenium-java-${SELENIUM_VERSION}/lib/*" -Xlint:unchecked -Xlint:deprecation SimpleTester.java
fi
