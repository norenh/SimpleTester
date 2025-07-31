@echo off
FOR /F "delims=" %%x IN (stenv.cfg) DO (SET "%%x")

IF DEFINED JAVA_HOME (
  set JAVAJ="%JAVA_HOME%\bin\java"
) ELSE (
  set JAVAJ="java"
)

"%JAVAJ%" -cp "SimpleTester.jar;slf4j-nop.jar;selenium-java-%SELENIUM_VERSION%\*" SimpleTester %*

