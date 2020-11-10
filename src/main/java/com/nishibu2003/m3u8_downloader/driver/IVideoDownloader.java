package com.nishibu2003.m3u8_downloader.driver;

public interface IVideoDownloader {
	public String getDownloaderName();
	
	public boolean download(String url) throws Exception;
	
	public void setPath(String path) throws Exception;
	
	public String getPath() throws Exception;
	
	public void setFileName(String fileName) throws Exception;
	
	public String getFileName() throws Exception;

}
