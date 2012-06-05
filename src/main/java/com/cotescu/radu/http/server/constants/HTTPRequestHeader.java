package com.cotescu.radu.http.server.constants;

public enum HTTPRequestHeader {

	Accept("Accept"),
	AcceptCharset("Accept-Charset"),
	AcceptEncoding("Accept-Encoding"),
	AcceptLanguage("Accept-Language"),
	Authorization("Authorization"),
	CacheControl("Cache-Control"),
	Connection("Connection"),
	Cookie("Cookie"),
	ContentLength("Content-Length"),
	ContentMD5("Content-MD5"),
	ContentType("Content-Type"),
	Date("Date"),
	Expect("Expect"),
	From("From"),
	Host("Host"),
	IfMatch("If-Match"),
	IfModifiedSince("If-Modified-Since"),
	IfNoneMatch("If-None-Match"),
	IfRange("If-Range"),
	IfUnmodifiedSince("If-Unmodified-Since"),
	MaxForwards("Max-Forwards"),
	Pragma("Pragma"),
	ProxyAuthorization("Proxy-Authorization"),
	Range("Range"),
	Referer("Referer"),
	TE("TE"),
	Upgrade("Upgrade"),
	UserAgent("User-Agent"),
	Via("Via"),
	Warning("Warning");
	
	private String header;
	
	private HTTPRequestHeader(String header) {
		this.header = header;
	}
	
	public String getHeader() {
		return header;
	}
}
