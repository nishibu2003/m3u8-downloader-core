package com.nishibu2003.m3u8_downloader.http;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class HttpContentsPost {

	private String _url = null;
	private List<Header> _headers = null;
	private List<Param> _params = null;
	
	public HttpContentsPost(String url) {
		_url = url;
		_headers = new ArrayList<Header>();
		_params = new ArrayList<Param>();
	}
	
	public HttpContentsPost addHeader(String name ,String value) {
		Header header = new BasicHeader(name, value);
		_headers.add(header);
		return this;
	}
	
	public HttpContentsPost addParam(String name ,String value) {
		Param param = new Param(name ,value);
		_params.add(param);
		return this;
	}
	
	public String getResponseContent() throws Exception {
		HttpPost httpPost = null;
		try {
			httpPost = new HttpPost(_url, _params);
			httpPost.addHeader(_headers);
			httpPost.execute();
			
			String charset = httpPost.getCharset();
			int contentLength = this.getContentLength(httpPost);
			ByteArrayOutputStream buffer = new ByteArrayOutputStream(contentLength);
			
			InputStream is = httpPost.getContents();
			int nRead;
			byte[] data = new byte[16384];
			while ((nRead = is.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}
			
			return new String(buffer.toByteArray() ,charset);
		} finally {
			if (httpPost != null) {
				httpPost.close();
			}
		}
	}

	private int getContentLength(HttpPost httpPost) {
		String val = httpPost.getFirstHeader("ContentLength");
		if (val == null) {
			return 4096;
		}
		
		try {
			return Integer.parseInt(val);
		} catch (NumberFormatException e) {
			return 4096;
		}
	}
}
