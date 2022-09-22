package com.nishibu2003.m3u8_downloader.util;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class JavascriptRunner {
	/**
	 * JavaScriptを実行する
	 * @param javascript
	 * @param variables
	 * @return
	 * @throws ScriptException
	 */
	public static Object[] runJavaScript(String javascript, String[] variables) throws ScriptException {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("JavaScript");
		engine.eval(javascript);
		List<Object> objects = new ArrayList<Object>();
		for (String variable : variables) {
			Object obj = engine.get(variable);
			objects.add(obj);
		}
		return objects.toArray(new Object[] {});
	}
	
	public static void main(String[] args) {
		try {
			StringBuffer javascript = new StringBuffer();
			javascript.append("var currentDate = new Date();").append("\n");
			javascript.append("var jsonObj = {a:100, b:200};").append("\n");
			javascript.append("var json = JSON.stringify(jsonObj);").append("\n");
			javascript.append("var CustomObj = function(){};").append("\n");
			javascript.append("CustomObj.prototype.add = function(a, b){").append("\n");
			javascript.append("  this.addResult = a + b;").append("\n");
			javascript.append("};").append("\n");
			javascript.append("var customObj = new CustomObj();").append("\n");
			javascript.append("customObj.add(1000, 100);").append("\n");
			javascript.append("var addResult = customObj.addResult;").append("\n");
			
			String[] variables = new String[] {"currentDate", "jsonObj", "json", "customObj", "addResult"};
			Object[] objects = JavascriptRunner.runJavaScript(javascript.toString(), variables);
			
			for (Object obj : objects) {
				System.out.println(obj.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
