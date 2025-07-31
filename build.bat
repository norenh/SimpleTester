@echo off
FOR /F "delims=" %%x IN (stenv.cfg) DO (SET "%%x")

IF DEFINED JAVA_HOME (
  set JAVAC="%JAVA_HOME%\bin\javac"
  set JAVAP="%JAVA_HOME%\bin\jar"
) ELSE (
  set JAVAC="javac"
  set JAVAP="jar"
)

%JAVAC% -cp "slf4j-nop.jar;selenium-java-%SELENIUM_VERSION%\*" -Xlint:unchecked -Xlint:deprecation SimpleTester.java

%JAVAP% cf SimpleTester.jar SimpleTester*.class
del *.class

