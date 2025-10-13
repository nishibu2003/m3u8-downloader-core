package com.nishibu2003.m3u8_downloader.m3u8;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class M3U8Analyzer {
	M3U8 m3u8 = null;
	
	String bestVideoURI = null;
	String bestAudioURI = null;
	
	public M3U8Analyzer(M3U8 m3u8) {
		this.m3u8 = m3u8;
		
		String[] bestURIs = this.analyze(m3u8.getLines(), m3u8.getStreamList());
		this.bestVideoURI = bestURIs[0];
		this.bestAudioURI = bestURIs[1];
	}
	
	public String[] analyze(String[] lines, List<String[]> streamList) {
		String[] bestVideoStream = this.choiceBestVideoStream(streamList);
		String bestVideoURIParam = bestVideoStream[0];
		String bestVideoURI = bestVideoStream[1];
		
		String audioId = null;
		if (bestVideoURIParam != null && !bestVideoURIParam.isEmpty()) {
			Pattern p = Pattern.compile("AUDIO=\"(.+?)\"");
			Matcher m = p.matcher(bestVideoStream[0]);
			if (m.find()) {
				audioId = m.group(1);
			}
		}
		
		String bestAudioURI = null;
		if (audioId != null && !audioId.isEmpty()) {
			String[] bestAudioStream = this.choiceBestAudioStream(lines, audioId);
			if (bestAudioStream != null && bestAudioStream.length > 0) {
				bestAudioURI = bestAudioStream[1];
			}
		}
		
		return new String[] {bestVideoURI, bestAudioURI};
	}
	
	public String[] choiceBestVideoStream(List<String[]> streamList) {
		Pattern p = Pattern.compile("BANDWIDTH=([0-9]+)");
		
		long bandwidth = -1;
		String[] value = null;
		for (String[] stream : streamList) {
			Matcher m = p.matcher(stream[0]);
			if (m.find()) {
				String s = m.group(1);
				long val = Long.parseLong(s);
				if (bandwidth < val) {
					bandwidth = val;
					value = stream;
				}
			}
		}
		
		if (bandwidth == -1) {
			value = streamList.get(0);
		}
		
		return value;
	}
	
	public String[] choiceBestAudioStream(String[] lines, String audioId) {
		// #EXT-X-MEDIA:TYPE=AUDIO,GROUP-ID="ts_AUDIO-0_2",NAME="...",DEFAULT=YES,AUTOSELECT=YES,LANGUAGE="und",URI="..."
		Pattern p = Pattern.compile("#EXT-X-MEDIA:.*GROUP-ID=\"(.+?)\".*URI=\"(.+?)\"");
		for (String line : lines) {
			Matcher m = p.matcher(line);
			if (m.find()) {
				String groupId = m.group(1);
				String uri = m.group(2);
				if (audioId.equals(groupId)) {
					return new String[] {line ,uri};
				}
			}
		}
		return null;
	}
	
	public String getBestVideoURI() {
		return this.bestAudioURI;
	}
	
	public String getBestAudioURI() {
		return this.bestAudioURI;
	}
	
	public String[] getBestUriList() {
		if (this.bestAudioURI != null) {
			return new String[] {this.bestVideoURI, this.bestAudioURI};
		}
		return new String[] {this.bestVideoURI};
	}
	
	// 下位互換用に残す
	public static String choiceStream(List<String[]> streamList) {
		Pattern p = Pattern.compile("BANDWIDTH=([0-9]+)");
		
		long bandwidth = -1;
		String value = "";
		for (String[] stream : streamList) {
			Matcher m = p.matcher(stream[0]);
			if (m.find()) {
				String s = m.group(1);
				long val = Long.parseLong(s);
				if (bandwidth < val) {
					bandwidth = val;
					value = stream[1];
				}
			}
		}
		
		if (bandwidth == -1) {
			value = streamList.get(0)[1];
		}
		
		return value;
	}

	public static String getKeyURI(String line) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}
}
