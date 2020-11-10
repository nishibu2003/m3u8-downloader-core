package com.nishibu2003.m3u8_downloader.m3u8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class M3U8Loader {
	public enum ATTR {NONE ,STREAM, KEY ,INF};
	
	public M3U8 load(String path ,String fileName) throws Exception {
		File file = new File(path + File.separatorChar + fileName);
		String[] lines = this.loadFile(file);

		return load(path ,lines);
	}

	public M3U8 load(String path ,String[] lines) throws Exception {
		if (lines.length == 0) {
			throw new Exception("Invalid m3u8 file.");
		}
		
		if (!"#EXTM3U".equals(lines[0])) {
			throw new Exception("Invalid m3u8 file.");
		}
		
		M3U8 m3u8 = new M3U8();
		m3u8.setLines(lines);
		
		Map<String ,String> paramMap = new LinkedHashMap<String ,String>();
		List<String[]> streamList = new ArrayList<String[]>();
		List<String[]> keyList = new ArrayList<String[]>();
		List<String[]> infList = new ArrayList<String[]>();
		
		ATTR flg = ATTR.NONE;
		String[] param = new String[]{"" ,""};
		for (String s : lines) {
			if (s.startsWith("#")) {
				if (s.startsWith("#EXT-X-STREAM-INF:")) {
					flg = ATTR.STREAM;
					param = new String[]{s ,""};
				} else if (s.startsWith("#EXT-X-KEY:")) {
						flg = ATTR.KEY;
						param = new String[]{s ,""};
				} else if (s.startsWith("#EXTINF:")) {
					flg = ATTR.INF;
					param = new String[]{s ,""};
				} else {
					flg = ATTR.NONE;
					int pos = s.indexOf(":");
					if (pos >= 0) {
						String p = s.substring(0, pos);
						String v = s.substring(pos + 1);
						paramMap.put(p, v);
					} else {
						paramMap.put(s, "");
					}
				}
			} else {
				if (flg == ATTR.STREAM) {
					param[1] = s;
					streamList.add(param);
				} else if (flg == ATTR.KEY) {
					param[1] = s;
					keyList.add(param);
				} else if (flg == ATTR.INF) {
					param[1] = s;
					infList.add(param);
				}
				flg = ATTR.NONE;
			}
		}
		
		m3u8.setParamMap(paramMap);
		m3u8.setStreamList(streamList);
		m3u8.setKeyList(keyList);
		m3u8.setInfList(infList);
		
		return m3u8;
	}
	
	public String[] loadFile(File file) throws IOException {
		List<String> list = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
		
		String str;
		while((str = br.readLine()) != null){
			list.add(str);
		}
		br.close();
		
		return list.toArray(new String[]{});
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String path = "C:\\ide\\work";
		String fileName = "gyao.m3u8";
		try {
			M3U8Loader loader = new M3U8Loader();
			M3U8 m3u8 = loader.load(path, fileName);
			System.out.println(m3u8.toString());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
