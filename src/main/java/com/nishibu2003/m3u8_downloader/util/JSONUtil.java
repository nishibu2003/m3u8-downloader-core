package com.nishibu2003.m3u8_downloader.util;

public class JSONUtil {
	public static String extractJSON(String html, int pos) throws Exception {
		if (html.charAt(pos) != '{') {
			throw new Exception("The first char must be '{'.");
		}
		
		int p = pos + 1;
		int cnt = 1;
		for (; p < html.length(); p++) {
			char c = html.charAt(p);
			if (c == '{') {
				cnt++;
			} else if (c == '}') {
				cnt--;
			}
			
			if (cnt == 0) {
				return html.substring(pos, p + 1);
			}
		}
		
		throw new Exception("The '}' is not found.");
	}
}
