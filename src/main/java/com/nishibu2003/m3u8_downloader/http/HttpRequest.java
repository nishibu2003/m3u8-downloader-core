package com.nishibu2003.m3u8_downloader.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

import com.nishibu2003.m3u8_downloader.util.HttpUtil;

public class HttpRequest {
	
	private String _url = null;
	private String _method = null;
	private HttpURLConnection _urlConn = null;
	private List<Header> _headers = null;
	private List<Param> _params = null;
	private boolean _followRedirects = true;
	private String _contentType = null;
	private String _charset = null;
	private InputStream _contents;
	private int _statusCode = -1;
	private Map<String, List<String>>_responseHeaderMap = null;
	private long _contentLength = -1;
	
	
	public HttpRequest(String method ,String url) {
		this(method, url, new ArrayList<Param>());
	}
	
	public HttpRequest(String method ,String url, List<Param> params) {
		_url = url;
		_method = method;
		_headers = new ArrayList<Header>();
		_params = params;
	}
	
	public HttpRequest addHeader(String name ,String value) {
		Header header = new BasicHeader(name, value);
		this.addHeader(header);
		return this;
	}
	
	public HttpRequest addHeader(Header header) {
		_headers.add(header);
		return this;
	}
	
	public HttpRequest addHeader(List<Header> headers) {
		for (Header header : headers) {
			_headers.add(header);
		}
		return this;
	}
	
	public void execute() throws Exception {
		// originプロパティのセットを許可
		HttpUtil.setAllowRestrictHeaders(true);
		
		_urlConn = null;
		URL url = new URL(_url);
		_urlConn = (HttpURLConnection)url.openConnection();
		if (_urlConn instanceof HttpsURLConnection) {
			((HttpsURLConnection)_urlConn).setSSLSocketFactory(new TLSSocketConnectionFactory());
		}
		
		_urlConn.setConnectTimeout(30000);
		
		_urlConn.setRequestMethod(_method);
		if (_params.size() > 0) {
			_urlConn.setDoOutput(true);
		}
		_urlConn.setInstanceFollowRedirects(_followRedirects);
		boolean hasUserAgent = false;
		StringBuffer cookie = new StringBuffer();
		for (Header header : _headers) {
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
		
		if (_params.size() > 0) {
			OutputStream os = _urlConn.getOutputStream();
			PrintStream ps = new PrintStream(os);
			for (int i = 0; i < _params.size(); i++) {
				if ( i > 0) {
					ps.print("&");
				}
				ps.print(URLEncoder.encode(_params.get(i).getName(), "UTF-8"));
				ps.print("=");
				ps.print(URLEncoder.encode(_params.get(i).getValue(), "UTF-8"));
			}
			os.close();
		}
		
		_statusCode = _urlConn.getResponseCode();
		_responseHeaderMap = _urlConn.getHeaderFields();
		_contentLength = _urlConn.getContentLength();
		_contentType = _urlConn.getContentType();
		_charset = this.getCharset(_contentType);
		InputStream is = null;
		if ((_statusCode / 100) == 2) {
			is = _urlConn.getInputStream();
		} else {
			is = _urlConn.getErrorStream();
		}
		
		String acceptHeader = this.getFirstHeader("Content-Encoding");
		if ("gzip".equals(acceptHeader)) {
			_contents = new GZIPInputStream(is);
		} else if ("deflate".equals(acceptHeader)) {
			_contents = new DeflaterInputStream(is);
		} else {
			_contents = is;
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
	
	public String getFirstHeader(String name) {
		if (_responseHeaderMap == null) {
			return null;
		}
		
		List<String> entry = _responseHeaderMap.get(name);
		if (entry == null) {
			return null;
		}
		
		return entry.get(0);
	}
	
	public void setInstanceFollowRedirects(boolean followRedirects) {
		_followRedirects = followRedirects;
	}
	
	public boolean getInstanceFollowRedirects() {
		return _followRedirects;
	}
}
