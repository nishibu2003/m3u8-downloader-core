package com.nishibu2003.m3u8_downloader.util;

import com.nishibu2003.m3u8_downloader.http.BasicHeader;
import com.nishibu2003.m3u8_downloader.http.Header;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class HeaderUtil {
	public static Header[] getCookie(Header[] headers) {
		if (headers == null) return null;
		
		List<Header> list = new ArrayList<Header>();
		
		Header[] setCookies = getHdaders(headers ,"Set-Cookie");
		for (Header cookie : setCookies) {
			String originalValue = cookie.getValue();
			int pos = originalValue.indexOf(";");
			String value;
			if (pos > 0) {
				value = originalValue.substring(0 ,pos);
			} else {
				value = originalValue;
			}
			Header header = new BasicHeader("Cookie", value);
			list.add(header);
		}
		
		Header[] cookies = getHdaders(headers ,"Cookie");
		list.addAll(Arrays.asList(cookies));
		
		return list.toArray(new Header[]{});
	}
	
	public static Header[] getHdaders(Header[] headers ,String name) {
		List<Header> list = new ArrayList<Header>();
		for (Header header : headers) {
			if (name.equals(header.getName())) {
				list.add(header);
			}
		}
		return list.toArray(new Header[0]);
	}
}
