@echo off
FOR /F "delims=" %%x IN (stenv.cfg) DO (SET "%%x")

IF DEFINED JAVA_HOME (
  %JAVA_HOME%\bin\java -cp "SimpleTester.jar;slf4j-nop.jar;selenium-java-%SELENIUM_VERSION%\*" SimpleTester %*
) ELSE (
  java -cp "SimpleTester.jar;slf4j-nop.jar;selenium-java-%SELENIUM_VERSION%\*" SimpleTester %*
)

