#!/bin/bash
source stenv.cfg

LIBDIR="selenium-java-${SELENIUM_VERSION}"

if [ -d "${LIBDIR}" ]; then
  echo "You already got ${LIBDIR}!"
  exit 0
fi

curl -L --output "${LIBDIR}.zip" "https://github.com/SeleniumHQ/selenium/releases/download/selenium-${SELENIUM_BASE_VERSION}/selenium-java-${SELENIUM_VERSION}.zip"
unzip -o "${LIBDIR}.zip" -d "${LIBDIR}"

if [ -d "${LIBDIR}" ]; then
  chmod u+w "${LIBDIR}"/*-sources.jar
  rm -f "${LIBDIR}"/*-sources.jar
else
  echo "Something went wrong, cannot find ${LIBDIR}"
  exit 1
fi

