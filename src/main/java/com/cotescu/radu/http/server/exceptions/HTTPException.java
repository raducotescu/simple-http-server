package com.cotescu.radu.http.server.exceptions;

import com.cotescu.radu.http.server.constants.HTTPStatusCode;

/**
 * This class describes an HTTPException generated because of an error during a {@link Request}'s processing.
 * 
 * @author Radu Cotescu
 * 
 */
public class HTTPException extends Exception {
	private static final long serialVersionUID = 3421460270223211443L;
	private HTTPStatusCode statusCode;

	public HTTPException(HTTPStatusCode statusCode) {
		super(statusCode.getStatusMessage());
		this.statusCode = statusCode;
	}

	public int getIntStatusCode() {
		return statusCode.getStatusCode();
	}
	
	public HTTPStatusCode getHTTPStatusCode() {
		return statusCode;
	}
}
