package com.nishibu2003.m3u8_downloader.http;


public class HttpGet extends HttpRequest {
	public HttpGet(String url) {
		super("GET", url);
	}
}
