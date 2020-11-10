package com.nishibu2003.m3u8_downloader.http;


public class HttpOptions extends HttpRequest {
	public HttpOptions(String url) {
		super("OPTIONS", url);
	}
}
