package com.nishibu2003.m3u8_downloader.video;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.nishibu2003.m3u8_downloader.http.HttpGet;
import com.nishibu2003.m3u8_downloader.m3u8.M3u8MultiDownloader;

public class M3U8VideoDownloader extends AbstractVideoDownloader {

	@Override
	public String getDownloaderName() {
		return "M3U8";
	}
	
	@Override
	public boolean download(String url) throws Exception {
		String m3u8URL = "";
		m3u8URL = this.analyze1(url);
		
		if (!"".equals(m3u8URL)) {
			M3u8MultiDownloader downloader = new M3u8MultiDownloader();
			downloader.execute(m3u8URL, this.getActualPath(), this.getActualFileName());
			return true;
		}
		
		return false;
	}

	public String analyze1(String url) throws Exception, IOException {
		String url2 = url.replace("[", "%5B").replace("]", "%5D");
		HttpGet get = null;
		try {
			get = new HttpGet(url2);
			get.execute();
			if (get.getStatusCode() != 200) {
				return "";
			}
			
			List<String> validContentsType = Arrays.asList("application/x-mpegURL");
			if (validContentsType.contains(get.getContentType())) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
				String fileName = sdf.format(new Date()) + "-m3u8";
				this.setDefaultPath(this.getCurrentDirectory() + File.separator + fileName);
				this.setDefaultFileName(fileName + ".ts");
				return url2;
			}
		} finally {
			if (get != null) {
				get.close();
			}
		}
		
		return "";
	}
	
	public static void main(String[] args) {
		try {
			IVideoDownloader downloader = new M3U8VideoDownloader();
			String url = args[0];
			downloader.setPath((args.length >= 2) ? args[1] : null);
			downloader.setFileName((args.length >= 3) ? args[2] : null);
			downloader.download(url);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
