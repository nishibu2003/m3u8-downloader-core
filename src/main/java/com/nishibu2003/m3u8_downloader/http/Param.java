package com.nishibu2003.m3u8_downloader.http;

public class Param {
	private String _name;
	private String _value;
	
	public Param(String name, String vallue) {
		_name = name;
		_value = vallue;
	}
	
	public String getName(){
		return _name;
	}
	
	public String getValue(){
		return _value;
	}
}
