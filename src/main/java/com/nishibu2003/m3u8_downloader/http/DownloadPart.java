package com.nishibu2003.m3u8_downloader.http;

public class DownloadPart {
	public enum DownloadStatus {READY ,DOWNLOADING ,FINISHED ,ERROR};
	
	private long _startByte;
	private long _endByte;
	private long _downloadedByte;
	private DownloadStatus _downloadStatus;
	private int _wait;
	private int _retryCount;
	
	DownloadPart(long startByte ,long endByte) {
		_startByte = startByte;
		_endByte = endByte;
		_downloadedByte = 0;
		_downloadStatus = DownloadStatus.READY;
		_wait = 0;
		_retryCount = 0;
	}
	
	public long getStartByte() {
		return _startByte;
	}
	
	public long getEndByte() {
		return _endByte;
	}
	
	public void setEndByte(long endByte) {
		_endByte = endByte;
	}
	
	public DownloadStatus getDownloadStatus() {
		return _downloadStatus;
	}
	
	public void setDownloadStatus(DownloadStatus downloadStatus) {
		_wait = 0;
		_downloadStatus = downloadStatus;
	}
	
	public synchronized int incrementWait() {
		_wait++;
		return _wait;
	}
	
	public int getRetryCount() {
		return _retryCount;
	}
	
	public synchronized int incrementRetryCount() {
		_retryCount++;
		return _retryCount;
	}

	public synchronized long increaseDownloadedByte(int len) {
		_downloadedByte += len;
		return _downloadedByte;
	}

	public synchronized long resetDownloadedByte() {
		_downloadedByte = 0;
		return 0;
	}
	
	public synchronized long getDownloadedByte() {
		return _downloadedByte;
	}
	
	public String toString() {
		return "startByte: " + _startByte + " ,endByte: " + _endByte + " ,downloadedByte: " + _downloadedByte + " ,status: " + _downloadStatus.name() + " ,retry: " + _retryCount;
	}

}
