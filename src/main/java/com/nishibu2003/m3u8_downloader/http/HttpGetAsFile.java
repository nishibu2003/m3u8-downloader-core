package com.nishibu2003.m3u8_downloader.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpRetryException;
import java.util.List;

public class HttpGetAsFile {
	private HttpGet _httpGet;
	private String _path;
	private String _fileName;
	private long _totalSize;
	private Header[] _responseHeaders;

	public HttpGetAsFile(String url, String path, String fileName) {
		_httpGet = new HttpGet(url);
		_path = path;
		_fileName = fileName;
		_responseHeaders = null;
	}
	
	public HttpGetAsFile addHeader(String name ,String value) {
		_httpGet.addHeader(name, value);
		return this;
	}
	
	public HttpGetAsFile addHeader(Header header) {
		_httpGet.addHeader(header);
		return this;
	}

	public HttpGetAsFile addHeader(List<Header> addHaderList) {
		_httpGet.addHeader(addHaderList);
		return this;
	}
	
	public void execute() throws Exception {
		_totalSize = 0;
		OutputStream os = null;
		InputStream is = null;
		try {
			_httpGet.execute();
			_responseHeaders = _httpGet.getAllHeaders();
			if (_httpGet.getStatusCode() == 200) {
				String filePath = _path + File.separatorChar + _fileName;
				os = new FileOutputStream(new File(filePath));
				is = _httpGet.getContents();
				
				int nRead;
				byte[] data = new byte[16384];
				while ((nRead = is.read(data, 0, data.length)) != -1) {
					os.write(data ,0 ,nRead);
					_totalSize += nRead;
				}
			} else {
				int statusCode = _httpGet.getStatusCode();
				String location = _httpGet.getUrl();
				throw new HttpRetryException("Response code: " + statusCode + " for " + location, statusCode, location);
			}
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					// Do nothing.
				}
			}
			
			if (_httpGet != null) {
				_httpGet.close();
			}
		}
	}

	public String getUrl() {
		return _httpGet.getUrl();
	}
	
	public int getStatusCode() {
		return _httpGet.getStatusCode();
	}
	
	public long getTotalSize() {
		return _totalSize;
	}
	
	public Header[] getResponseHeaders() {
		return _responseHeaders;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}


}
