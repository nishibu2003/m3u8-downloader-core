package com.nishibu2003.m3u8_downloader.driver;

import java.util.ArrayList;
import java.util.List;

public class VideoDownloadManager {
	private static List<IVideoDownloader> _list = new ArrayList<IVideoDownloader>();
	
	public static synchronized void register(IVideoDownloader downloader) {
		_list.add(downloader);
	}
	
	public static synchronized IVideoDownloader[] getVideoDownloaders() {
		return _list.toArray(new IVideoDownloader[]{});
	}
	
	public static synchronized List<IVideoDownloader> getVideoDownloaderList() {
		return _list;
	}

}
