package com.nishibu2003.m3u8_downloader.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.nishibu2003.m3u8_downloader.http.BasicHeader;
import com.nishibu2003.m3u8_downloader.http.Header;
import com.nishibu2003.m3u8_downloader.http.ISync;
import com.nishibu2003.m3u8_downloader.m3u8.MultiDownloadItems;
import com.nishibu2003.m3u8_downloader.m3u8.MultiDownloader;

public class FileDownloader implements ISync {
	private List<Header> _addHeaderList = new ArrayList<Header>();

	private int _threadCount;
	
	public FileDownloader() {
		_threadCount = 0;
	}

	public FileDownloader addHeader(String name ,String value) {
		Header header = new BasicHeader(name, value);
		_addHeaderList.add(header);
		return this;
	}

	public void execute(String url, String path, String fileName) throws Exception {
		this.execute(url, path, fileName, 20);
	}

	public void execute(String url, String path, String fileName ,int maxThread) throws Exception {
		this.mkdir(path);
		
		List<Header> requestHeaderList = new ArrayList<Header>();
		requestHeaderList.addAll(_addHeaderList);
		
		MultiDownloadItems items = new MultiDownloadItems();
		items.add(url, path, fileName);
		
		this.download(items ,requestHeaderList.toArray(new Header[]{}) ,maxThread);
		System.out.println("Completed!!!");
	}

	public void mkdir(String path) {
		File file = new File(path);
		if (!file.exists() || file.isFile()) {
			file.mkdir();
		}
	}
	
	/**
	 * download
	 * 
	 * @param items
	 * @param headers 
	 * @param maxThread 
	 * @throws InterruptedException 
	 */
	public void download(MultiDownloadItems items, Header[] headers, int maxThread) throws InterruptedException {
		for (int i = 0; i < maxThread; i++) {
			MultiDownloader downloader = new MultiDownloader(this);
			downloader.setThreadId(i);
			downloader.setItems(items);
			for (Header header : headers) {
				downloader.addHeader(header.getName() ,header.getValue());
			}
			Thread thread = new Thread(downloader);
			thread.start();
		}
		this.waitSync();
	}

	/**
	 * スレッド数を返す
	 * @return
	 */
	@Override
	public int getThreadCount() {
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
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			FileDownloader downloader = new FileDownloader();
			//String url = args[0];
//			downloader.setPath((args.length >= 2) ? args[1] : null);
//			downloader.setFileName((args.length >= 3) ? args[2] : null);
//			downloader.download(url);
			
			downloader.addHeader("Range" , "bytes=0-");
			downloader.addHeader("Referer", "http://lemonlemon.dip.jp/mp3/index-441.html");
			
			String url = "http://lemonlemon.dip.jp/mp3/mp3-459/HideakiTokunaga-057.mp3";
			String path = "C:\\Users\\Anonymous\\AppData\\Local\\Temp";
			String fileName = "徳永英明 - 壊れかけのRadio.mp3";
			int maxThread = 1;
			downloader.execute(url, path, fileName, maxThread);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
