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
			while ((values = _items.assign()) != null) {
				String url = values[MultiDownloadItems.URL];
				String path = values[MultiDownloadItems.PATH];
				String fileName = values[MultiDownloadItems.FILE_NAME];
				String threadId = String.format("%2d", this.getThreadId());
				try {
					long resumeStart = parseLong(values[MultiDownloadItems.RESUME_START], 0);
					if (resumeStart > 0) {
						this.downloadContinue(url, path, fileName, resumeStart);
					} else {
						this.download(url, path, fileName);
					}
					System.out.println(threadId + " : File Download Completed!!! " + fileName + " ,URL : " + url);
					values[MultiDownloadItems.STATUS] = "DONE";
				} catch (Exception e) {
					int retryCount = (int)parseLong(values[MultiDownloadItems.RETRY_COUNT], 0) + 1;
					values[MultiDownloadItems.RETRY_COUNT] = String.valueOf(retryCount);
					System.out.println(threadId + " : File Download RETRY!!! (" + retryCount + "/20) " + fileName + " ,URL : " + url + " ,Exception : " + e.toString());
					if (retryCount >= 20) {
						values[MultiDownloadItems.STATUS] = "ERROR";
						e.printStackTrace();
					} else {
						long downloadedSize = this.getDownloadedSize(path, fileName);
						// 書き込み途中の末尾ブロックを少し巻き戻し、安全側から再開する。
						long resumeStart = downloadedSize > 8096 ? downloadedSize - 8096 + 1 : 0;
						values[MultiDownloadItems.RESUME_START] = String.valueOf(resumeStart);
						values[MultiDownloadItems.STATUS] = "RETRY";
						// 失敗したワーカーは終了し、別の Java スレッドに再試行を引き継ぐ。
						startReplacementWorker();
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			_sync.delSync();
		}
	}

	private long parseLong(String value, long defaultValue) {
		try {
			return Long.parseLong(value);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	private void startReplacementWorker() {
		MultiDownloader downloader = new MultiDownloader(_sync);
		downloader.setThreadId(_threadId);
		downloader.setItems(_items);
		for (Header header : _addHeaderList) {
			downloader.addHeader(header.getName(), header.getValue());
		}
		Thread thread = new Thread(downloader, "FileDownloader-retry-" + _threadId);
		thread.start();
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
		httpGet.setReadTimeout(30000);
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
		httpGet.setReadTimeout(30000);
			
			for (Header header : _addHeaderList) {
				httpGet.addHeader(header);
			}
			String byteRange = "bytes=" + startRange + "-";
			httpGet.addHeader("Range", byteRange);
			
			httpGet.execute();
			InputStream is = httpGet.getContents();
			int responseCode = httpGet.getStatusCode();
			if (responseCode == 200) {
				// サーバーが Range を無視した場合、途中位置に全ファイルを書き込まない。
				httpGet.close();
				httpGet = null;
				this.deleteFile(path, fileName);
				this.download(innerUrl, path, fileName);
				return;
			}
			if (responseCode == 206) {
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
