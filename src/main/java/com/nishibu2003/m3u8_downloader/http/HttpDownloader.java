package com.nishibu2003.m3u8_downloader.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import com.nishibu2003.m3u8_downloader.http.DownloadPart.DownloadStatus;

public class HttpDownloader implements Runnable {
	
	public static final int BLOCK_SIZE = 4096;
	public static final int BUFFER_SIZE = 4096;
	public static final int MIN_DOWNLOAD_SIZE = BLOCK_SIZE * 100;
	
	
	private List<Header> _addHeaderList = new ArrayList<Header>();

	private ISync _sync;
	
	private int _threadId;
	private String _url;
	private String _path;
	private String _fileName;
	List<DownloadPart> _downloadPartList;
	
	public HttpDownloader(ISync sync) {
		_sync = sync;
		_sync.addSync();
	}

	public HttpDownloader addHeader(String name, String value) {
		Header header = new BasicHeader(name, value);
		_addHeaderList.add(header);
		return this;
	}
	
	@Override
	public void run() {
		try {
			String url = this.getUrl();
			String path = this.getPath();
			String fileName = this.getFileName();
			
			this.download(url, path, fileName);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		_sync.delSync();
	}

	private DownloadPart changeStatus(DownloadStatus downloadStatus) {
		List<DownloadPart> downloadPartList = this.getDownloadPartList();
		synchronized (downloadPartList) {
			int threadId = this.getThreadId();
			DownloadPart downloadPart = downloadPartList.get(threadId);
			downloadPart.setDownloadStatus(downloadStatus);
			if (downloadStatus == DownloadStatus.READY) {
				downloadPart.resetDownloadedByte();
			}
			return downloadPart;
		}
	}

	public void download(String url, String path, String fileName) throws UnsupportedOperationException, IOException {
		DownloadPart downloadPart = this.changeStatus(DownloadStatus.DOWNLOADING);
		
		RandomAccessFile raf = null;
		InputStream is = null;
		HttpGet request = null;
		try {
			request = new HttpGet(url);
			StringBuffer cookie = new StringBuffer();
			for (Header header : _addHeaderList) {
				if ("Cookie".equals(header.getName())) {
					if (cookie.length() > 0) {
						cookie.append("; ");
					}
					cookie.append(header.getValue());
				} else {
					request.addHeader(header);
				}
			}
			if (cookie.length() > 0) {
				request.addHeader("Cookie" ,cookie.toString());
			}
			
			String byteRange = downloadPart.getStartByte() + "-" + downloadPart.getEndByte();
			request.addHeader("Range" ,"bytes=" + byteRange);
			
			request.execute();
			
			int statusCode = request.getStatusCode();
			
			System.out.println(this.getThreadId() + " : Request Url: " + request.getUrl());
			System.out.println(this.getThreadId() + " : Range: " + byteRange);
			System.out.println(this.getThreadId() + " : Response Code: " + statusCode);
			
			if (statusCode / 100 != 2) {
				this.changeStatus(DownloadStatus.READY);
				return;
			}
			
			String filePath = path + File.separatorChar + fileName;
			raf = new RandomAccessFile(filePath, "rw");
			raf.seek(downloadPart.getStartByte());

			is = request.getContents();
			int len;
			byte[] bytes = new byte[8192];
			while ((len = is.read(bytes)) != -1) {
				long startByte = downloadPart.getStartByte();
				long endByte = downloadPart.getEndByte();
				long downloadedByte = downloadPart.getDownloadedByte();
				long currentEndByte = startByte + downloadedByte -1;
				if (currentEndByte + len >= endByte) {
					int len2 = (int)(endByte - currentEndByte);
					raf.write(bytes ,0 ,len2);
					this.increaseDownloadedByte(len2);
					break;
				} else {
					raf.write(bytes ,0 ,len);
					this.increaseDownloadedByte(len);
				}
			}

			long startByte = downloadPart.getStartByte();
			long endByte = downloadPart.getEndByte();
			long targetByte = endByte - startByte + 1;
			long downloadedByte = downloadPart.getDownloadedByte();
			String msg = startByte + "-" + endByte + " ,target :" + targetByte + " ,real :" + downloadedByte;
			System.out.println(this.getThreadId() + " Completed!!! " + msg);
			request.close();
			this.changeStatus(DownloadStatus.FINISHED);
			Thread.sleep(2000);
		} catch (Exception e) {
			e.printStackTrace();
			this.changeStatus(DownloadStatus.READY);
		} finally {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {}
			}
			
			if (request != null) {
				request.close();
			}
		}
    }

	private long increaseDownloadedByte(int len) {
		List<DownloadPart> downloadPartList = this.getDownloadPartList();
		synchronized (downloadPartList) {
			int threadId = this.getThreadId();
			DownloadPart downloadPart = downloadPartList.get(threadId);
			return downloadPart.increaseDownloadedByte(len);
		}
	}

	public int getThreadId() {
		return _threadId;
	}

	public void setThreadId(int _threadId) {
		this._threadId = _threadId;
	}

	public String getUrl() {
		return _url;
	}

	public void setUrl(String _url) {
		this._url = _url;
	}

	public String getPath() {
		return _path;
	}

	public void setPath(String _path) {
		this._path = _path;
	}

	public String getFileName() {
		return _fileName;
	}

	public void setFileName(String _fileName) {
		this._fileName = _fileName;
	}

	public void setDownloadPartList(List<DownloadPart> downloadPartList) {
		_downloadPartList = downloadPartList;
	}
	
	public List<DownloadPart> getDownloadPartList() {
		return _downloadPartList;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
