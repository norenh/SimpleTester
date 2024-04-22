/**
   This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

   You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

   Copyright 2023 Henning Norén <henning.noren@gmail.com>
 */

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.remote.NoSuchDriverException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.Capabilities;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;

public class SimpleTester {
    
    private final static HashMap<String, ArrayList<By>> selectors = new HashMap<String, ArrayList<By>>();
    private static WebDriver curr_driver;
    private static ArrayList<By> curr_by_list;
    private static WebElement curr_element;
    private static FileReader config_file;
    private static FileReader script_file;
    private static boolean script_done = false;
    private static EnumStmt stmt;
    private static EnumBy enumby;
    private static boolean isMac = false;
    private static int linenr = 0;
    private static String curr_line = "";
    private enum EnumStmt {
	FIND,
	SELECT,
	ASSERTTXT,
	ASSERTATR,
	ASSERTCSS,
	CLICK,
	DWAITFOR,
	TYPE,
	TYPECLR,
	WAITFORATR,
	WAITFORTXT,
	WAITFOR,
	WAIT,
	FINISH;
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
	    put("find",      stmt.FIND);
	    put("select",    stmt.SELECT);
	    put("asserttxt", stmt.ASSERTTXT);
	    put("assertatr", stmt.ASSERTATR);
	    put("assertcss", stmt.ASSERTCSS);
	    put("click",     stmt.CLICK);
	    put("dwaitfor",  stmt.DWAITFOR);
	    put("type",      stmt.TYPE);
	    put("typeclr",   stmt.TYPECLR);
	    put("waitforatr",stmt.WAITFORATR);
	    put("waitfortxt",stmt.WAITFORTXT);
	    put("waitfor",   stmt.WAITFOR);
	    put("wait",      stmt.WAIT);
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
		if(curr_line.length() == 0 || (curr_line.length() > 0 && curr_line.charAt(0) == '#')) {
		    curr_line = buffer.readLine();
		    continue;
		}
		int index1 = curr_line.indexOf(' ');
		if(index1 == -1)
		    return false;
		String statement = curr_line.substring(0,index1);
		if(selectors.containsKey(statement)) {
		    System.out.println("ERROR: Duplicate statement ("+statement+")?");
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
			System.out.println("ERROR: \""+by_string+"\" not a valid Locator");
			return false;
		    }
		    index1 = index2+1;
		    index2 = curr_line.indexOf('"', index1+1);
		    if(index2 == -1) {
			return false;
		    }
		    String selector = curr_line.substring(index1+1, index2);
		    By by;
		    switch(ret) {
		    case ID:
			by = By.id(selector);
			break;
		    case NAME:
			by = By.name(selector);
			break;
		    case CSSSELECTOR:
			by = By.cssSelector​(selector);
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

    private static class ParsingException extends Exception {
	public ParsingException(String message) {
	    super(message);
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
	if(curr_line.charAt(currPos) != '"') {
	    throw new ParsingException("Expected starting '\"'-character!");
	}
	currPos++;  // eat the quotation mark
	int index2 = curr_line.indexOf('"', currPos);
	if(index2 == -1) {
	    throw new ParsingException("Expected ending '\"'-character!");
	}
	String s = curr_line.substring(currPos, index2);
	if(s == null || s.length() < 1) {
	    throw new ParsingException("Empty string!");
	}
	currPos=index2+1;
	//System.out.println("String: "+s);
	return s;
    }
    
    
    private static boolean runStatement(boolean novalidate) {
	ArrayList<By> list = null;
	String s1;
	String s2;
	String ret;
	int x;
	try {
	    EnumStmt st = readStmt();
	    int index1 = currPos;
	    switch(st) {
	    case FINISH:
		script_done = true;
		return true;
	    case WAIT:
		x = readInt();
		if(!novalidate) {
		    sleep(x*100);
		}
		return true;
	    case FIND:
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
	    case CLICK:
		list = readSel(false);
		if(novalidate)
		    return true;
		findElement(list);
		curr_element.click();
		//System.out.println(curr_element.toString());
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
	    case ASSERTTXT:
		list = readSel(false);
		s1 = readString();
		if(novalidate)
		    return true;
		findElement(list);
		ret = curr_element.getText();
		if(!ret.equals(s1)) {
		    System.out.println("INFO: ASSERTTXT got \""+ret+"\", expected \""+s1+"\"");
		    return false;
		}
		return true;
	    case WAITFORTXT:
		list = readSel(false);
		s1 = readString();
		if(novalidate)
		    return true;
		for(int i=0;i<100;i++) {
		    findElement(list);
		    ret = curr_element.getText();
		    if(ret.equals(s1))
			return true;
		    sleep(200);
		}
		return false;
	    case ASSERTATR:
		list = readSel(false);
		s1 = readString();
		s2 = readString();
		if(novalidate)
		    return true;
		findElement(list);
		ret = curr_element.getAttribute(s1);
		if(!ret.equals(s2)) {
		    System.out.println("INFO: ASSERTATR got \""+ret+"\", expected \""+s2+"\"");
		    return false;
		}
		return true;
	    case ASSERTCSS:
		list = readSel(false);
		s1 = readString();
		s2 = readString();
		if(novalidate)
		    return true;
		findElement(list);
		ret = curr_element.getCssValue(s1);
		if(!ret.equals(s2)) {
		    System.out.println("INFO: ASSERTCSS got \""+ret+"\", expected \""+s2+"\"");
		    return false;
		}
		return true;
	    case WAITFORATR:
		list = readSel(false);
		s1 = readString();
		s2 = readString();
		if(novalidate)
		    return true;
		for(int i=0;i<100;i++) {
		    findElement(list);
		    ret = curr_element.getAttribute(s1);
		    if(ret.equals(s2))
			return true;
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
		if(curr_line.length() == 0 || (curr_line.length() > 0 && curr_line.charAt(0) == '#')) {
		    curr_line = buffer.readLine();
		    continue;
		}
		if(!runStatement(novalidate)) {
		    return false;
		}
		curr_line = buffer.readLine();
	    }
	}
	catch(NoSuchElementException e) {
	    return false;
	}
	catch(Exception e) {
	    System.out.println("ERROR: "+e.toString());
	    return false;
	}
	return true;
    }

    public static void main(String[] args) {
	if(args.length < 3) {
	    System.out.println("Usage: ./run.sh <configfile> <URL> <script>");
	    System.exit(1);
	}
	enumDriver edrive = enumDriver.CHROME;
	int argi = 0;
	boolean headless = false;
	{
	    String os =  System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
	    if((os.indexOf("mac") >= 0) || (os.indexOf("darwin") >= 0)) {
		isMac = true;
	    }
	}
	if(args[argi].equals("-h")) {
	    headless = true;
	    argi++;
	}
	if(args[argi].equals("-b")) {
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
	}
	String cfgfile = args[argi];
	String url = args[argi+1];
	String sfile = "";

	System.out.println("INFO: Using Config "+cfgfile);
	System.out.println("INFO: Using URL: "+url);

	try {
	    // read in and parse config file
	    config_file = new FileReader(cfgfile);
	    
	    if(!parseConfig()) {
		System.out.println("FAIL: Unable to parse config file "+cfgfile+":"+linenr+":"+curr_line);
		System.exit(2);
	    }
	    config_file.close();
	    
	    // read in and parse script files
	    for(int i=argi+2; i < args.length; i++) {
		sfile = args[i];
		System.out.println("INFO: Using Script: "+sfile);
		script_file = new FileReader(sfile);

		if(!parseScript(true)) {
		    System.out.println("FAIL: Unable to parse script file "+sfile+":"+linenr+":"+curr_line);
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
		if(headless) {
		    ChromeOptions options = new ChromeOptions();
		    options.addArguments("--headless=new");
		    options.addArguments("--window-size=1920,1080");
		    curr_driver = new ChromeDriver(options);
		}
		else
		    curr_driver = new ChromeDriver();
		break;
	    case FIREFOX:
		curr_driver = new FirefoxDriver();
		break;
	    case SAFARI:
		curr_driver = new SafariDriver();
		break;
	    case EDGE:
		curr_driver = new EdgeDriver();
	    default:
		System.out.println("Unsupported driver");
		System.exit(1);
	    }
	} catch(NoSuchDriverException e) {
	    System.out.println("ERROR: Is the browser installed?");
	    System.exit(1);
	}
	curr_driver.get(url);
	//String title = curr_driver.getTitle();
	//System.out.println(title);

	try {
	    for(int i=argi+2; i < args.length; i++) {
		sfile = args[i];
		script_file = new FileReader(sfile);
		if(!parseScript(false)) {
		    System.out.println("FAIL: "+sfile+":"+linenr+":"+curr_line);
		    takeScreenshot(sfile+".png");
		    script_file.close();
		    curr_driver.quit();
		    System.exit(1);
		}
		script_file.close();
	    }
	    System.out.println("SUCCESS: "+sfile);
	}
	catch(Exception e) {
	    //e.printStackTrace(System.out);
	    System.out.println(e.toString());
	    System.out.println("FAIL: Running "+sfile+":"+linenr+":"+curr_line);
	    System.exit(2);
	}
	curr_driver.quit();
	System.exit(0);
    }
}
