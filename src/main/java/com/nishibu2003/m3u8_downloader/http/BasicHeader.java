package com.nishibu2003.m3u8_downloader.http;

public class BasicHeader implements Header {
    private String _name;
    private String _value;

    public BasicHeader(String name, String value) {
        this._name = name;
        this._value = value;
    }

    public String getName() {
        return _name;
    }

    public String getValue() {
        return _value;
    }

}
