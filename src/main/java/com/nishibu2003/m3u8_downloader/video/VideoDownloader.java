package com.nishibu2003.m3u8_downloader.video;

import java.util.List;

import javax.net.ssl.SSLException;

import com.nishibu2003.m3u8_downloader.util.ClassFinder;

public class VideoDownloader {
	
	public VideoDownloader() throws Exception {
		this.initialize();
	}
	
	public void initialize() throws Exception {
		// Driver(VideoDownloader）を読み込む
		new ClassFinder().findClasses("com.nishibu2003.m3u8_downloader.video.driver");
		
		// ドライバー一覧
		System.out.println("Driver list ----------------------");
		List<IVideoDownloader> list = VideoDownloadManager.getVideoDownloaderList();
		for (IVideoDownloader downloader : list) {
			System.out.println("  ・" + downloader.getDownloaderName());
		}
		System.out.println("----------------------------------");
	}
	
	public void download(String url) throws Exception {
		boolean ret = false;
		List<IVideoDownloader> list = VideoDownloadManager.getVideoDownloaderList();
		for (IVideoDownloader downloader : list) {
			try {
				ret = downloader.download(url);
			} catch (SSLException e) {
				//
				System.err.println(downloader.getClass().getSimpleName() + " : " + e.toString());
			}
			if (ret) return;
		}
		
		M3U8VideoDownloader m3u8Downloader = new M3U8VideoDownloader();
		ret = m3u8Downloader.download(url);
		if (ret) return;
		
		System.out.println("No suitable driver.");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String url = args[0];
			
			VideoDownloader downloader = new VideoDownloader();
			downloader.download(url);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
