package com.nishibu2003.m3u8_downloader.file;

import java.io.UnsupportedEncodingException;

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
	
	public static String truncateForDir(String str, int len) {
		
		byte[] bytes = null;
		try {
			bytes = str.getBytes("Windows-31j");
		} catch (UnsupportedEncodingException e) {
			// Do nothing
		}

		if (bytes.length <= len) {
			return str;
		}

		// 切り捨てたバイト列を格納する
		byte[] truncated = new byte[len];
		System.arraycopy(bytes, 0, truncated, 0, len);

		// バイト列から文字列に戻す際、文字化けを防ぐために
		// 末尾から順に1バイトずつ削って、正しくデコードできる位置を探す
		for (int i = len; i >= 0; i--) {
			try {
				return new String(truncated, 0, i, "Windows-31j");
			} catch (Exception e) {
				// 無視して次へ
			}
		}

		return ""; // すべて失敗した場合は空文字を返す
	}
}
