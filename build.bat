@echo off
FOR /F "delims=" %%x IN (stenv.cfg) DO (SET "%%x")

IF DEFINED JAVA_HOME (
  %JAVA_HOME%\bin\javac -cp "slf4j-nop.jar;selenium-java-%SELENIUM_VERSION%\*" -Xlint:unchecked -Xlint:deprecation SimpleTester.java
) ELSE (
  javac -cp "slf4j-nop.jar;selenium-java-%SELENIUM_VERSION%\*" -Xlint:unchecked -Xlint:deprecation SimpleTester.java
)
