SimpleTester is a quickly hacked together Selenium Webdriver wrapper that takes
 a script file and executes it on a webpage. To make the selectors simple, a 
configuration file with unique names for the selectors is included.
The idea is to have a simple scriptable webdriver for a specific webpage by
having a config file definining the selectors needed for a specific element and 
interacting with them by these unique names.
If a selector changes you should only need to change the config file and you 
can have multiple different scripts interacting with the same webpage, reusing
the same config file.

This is mostly a prototype and is subject to change in case it turns out to be
useful. It is not supposed to be a full blown webtester and is only meant to 
cover a lot of the low hanging fruit while still be simple for non-programmers
to automate data-entries.

The requirements to build is:

Java (unknown version, should work with JDK 8)
Selenium Webdriver 4.11.0
Chrome Browser
Linux terminal to use the included script files (write your own for windows)

You can download the selenium webdriver from the webpage 
 ( https://www.selenium.dev/downloads/ ) 
or use the included getdep.sh-script to grab and remove the included src-files

Build the program with "build.sh" (or the content of it, adapted for your OS)
Run it with "run.sh"

The config-file should have the following structure:
ELEMENTNAME [[id|name|cssSelector|tagName|className|linkTest|xpath] "string"]+

The idea is that every element you want to interact with should have a unique 
name, followed by one or more selectors that each have a type and a text.

The script supports the following statements for now:
--
find ELEMENTNAME
select ELEMENTNAME "dropdownoption"
asserttxt ELEMENTNAME "text"
assertval ELEMENTNAME "value"
click ELEMENTNAME
type ELEMENTNAME "text"
waitfor ELEMENTNAME
wait INTEGER
finish
--

Statements in script does the following:

find - returns true if element is found
asserttxt - do getText() on element and check against "text"
assertval  - do getValue() on element and check against "value"
click - click() on element
type -  sendKeys("text") on element
waitfor - does a find, up until 100 times until element is found
wait - just sleeps INTEGER*0.1s
finish - closes the driver and the browser

Both files ignores lines without content or lines starting with '#'.

Example:

$ ./build.sh
$ cat config.txt
message id "message"
textbox name "my-text"
submitButton cssSelector "button"
dropdown name "my-select"
$ cat script.txt 
wait 30
select dropdown "One"
type textbox "my-text"
click submitButton
wait 10
asserttxt message "Received!" 
$ ./run.sh config.txt https://www.selenium.dev/selenium/web/web-form.html script.txt
INFO: Google Chrome 115.0.5790.170
INFO: Using Config config.txt
INFO: Using URL: https://www.selenium.dev/selenium/web/web-form.html
INFO: Using Script: script.txt
SLF4J: No SLF4J providers were found.
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See https://www.slf4j.org/codes.html#noProviders for further details.
SUCCESS: script.txt
$

