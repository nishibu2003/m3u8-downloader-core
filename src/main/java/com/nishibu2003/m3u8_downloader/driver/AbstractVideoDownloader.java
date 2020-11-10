package com.nishibu2003.m3u8_downloader.driver;

public abstract class AbstractVideoDownloader implements IVideoDownloader {

	private String _path;
	private String _fileName;
	
	private String _defaultPath;
	private String _defaultFileName;

	public AbstractVideoDownloader() {
		super();
	}

	@Override
	public void setPath(String path) throws Exception {
		_path = path;
	}

	@Override
	public String getPath() throws Exception {
		return _path;
	}

	@Override
	public void setFileName(String fileName) throws Exception {
		_fileName = fileName;
	}

	@Override
	public String getFileName() throws Exception {
		return _fileName;
	}


	public String getDefaultPath() {
		return _defaultPath;
	}

	public void setDefaultPath(String defaultPath) {
		this._defaultPath = defaultPath;
	}

	public String getDefaultFileName() {
		return _defaultFileName;
	}

	public void setDefaultFileName(String defaultFileName) {
		this._defaultFileName = defaultFileName;
	}

	public String getActualPath() throws Exception {
		String ret;
		
		ret = this.getPath();
		if (ret != null && !"".equals(ret)) return ret;
		
		ret = this.getDefaultPath();
		if (ret != null && !"".equals(ret)) return ret;
		
		ret = this.getCurrentDirectory();
		return ret;
	}
	
	public String getCurrentDirectory() {
		return System.getProperty("user.dir");
	}

	public String getActualFileName() throws Exception {
		String ret;
		
		ret = this.getFileName();
		if (ret != null && !"".equals(ret)) return ret;
		
		ret = this.getDefaultFileName();
		if (ret != null && !"".equals(ret)) return ret;
		
		ret = "video.ts";
		return ret;
	}
	
	
	public String convert4FileName(String s) {
		if (s == null || s.length() == 0) return "";
		
		char[] cc = s.toCharArray();
		int len = cc.length;
		
		int st = 0;
		while ((st < len) && (cc[st] <= ' ')) {
		    st++;
		}
		
		int ed = cc.length;
		while ((ed > st) && ((cc[ed -1] <= ' ') || cc[ed -1] == '.')) {
		    ed--;
		}

		// \/:*?<>|"
		char[] rr = new char[cc.length];
		int cnt = 0;
		for (int i = st; i < ed; i++) {
			char c = cc[i];
			if (c < ' ') continue;
			
			if (c == '\\' || c == '/' || c ==':') {
				c = '_';
			} else if (c == '*' || c == '?') {
				c = '~';
			} else if (c == '<' || c == '>' || c == '|') {
				c = '~';
			}
			
			cnt++;
			rr[cnt -1] = c;
		}
		
		return new String(rr ,0 ,cnt);
	}

}