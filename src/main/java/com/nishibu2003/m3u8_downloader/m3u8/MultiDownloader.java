package com.nishibu2003.m3u8_downloader.m3u8;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpRetryException;
import java.util.ArrayList;
import java.util.List;

import com.nishibu2003.m3u8_downloader.http.BasicHeader;
import com.nishibu2003.m3u8_downloader.http.Header;
import com.nishibu2003.m3u8_downloader.http.HttpGet;
import com.nishibu2003.m3u8_downloader.http.HttpGetAsFile;
import com.nishibu2003.m3u8_downloader.http.ISync;


public class MultiDownloader implements Runnable {
	private List<Header> _addHeaderList = new ArrayList<Header>();

	private ISync _sync;
	
	private int _threadId;
	private String _url;
	private String _path;
	private String _fileName;
	private MultiDownloadItems _items;
	
	public MultiDownloader() {
		ISync sync = new ISync(){

			@Override
			public void addSync() {
			}

			@Override
			public void delSync() {
			}

			@Override
			public void waitSync() {
			}

			@Override
			public int getThreadCount() {
				return 0;
			}
		};
		_sync = sync;
		_sync.addSync();
	}
	
	public MultiDownloader(ISync sync) {
		_sync = sync;
		_sync.addSync();
	}

	public MultiDownloader addHeader(String name, String value) {
		Header header = new BasicHeader(name, value);
		_addHeaderList.add(header);
		return this;
	}
	
	@Override
	public void run() {
		try {
			String[] values;
			while ((values = _items.assign()) != null){
				String url = values[MultiDownloadItems.URL];
				String path = values[MultiDownloadItems.PATH];
				String fileName = values[MultiDownloadItems.FILE_NAME];
				int initial = 20;
				try {
					this.download(url ,path ,fileName);
					System.out.println(this.getThreadId() + " : File Download Completed!!! " + fileName + " ,URL : " + url);
					values[MultiDownloadItems.STATUS] = "DONE";
				} catch (Exception e1) {
					int counter = initial -1;
					System.out.println(this.getThreadId() + " : File Download RETRY!!! (" + (initial - counter) + "/" + initial + ") " + fileName + " ,URL : " + url + " ,Exception : " + e1.toString());
					Exception causedEx = e1;
					while(counter > 0) {
						if (counter <= 0) {
							System.out.println(this.getThreadId() + " : File Download ERROR!!! " + fileName + " ,URL : " + url);
							causedEx.printStackTrace();
							values[MultiDownloadItems.STATUS] = "ERROR";
						} else {
							long downloadedSize = this.getDownloadedSize(path ,fileName);
							// TODO 8096byte
							downloadedSize = downloadedSize - 8096;
							try {
								if (downloadedSize > 0) {
									this.downloadContinue(url ,path ,fileName ,downloadedSize + 1);
									System.out.println(this.getThreadId() + " : File Download Completed!!! " + fileName + " (Range " + (downloadedSize + 1) + "-) ,URL : " + url);
								} else {
									this.download(url ,path ,fileName);
									System.out.println(this.getThreadId() + " : File Download Completed!!! " + fileName + " ,URL : " + url);
								}
								values[MultiDownloadItems.STATUS] = "DONE";
								counter = 0;
								break;
							} catch (Exception e3) {
								if (e3 instanceof HttpRetryException) {
									HttpRetryException httpRetryException = (HttpRetryException)e3;
									if (httpRetryException.responseCode() == 416) {
										try {
											this.deleteFile(path ,fileName);
										} catch (Exception e) {
											// ignore
										}
									}
								}
								System.out.println(this.getThreadId() + " : File Download RETRY!!! (" + (initial - counter) + "/" + initial + ") " + fileName + " ,URL : " + url + " ,Exception : " + e3.toString());
								values[MultiDownloadItems.STATUS] = "RETRY";
								
								causedEx = e3;
								Thread.sleep((long)(Math.random() * 20000));
							}
						}
						counter--;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		_sync.delSync();
	}

	/**
	 * ダウンロード済みファイルサイズを取得する
	 * 
	 * @param path
	 * @param fileName
	 * @return
	 */
	private long getDownloadedSize(String path, String fileName) {
		String filePath = path + File.separatorChar + fileName;
		File f = new File(filePath);
		if (f.exists() && f.isFile()) {
			long length = f.length();
			return length;
		}
		return 0;
	}

	/**
	 * ファイルを削除する
	 * 
	 * @param path
	 * @param fileName
	 */
	private void deleteFile(String path, String fileName) {
		File f = new File(path + File.separatorChar + fileName);
		if (f.exists() && f.isFile()) {
			f.delete();
		}
	}

	public void download(String url, String path, String fileName) throws UnsupportedOperationException, IOException {
		HttpGetAsFile httpGet = new HttpGetAsFile(url ,path ,fileName);
		for (Header header : _addHeaderList) {
			httpGet.addHeader(header);
		}
		try {
			httpGet.execute();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	/**
	 * 続きをダウンロードする
	 * 
	 * @param innerUrl
	 * @param path
	 * @param fileName
	 * @param startRange
	 */
	private void downloadContinue(String innerUrl, String path, String fileName, long startRange) throws Exception  {
		RandomAccessFile raf = null;
		HttpGet httpGet = null;
		try {
			httpGet = new HttpGet(innerUrl);
			
			for (Header header : _addHeaderList) {
				httpGet.addHeader(header);
			}
			String byteRange = "bytes=" + startRange + "-";
			httpGet.addHeader("Range", byteRange);
			
			httpGet.execute();
			InputStream is = httpGet.getContents();
			int responseCode = httpGet.getStatusCode();
			if (responseCode / 100 == 2) {
				String filePath = path + File.separatorChar + fileName;
				raf = new RandomAccessFile(filePath, "rw");
				raf.seek(startRange);
				
				int len;
				byte[] bytes = new byte[8192];
				while ((len = is.read(bytes)) != -1) {
					raf.write(bytes ,0 ,len);
				}
			} else {
				throw new HttpRetryException("Response code: " + responseCode + " for " + innerUrl, responseCode, innerUrl);
			}
		} finally {
			if (httpGet != null) {
				httpGet.close();
			}
			if (raf != null) {
				raf.close();
			}
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

	public void setItems(MultiDownloadItems items) {
		_items = items;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			MultiDownloadItems items = new MultiDownloadItems();
			items.add("https://hlsf-yolk.c.yimg.jp/yolk/110/11/1003/464/3469966_PNg5rCtv3464268.ts?B=1", "C:\\ide\\work", "01.ts");
			items.add("https://hlsf-yolk.c.yimg.jp/yolk/110/11/1003/464/3469966_PNg5rCtv3464268.ts?B=2", "C:\\ide\\work", "02.ts");
			items.add("https://hlsf-yolk.c.yimg.jp/yolk/110/11/1003/464/3469966_PNg5rCtv3464268.ts?B=3", "C:\\ide\\work", "03.ts");
			items.add("https://hlsf-yolk.c.yimg.jp/yolk/110/11/1003/464/3469966_PNg5rCtv3464268.ts?B=4", "C:\\ide\\work", "04.ts");
			items.add("https://hlsf-yolk.c.yimg.jp/yolk/110/11/1003/464/3469966_PNg5rCtv3464268.ts?B=5", "C:\\ide\\work", "05.ts");
			items.add("https://hlsf-yolk.c.yimg.jp/yolk/110/11/1003/464/3469966_PNg5rCtv3464268.ts?B=6", "C:\\ide\\work", "06.ts");
			items.add("https://hlsf-yolk.c.yimg.jp/yolk/110/11/1003/464/3469966_PNg5rCtv3464268.ts?B=7", "C:\\ide\\work", "07.ts");
			items.add("https://hlsf-yolk.c.yimg.jp/yolk/110/11/1003/464/3469966_PNg5rCtv3464268.ts?B=8", "C:\\ide\\work", "08.ts");
			items.add("https://hlsf-yolk.c.yimg.jp/yolk/110/11/1003/464/3469966_PNg5rCtv3464268.ts?B=9", "C:\\ide\\work", "09.ts");
			items.add("https://hlsf-yolk.c.yimg.jp/yolk/110/11/1003/464/3469966_PNg5rCtv3464268.ts?B=10", "C:\\ide\\work", "10.ts");
			
			for (int i = 0; i < 3; i++) {
				MultiDownloader downloader = new MultiDownloader();
				downloader.setThreadId(i);
				downloader.setItems(items);
				Thread thread = new Thread(downloader);
				thread.start();
			}
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}


}
