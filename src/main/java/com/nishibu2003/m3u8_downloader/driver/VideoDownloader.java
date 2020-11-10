package com.nishibu2003.m3u8_downloader.driver;

import com.nishibu2003.m3u8_downloader.util.ClassFinder;

import java.util.List;

import javax.net.ssl.SSLException;


public class VideoDownloader {
	
	public VideoDownloader() throws Exception {
		this.initialize();
	}
	
	public void initialize() throws Exception {
		// Driver(VideoDownloader）を読み込む
		new ClassFinder().findClasses("com.nishibu2003.m3u8_downloader.driver");
		
		// ドライバー一覧
		System.out.println("Driver list ----------------------");
		List<IVideoDownloader> list = VideoDownloadManager.getVideoDownloaderList();
		for (IVideoDownloader downloader : list) {
			System.out.println("  ・" + downloader.getDownloaderName());
		}
		System.out.println("----------------------------------");
	}
	
	public void download(String url) throws Exception {
		List<IVideoDownloader> list = VideoDownloadManager.getVideoDownloaderList();
		for (IVideoDownloader downloader : list) {
			boolean ret = false;
			try {
				ret = downloader.download(url);
			} catch (SSLException e) {
				//
				System.err.println(downloader.getClass().getSimpleName() + " : " + e.toString());
			}
			if (ret) return;
		}
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
