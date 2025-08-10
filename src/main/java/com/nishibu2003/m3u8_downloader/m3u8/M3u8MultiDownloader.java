package com.nishibu2003.m3u8_downloader.m3u8;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nishibu2003.m3u8_downloader.http.BasicHeader;
import com.nishibu2003.m3u8_downloader.http.Header;
import com.nishibu2003.m3u8_downloader.http.HttpGetAsFile;
import com.nishibu2003.m3u8_downloader.http.ISync;
import com.nishibu2003.m3u8_downloader.util.HeaderUtil;
import com.nishibu2003.m3u8_downloader.util.URLutil;

public class M3u8MultiDownloader implements ISync{
	private List<Header> _addHeaderList = new ArrayList<Header>();

	private int _threadCount;
	
	public M3u8MultiDownloader() {
		_threadCount = 0;
	}

	public M3u8MultiDownloader addHeader(String name ,String value) {
		Header header = new BasicHeader(name, value);
		_addHeaderList.add(header);
		return this;
	}

	public void execute(String url, String path) throws Exception {
		this.execute(url, path, 20);
	}

	@Deprecated
	public void execute(String url, String path, String fileName) throws Exception {
		this.execute(url, path, 20);
	}

	@Deprecated
	public void execute(String url, String path, String fileName ,int maxThread) throws Exception {
		this.execute(url, path, maxThread);
	}

	public void execute(String url, String path ,int maxThread) throws Exception {
		this.mkdir(path);
		
		List<Header> requestHeaderList = new ArrayList<Header>();
		requestHeaderList.addAll(_addHeaderList);

		// M3U8ファイルのダウンロード
		String originalM3u8FileName = "original.m3u8";
		Header[] m3u8ResponseHeaders = this.downloadM3U8(url, path, originalM3u8FileName ,requestHeaderList.toArray(new Header[]{}));
		requestHeaderList.addAll(Arrays.asList(HeaderUtil.getCookie(m3u8ResponseHeaders)));
		M3U8Loader loader = new M3U8Loader();
		M3U8 m3u8 = loader.load(path, originalM3u8FileName);

		if (m3u8.getInfList().size() > 0) {
			String prefix = "";
			String baseURL = url;
			this.downloadFiles(baseURL, m3u8, path, prefix ,maxThread);
		} else {
			M3U8Analyzer m3u8Analyzer = new M3U8Analyzer(m3u8);
			String[] bestUriList = m3u8Analyzer.getBestUriList();
			Map<String, String> bestUrIMap = new LinkedHashMap<String, String>();
			if (bestUriList.length == 1) {
				bestUrIMap.put("", bestUriList[0]);
			} else if (bestUriList.length == 2) {
				bestUrIMap.put("v-", bestUriList[0]);
				bestUrIMap.put("a-", bestUriList[1]);
			}
			
			for (Entry<String, String> entry : bestUrIMap.entrySet()) {
				String prefix = entry.getKey();
				String streamURL = entry.getValue();
				String baseURL = URLutil.normalize(url, streamURL);
				
				String qualityM3u8FileName = prefix + "quality.m3u8";
				Header[] m3u8ResponseHeaders2 = this.downloadM3U8(baseURL, path, qualityM3u8FileName ,requestHeaderList.toArray(new Header[]{}));
				requestHeaderList.addAll(Arrays.asList(HeaderUtil.getCookie(m3u8ResponseHeaders2)));
				m3u8 = loader.load(path, qualityM3u8FileName);
				this.downloadFiles(baseURL, m3u8, path, prefix ,maxThread);
			}
		}
		
		System.out.println("Completed!!!");
	}

	public void downloadFiles(String baseURL, M3U8 m3u8, String path, String prefix, int maxThread) throws Exception {
		List<Header> requestHeaderList = new ArrayList<Header>();
		requestHeaderList.addAll(_addHeaderList);
		
		String[] lines = m3u8.getLines();
		if (lines.length > 0) {
			MultiDownloadItems items = new MultiDownloadItems();
			
			Pattern p1 = Pattern.compile("URI=\"(.+)\"");
			
			int keyIdx = 0;
			int infIdx = 0;
			boolean infFlg = false;
			String[] newLines = new String[lines.length];
			String[] urlLines = new String[lines.length];
			for (int i = 0; i < lines.length; i++) {
				String line = lines[i];
				newLines[i] = lines[i];
				urlLines[i] = lines[i];
				if (line.startsWith("#EXT-X-KEY:")) {
					Matcher m1 = p1.matcher(line);
					if (m1.find()) {
						String keyURI = m1.group(1);
						keyIdx++;
						String keyName = prefix + String.format("encrypt%d.key", keyIdx);
						String keyUrl = URLutil.normalize(baseURL, keyURI);
						items.add(keyUrl, path, keyName);
						
						newLines[i] = m1.replaceAll("URI=\"" + keyName + "\"");
						urlLines[i] = m1.replaceAll("URI=\"" + keyUrl + "\"");
					}
				} else if (line.startsWith("#EXTINF")) {
					infIdx++;
					infFlg = true;
				} else if (infFlg && !line.startsWith("#")) {
					String tsName = prefix + String.format("%05d", infIdx) + ".ts";
					String infUrl = URLutil.normalize(baseURL, line);
					items.add(infUrl, path, tsName);
					newLines[i] = tsName;
					urlLines[i] = infUrl;
					infFlg = false;
				}
			}

			M3U8Loader loader = new M3U8Loader();
			M3U8 newM3u8 = loader.load(path, newLines);
			M3U8Writer writer = new M3U8Writer();
			writer.write(path ,prefix + "new.m3u8" ,newM3u8);
			writer.write(path ,prefix + "new2.m3u8" ,urlLines);
			
			this.download(items ,requestHeaderList.toArray(new Header[]{}) ,maxThread);
		}
	}

	public void mkdir(String path) {
		File file = new File(path);
		if (!file.exists() || file.isFile()) {
			file.mkdir();
		}
	}

	/**
	 * M3U8ファイルのダウンロード
	 * 
	 * @param url
	 * @param path
	 * @param fileName
	 * @param headers 
	 * @throws Exception 
	 */
	public Header[] downloadM3U8(String url, String path, String fileName, Header[] headers) throws Exception {
		HttpGetAsFile downloader = new HttpGetAsFile(url, path, fileName);
		if (headers != null && headers.length > 0) {
			for (Header header : headers) {
				downloader.addHeader(header.getName(), header.getValue());
			}
		}
		downloader.execute();
		
		Header[] responseHeaders = downloader.getResponseHeaders();
		return responseHeaders;
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

	@Override
	public int getThreadCount() {
		return _threadCount;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String url = "https://manifest.streaks.jp/v6/tver-ntv/05bd9a75e6db4e978353f323d62fedd3/3596a38ecd054515a0f8398339a0dab3/hls/v3/manifest.m3u8?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYyI6IjgxOWNmZGMwMjBhYzQ4MGI5YjA2MDBiNWFmYjMzMWEzIiwiZWRnZSI6IjkzOTdhNjg0NTg3NTQ4OWM4NjdmZGI2MzVkNTBlNzkyIiwiY29kZWNzIjoiYXV0byIsImV4cCI6MTc1NDk4OTIwMCwidnA5IjoxLCJzbSI6Ijc0ZDQyZDczMmY3ODQ4NTY4YWQ4Njk1MTdkMGM2ZDk5IiwicHB3IjoiNDc1In0.eCquwFiVQsH2LVpmdg6_Duji35l8d2QyOAMnQ1lir48";
			String path = "C:\\Users\\Anonymous\\Documents\\workspace-sts-m3u8\\m3u8-downloader-driver\\Test";
			
//			String url = args[0];
//			String path = args[1];
			
			M3u8MultiDownloader m3u8Downloader = new M3u8MultiDownloader();
			m3u8Downloader.execute(url, path);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
