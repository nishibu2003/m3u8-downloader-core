package com.nishibu2003.m3u8_downloader.m3u8;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class M3U8Writer {

	public void write(String path, String fileName, M3U8 m3u8) throws Exception {
		File file = new File(path + File.separatorChar + fileName);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-8"));

		for (String line : m3u8.getLines()) {
			bw.write(line);
			bw.write("\n");
		}
		
		bw.close();
	}
	
	public void write(String path, String fileName, String[] lines) throws Exception {
		File file = new File(path + File.separatorChar + fileName);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-8"));

		for (String line : lines) {
			bw.write(line);
			bw.write("\n");
		}
		
		bw.close();
	}

}
