CHROME_VERSION=$(google-chrome --version)
echo "INFO: $CHROME_VERSION"
java -cp "slf4j-nop.jar:selenium-java-4.19.1/*:selenium-java-4.19.1/lib/*:." SimpleTester "$@"
