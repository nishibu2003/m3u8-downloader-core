package com.nishibu2003.m3u8_downloader.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import com.nishibu2003.m3u8_downloader.http.DownloadPart.DownloadStatus;

public class HttpMultiDownloader implements ISync{
	
	private class HttpDownloadManager implements Runnable {
		private ISync _sync;
		private List<DownloadPart> _downloadPartList;
		private long _totalByte = 0;
		private long _downloadedByte = 0;
		
		public HttpDownloadManager(ISync sync) {
			_sync = sync;
			_sync.addSync();
		}
		
		@Override
		public void run() {
			NumberFormat nfNum = NumberFormat.getNumberInstance();
			NumberFormat nfPer = NumberFormat.getPercentInstance();
			try {
				this.calc();
				long downloadByte = this.getDownloadedByte();
				long totalByte = this.getTotalByte();
				while (downloadByte < totalByte) {
					double per = (double)downloadByte / (double)totalByte;
					System.out.println(nfNum.format(downloadByte) + " bytes Of " + nfNum.format(totalByte) + " (" + nfPer.format(per) + ")");
					Thread.sleep(1000);
					
					this.calc();
					downloadByte = this.getDownloadedByte();
					totalByte = this.getTotalByte();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			_sync.delSync();
		}

		public void setDownloadPartList(List<DownloadPart> downloadPartList) {
			_downloadPartList = downloadPartList;
		}
		
		public long getTotalByte() {
			return _totalByte;
			
		}
		
		public long getDownloadedByte() {
			return _downloadedByte;
		}
		
		public void calc() {
			synchronized (_downloadPartList) {
				long totalByte = 0;
				long downloadedByte = 0;
				
				for (int i = 0; i < _downloadPartList.size(); i++) {
					DownloadPart downloadPart = _downloadPartList.get(i);
					totalByte += downloadPart.getEndByte() - downloadPart.getStartByte() + 1;
					downloadedByte += downloadPart.getDownloadedByte();
				}
				
				_totalByte = totalByte;
				_downloadedByte = downloadedByte;
			}
		}
	}
	
	
	
	private int _maxThreadCount = 20;
	private String _url;
	private String _path;
	private String _fileName;
	private List<DownloadPart> _downloadPartList;
	
	private List<Header> _addHeaderList = new ArrayList<Header>();

	private int _threadCount;
	
	public HttpMultiDownloader() {
		_threadCount = 0;
	}
	
	public HttpMultiDownloader(String url, String path, String fileName) throws Exception {
		_threadCount = 0;
		this.execute(url, path, fileName);
	}
	
	public HttpMultiDownloader addHeader(String name ,String value) {
		Header header = new BasicHeader(name, value);
		_addHeaderList.add(header);
		return this;
	}
	
	public void execute(String url, String path, String fileName) throws Exception {
		_url = url;
		_path = path;
		_fileName = fileName;
		
		this.mkdir(path);
		
		HttpGet request = null;
		try {
			request = new HttpGet(url);
			request.addHeader(_addHeaderList);
			request.execute();
			
			// Make sure the response code is in the 200 range.
			int responseCode = request.getStatusCode();
			if (responseCode / 100 != 2) {
				throw new Exception("Response Code : " + responseCode + " ,Request URL : " + url);
			}
			
			System.out.println("Response headers *************");
			Header[] headers = request.getAllHeaders();
			for (Header header : headers) {
				System.out.println("   " + header.getName() + ": " + header.getValue());
			}
			
			// supported partial content?
			String acceptRange = request.getFirstHeader("Accept-Ranges");
			long contentLength = request.getContentLength();
			boolean isSupportPartialContent = (acceptRange != null && "bytes".equals(acceptRange) && contentLength > HttpDownloader.MIN_DOWNLOAD_SIZE);
			
			if (isSupportPartialContent) {
				request.close();
				this.multiDownload(contentLength);
			} else {
				this.singleDownload(request.getContents());
			}
		} finally {
			request.close();
        }
	}

	private void multiDownload(long contentLength) {
		System.out.println("File Size: " + contentLength + "byte.");

		int maxThreadCount = this.getMaxThreadCount();
		int partSize = (int)Math.ceil((double)contentLength / maxThreadCount / HttpDownloader.BLOCK_SIZE) * HttpDownloader.BLOCK_SIZE;
		System.out.println("Part size: " + partSize);
		
		_downloadPartList = new ArrayList<DownloadPart>();
		
		long startByte = 0;
		long endByte = partSize - 1;
		if (endByte > contentLength -1) {
			endByte = contentLength -1;
		}
		_downloadPartList.add(new DownloadPart(startByte ,endByte));
		while (endByte < contentLength -1) {
			startByte = endByte + 1;
			endByte += partSize;
			if (endByte >= contentLength -1) {
				endByte = contentLength -1;
			}
			_downloadPartList.add(new DownloadPart(startByte ,endByte));
		}
		
		for (int i = 0; i < _downloadPartList.size(); i++) {
			System.out.println( i + ": " + _downloadPartList.get(i).toString());
		}

		for (int i = 0; i < maxThreadCount; i++) {
			this.startDownloadThread(i);
		}
		
		HttpDownloadManager manager = new HttpDownloadManager(this);
		manager.setDownloadPartList(_downloadPartList);
		Thread thread = new Thread(manager);
		thread.start();
		
		this.waitSync();
	}
	
	private void startDownloadThread(int threadId) {
		HttpDownloader downloader = new HttpDownloader(this);
		downloader.setDownloadPartList(_downloadPartList);
		downloader.setThreadId(threadId);
		downloader.setUrl(_url);
		downloader.setPath(_path);
		downloader.setFileName(_fileName);
		for (Header header : _addHeaderList) {
			downloader.addHeader(header.getName() ,header.getValue());
		}
		Thread thread = new Thread(downloader);
		thread.start();
	}

	private void singleDownload(InputStream is) throws UnsupportedOperationException, IOException {

        String filePath = _path + File.separatorChar + _fileName;
        FileOutputStream fos = new FileOutputStream(new File(filePath));

        int len;
        byte[] bytes = new byte[8192];
        while ((len = is.read(bytes)) != -1) {
            fos.write(bytes ,0 ,len);
        }

        fos.close();
	}

	public void mkdir(String path) {
		File file = new File(path);
		if (!file.exists() || file.isFile()) {
			file.mkdir();
		}
	}

	@Override
	public synchronized int getThreadCount() {
		return _threadCount;
	}

	/**
	 * 同期オブジェクトの追加
	 */
	public synchronized void addSync() {
		_threadCount++;
	}
	
	/**
	 * 同期オブジェクトの削除
	 */
	public synchronized void delSync() {
		_threadCount--;
		notify();
	}
	
	/**
	 * 同期オブジェクトの待ち合わせ（子がすべて終了するまで待つ）
	 */
	public synchronized void waitSync() {
		while (_threadCount > 0) {
			try {
				wait();
				
				// いずれかのThreadが終了した後の処理
				int freeThreadCount = this.getMaxThreadCount() - this.getThreadCount() + 1;
				if (freeThreadCount > 0) {
					for (int i = 0; i < _downloadPartList.size(); i++) {
						DownloadPart downloadPart = _downloadPartList.get(i);
						if (downloadPart.getDownloadStatus() == DownloadStatus.READY) {
							this.startDownloadThread(i);
							break;
						}
					}

					for (int i = 0; i < _downloadPartList.size(); i++) {
						DownloadPart downloadPart = _downloadPartList.get(i);
						if (downloadPart.getDownloadStatus() == DownloadStatus.ERROR) {
							int wait = downloadPart.incrementWait();
							if (wait < 10) {
								downloadPart.setDownloadStatus(DownloadStatus.READY);
								this.startDownloadThread(i);
								break;
							}
						}
					}
					
					long maxEndByte = -1;
					long maxStartByte = -1;
					long maxDownloadedByte = -1;
					long maxRest = -1;
					int maxThreadId = -1;
					for (int i = 0; i < _downloadPartList.size(); i++) {
						DownloadPart downloadPart = _downloadPartList.get(i);
						if (downloadPart.getDownloadStatus() == DownloadStatus.DOWNLOADING) {
							long endByte = downloadPart.getEndByte();
							long startByte = downloadPart.getStartByte();
							long downloadedByte = downloadPart.getDownloadedByte();
							long rest = endByte - startByte - downloadedByte;
							if (rest > maxRest) {
								maxThreadId = i;
								maxEndByte = endByte;
								maxStartByte = startByte;
								maxDownloadedByte = downloadedByte;
								maxRest = rest;
							}
						}
					}
					if (maxThreadId >= 0) {
						long halfRest = maxRest / 2;
						if (halfRest > HttpDownloader.MIN_DOWNLOAD_SIZE) {
							long newEndByte = maxStartByte + maxDownloadedByte + halfRest;
							DownloadPart downloadPart = _downloadPartList.get(maxThreadId);
							downloadPart.setEndByte(newEndByte);
							
							int newThreadId = _downloadPartList.size();
							long newStartByte = newEndByte + 1;
							_downloadPartList.add(new DownloadPart(newStartByte ,maxEndByte));
							this.startDownloadThread(newThreadId);
							
							System.out.println(maxThreadId + " : " + downloadPart.toString());
							System.out.println(newThreadId + " : " + _downloadPartList.get(newThreadId).toString());
							break;
						}
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	
	public int getMaxThreadCount() {
		return _maxThreadCount;
	}

	public void setMaxThreadCount(int maxThreadCount) {
		this._maxThreadCount = maxThreadCount;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String url = "http://dy43ylo5q3vt8.cloudfront.net/SDMU-528/SDMU-528-512k.mp4";
			String path = "C:\\ide\\work";
			String fileName = "SDMU-528.mp4";
			
//			String url = args[0];
//			String path = args[1];
//			String fileName = args[2];
			
			HttpMultiDownloader downloader = new HttpMultiDownloader();
			downloader.execute(url, path, fileName);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
