. stenv.sh

if [ -z ${JAVA_HOME} ]; then
  java -cp "slf4j-nop.jar:selenium-java-${SELENIUM_VERSION}/*:selenium-java-${SELENIUM_VERSION}/lib/*:." SimpleTester "$@"
else
  ${JAVA_HOME}/bin/java -cp "slf4j-nop.jar:selenium-java-${SELENIUM_VERSION}/*:selenium-java-${SELENIUM_VERSION}/lib/*:." SimpleTester "$@"
fi
