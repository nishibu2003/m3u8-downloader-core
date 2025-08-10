package com.nishibu2003.m3u8_downloader.file;

public class FileUtil {
	public static char[][] FORBIDDEN_CHAR = {
			{'\\','￥'}, {'/','／'}, {':','：'},
			{'*','＊'}, {'?','？'}, {'"','”'},
			{'<','＜'}, {'>','＞'}, {'|','｜'},
			};
	
	public static String forFileName(String name) {
		if (name == null) return null;
		if (name.length() == 0) return name;
		
		char[] buf = name.toCharArray();
		for (int i = 0; i < buf.length; i++) {
			char c = buf[i];
			for (int j = 0; j < FORBIDDEN_CHAR.length; j++) {
				if (c == FORBIDDEN_CHAR[j][0]) {
					buf[i] = FORBIDDEN_CHAR[j][1];
					break;
				}
			}
		}
		return new String(buf);
	}

}
