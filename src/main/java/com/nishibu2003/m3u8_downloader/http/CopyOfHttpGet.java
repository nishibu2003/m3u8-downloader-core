package com.nishibu2003.m3u8_downloader.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class CopyOfHttpGet {
	
	private String _url = null;
	private HttpURLConnection _urlConn = null;
	private List<Header> _headerList = null;
	private String _contentType = null;
	private String _charset = null;
	private InputStream _contents;
	private int _statusCode = -1;
	private Map<String, List<String>>_responseHeaderMap = null;
	private long _contentLength = -1;
	
	public CopyOfHttpGet(String url) {
		_url = url;
		_headerList = new ArrayList<Header>();
	}
//	
//	public HttpGet(String url ,String path ,String fileName) throws FileNotFoundException {
//		_url = url;
//		_path = path;
//		_fileName = fileName;
//		_addHeaderList = new ArrayList<Header>();
//	}
	
	public CopyOfHttpGet addHeader(String name ,String value) {
		Header header = new BasicHeader(name, value);
		this.addHeader(header);
		return this;
	}
	
	public CopyOfHttpGet addHeader(Header header) {
		_headerList.add(header);
		return this;
	}

	public CopyOfHttpGet addHeader(List<Header> haderList) {
		_headerList = haderList;
		return this;
	}
	
	public void execute() throws Exception {
		_urlConn = null;
		URL url = new URL(_url);
		_urlConn = (HttpURLConnection)url.openConnection();
		if (_urlConn instanceof HttpsURLConnection) {
			((HttpsURLConnection)_urlConn).setSSLSocketFactory(new TLSSocketConnectionFactory());
		}
		
		_urlConn.setConnectTimeout(30000);
		
		_urlConn.setRequestMethod("GET");
		//_urlConn.setInstanceFollowRedirects(false);
		_urlConn.setInstanceFollowRedirects(true);
		boolean hasUserAgent = false;
		StringBuffer cookie = new StringBuffer();
		for (Header header : _headerList) {
			if ("User-Agent".equals(header.getName())) {
				hasUserAgent = true;
			}
			if ("Cookie".equals(header.getName())) {
				if (cookie.length() > 0) {
					cookie.append("; ");
				}
				cookie.append(header.getValue());
			} else {
				_urlConn.setRequestProperty(header.getName(), header.getValue());
			}
		}
		if (cookie.length() > 0) {
			_urlConn.setRequestProperty("Cookie" ,cookie.toString());
		}
		if (!hasUserAgent) {
			_urlConn.setRequestProperty("User-Agent" ,"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36");
		}
		
		_urlConn.connect();
		
		_statusCode = _urlConn.getResponseCode();
		_responseHeaderMap = _urlConn.getHeaderFields();
		_contentLength = _urlConn.getContentLength();
		_contentType = _urlConn.getContentType();
		_charset = this.getCharset(_contentType);
		if ((_statusCode / 100) == 2) {
			_contents = _urlConn.getInputStream();
		} else {
			_contents = _urlConn.getErrorStream();
		}
	}
	
	public void close() {
		if (_contents != null) {
			try {
				_contents.close();
			} catch (IOException e) {
				// Do nothing.
			}
		}
		if (_urlConn != null) {
			_urlConn.disconnect();
		}
	}
	
	private String getCharset(String contentType) {
		if (contentType == null) {
			return "UTF-8";
		}
		
		String r1 = "charset=([0-9a-zA-Z_\\-]+)";
		Pattern p1 = Pattern.compile(r1);
		Matcher m1 = p1.matcher(contentType);
		if (m1.find()) {
			return m1.group(1);
		}

		String r2 = "encoding=([0-9a-zA-Z_\\-]+)";
		Pattern p2 = Pattern.compile(r2);
		Matcher m2 = p2.matcher(contentType);
		if (m2.find()) {
			return m2.group(1);
		}
		
		String r3 = "charset=\"([0-9a-zA-Z_\\-]+)\"";
		Pattern p3 = Pattern.compile(r3);
		Matcher m3 = p3.matcher(contentType);
		if (m3.find()) {
			return m3.group(1);
		}
		
		return "UTF-8";
	}
	
	public String getUrl() {
		return _url;
	}
	
	public InputStream getContents() {
		return _contents;
	}
	
	public int getStatusCode() {
		return _statusCode;
	}
	
	public long getContentLength() {
		return _contentLength;
	}
	
	public String getContentType() {
		return _contentType;
	}
	
	public String getCharset() {
		return _charset;
	}
	
	public Map<String, List<String>> getResponseHeaderMap() {
		return _responseHeaderMap;
	}
	
	public Header[] getAllHeaders() {
		if (_responseHeaderMap == null) {
			return null;
		}
		
		List<Header> list = new ArrayList<Header>();
		for (Entry<String, List<String>> entry : _responseHeaderMap.entrySet()) {
			List<String> values = entry.getValue();
			for (int i = 0; i < values.size(); i++) {
				Header header = new BasicHeader(entry.getKey(), values.get(i));
				list.add(header);
			}
		}
		return list.toArray(new Header[0]);
	}
	
	public Header getFirstHeader(String name) {
		if (_responseHeaderMap == null) {
			return null;
		}
		
		List<String> entry = _responseHeaderMap.get(name);
		if (entry == null) {
			return null;
		}
		
		return new BasicHeader(name, entry.get(0));
	}
}
