package com.nishibu2003.m3u8_downloader.http;

import java.util.ArrayList;
import java.util.List;

public class HttpPost extends HttpRequest {
	public HttpPost(String url) {
		super("POST", url, new ArrayList<Param>());
	}
	public HttpPost(String url, List<Param> params) {
		super("POST", url, params);
	}
}
