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
    private enum EnumStmt {
	FIND,
	SELECT,
	ASSERTTXT,
	ASSERTATR,
	ASSERTCSS,
	CLICK,
	TYPE,
	TYPECLR,
	WAITFORATR,
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
	    put("type",      stmt.TYPE);
	    put("typeclr",   stmt.TYPECLR);
	    put("waitforatr",stmt.WAITFORATR);
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

    
    private static int linenr = 0;
    private static String curr_line = "";
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
		ArrayList<By> l = new ArrayList<By>();
		selectors.put(statement,l);
		while(true) {
		    int index2 = curr_line.indexOf(' ', index1+1);
		    if(index2 == -1)
			return false;
		    String by_string = curr_line.substring(index1+1, index2);
		    EnumBy ret = by_names.get(by_string);
		    if(ret == null)
			return false;
		    index1 = index2+1;
		    index2 = curr_line.indexOf('"', index1+1);
		    if(index2 == -1)
			return false;
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

    private static boolean findElement(ArrayList<By> s) {
	curr_element = curr_driver.findElement(s.get(0));
	for (int i = 1; i < s.size(); i++) {
	    if(curr_element == null)
		return false;
	    curr_element = curr_element.findElement(s.get(i));
	}
	return true;
    }

    private static boolean runStatement(boolean novalidate) {
	int index1 = curr_line.indexOf(' ');
	if(index1 == -1)
	    return false;
	String statement = curr_line.substring(0,index1);
	EnumStmt st = statements.get(statement);
	if(st == null)
	    return false;

	// 0 argument
	switch(st) {
	case FINISH:
	    script_done = true;
	    return true;
	default:
	    break;
	}
	//System.out.println("Statement: "+statement);
	// more arguments
	int index2 = index1+1;
	index2 = curr_line.indexOf(' ', index2);
	if(index2 == -1)
	    index2 = curr_line.length();
	String selector = curr_line.substring(index1+1, index2);
	boolean not_sel = false;
	ArrayList<By> list = null;
	if(selector.charAt(0) == '!') {
	    if(selector.length() == 1)
		return false;
	    selector = selector.substring(1,selector.length());
	    not_sel = true;
	}
	if(st != stmt.WAIT) {
	    //System.out.println("!wait");
	    list = selectors.get(selector);
	    if(list == null)
		return false;
	}
	//System.out.println("Statement: "+selector);
	// 1 argument
	switch(st) {
	case FIND:
	case WAIT:
	    if(not_sel)
		return false;
	    Integer tmp = Integer.valueOf(selector);
	    if(tmp == null)
		return false;
	    if(!novalidate) {
		int x = tmp.intValue();
		sleep(x*100);
	    }
	    return true;
	case WAITFOR:
	    if(novalidate)
		return true;
	    for(int i=0; i<100; i++) {
		try {
		    findElement(list);
		    if(!not_sel)
			return true;
		}
		catch(NoSuchElementException e) {
		    if(not_sel)
			return true;
		}
		sleep(200); // try every 200ms for 20s
	    }
	    return false;
	case CLICK:
	    if(not_sel)
		return false;
	    if(novalidate)
		return true;
	    findElement(list);
	    curr_element.click();
	    //System.out.println(curr_element.toString());
	    return true;
	default:
	    break;
	}

	// 2 arguments
	index1 = index2+1;
	index2 = curr_line.indexOf(' ', index1);
	if(index2 == -1)
	    index2 = curr_line.length();
	String arg3 = curr_line.substring(index1, index2);
	
	// just make sure the 3rd arg starts and ends with quotation marks, take everything in between
	if(arg3.length() < 3 || arg3.charAt(0) != '"' || arg3.charAt(arg3.length()-1) != '"') {
	    return false;
	}
	String s = arg3.substring(1,arg3.length()-1);
	if(s == null || s.length() < 1)
	    return false;
	//System.out.println("Statement: "+s);
	String ret;
	switch(st) {
	case SELECT:
	    if(novalidate)
		return true;
	    findElement(list);
	    Select dropdown = new Select(curr_element);
	    dropdown.selectByVisibleText(s);
	    return true;
	case TYPE:
	    if(novalidate)
		return true;
	    findElement(list);
	    curr_element.sendKeys(s);
	    return true;
	case TYPECLR:
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
	    curr_element.sendKeys(s);
	    return true;
	case ASSERTTXT:
	    if(novalidate)
		return true;
	    findElement(list);
	    ret = curr_element.getText();
	    return ret.equals(s);
	default:
	    break;
	}

	// 3 arguments (we already know it starts and stops with quotation marks, find two more
	index2 = curr_line.indexOf("\" \"", index1);
	if(index2 == -1)
	    index2 = curr_line.length();
	String s1 = curr_line.substring (index1+1,index2);
	if(s1 == null || s1.length() < 1)
	    return false;
	//System.out.println("Statement1: "+s1);
	String s2 = curr_line.substring(index2+3,curr_line.length()-1);
	if(s2 == null || s2.length() < 1)
	    return false;
	//System.out.println("Statement2: "+s2);
	
	switch(st) {
	case ASSERTATR:
	    if(novalidate)
		return true;
	    findElement(list);
	    ret = curr_element.getAttribute(s1);
	    return ret.equals(s2);
	case ASSERTCSS:
	    if(novalidate)
		return true;
	    findElement(list);
	    ret = curr_element.getCssValue(s1);
	    return ret.equals(s2);
	case WAITFORATR:
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
	return false;
    }

    
    private static boolean parseScript(boolean novalidate) {
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
		if(!runStatement(novalidate))
		    return false;
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
