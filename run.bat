@echo off
FOR /F "delims== tokens=1,2" %%x IN (stenv.cfg) DO (SET %%~x=%%~y)

IF DEFINED JAVA_HOME (
  set JAVAJ=%JAVA_HOME%\bin\java
) ELSE (
  set JAVAJ=java
)

"%JAVAJ%" -cp "lib\SimpleTester.jar;lib\slf4j-nop.jar;lib\selenium-java-%SELENIUM_VERSION%\*" SimpleTester %*

