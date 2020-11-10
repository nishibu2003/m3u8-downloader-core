package com.nishibu2003.m3u8_downloader.m3u8;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class M3U8Analyzer {

	public static String choiceStream(List<String[]> streamList) {
		Pattern p = Pattern.compile("BANDWIDTH=([0-9]+)");
		
		long bandwidth = -1;
		String value = "";
		for (String[] stream : streamList) {
			Matcher m = p.matcher(stream[0]);
			if (m.find()) {
				String s = m.group(1);
				long val = Long.parseLong(s);
				if (bandwidth < val) {
					bandwidth = val;
					value = stream[1];
				}
			}
		}
		
		if (bandwidth == -1) {
			value = streamList.get(0)[1];
		}
		
		return value;
	}

	public static String getKeyURI(String line) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}
}
