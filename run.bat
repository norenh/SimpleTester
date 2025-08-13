@echo off
FOR /F "delims== tokens=1,2" %%x IN (stenv.cfg) DO (SET %%~x=%%~y)

IF DEFINED JAVA_HOME (
  set JAVAJ=%JAVA_HOME%\bin\java
) ELSE (
  set JAVAJ=java
)

"%JAVAJ%" -cp "SimpleTester.jar;slf4j-nop.jar;selenium-java-%SELENIUM_VERSION%\*" SimpleTester %*

