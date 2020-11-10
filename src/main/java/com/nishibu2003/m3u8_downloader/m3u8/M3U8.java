package com.nishibu2003.m3u8_downloader.m3u8;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class M3U8 {
	String[] _lines;
	Map<String, String> _paramMap;
	List<String[]> _streamList;
	List<String[]> _keyList;
	List<String[]> _infList;
	
	public String[] getLines() {
		return _lines;
	}
	
	public void setLines(String[] lines) {
		_lines = lines;
	}

	public void setParamMap(Map<String, String> paramMap) {
		_paramMap = paramMap;
	}

	public Map<String, String> getParamMap() {
		return _paramMap;
	}

	public void setStreamList(List<String[]> streamList) {
		_streamList = streamList;
	}

	public List<String[]> getStreamList() {
		return _streamList;
	}

	public void setKeyList(List<String[]> keyList) {
		_keyList = keyList;
	}

	public List<String[]> getKeyList() {
		return _keyList;
	}

	public void setInfList(List<String[]> infList) {
		_infList = infList;
	}

	public List<String[]> getInfList() {
		return _infList;
	}

	public M3U8 clone() {
		M3U8 m3u8 = new M3U8();
		m3u8.setLines(this.getLines().clone());
		
		Map<String ,String> paramMap = new LinkedHashMap<String, String>();
		for (String name : this.getParamMap().keySet()) {
			paramMap.put(name, this.getParamMap().get(name));
		}
		m3u8.setParamMap(paramMap);
		
		List<String[]> streamList = new ArrayList<String[]>();
		for (String[] stream : this.getStreamList()) {
			streamList.add(stream);
		}
		m3u8.setStreamList(streamList);
		
		List<String[]> keyList = new ArrayList<String[]>();
		for (String[] key : this.getKeyList()) {
			keyList.add(key);
		}
		m3u8.setKeyList(keyList);
		
		List<String[]> infList = new ArrayList<String[]>();
		for (String[] inf : this.getInfList()) {
			infList.add(inf);
		}
		m3u8.setInfList(infList);
		
		return m3u8;
	}
}
