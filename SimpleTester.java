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
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.remote.NoSuchDriverException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.Rectangle;

import java.time.Duration;
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

// for validation only
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathExpressionException;

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
	ASSERT,
	ASSERTATR,
	ASSERTCSS,
	ASSERTTXT,
	CLICK,
	DRAWBOX,
	DWAITFOR,
	PRINTATR,
	PRINTCSS,
	PRINTTXT,
	SCREENSHOT,
	SELECT,
	TYPE,
	TYPECLR,
	WAIT,
	WAITFOR,
	WAITFORATR,
	WAITFORTXT,
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
	    put("assert",    stmt.ASSERT);
	    put("assertatr", stmt.ASSERTATR);
	    put("assertcss", stmt.ASSERTCSS);
	    put("asserttxt", stmt.ASSERTTXT);
	    put("click",     stmt.CLICK);
	    put("drawbox",   stmt.DRAWBOX);
	    put("dwaitfor",  stmt.DWAITFOR);
	    put("printatr",  stmt.PRINTATR);
	    put("printcss",  stmt.PRINTCSS);
	    put("printtxt",  stmt.PRINTTXT);
	    put("screenshot",stmt.SCREENSHOT);
	    put("select",    stmt.SELECT);
	    put("type",      stmt.TYPE);
	    put("typeclr",   stmt.TYPECLR);
	    put("wait",      stmt.WAIT);
	    put("waitfor",   stmt.WAITFOR);
	    put("waitforatr",stmt.WAITFORATR);
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
			try {
			    // try to compile it, we do not use this
			    // only for validating it is a proper XPath
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

    static class StrOrRegex {
	private String str;
	private Pattern pat;
	private boolean type = true; // true = str, false = pat
	public StrOrRegex()  throws ParsingException {
	    currPos++;
	    if(currPos >= curr_line.length())
		throw new ParsingException("Expected String");

	    if(curr_line.charAt(currPos) == '%') {
		currPos++;  // eat the quotation mark
		int index2 = curr_line.lastIndexOf('%', curr_line.length()-1);
		if(index2 == -1 || index2 <= currPos) {
		    throw new ParsingException("Expected ending '%'-character!");
		}
		String s = curr_line.substring(currPos, index2);
		pat = Pattern.compile(s);
		str = s;
		currPos=index2+1;
		type = false;
		return;
	    }
	    if(curr_line.charAt(currPos) != '"') {
		throw new ParsingException("Expected starting '\"'-character!");
	    }
	    currPos++;  // eat the quotation mark
	    int index2 = curr_line.indexOf('"', currPos);
	    if(index2 == -1) {
		throw new ParsingException("Expected ending '\"'-character!");
	    }
	    String s = curr_line.substring(currPos, index2);
	    if(s == null) {
		throw new ParsingException("Impossible string!");
	    }
	    currPos=index2+1;
	    //System.out.println("String: "+s);
	    str = s;
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
	if(s == null) {
	    throw new ParsingException("Impossible string!");
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
	StrOrRegex sor;
	int x;
	try {
	    EnumStmt st = readStmt();
	    int index1 = currPos;
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
	    case FINISH:
		script_done = true;
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
	    case CLICK:
		list = readSel(false);
		if(novalidate)
		    return true;
		findElement(list);
		curr_element.click();
		//System.out.println(curr_element.toString());
		return true;
	    case PRINTATR:
		list = readSel(false);
		s1 = readString();
		if(novalidate)
		    return true;
		findElement(list);
		ret = curr_element.getAttribute(s1);
		System.out.println("PRINT:"+linenr+":\""+ret+"\"");
		return true;
	    case PRINTCSS:
		list = readSel(false);
		s1 = readString();
		if(novalidate)
		    return true;
		findElement(list);
		ret = curr_element.getCssValue(s1);
		System.out.println("PRINT:"+linenr+":\""+ret+"\"");
		return true;
	    case PRINTTXT:
		list = readSel(false);
		if(novalidate)
		    return true;
		findElement(list);
		ret = curr_element.getText();
		System.out.println("PRINT:"+linenr+":\""+ret+"\"");
		return true;
	    case SCREENSHOT:
		s1 = readString();
		if(novalidate)
		    return true;
		takeScreenshot(s1);
		System.out.println("INFO: Screenshot taken \""+s1+"\"");
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
		sor = new StrOrRegex();
		if(novalidate)
		    return true;
		findElement(list);
		ret = curr_element.getText();
		if(!sor.matches(ret)) {
		    if(sor.matches(ret.strip())) {
			System.out.println("WARN: ASSERTTXT got \""+ret+"\", expected \""+sor.toString()+"\"");
			return true;
		    }
		    System.out.println("INFO: ASSERTTXT got \""+ret+"\", expected \""+sor.toString()+"\"");
		    return false;
		}
		return true;
	    case WAITFORTXT:
		list = readSel(false);
		sor = new StrOrRegex();
		if(novalidate)
		    return true;
		for(int i=0;i<100;i++) {
		    findElement(list);
		    ret = curr_element.getText();
		    if(!sor.matches(ret)) {
			return true;
		    }
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
		    if(ret.equals(s2.strip())) {
			System.out.println("WARN: ASSERTATR got \""+ret+"\", expected \""+s2+"\"");
			return true;
		    }
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
		    if(ret.equals(s2.strip())) {
			System.out.println("WARN: ASSERTCSS got \""+ret+"\", expected \""+s2+"\"");
			return true;
		    }
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
	    System.out.println("Usage: ./run.sh [-h] [-p] [-b [firefox|chrome|safari|edge]] <configfile> <URL> <script>");
	    System.exit(1);
	}
	enumDriver edrive = enumDriver.CHROME;
	int argi = 0;
	boolean headless = false;
	boolean stay_open = false;
	{
	    String os =  System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
	    if((os.indexOf("mac") >= 0) || (os.indexOf("darwin") >= 0)) {
		isMac = true;
	    }
	}
	if(args[argi].equals("-h")) {
	    headless = true;
	    stay_open = false;
	    argi++;
	}
	if(args[argi].equals("-p")) {
	    if(!headless) {
		stay_open = true;
	    }
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
		if(headless) {
		    FirefoxOptions options = new FirefoxOptions();
		    options.addArguments("-headless");
		    curr_driver = new FirefoxDriver(options);
		}
		else
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

	int nr_of_scripts = args.length-argi-2;
	int script_nr = 1;
	try {
	    for(script_nr=1; script_nr <= nr_of_scripts; script_nr++) {
		sfile = args[script_nr+argi+1];
		script_file = new FileReader(sfile);
		if(!parseScript(false)) {
		    System.out.println("FAIL: "+sfile+" ("+script_nr+"/"+nr_of_scripts+")"+":"+linenr+":"+curr_line);
		    takeScreenshot(sfile+".png");
		    script_file.close();
		    if(!stay_open || headless) {
			curr_driver.quit();
		    }
		    System.exit(1);
		}
		script_file.close();
		System.out.println("INFO: SUCCESS: "+sfile+" ("+script_nr+"/"+nr_of_scripts+")");
	    }
	    script_nr--;
	    System.out.println("SUCCESS: "+sfile+" ("+script_nr+"/"+nr_of_scripts+")");
	}
	catch(Exception e) {
	    //e.printStackTrace(System.out);
	    System.out.println(e.toString());
	    System.out.println("FAIL: "+sfile+" ("+script_nr+"/"+nr_of_scripts+")"+":"+linenr+":"+curr_line);
	    System.exit(2);
	}
	if(!stay_open || headless) {
	    curr_driver.quit();
	}
	System.exit(0);
    }
}
