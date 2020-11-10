package com.nishibu2003.m3u8_downloader.util;

import java.util.Map;

public class JsonWrapper {
	private String _json;
	private Map<?, ?> _map;

	public JsonWrapper(String json) throws Exception {
		_json = json;
		_map = JSONParser.jsonToMap(json);
	}

	public <T> T getValue(String key, Class<T> className) throws Exception {
		return JSONParser.getValue(this._map, key, className);
	}
	
	public String getJson() {
		return _json;
	}

}
