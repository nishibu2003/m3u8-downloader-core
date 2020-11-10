package com.nishibu2003.m3u8_downloader.m3u8;

import com.nishibu2003.m3u8_downloader.http.HttpGetAsFile;
import com.nishibu2003.m3u8_downloader.util.URLutil;

import java.net.URI;
import java.util.List;


public class M3u8Downloader {
	
	public void execute(String url, String path, String fileName) throws Exception {
		
		// M3U8ファイルのダウンロード
		this.downloadM3U8(url, path, fileName);
		M3U8Loader loader = new M3U8Loader();
		M3U8 m3u8 = loader.load(path, fileName);
		
		List<String[]> streamList = m3u8.getStreamList();
		if (streamList.size() > 0) {
			String streamURL = M3U8Analyzer.choiceStream(streamList);
			URI uri = URLutil.resolve(new URI(url), streamURL);
			this.downloadM3U8(uri.toString(), path, "quality.m3u8");
			m3u8 = loader.load(path, "quality.m3u8");
		}
		
		M3U8 newM3u8 = m3u8.clone();
		
		List<String[]> keyList = m3u8.getKeyList();
		if (keyList.size() > 0) {
			for (int i = 0; i < keyList.size(); i++) {
				String[] key = keyList.get(i);
				String keyName = String.format("encrypt%d", i) + ".key";
				String keyURI = M3U8Analyzer.getKeyURI(key[1]);
				if (keyURI != null) {
					URI uri = URLutil.resolve(new URI(url), keyURI);
					HttpGetAsFile downloader = new HttpGetAsFile(uri.getRawPath(), path, "key");
					downloader.execute();
					key[2] = uri.toString();
					//Downloader2 downloader = new Downloader2();
					//URI uri = URLutil.resolve(new URI(url), keyURI);
					//downloader.download(uri.toString(), path, "key");
					
					newM3u8.getParamMap().put("#EXT-X-KEY", m3u8.getParamMap().get("#EXT-X-KEY").replaceAll("URI=\"(.+)\"", "URI=\"" + keyName + "\""));
				}
						
			}
			
		}
		
		List<String[]> infList = m3u8.getInfList();
		if (infList.size() > 0) {
			
			for (int i = 0; i < infList.size(); i++) {
				String[] inf = infList.get(i);
				String tsName = String.format("%05d", i) + ".ts";
				URI uri = URLutil.resolve(new URI(url), inf[1]);
				HttpGetAsFile downloader = new HttpGetAsFile(uri.getRawPath(), path, tsName);
				downloader.execute();
//				Downloader2 downloader = new Downloader2();
//				downloader.download(uri.toString(), path, tsName);
				
				newM3u8.getInfList().get(i)[1] = tsName;
				newM3u8.getInfList().get(i)[2] = uri.toString();
			}
			
			M3U8Writer writer = new M3U8Writer();
			writer.write(path ,"new.m3u8" ,newM3u8);
		}
	}
	
	/**
	 * M3U8ファイルのダウンロード
	 * 
	 * @param url
	 * @param path
	 * @param fileName
	 * @throws Exception 
	 */
	public void downloadM3U8(String url, String path, String fileName) throws Exception {
		HttpGetAsFile downloader = new HttpGetAsFile(url, path, fileName);
		downloader.execute();
		//Downloader2 downloader = new Downloader2();
		//downloader.download(url, path, fileName);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String url = "https://n24-cdn-live.ntv.co.jp/ch01/High.m3u8";
			String path = "C:\\ide\\work";
			String fileName = "nittere.m3u8";
			
			M3u8Downloader m3u8Downloader = new M3u8Downloader();
			m3u8Downloader.execute(url, path, fileName);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
