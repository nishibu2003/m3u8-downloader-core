package com.nishibu2003.m3u8_downloader.http;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class HttpContentsGet {

	private String _url = null;
	private List<Header> _addHeaderList = null;
	
	public HttpContentsGet(String url) {
		_url = url;
		_addHeaderList = new ArrayList<Header>();
	}
	
	public HttpContentsGet addHeader(String name ,String value) {
		Header header = new BasicHeader(name, value);
		_addHeaderList.add(header);
		
		return this;
	}
	
	public String getResponseContent() throws Exception {
		HttpGet httpGet = null;
		try {
			httpGet = new HttpGet(_url);
			httpGet.addHeader(_addHeaderList);
			httpGet.execute();
			
			String charset = httpGet.getCharset();
			int contentLength = this.getContentLength(httpGet);
			ByteArrayOutputStream buffer = new ByteArrayOutputStream(contentLength);
			
			InputStream is = httpGet.getContents();
			int nRead;
			byte[] data = new byte[16384];
			while ((nRead = is.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}
			
			return new String(buffer.toByteArray() ,charset);
		} finally {
			if (httpGet != null) {
				httpGet.close();
			}
		}
	}

	private int getContentLength(HttpGet httpGet) {
		String val = httpGet.getFirstHeader("ContentLength");
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
