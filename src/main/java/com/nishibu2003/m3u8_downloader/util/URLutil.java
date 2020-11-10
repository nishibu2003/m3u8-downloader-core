package com.nishibu2003.m3u8_downloader.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLutil {
	/**
	 * ベースのURLに対し、相対パスをマージしたURLを返す
	 * 
	 * @param url1
	 * @param url2
	 * @return
	 * @throws MalformedURLException
	 */
	public static String normalize(String url1 ,String url2) throws MalformedURLException {
		if (url2.indexOf(":") > 0) return url2;
		
		URL url = new URL(new URL(url1) ,url2);
		return url.toString();
	}

	public static URI resolve(URI baseURI, String reference) throws Exception {
		if (reference == null || reference.isEmpty()) {
			return baseURI;
		}

		String r1 = "(.*?//.*?)/(.*)";
		Pattern p1 = Pattern.compile(r1);
		Matcher m1 = p1.matcher(baseURI.toASCIIString());
		if (m1.find()) {
			String host = m1.group(1);
			String base = m1.group(2);

			if (reference.startsWith("/")) {
				return new URI(host + reference);
			}

			String[] baseArray = base.split("/");
			String[] refArray = reference.split("/");

			int baseIdx = baseArray.length -2;
			if (base.endsWith("/")) {
				baseIdx++;
			}

			int refIdx = 0;
			for (refIdx = 0; refIdx < refArray.length; refIdx++) {
				if (".".equals(refArray[refIdx])) {
					// Do nothing
				} else if ("..".equals(refArray[refIdx])) {
					baseIdx--;
				} else {
					break;
				}
			}

			StringBuffer sb = new StringBuffer();
			sb.append(host);
			for (int i = 0; i <= baseIdx; i++) {
				sb.append("/").append(baseArray[i]);
			}
			for (int i = refIdx; i < refArray.length; i++) {
				sb.append("/").append(refArray[i]);
			}

			return new URI(sb.toString());
		}
		throw new Exception("Invalid URL syntax");
	}
}
