package com.nishibu2003.m3u8_downloader.m3u8;

import java.util.ArrayList;
import java.util.List;

public class MultiDownloadItems {
	public static int INDEX = 0;
	public static int STATUS = 1;
	public static int URL = 2;
	public static int PATH = 3;
	public static int FILE_NAME = 4;
	
	private int _lastIndex = -1;
	private int _assignedIndex = -1;
	
	private List<String[]> _list;
	
	public MultiDownloadItems() {
		_list = new ArrayList<String[]>();
	}
	
	public synchronized String[] add(String url ,String path ,String fileName) {
		_lastIndex++;
		String[] values = new String[5];
		values[INDEX] = String.valueOf(_lastIndex);
		values[STATUS] = "NOT YET";
		values[URL] = url;
		values[PATH] = path;
		values[FILE_NAME] = fileName;
		
		_list.add(values);
		
		return values;
	}
	
	public synchronized String[] assign() {
		if (_assignedIndex >= _list.size() -1) return null;
		
		_assignedIndex++;
		String[] values = _list.get(_assignedIndex);
		values[STATUS] = "ASSIGNED";
		
		return values;
	}
	
	public synchronized int size() {
		return _list.size();
	}
}
