/**
   This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

   You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

   Copyright 2025 Henning Norén <henning.noren@gmail.com>
 */

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.remote.NoSuchDriverException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.Dimension;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// for validation only
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathExpressionException;

public class SimpleTester {

    private final static HashMap<String, ArrayList<By>> selectors = new HashMap<String, ArrayList<By>>();
    private final static HashMap<String, String> defines = new HashMap<String, String>();
    private static WebDriver curr_driver;
    private static JavascriptExecutor js;
    private static ArrayList<By> curr_by_list;
    private static WebElement curr_element;
    private static FileReader config_file;
    private static FileReader script_file;
    private static boolean script_done = false;
    private static EnumStmt stmt;
    private static EnumBy enumby;
    private static boolean isMac = false;
    private static boolean isSafari = false;
    private static boolean isFirefox = false;
    private static boolean isChrome = false;
    private static boolean isEdge = false;
    private static int linenr = 0;
    private static int lastDelta = 0;
    private static String curr_line = "";
    /** line_history is a ring-list and contains curr_line history.
	null means it is not used, null-entries are not yet populated **/
    private static String line_history[];
    /** pointer to the current position in the ring-list **/
    private static int line_history_position = 0;

    private enum EnumStmt {
	ASSERT,
	ASSERTATR,
	ASSERTCLK,
	ASSERTCSS,
	ASSERTPRO,
	ASSERTSEL,
	ASSERTTXT,
	CLICK,
	CLICKFOR,
	CLICKFORCE,
	DRAWBOX,
	DWAITFOR,
	FINISH,
	PRINT,
	PRINTATR,
	PRINTCSS,
	PRINTPRO,
	PRINTTIME,
	PRINTTXT,
	SCREENSHOT,
	SCROLLTO,
	SELECT,
	SETTOGGLE,
	TYPE,
	TYPECLR,
	TYPEKEY,
	WAIT,
	WAITFOR,
	WAITFORATR,
	WAITFORCSS,
	WAITFORENABLED,
	WAITFORPRO,
	WAITFORTXT;
    }

    private enum EnumBy {
	ID,
	NAME,
	CSSSELECTOR,
	TAGNAME,
	CLASSNAME,
	LINKTEXT,
	XPATH;
    }

    private enum enumDriver {
	CHROME,
	FIREFOX,
	EDGE,
	SAFARI,
	UNDEFINED;
    }

    private final static HashMap<String, EnumStmt> statements = new HashMap<String, EnumStmt>() {{
	    put("assert",    stmt.ASSERT);
	    put("assertatr", stmt.ASSERTATR);
	    put("assertclk", stmt.ASSERTCLK);
	    put("assertcss", stmt.ASSERTCSS);
	    put("assertpro", stmt.ASSERTPRO);
	    put("assertsel", stmt.ASSERTSEL);
	    put("asserttxt", stmt.ASSERTTXT);
	    put("click",     stmt.CLICK);
	    put("clickfor",  stmt.CLICKFOR);
	    put("clickforce",stmt.CLICKFORCE);
	    put("drawbox",   stmt.DRAWBOX);
	    put("dwaitfor",  stmt.DWAITFOR);
	    put("print",     stmt.PRINT);
	    put("printatr",  stmt.PRINTATR);
	    put("printcss",  stmt.PRINTCSS);
	    put("printpro",  stmt.PRINTPRO);
	    put("printtime", stmt.PRINTTIME);
	    put("printtxt",  stmt.PRINTTXT);
	    put("screenshot",stmt.SCREENSHOT);
	    put("scrollto",  stmt.SCROLLTO);
	    put("select",    stmt.SELECT);
	    put("settoggle", stmt.SETTOGGLE);
	    put("type",      stmt.TYPE);
	    put("typeclr",   stmt.TYPECLR);
	    put("typekey",   stmt.TYPEKEY);
	    put("wait",      stmt.WAIT);
	    put("waitfor",   stmt.WAITFOR);
	    put("waitforatr",stmt.WAITFORATR);
	    put("waitforcss",stmt.WAITFORCSS);
	    put("waitforenabled",stmt.WAITFORENABLED);
	    put("waitforpro",stmt.WAITFORPRO);
	    put("waitfortxt",stmt.WAITFORTXT);
	    put("finish",    stmt.FINISH);
	}};

    private final static HashMap<String, EnumBy> by_names = new HashMap<String, EnumBy>() {{
	    put("id",          enumby.ID);
	    put("name",        enumby.NAME);
	    put("cssSelector", enumby.CSSSELECTOR);
	    put("tagName",     enumby.TAGNAME);
	    put("className",   enumby.CLASSNAME);
	    put("linkText",    enumby.LINKTEXT);
	    put("xpath",       enumby.XPATH);
	}};

    private final static HashMap<String, Keys> key_names = new HashMap<String, Keys>() {{
	    put("ALT",          Keys.ALT);
	    put("ARROW_DOWN",   Keys.ARROW_DOWN);
	    put("ARROW_LEFT",   Keys.ARROW_LEFT);
	    put("ARROW_RIGHT",  Keys.ARROW_RIGHT);
	    put("ARROW_UP",     Keys.ARROW_UP);
	    put("BACKSPACE",    Keys.BACK_SPACE);
	    put("COMMAND",      Keys.COMMAND);
	    put("CONTROL",      Keys.CONTROL);
	    put("DELETE",       Keys.DELETE);
	    put("END",          Keys.END);
	    put("ENTER",        Keys.ENTER);
	    put("ESCAPE",       Keys.ESCAPE);
	    put("HOME",         Keys.HOME);
	    put("INSERT",       Keys.INSERT);
	    put("META",         Keys.META);
	    put("NULL",         Keys.NULL);
	    put("PAGE_DOWN",    Keys.PAGE_DOWN);
	    put("PAGE_UP",      Keys.PAGE_UP);
	    put("TAB",          Keys.TAB);
	}};

    private static void takeScreenshot(String pathname) {
	try {
	    File src = ((TakesScreenshot) curr_driver).getScreenshotAs(OutputType.FILE);
	    Files.move(src.toPath(), new File(pathname).toPath(),StandardCopyOption.REPLACE_EXISTING);
	    //System.out.println(src.getPath());
	}
	catch(Exception e) {
	    System.out.println("WARN: Unable to take screenshot "+e.toString());
	}
    }

    private static void sleep(int x) {
	try {
	    Thread.sleep(x);
	} catch(InterruptedException e) {}
    }

    private static boolean parseConfig() {
	BufferedReader buffer = new BufferedReader(config_file);
	linenr = 0;
	try {
	    curr_line = buffer.readLine();
	    while(curr_line != null) {
		linenr++;
		curr_line = curr_line.trim();
		//System.out.println(curr_line);
		if(curr_line.length() == 0 ||
		   (curr_line.length() > 0 && curr_line.charAt(0) == '#')) {
		    curr_line = buffer.readLine();
		    continue;
		}
		int index1 = curr_line.indexOf(' ');
		if(index1 == -1)
		    return false;
		String statement = curr_line.substring(0,index1);
		if(statement.equals("DEFINE")) {
		    int index2 = curr_line.indexOf(' ', index1+1);
		    if(index2 == -1) {
			System.out.println("ERROR: Unexpected EOL, did you miss a keyword?");
			return false;
		    }
		    String define_name = curr_line.substring(index1+1, index2);
		    String define_string = curr_line.substring(index2+1, curr_line.length());
		    if(defines.containsKey(define_name)) {
			System.out.println("ERROR: Duplicate define ("+define_name+")");
			return false;
		    }
		    defines.put(define_name,define_string);
		}
		else {
		    if(selectors.containsKey(statement)) {
			System.out.println("ERROR: Duplicate statement ("+statement+")");
			return false;
		    }
		    ArrayList<By> l = new ArrayList<By>();
		    selectors.put(statement,l);
		    while(true) {
			int index2 = curr_line.indexOf(' ', index1+1);
			if(index2 == -1) {
			    System.out.println("ERROR: Unexpected EOL, did you miss a keyword?");
			    return false;
			}
			String by_string = curr_line.substring(index1+1, index2);
			EnumBy ret = by_names.get(by_string);
			if(ret == null) {
			    System.out.println("ERROR: \""+by_string+
					       "\" not a valid Locator");
			    return false;
			}
			index1 = index2+1;
			index2 = curr_line.indexOf('"', index1+1);
			if(index2 == -1) {
			    return false;
			}
			String selector = curr_line.substring(index1+1, index2);
			XPathFactory xpathFactory = XPathFactory.newInstance();
			By by;
			switch(ret) {
			case ID:
			    by = By.id(selector);
			    break;
			case NAME:
			    by = By.name(selector);
			    break;
			case CSSSELECTOR:
			    by = By.cssSelector(selector);
			    break;
			case TAGNAME:
			    by = By.tagName(selector);
			    break;
			case CLASSNAME:
			    by = By.className(selector);
			    break;
			case LINKTEXT:
			    by = By.linkText(selector);
			    break;
			case XPATH:
			    try {
				// try to compile it, not really used.
				// only for validating it is a proper XPath.
				xpathFactory.newXPath().compile(selector);
			    }
			    catch(XPathExpressionException e) {
				System.out.println("ERROR: Invalid Xpath: "+selector);
				return false;
			    }
			    by = By.xpath(selector);
			    break;
			default:
			    return false;
			}
			l.add(by);

			//System.out.println(statement+"' '"+ret.toString()+by_string+"' '"+selector+"' " + index2 + " "+ curr_line.length());

			index1 = index2+1;
			if(curr_line.length() <= index1)
			    break;
			selectors.put(statement,l);
		    }
		}
		curr_line = buffer.readLine();
	    }
	    buffer.close();
	}
	catch(Exception e) {
	    System.out.println("FAIL: Error parsing config file "+e.toString());
	    System.exit(2);
	}
	return true;
    }

    private static void findElement(ArrayList<By> s) {
	curr_element = curr_driver.findElement(s.get(0));
	for (int i = 1; i < s.size(); i++) {
	    if(curr_element == null)
		return;
	    curr_element = curr_element.findElement(s.get(i));
	}
    }

    private static int currPos;
    private static boolean notSel = false;
    private static boolean notStr = false;

    private static class ParsingException extends Exception {
	public ParsingException(String message) {
	    super(message);
	}
    }

    /**
       handles reading a string on current line and postion but also handles
       defines.

       expectChar can be '"','%' or '?' where '?' allow for all kinds of strings
       but the other expects that the first char is matching the expected char

       Returns the string parsed (might be from a define) but if expectChar is ?
       then the returned string will start with the resulting type (" or %).

       Yes, it is a mess. Should rewrite one day
     */
    private static String readStrGen(char expectChar) throws ParsingException {
	int index2;
	boolean isDef = false;

	// the compiler is not smart enough to realise they will be initialized
	int tmpPos = 0, endIndex = 0;
	String tmpLine = "";

	// If this is a DEFINE, read it first and replace curr_line with it.
	// We will replace curr_line and currPos back after the DEFINE
	// has been parsed and validated it
	if(curr_line.charAt(currPos) == '_') {
	    currPos++;
	    endIndex = curr_line.indexOf(' ', currPos);
	    if(endIndex == -1)
		endIndex = curr_line.length();
	    String s = curr_line.substring(currPos, endIndex);
	    tmpLine = curr_line;
	    curr_line = defines.get(s);
	    if(curr_line == null) {
		curr_line = tmpLine;
		throw new ParsingException("Unknown Define: "+s);
	    }
	    tmpPos = currPos;
	    currPos = 0;
	    isDef = true;
	}
	//System.out.println("String("+currPos+":"+curr_line.charAt(currPos)+"): "+curr_line);
	if(curr_line.charAt(currPos) == '%' && expectChar == '"') {
	    if(isDef) {
		currPos = tmpPos;
		curr_line = tmpLine;
	    }
	    throw new ParsingException("Expected starting '\"'-character!");
	}
	if(curr_line.charAt(currPos) == '"' && expectChar == '%') {
	    if(isDef) {
		currPos = tmpPos;
		curr_line = tmpLine;
	    }
	    throw new ParsingException("Expected starting '%'-character!");
	}

	char c = '"';
	switch(curr_line.charAt(currPos)) {
	case '"':
	    currPos++;  // eat the quotation mark
	    index2 = curr_line.indexOf('"', currPos);
	    if(index2 == -1) {
		if(isDef) {
		    currPos = tmpPos;
		    curr_line = tmpLine;
		}
		throw new ParsingException("Expected ending '\"'-character!");
	    }
	    break;
	case '%':
	    currPos++;  // eat the quotation mark
	    index2 = curr_line.lastIndexOf('%', curr_line.length()-1);
	    if(index2 == -1 || index2 <= currPos) {
		if(isDef) {
		    currPos = tmpPos;
		    curr_line = tmpLine;
		}
		throw new ParsingException("Expected ending '%'-character!");
	    }
	    c = '%';
	    break;
	default:
	    if(isDef) {
		currPos = tmpPos;
		curr_line = tmpLine;
	    }
	    throw new ParsingException("Expected starting '\"'-character!");
	}

	String s = curr_line.substring(currPos, index2);
	if(s == null) {
	    if(isDef) {
		currPos = tmpPos;
		curr_line = tmpLine;
	    }
	    throw new ParsingException("Impossible string!");
	}
	currPos=index2+1;

	// restore curr_line and currPos if this was a DEFINE
	if(isDef) {
	    curr_line = tmpLine;
	    currPos = tmpPos + endIndex;
	}
	if(expectChar == '?')
	    return c+s;
	else
	    return s;
    }

    static class StrOrRegex {
	private String str;
	private Pattern pat;
	private boolean type = true; // true = str, false = pat
	public StrOrRegex()  throws ParsingException {
	    currPos++;
	    if(currPos >= curr_line.length())
		throw new ParsingException("Expected String");

	    String s = readStrGen('?');
	    str = s.substring(1,s.length());
	    if(s.charAt(0) == '%') {
		pat = Pattern.compile(str);
		type = false;
		return;
	    }
	    //System.out.println("String: "+s);
	}
	public boolean matches(String s) {
	    if(type)
		return str.equals(s);
	    else {
		Matcher m = pat.matcher(s);
		return m.matches();
	    }
	}

	public String toString() {
	    return str;
	}
    }

    private static EnumStmt readStmt() throws ParsingException {
	currPos = curr_line.indexOf(' ');
	if(currPos == -1) {
	    currPos = curr_line.length();
	}
	EnumStmt st = statements.get(curr_line.substring(0,currPos));
	if(st == null) {
	    throw new ParsingException(curr_line.substring(0, currPos)+" is not a statement!");
	}
	return st;
    }

    private static int readInt() throws ParsingException {
	currPos++;
	if(currPos >= curr_line.length())
	    throw new ParsingException("Expected INTEGER");
	int index2 = curr_line.indexOf(' ', currPos);
	if(index2 == -1)
	    index2 = curr_line.length();
	Integer tmp = Integer.valueOf(curr_line.substring(currPos, index2));
	if(tmp == null) {
	    throw new ParsingException(curr_line.substring(currPos, index2)+" is not a number!");
	}
	currPos = index2;
	return tmp.intValue();
    }

    private static Keys readKey() throws ParsingException {
	currPos++;
	if(currPos >= curr_line.length())
	    throw new ParsingException("Expected KEY");
	int index2 = curr_line.indexOf(' ', currPos);
	if(index2 == -1)
	    index2 = curr_line.length();

	String str = curr_line.substring(currPos, index2);
	currPos = index2;

	Keys ret = key_names.get(str);
	if(ret == null) {
	    throw new ParsingException("Expected a KEY, got \""+str+"\"");
	}
	return ret;
    }

    private static ArrayList<By> readSel(boolean neg) throws ParsingException {
	currPos++;
	if(currPos >= curr_line.length())
	    throw new ParsingException("Expected ELEMENTNAME");
	int index2 = curr_line.indexOf(' ', currPos);
	if(index2 == -1)
	    index2 = curr_line.length();

	String selector = curr_line.substring(currPos, index2);
	currPos = index2;
	if(selector.charAt(0) == '!') {
	    if(selector.length() == 1)
		throw new ParsingException("No ELEMENTNAME defined!");
	    if(!neg)
		throw new ParsingException("Unexpected Negation!");
	    notSel = true;
	    selector = selector.substring(1,selector.length());
	}
	else
	    notSel = false;

	//System.out.println("Selector:"+notSel+":"+selector);
	ArrayList<By> list = null;
	list = selectors.get(selector);
	if(list == null)
	    throw new ParsingException(selector+" is not a ELEMENTNAME!");
	return list;
    }

    private static String readString() throws ParsingException {
	currPos++;
	if(currPos >= curr_line.length())
	    throw new ParsingException("Expected String");

	return readStrGen('"');
    }

    private static String readString(boolean neg) throws ParsingException {
	currPos++;
	if(currPos >= curr_line.length())
	    throw new ParsingException("Expected String");

	if(curr_line.charAt(currPos) == '!') {
	    notStr = true;
	    currPos++;
	    if(currPos >= curr_line.length())
		throw new ParsingException("Expected String");
	}
	else
	    notStr = false;
	return readStrGen('"');
    }

    private static boolean readBool() throws ParsingException {
	String s = readString();
	if(s.equals("true"))
	    return true;
	else if(s.equals("false"))
	    return false;
	throw new ParsingException("Not true or false");
    }

    private static boolean tryClick() {
	boolean retry_click = false;
	try {
	    if(!curr_element.isDisplayed()) {
		js.executeScript("arguments[0].scrollIntoView();", curr_element);
	    }
	    curr_element.click();
	}
	catch(ElementNotInteractableException e) {
	    retry_click = true;
	}
	catch(WebDriverException e) {
	    if(isSafari)
		retry_click = true;
	    else
		throw e;
	}
	if(retry_click) {
	    js.executeScript("arguments[0].scrollIntoView();", curr_element);
	    sleep(70);
	    try {
		curr_element.click();
	    }
	    catch(ElementNotInteractableException e) {
		//System.out.println("ERROR: Element "+ curr_element+" probably hidden by other element!");
		//System.out.println("ERROR: "+e.getMessage());
		System.out.println("WARN: Click failed with "+e.getClass().getCanonicalName());
		return false;
	    }
	    catch(WebDriverException e) {
		if(isSafari) {
		    System.out.println("WARN: Click failed with "+e.getClass().getCanonicalName());
		    return false;
		}
		else
		    throw e;
	    }
	}
	return true;
    }

    private static boolean runStatement(boolean novalidate) {
	ArrayList<By> list = null;
	String s1;
	String s2;
	String ret;
	StrOrRegex sor;
	int x;
	notSel = false;
	notStr = false;
	try {
	    EnumStmt st = readStmt();
	    int index1 = currPos;
	    boolean b;
	    switch(st) {
	    case ASSERT:
		list = readSel(true);
		if(novalidate)
		    return true;

		try {
		    findElement(list);
		    if(!notSel)
			return true;
		}
		catch(NoSuchElementException e) {
		    if(notSel)
			return true;
		}
		return false;
	    case ASSERTATR:
		list = readSel(false);
		s1 = readString(true);
		s2 = readString();
		if(novalidate)
		    return true;
		findElement(list);
		ret = curr_element.getDomAttribute(s1);
		if(ret == null)
		    return notStr;
		else if(notStr)
		    return false;

		if(!ret.equals(s2)) {
		    if(ret.equals(s2.strip())) {
			System.out.println("WARN: ASSERTATR got \""+ret+"\", expected \""+s2+"\"");
			return true;
		    }
		    // Workaround for Safari returning true for attributes with a empty value
		    if(isSafari && s2.equals("") && ret.equals("true"))
			return true;

		    System.out.println("INFO: ASSERTATR got \""+ret+"\", expected \""+s2+"\"");
		    return false;
		}
		return true;
	    case ASSERTCLK:
		list = readSel(true);
		if(novalidate)
		    return true;
		findElement(list);

		// Element might still be hidden behind other, but this should do for now
		b = curr_element.isDisplayed() && curr_element.isEnabled();

		if(!notSel)
		    return b;
		else
		    return !b;
	    case ASSERTCSS:
		list = readSel(false);
		s1 = readString();
		sor = new StrOrRegex();
		if(novalidate)
		    return true;
		findElement(list);
		ret = curr_element.getCssValue(s1);
		if(ret == null)
		    return false;
		if(!sor.matches(ret)) {
		    if(sor.matches(ret.strip())) {
			System.out.println("WARN: ASSERTCSS got \""+ret+"\", expected \""+sor.toString()+"\"");
			return true;
		    }
		    System.out.println("INFO: ASSERTCSS got \""+ret+"\", expected \""+sor.toString()+"\"");
		    return false;
		}
		return true;
	    case ASSERTPRO:
		list = readSel(false);
		s1 = readString(true);
		s2 = readString();
		if(novalidate)
		    return true;
		findElement(list);
		ret = curr_element.getDomProperty(s1);
		if(ret == null)
		    return notStr;
		else if(notStr)
		    return false;
		if(!ret.equals(s2)) {
		    if(ret.equals(s2.strip())) {
			System.out.println("WARN: ASSERTPRO got \""+ret+"\", expected \""+s2+"\"");
			return true;
		    }
		    // Workaround for Safari returning true for attributes with a empty value
		    if(isSafari && s2.equals("") && ret.equals("true"))
			return true;

		    System.out.println("INFO: ASSERTPRO got \""+ret+"\", expected \""+s2+"\"");
		    return false;
		}
		return true;
	    case ASSERTSEL:
		list = readSel(false);
		b = readBool();
		if(novalidate)
		    return true;
		findElement(list);
		return curr_element.isSelected() == b;
	    case ASSERTTXT:
		list = readSel(false);
		sor = new StrOrRegex();
		if(novalidate)
		    return true;
		findElement(list);
		ret = curr_element.getText();
		if(ret == null)
		    return false;
		if(!sor.matches(ret)) {
		    if(sor.matches(ret.strip())) {
			System.out.println("WARN: ASSERTTXT got \""+ret+"\", expected \""+sor.toString()+"\"");
			return true;
		    }
		    System.out.println("INFO: ASSERTTXT got \""+ret+"\", expected \""+sor.toString()+"\"");
		    return false;
		}
		return true;
	    case CLICK:
		list = readSel(false);
		if(novalidate)
		    return true;
		findElement(list);
		//System.out.println(curr_element.toString());
		return tryClick();
	    case CLICKFOR:
		list = readSel(false);
		{
		    ArrayList<By> untilList = readSel(true);
		    if(novalidate)
			return true;
		    for(int i=0;i<100;i++) {
			try {
			    findElement(untilList);
			    if(!notSel)
				return true;
			}
			catch(NoSuchElementException e) {
			    if(notSel)
				return true;
			}
			findElement(list);
			tryClick();
			sleep(200);
		    }
		}
		return true;
	    case CLICKFORCE:
		list = readSel(false);
		if(novalidate)
		    return true;
		findElement(list);

		if(tryClick()) {
		    return true;
		}
		for(int i=0;i<100;i++) {
		    sleep(200);
		    if(tryClick()) {
			return true;
		    }
		}
		return false;
	    case DRAWBOX:
		list = readSel(false);
	        x = readInt();
		if(x < 0)
		    return false;
		if(novalidate)
		    return true;
		findElement(list);
		{
		    // get dimensions of element
		    Rectangle rect = curr_element.getRect();
		    int topLeftX = rect.getX();
		    int topLeftY = rect.getY();
		    int height = rect.getHeight()-(x*2);
		    int width = rect.getWidth()-(x*2);
		    if(height < 1 || width < 1) {
			System.out.println("INFO: Offset*2 > width or height");
			return false;
		    }
		    //System.out.println("X: "+topLeftX+" Y: "+topLeftY);
		    //System.out.println("height: "+height+" width: "+width);

		    Actions painter = new Actions(curr_driver);

		    painter.moveToLocation(topLeftX+x, topLeftY+x)
			.clickAndHold()
			.perform();
		    painter.moveByOffset(width-x,0).perform();
		    painter.moveByOffset(0,height-x).perform();
		    painter.moveByOffset(-width+x,0).perform();
		    painter.moveByOffset(0,-height+x).perform();
		    painter.moveByOffset(width-x,height-x).perform();
		    painter.release().perform();
		}
		return true;
	    case DWAITFOR:
		x = readInt();
		if(!novalidate) {
		    sleep(x*100);
		}
		list = readSel(true);
		if(novalidate)
		    return true;

		for(int i=0; i<100; i++) {
		    try {
			findElement(list);
			if(!notSel)
			    return true;
		    }
		    catch(NoSuchElementException e) {
			if(notSel)
			    return true;
		    }
		    sleep(200); // try every 200ms for 20s
		}
		return false;
	    case FINISH:
		script_done = true;
		return true;
	    case PRINT:
		s1 = readString();
		if(novalidate)
		    return true;
		System.out.println("PRINT:"+linenr+":\""+s1+"\"");
		return true;
	    case PRINTATR:
		list = readSel(false);
		s1 = readString();
		if(novalidate)
		    return true;
		findElement(list);
		ret = curr_element.getDomAttribute(s1);
		System.out.println("PRINTATR:"+linenr+":\""+ret+"\"");
		return true;
	    case PRINTCSS:
		list = readSel(false);
		s1 = readString();
		if(novalidate)
		    return true;
		findElement(list);
		ret = curr_element.getCssValue(s1);
		System.out.println("PRINTCSS:"+linenr+":\""+ret+"\"");
		return true;
	    case PRINTPRO:
		list = readSel(false);
		s1 = readString();
		if(novalidate)
		    return true;
		findElement(list);
		ret = curr_element.getDomProperty(s1);
		System.out.println("PRINTPRO:"+linenr+":\""+ret+"\"");
		return true;
	    case PRINTTIME:
		if(novalidate)
		    return true;
		System.out.println("PRINTTIME:"+linenr+":"+Instant.now());
		return true;
	    case PRINTTXT:
		list = readSel(false);
		if(novalidate)
		    return true;
		findElement(list);
		ret = curr_element.getText();
		System.out.println("PRINTTXT:"+linenr+":\""+ret+"\"");
		return true;
	    case SCREENSHOT:
		s1 = readString();
		if(novalidate)
		    return true;
		takeScreenshot(s1);
		System.out.println("INFO: Screenshot taken \""+s1+"\"");
		return true;
	    case SCROLLTO:
		list = readSel(false);
		if(novalidate)
		    return true;
		findElement(list);
		{
		    js.executeScript("arguments[0].scrollIntoView();", curr_element);
		    sleep(10);
		    /**
		       if(isFirefox) {

			//   workaround since scrollToElement does
			//   not seem to work in firefox.
			//   Just scroll back to top and then
			//   rely on scrollByAmount instead

			Rectangle rect = curr_element.getRect();
			int deltaY = rect.y + rect.height;
			Actions scroller = new Actions(curr_driver);
			System.out.println("deltaY: "+deltaY);
			scroller
			    .scrollByAmount(0,lastDelta)
			    .scrollByAmount(0,deltaY)
			    .perform();
			lastDelta = -deltaY;
		    }
		    else {
			Actions scroller = new Actions(curr_driver);
			scroller
			    .scrollToElement(curr_element)
			    .perform();
		    }*/
		}
		return true;
	    case SELECT:
		list = readSel(false);
		s1 = readString();
		if(novalidate)
		    return true;
		findElement(list);
		Select dropdown = new Select(curr_element);
		dropdown.selectByVisibleText(s1);
		return true;
	    case SETTOGGLE:
		// should return true if radiobutton/checkbox is set to chosen value
		list = readSel(false);
		b = readBool();
		if(novalidate)
		    return true;
		findElement(list);
		if(curr_element.isSelected() != b && curr_element.isEnabled()) {
		    try {
			curr_element.click();
		    }
		    catch(ElementNotInteractableException e) {
			System.out.println("ERROR: Element "+ curr_element+" probably hidden by other element!");
			System.out.println("ERROR: "+e.getMessage());
			return false;
		    }
		}
		return true;
	    case TYPE:
		list = readSel(false);
		s1 = readString();
		if(novalidate)
		    return true;
		findElement(list);
		curr_element.sendKeys(s1);
		return true;
	    case TYPECLR:
		list = readSel(false);
		s1 = readString();
		if(novalidate)
		    return true;
		findElement(list);
		if(isMac) {
		    curr_element.sendKeys(Keys.COMMAND + "a");
		    curr_element.sendKeys(Keys.BACK_SPACE);
		}
		else {
		    curr_element.sendKeys(Keys.CONTROL + "a");
		    curr_element.sendKeys(Keys.DELETE);
		}
		curr_element.sendKeys(s1);
		return true;
	    case TYPEKEY:
		list = readSel(false);
		Keys k = readKey();
		if(novalidate)
		    return true;
		findElement(list);
		curr_element.sendKeys(k);
		return true;
	    case WAIT:
		x = readInt();
		if(!novalidate) {
		    sleep(x*100);
		}
		return true;
	    case WAITFOR:
		list = readSel(true);
		if(novalidate)
		    return true;

		for(int i=0; i<100; i++) {
		    try {
			findElement(list);
			if(!notSel)
			    return true;
		    }
		    catch(NoSuchElementException e) {
			if(notSel)
			    return true;
		    }
		    sleep(200); // try every 200ms for 20s
		}
		return false;
	    case WAITFORATR:
		list = readSel(false);
		s1 = readString();
		s2 = readString();
		if(novalidate)
		    return true;
		for(int i=0;i<100;i++) {
		    findElement(list);
		    ret = curr_element.getDomAttribute(s1);
		    if(ret != null && ret.equals(s2))
			return true;
		    sleep(200);
		}
		return false;
	    case WAITFORCSS:
		list = readSel(false);
		s1 = readString();
		sor = new StrOrRegex();
		if(novalidate)
		    return true;
		ret = null; // compiler not smart enough to see it always is initalised
		for(int i=0; i<100; i++) {
		    findElement(list);
		    ret = curr_element.getCssValue(s1);
		    if(ret == null)
			continue;
		    if(!sor.matches(ret)) {
			if(sor.matches(ret.strip())) {
			    System.out.println("WARN: ASSERTCSS got \""+ret+"\", expected \""+sor.toString()+"\"");
			    return true;
			}
		    }
		    else {
			return true;
		    }
		    sleep(200);
		}
		System.out.println("INFO: ASSERTCSS got \""+ret+"\", expected \""+sor.toString()+"\"");
		return false;
	    case WAITFORENABLED:
		list = readSel(true);
		if(novalidate)
		    return true;

		for(int i=0; i<100; i++) {
		    try {
			findElement(list);
			if(curr_element.isEnabled()) {
			    if(!notSel)
				return true;
			}
			else {
			    if(notSel)
				return true;
			}
		    }
		    catch(NoSuchElementException e) {
			if(notSel)
			    return true;
		    }
		    sleep(200); // try every 200ms for 20s
		}
		return false;
	    case WAITFORPRO:
		list = readSel(false);
		s1 = readString();
		s2 = readString();
		if(novalidate)
		    return true;
		for(int i=0;i<100;i++) {
		    findElement(list);
		    ret = curr_element.getDomProperty(s1);
		    if(ret != null && ret.equals(s2))
			return true;
		    sleep(200);
		}
		return false;
	    case WAITFORTXT:
		list = readSel(false);
		sor = new StrOrRegex();
		if(novalidate)
		    return true;
		for(int i=0;i<100;i++) {
		    findElement(list);
		    ret = curr_element.getText();
		    if(sor.matches(ret)) {
			return true;
		    }
		    else if(sor.matches(ret.strip())) {
			System.out.println("WARN: WAITFORTXT got \""+ret+"\", expected \""+sor.toString()+"\"");
			return true;
		    }
		    sleep(200);
		}
		return false;
	    default:
		break;
	    }
	}
	catch(ParsingException e) {
	    System.out.println("ERROR: "+e.toString());
	}
	return false;
    }

    
    private static boolean parseScript(boolean novalidate) {
	script_done = false;
	BufferedReader buffer = new BufferedReader(script_file);
	linenr = 0;
	try {
	    curr_line = buffer.readLine();
	    while(curr_line != null && script_done == false) {
		linenr++;
		//System.out.println(linenr+":"+curr_line + " " + curr_line.trim().length());

		curr_line = curr_line.trim();
		if(curr_line.length() == 0 ||
		   (curr_line.length() > 0 && curr_line.charAt(0) == '#')) {
		    curr_line = buffer.readLine();
		    continue;
		}
		if(!runStatement(novalidate)) {
		    return false;
		}
		// populate the history ring buffer with current line
		else if(!novalidate && line_history != null) {
		    line_history[line_history_position] = curr_line;
		    line_history_position = (line_history_position+1) % line_history.length;
		}
		curr_line = buffer.readLine();
	    }
	}
	catch(NoSuchElementException e) {
	    return false;
	}
	catch(Exception e) {
	    //e.printStackTrace(System.out);
	    System.out.println("ERROR: "+e.toString());
	    return false;
	}
	return true;
    }

    private static void printUsage() {
	System.out.println("Usage: ./run.sh [-e FILENAME] [-f PATH] [-h] [-p] [-b [firefox|chrome|safari|edge]] [-r HEIGHTxWIDTH] <configfile> <URL> <script>");
    }

    public static void main(String[] args) {

	if(args.length < 3) {
	    printUsage();
	    System.exit(1);
	}

	enumDriver edrive = enumDriver.CHROME;
	int argi = 0;
	boolean headless = false;
	boolean stay_open = false;
	boolean screenshot_p = false;
	Path screenshot_path = Paths.get("ERROR");
	boolean binary_p = false;
	boolean dev_mode = false;
	Path binary_path = null;
	boolean debug_connect = false;
	String debug_connect_address = "";
	int resolution_x = 0, resolution_y = 0;

	{
	    String os =  System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
	    if((os.indexOf("mac") >= 0) || (os.indexOf("darwin") >= 0)) {
		isMac = true;
	    }
	}

	/** Parse optional arguments **/
	while(argi < args.length &&
	      args[argi].charAt(0) == '-' &&
	      args[argi].length() == 2) {
	    switch(args[argi].charAt(1)) {
	    case 'b':
		argi++;
		if(args[argi].equals("firefox"))
		    edrive = enumDriver.FIREFOX;
		else if(args[argi].equals("chrome"))
		    edrive = enumDriver.CHROME;
		else if(args[argi].equals("safari"))
		    edrive = enumDriver.SAFARI;
		else if(args[argi].equals("edge"))
		    edrive = enumDriver.EDGE;
		else {
		    System.out.println("Unsupported driver: "+args[argi]);
		    System.exit(1);
		}
		argi++;
		break;
	    case 'c':
		debug_connect = true;
		argi++;
		debug_connect_address = args[argi];
		argi++;
		break;
	    case 'd':
		dev_mode = true;
		argi++;
		break;
	    case 'e':
		argi++;
		screenshot_p = true;
		screenshot_path = Paths.get(args[argi]);
		argi++;
		break;
	    case 'f':
		argi++;
		binary_p = true;
		binary_path = Paths.get(args[argi]);
		argi++;
		break;
	    case 'h':
		headless = true;
		stay_open = false;
		argi++;
		break;
	    case 'p':
		if(!headless) {
		    stay_open = true;
		}
		argi++;
		break;
	    case 'r':
		argi++;
		if(args[argi].matches("^\\d+x\\d+$")) {
		    String[] res=args[argi].split("x");
		    resolution_x = Integer.valueOf(res[0]);
		    resolution_y = Integer.valueOf(res[1]);
		}
		else {
		    System.out.println("Invalid resolution: "+args[argi]);
		    System.exit(1);
		}
		argi++;
		break;
	    case 'v':
		argi++;
		line_history = new String[8];
		break;
	    }
	}
	if(argi >= args.length) {
	    printUsage();
	    System.exit(1);
	}

	String cfgfile = args[argi];
	String url = args[argi+1];
	String sfile = "";

	System.out.println("INFO: Using Driver: "+edrive);
	if(binary_p) {
	    System.out.println("INFO: Browser Path: "+binary_path.toString());
	}
	System.out.println("INFO: Using Config "+cfgfile);
	System.out.println("INFO: Using URL: "+url);

	try {
	    // read in and parse config file
	    config_file = new FileReader(cfgfile);
	    
	    if(!parseConfig()) {
		System.out.println("FAIL: Unable to parse config file "+
				   cfgfile+":"+linenr+":"+curr_line);
		System.exit(2);
	    }
	    config_file.close();
	    
	    // read in and parse script files
	    for(int i=argi+2; i < args.length; i++) {
		sfile = args[i];
		System.out.println("INFO: Using Script: "+sfile);
		script_file = new FileReader(sfile);

		if(!parseScript(true)) {
		    System.out.println("FAIL: Unable to parse script file "+
				       sfile+":"+linenr+":"+curr_line);
		    System.exit(2);
		}
		script_file.close();
	    }
	}
	catch(Exception e) {
	    System.out.println("FAIL: Unable to open file "+e.toString());
	    System.exit(2);
	}
	try {
	    switch(edrive) {
	    case CHROME:
		{
		    isChrome = true;
		    ChromeOptions options = new ChromeOptions();
		    if(resolution_x > 0 && resolution_y > 0) {
			options.addArguments("--window-size="+resolution_x+
					     ","+resolution_y);
		    }
		    if(headless) {
			options.addArguments("--headless=new");
		    }
		    if(binary_p) {
			options.setBinary(binary_path.toString());
		    }
		    if(dev_mode) {
			options.addArguments("--auto-open-devtools-for-tabs");
		    }
		    if(debug_connect) {
			options.setExperimentalOption("debuggerAddress", debug_connect_address);
		    }
		    curr_driver = new ChromeDriver(options);
		}
		break;
	    case FIREFOX:
		{
		    isFirefox = true;
		    FirefoxOptions options = new FirefoxOptions();
		    options.setCapability("webSocketUrl", true);
		    if(resolution_x > 0 && resolution_y > 0) {
			options.addArguments("--width="+resolution_x);
			options.addArguments("--height="+resolution_y);
		    }
		    if(headless) {
			options.addArguments("--headless");
		    }
		    if(binary_p) {
			options.setBinary(binary_path);
		    }
		    if(debug_connect) {
			System.out.println("WARN: remote debug (-c) not supported with FIREFOX");
		    }
		    if(dev_mode) {
			System.out.println("WARN: devel mode (-d) not supported with FIREFOX");
		    }

		    curr_driver = new FirefoxDriver(options);
		}
		break;
	    case SAFARI:
		{
		    isSafari = true;
		    if(headless) {
			System.out.println("WARN: headless mode (-h) not supported with SAFARI");
		    }
		    if(binary_p) {
			System.out.println("WARN: setBinary (-b) not supported with SAFARI");
		    }
		    if(debug_connect) {
			System.out.println("WARN: remote debug (-c) not supported with SAFARI");
		    }
		    if(dev_mode) {
			System.out.println("WARN: devel mode (-d) not supported with SAFARI");
		    }

		    curr_driver = new SafariDriver();
		    if(resolution_x > 0 && resolution_y > 0) {
			curr_driver.manage().window().setSize(new Dimension(resolution_x, resolution_y));
		    }
		}
		break;
	    case EDGE:
		{
		    isEdge = true;
		    EdgeOptions options = new EdgeOptions();
		    if(resolution_x > 0 && resolution_y > 0) {
			options.addArguments("--window-size="+resolution_x+
					     ","+resolution_y);
		    }
		    if(headless) {
			options.addArguments("--headless=new");
		    }
		    if(binary_p) {
			options.setBinary(binary_path.toString());
		    }
		    if(debug_connect) {
			System.out.println("WARN: remote debug (-c) not supported with EDGE");
		    }
		    if(dev_mode) {
			System.out.println("WARN: devel mode (-d) not supported with EDGE");
		    }

		    curr_driver = new EdgeDriver(options);
		}
		break;
	    default:
		System.out.println("Unsupported driver");
		System.exit(1);
	    }
	} catch(NoSuchDriverException e) {
	    System.out.println("ERROR: Is the browser installed?");
	    System.exit(1);
	}
	curr_driver.get(url);
	js = (JavascriptExecutor) curr_driver;
	//String title = curr_driver.getTitle();
	//System.out.println(title);

	int nr_of_scripts = args.length-argi-2;
	int script_nr = 1;
	try {
	    for(script_nr=1; script_nr <= nr_of_scripts; script_nr++) {
		sfile = args[script_nr+argi+1];
		script_file = new FileReader(sfile);
		if(!parseScript(false)) {
		    if(screenshot_p)
			takeScreenshot(screenshot_path.toString()+".png");
		    else
			takeScreenshot(sfile+".png");

		    /** print out history if we have one **/
		    if(line_history != null) {
			for(int i=0;i<line_history.length;i++) {
			    // skip null-entries in case we bail out too early
			    if(line_history[line_history_position] != null) {
				System.out.println("FAIL: Previous: "+
						   line_history[line_history_position]);
			    }
			    line_history_position = (line_history_position+1) % line_history.length;
			}
		    }
		    System.out.println("FAIL: "+sfile+" ("+script_nr+"/"+
				       nr_of_scripts+")"+":"+linenr+":"+curr_line);
		    script_file.close();
		    if(!stay_open || headless) {
			curr_driver.quit();
		    }
		    System.exit(1);
		}
		script_file.close();
		System.out.println("INFO: SUCCESS: "+sfile+" ("+script_nr+
				   "/"+nr_of_scripts+")");
	    }
	    script_nr--;
	    System.out.println("SUCCESS: "+sfile+" ("+script_nr+"/"+
			       nr_of_scripts+")");
	}
	catch(Exception e) {
	    //e.printStackTrace(System.out);
	    System.out.println(e.toString());
	    System.out.println("FAIL: "+sfile+" ("+script_nr+"/"+
			       nr_of_scripts+")"+":"+linenr+":"+curr_line);
	    System.exit(2);
	}
	if(!stay_open || headless) {
	    curr_driver.quit();
	}
	System.exit(0);
    }
}
