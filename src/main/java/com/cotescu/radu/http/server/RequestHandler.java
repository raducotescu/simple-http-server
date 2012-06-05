package com.cotescu.radu.http.server;

import java.io.IOException;

import com.cotescu.radu.http.server.constants.HTTPStatusCode;
import com.cotescu.radu.http.server.exceptions.HTTPException;

/**
 * Abstract class that defines a Request handler.
 * 
 * @author Radu Cotescu
 * 
 */
public abstract class RequestHandler
{
	protected Request request;
	protected Response response;

	/**
	 * Creates a Request handler based on a request.
	 * 
	 * @param request
	 *            the Request that is handled
	 * @param response
	 *            the Response that is processed for the Request
	 * 
	 */
	public RequestHandler(Request request, Response response)
	{
		this.request = request;
		this.response = response;
	}

	/**
	 * Checks if the requested file from the {@link Request} can be found and read.
	 * 
	 * @throws HTTPException
	 *             in case the file cannot be found or read
	 */
	protected void checkRequestedFile() throws HTTPException
	{
		if (!request.getFile().exists())
		{
			throw new HTTPException(HTTPStatusCode.HTTP_NOT_FOUND);
		}

		if (!request.getFile().canRead())
		{
			throw new HTTPException(HTTPStatusCode.HTTP_FORBIDDEN);
		}
	}

	/**
	 * Processes a {@link Request} by sending the appropriate {@link Response}.
	 * 
	 * @throws HTTPException
	 *             for any HTTP error
	 * @throws IOException
	 *             if any I/O error occurs
	 */
	public abstract void processRequest() throws HTTPException, IOException;
}
