package com.nishibu2003.m3u8_downloader.m3u8;

import java.util.ArrayList;
import java.util.List;

public class MultiDownloadItems {
	public static int INDEX = 0;
	public static int STATUS = 1;
	public static int URL = 2;
	public static int PATH = 3;
	public static int FILE_NAME = 4;
	/** 再開時の Range 開始位置。空欄または 0 は先頭から。 */
	public static int RESUME_START = 5;
	/** 再試行回数 */
	public static int RETRY_COUNT = 6;

	private int _lastIndex = -1;
	private List<String[]> _list;

	public MultiDownloadItems() {
		_list = new ArrayList<String[]>();
	}

	public synchronized String[] add(String url, String path, String fileName) {
		_lastIndex++;
		String[] values = new String[7];
		values[INDEX] = String.valueOf(_lastIndex);
		values[STATUS] = "NOT YET";
		values[URL] = url;
		values[PATH] = path;
		values[FILE_NAME] = fileName;
		values[RESUME_START] = "0";
		values[RETRY_COUNT] = "0";
		_list.add(values);
		return values;
	}

	/** 未処理または再試行待ちの項目を、1つのワーカーに割り当てる。 */
	public synchronized String[] assign() {
		for (int i = 0; i < _list.size(); i++) {
			String[] values = _list.get(i);
			if ("NOT YET".equals(values[STATUS]) || "RETRY".equals(values[STATUS])) {
				values[STATUS] = "ASSIGNED";
				return values;
			}
		}
		return null;
	}

	public synchronized int size() {
		return _list.size();
	}

	/** すべてのワーカー終了後、失敗した項目が残っているか確認する。 */
	public synchronized boolean hasError() {
		for (int i = 0; i < _list.size(); i++) {
			if ("ERROR".equals(_list.get(i)[STATUS])) {
				return true;
			}
		}
		return false;
	}
}
