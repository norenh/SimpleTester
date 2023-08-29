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
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.time.Duration;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;
import java.nio.file.Files;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Scanner;
import java.nio.file.StandardCopyOption;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.Capabilities;

// javac -cp "selenium-java-4.11.0/*:selenium-java-4.11.0/lib/*" SimpleTester.java
// java -cp "selenium-java-4.11.0/*:selenium-java-4.11.0/lib/*:." SimpleTester

public class SimpleTester {
    
    private final static HashMap<String, ArrayList<By>> selectors = new HashMap<String, ArrayList<By>>();
    private static WebDriver curr_driver;
    private static ArrayList<By> curr_by_list;
    private static WebElement curr_element;
    private static FileReader config_file;
    private static FileReader script_file;
    private static boolean script_done = false;
    private static EnumStmt stmt;
    private enum EnumStmt {
	FIND,
	SELECT,
	ASSERTTXT,
	ASSERTVAL,
	CLICK,
	TYPE,
	WAITFOR,
	WAIT,
	FINISH;
    }
    
    private final static HashMap<String, EnumStmt> statements = new HashMap<String, EnumStmt>() {{
	    put("find",      stmt.FIND);
	    put("select",    stmt.SELECT);
	    put("asserttxt", stmt.ASSERTTXT);
	    put("assertval", stmt.ASSERTVAL);
	    put("click",     stmt.CLICK);
	    put("type",      stmt.TYPE);
	    put("waitfor",   stmt.WAITFOR);
	    put("wait",      stmt.WAIT);
	    put("finish",    stmt.FINISH);
	}};

    private final static HashMap<String, Integer> by_names = new HashMap<String, Integer>() {{
	    put("id",          0);
	    put("name",        1);
	    put("cssSelector", 2);
	    put("tagName",     3);
	    put("className",   4);
	    put("linkText",    5);
	    put("xpath",       6);
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
    private static boolean parseConfig() {
	BufferedReader buffer = new BufferedReader(config_file);
	linenr = 0;
	try {
	    String curr_line = buffer.readLine();
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
		    Integer ret = by_names.get(by_string);
		    if(ret == null)
			return false;
		    index1 = index2+1;
		    index2 = curr_line.indexOf('"', index1+1);
		    if(index2 == -1)
			return false;
		    String selector = curr_line.substring(index1+1, index2);
		    By by;
		    switch(ret.intValue()) {
		    case 0:
			by = By.id(selector);
			break;
		    case 1:
			by = By.name(selector);
			break;
		    case 2:
			by = By.cssSelector​(selector);
			break;
		    case 3:
			by = By.tagName(selector);
			break;
		    case 4:
			by = By.className(selector);
			break;
		    case 5:
			by = By.linkText(selector);
			break;
		    case 6:
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

    private static boolean runStatement(boolean novalidate, String curr_line) {
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
		try { Thread.sleep(x*100); } catch(Exception e) {}
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
	case ASSERTTXT:
	    if(novalidate)
		return true;
	    findElement(list);
	    ret = curr_element.getText();
	    return ret.equals(s);
	case ASSERTVAL:
	    if(novalidate)
		return true;
	    findElement(list);
	    ret = curr_element.getAttribute("value");
	    return ret.equals(s);
	default:
	    return false;
	}
	//return true;
    }

    
    private static boolean parseScript(boolean novalidate) {
	BufferedReader buffer = new BufferedReader(script_file);
	String curr_line = "";
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
		if(!runStatement(novalidate, curr_line))
		    return false;
		curr_line = buffer.readLine();
	    }
	}
	catch(Exception e) {
	    if(novalidate)
		System.out.println("FAIL: "+linenr+":"+curr_line);
	    else {
		
		System.out.println("FAIL: Error parsing script file "+e.toString());
		e.printStackTrace(System.out);
	    }
	    System.exit(2);
	}
	return true;
    }

    public static void main(String[] args) {
	if(args.length < 3) {
	    System.out.println("Usage: ./run.sh <configfile> <URL> <script>");
	    System.exit(1);
	}
	String cfgfile = args[0];
	String url = args[1];
	String sfile = args[2];

	System.out.println("INFO: Using Config "+cfgfile);
	System.out.println("INFO: Using URL: "+url);
	System.out.println("INFO: Using Script: "+sfile);

	try {
	    config_file = new FileReader(cfgfile);
	    script_file = new FileReader(sfile);
	}
	catch(Exception e) {
	    System.out.println("FAIL: Unable to open file "+e.toString());
	    System.exit(2);
	}
	if(!parseConfig()) {
	    System.out.println("FAIL: Unable to parse config file "+cfgfile+":"+linenr);
	    System.exit(2);
	}
	
	if(!parseScript(true)) {
	    System.out.println("FAIL: Unable to parse script file "+sfile+":"+linenr);
	    System.exit(2);
	}
	try {
	    config_file.close();
	    script_file.close();
	    script_file = new FileReader(sfile);
	}
	catch(Exception e) {
	    System.out.println("FAIL: Unable to open script file "+e.toString());
	    e.printStackTrace(System.out);
	    System.exit(2);
	}
	
	curr_driver = new ChromeDriver();
	curr_driver.get(url);
	//String title = curr_driver.getTitle();
	//System.out.println(title);

	try {
	    if(parseScript(false)) {
		System.out.println("SUCCESS: "+sfile);
	    }
	    else {
		System.out.println("FAIL: "+sfile+":"+linenr);
		takeScreenshot(sfile+".png");
	    }
	}
	catch(Exception e) {
	    e.printStackTrace(System.out);
	    System.out.println(e.toString());
	    System.out.println("FAIL: Running "+sfile+":"+linenr);
	    System.exit(2);
	}
	curr_driver.quit();
	System.exit(0);
    }

}
