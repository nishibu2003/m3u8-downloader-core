package com.nishibu2003.m3u8_downloader.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class HttpUtil {
	/**
	 * HTTPリクエストヘッダに特定のプロパティをセットする制限の許可
	 * ※Originなどをセットする場合は制限を解除（trueをセット)する必要がある
	 * 
	 * ※"Access-Control-Request-Headers", "Access-Control-Request-Method", "Connection", 
	 *   "Content-Length", "Content-Transfer-Encoding", "Expect", "Host", "Keep-Alive", "Origin", 
	 *   "Trailer", "Transfer-Encoding", "Upgrade", "Via"
	 * @param allwResriceHeaders
	 */
	public static void setAllowRestrictHeaders(boolean allwResriceHeaders) {
		try {
			String strClass = "sun.net.www.protocol.http.HttpURLConnection";
			Class<?> c = Class.forName(strClass);
			Field f = c.getDeclaredField("allowRestrictedHeaders");
			f.setAccessible(true);
			
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
			
			f.set(null, allwResriceHeaders);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
