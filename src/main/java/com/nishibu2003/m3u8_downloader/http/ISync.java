package com.nishibu2003.m3u8_downloader.http;

public interface ISync {
	/**
	 * スレッド数を返す
	 * @return
	 */
	public int getThreadCount();
	
	/**
	 * 同期オブジェクトの追加
	 */
	public void addSync();
	
	/**
	 * 同期オブジェクトの削除
	 */
	public void delSync();
	
	/**
	 * 同期オブジェクトの待ち合わせ（子がすべて終了するまで待つ）
	 */
	public void waitSync();

}
