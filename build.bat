@echo off
FOR /F "delims== tokens=1,2" %%x IN (stenv.cfg) DO (SET %%~x=%%~y)

IF DEFINED JAVA_HOME (
  set JAVAC=%JAVA_HOME%\bin\javac
  set JAVAP=%JAVA_HOME%\bin\jar
) ELSE (
  set JAVAC=javac
  set JAVAP=jar
)

cd src

"%JAVAC%" -cp "..\lib\slf4j-nop.jar;..\lib\selenium-java-%SELENIUM_VERSION%\*" -Xlint:unchecked -Xlint:deprecation SimpleTester.java

"%JAVAP%" cf "..\lib\SimpleTester.jar" SimpleTester*.class
del *.class

cd ..

