. stenv.sh
wget "https://github.com/SeleniumHQ/selenium/releases/download/selenium-${SELENIUM_BASE_VERSION}/selenium-java-${SELENIUM_VERSION}.zip"
unzip "selenium-java-${SELENIUM_VERSION}.zip" -d "selenium-java-${SELENIUM_VERSION}"
chmod u+w selenium-java-${SELENIUM_VERSION}/*-sources.jar
rm selenium-java-${SELENIUM_VERSION}/*-sources.jar
