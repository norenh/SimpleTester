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

Java (unknown version, should work with JDK 17 for latest selenium)
Selenium Webdriver 4.35.0 (change stenv.cfg to set intended version)
Linux terminal to use the included script files (write your own for windows)

You can download the selenium webdriver from the webpage 
 ( https://www.selenium.dev/downloads/ ) 
or use the included getdep.sh-script to grab and remove the included src-files

Build the program with "build.sh" (or the content of it, adapted for your OS)
Run it with "run.sh"

Arguments supported are (not all are valid for all browsers):

 -e filename used for screenshot if script fails
 -h for running in headless mode (only firefox and chrome and edge supported)
 -p for browser window persistance after script finishes. In headless mode, does nothing
 -b [firefox|chrome|safari|edge] sets the browser to use
 -f path to browser (only needed if not in path, safari not supported)
 -r [HEIGHTxWIDTH] sets initial browser size to height x width, example "-r 1600x1200"
 -t test run only, only checks config, scripts and some arguments in a dry run

The config-file should have the following structure:
ELEMENTNAME [[id|name|cssSelector|tagName|className|linkTest|xpath] "string"]+

The idea is that every element you want to interact with should have a unique 
name, followed by one or more selectors that each have a type and a text.

The script supports the following statements for now:
--
assert [!]ELEMENTNAME
assertatr ELEMENTNAME [!]"attribute" "value" (Warning! use assertpro for properties)
assertclk [!]ELEMENTNAME
assertcss ELEMENTNAME "property" "value"
assertpro ELEMENTNAME [!]"property" "value"
assertsel ELEMENTNAME "boolean"
asserttxt ELEMENTNAME "text"
click ELEMENTNAME
drawbox ELEMENTNAME "offset"
dwaitfor INTEGER [!]ELEMENTNAME
print "text"
printatr ELEMENTNAME "attribute"
printcss ELEMENTNAME "property"
printjs "text"
printpro ELEMENTNAME "property" (Warning! use printpro for properties)
printtime
printtxt ELEMENTNAME
screenshot "text"
scrollto ELEMENTNAME
select ELEMENTNAME "dropdownoption"
settoggle ELEMENTNAME "boolean"
type ELEMENTNAME "text"
typeclr ELEMENTNAME "text"
waitforatr ELEMENTNAME "attribute" "value" (Warning! use waitforpro for properties)
waitforenabled ELEMENTNAME
waitforpro ELEMENTNAME "property" "value"
waitfortxt ELEMENTNAME "text"
waitfor [!]ELEMENTNAME
wait INTEGER
finish
--

Statements in script does the following:

assert - returns true if element is found
assertatr  - do getAttribute on "attribute" of element compare with "value"
assertclk - do isVisible() & isEnabled() of element
assertcss - do getCssValue on "property" of element compare with "value"
assertpro  - do getDomProperty on "property" of element compare with "value"
assertsel - do isSelected() and compare with boolean
asserttxt - do getText() on element and compare with "text"
click - click() on element (will try scrolling if element is hidden)
drawbox - draws a box with the mouse inside the element, "offset" pixels in
dwaitfor - same as waitfor, but delayed with INTEGER*0.1s
print - print string
printatr - print result of getAttribute on "attribute" of element
printcss - print result of getCssValue on "property" of element
printjs - executes the text-line as js in context and tries to print the returned result
printpro - print result of getDomProperty on "property" of element
printtime - print out a current timestamp
printtxt - print out value of getText() on element
screenshot - takes a screenshot, saves it with filename "text"
scrollto - scroll so element is visible
select - selects a element in a dropdown
settoggle - sets a checkbox/radiobutton to checked if true, unchecked if false
type -  sendKeys("text") on element
typeclr - Sends CTRL+a+DEL before sendKeys("text")
waitforatr - wait until element is found and has "attribute" set to "value"
waitforenabled - wait until element is enabled
waitforpro - wait until element is found and has "property" set to "value"
waitfortxt - wait until element is found and has text set to "text"
waitfor - wait until element is found for up to 20s
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
waitfor dropdown
select dropdown "One"
type textbox "my-text"
click submitButton
waitfor !dropdown
waitfor message
asserttxt message "Received!" 

assertatr message "class" "lead"
assertcss message "font-size" "20px"
$ ./run.sh config.txt https://www.selenium.dev/selenium/web/web-form.html script.txt
INFO: Using Driver: 'CHROME'
INFO: Using Config: 'config.txt'
INFO: Using URL: 'https://www.selenium.dev/selenium/web/web-form.html'
INFO: Using Script: 'script.txt'
INFO: SUCCESS: script.txt (1/1)
SUCCESS: script.txt (1/1)

#Example FAIL with multiple scripts (here, reusing the same one):
$ ./run.sh config.txt https://www.selenium.dev/selenium/web/web-form.html script.txt script.txt
INFO: Using Driver: 'CHROME'
INFO: Using Config: 'config.txt'
INFO: Using URL: 'https://www.selenium.dev/selenium/web/web-form.html'
INFO: Using Script: 'script.txt'
INFO: Using Script: 'script.txt'
INFO: SUCCESS: script.txt (1/2)
FAIL: Previous: settoggle defcheckbox "false"
FAIL: Previous: scrollto submitButton
FAIL: Previous: click submitButton
FAIL: Previous: waitfor !dropdown
FAIL: Previous: waitfor message
FAIL: Previous: asserttxt message _recv
FAIL: Previous: assertatr message "class" "lead"
FAIL: Previous: assertcss message "font-size" "20px"
FAIL: script.txt (2/2):1:waitfor dropdown


