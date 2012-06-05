package com.cotescu.radu.http.server;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.cotescu.radu.http.server.constants.HTTPResponseHeader;
import com.cotescu.radu.http.server.constants.HTTPStatusCode;
import com.cotescu.radu.http.server.utils.HTTPDateFormatter;

/**
 * This class represents an HTTP response, based on an HTTP request.
 * 
 * @author Radu Cotescu
 * 
 */
public class Response
{
	private static final String SP = " ";
	private static final String EOL = "\r\n";

	private BufferedOutputStream out;
	private Socket socket;
	private Map<String, String> headers;
	private HTTPStatusCode statusCode;

	/**
	 * Creates a Response based on a {@link Request}.
	 * 
	 * @param request
	 *            the {@link Request} for which this Response should be created
	 * @throws IOException
	 *             if any I/O error occurs
	 */
	public Response(Request request) throws IOException
	{
		socket = request.getSocket();
		out = new BufferedOutputStream(socket.getOutputStream());
		headers = new HashMap<String, String>(3);
		populateDefaultResponseHeaders();
	}

	/**
	 * Sends the first line of the HTTP Response.
	 * 
	 * @param statusCode
	 *            the {@link HTTPStatusCode} for this Response
	 * @throws IOException
	 *             if any I/O error occurs
	 */
	public void sendResponseStatus(HTTPStatusCode statusCode) throws IOException
	{
		this.statusCode = statusCode;
		StringBuilder sb = new StringBuilder();
		sb.append("HTTP/1.1 ").append(statusCode.getStatusCode()).append(SP).append(statusCode.getStatusMessage()).append(EOL);
		out.write(sb.toString().getBytes());
	}

	/**
	 * Adds a standard header to this Reponse's headers map.
	 * 
	 * @param header
	 *            the {@link HTTPResponseHeader}
	 * @param headerValue
	 *            the {@link HTTPResponseHeader}'s value
	 */
	public void addHeader(HTTPResponseHeader header, String headerValue)
	{
		headers.put(header.getHeader(), headerValue);
	}

	/**
	 * Adds a custom header to this Reponse's headers map.
	 * 
	 * @param header
	 *            the header's name
	 * @param headerValue
	 *            the header's value
	 */
	public void addCustomHeader(String header, String headerValue)
	{
		headers.put(header, headerValue);
	}

	/**
	 * Sends the headers for this Reponse.
	 * 
	 * @throws IOException
	 *             if any I/O error occurs
	 */
	public void sendHeaders() throws IOException
	{
		StringBuilder sb = new StringBuilder();
		for (Entry<String, String> headerEntry : headers.entrySet())
		{
			sb.append(headerEntry.getKey()).append(":").append(SP).append(headerEntry.getValue()).append(EOL);
		}
		sb.append(EOL);
		out.write(sb.toString().getBytes());
		out.flush();
	}

	/**
	 * Writes a String to this Response's {@link BufferedOutputStream}.
	 * 
	 * @param string
	 *            the String to be written to the BufferedOutputStream
	 * @throws IOException
	 *             if any I/O error occurs
	 */
	public void write(String string) throws IOException
	{
		out.write(string.getBytes());
	}

	/**
	 * Wrapper that offers an easy way to write to this Response's BufferedOutputStream from an InputStream.
	 * 
	 * @param buffer
	 *            the data
	 * @param offset
	 *            the start offset in the data
	 * @param length
	 *            the number of bytes to write
	 * @throws IOException
	 *             if any I/O error occurs
	 */
	public void write(byte[] buffer, int offset, int length) throws IOException
	{
		out.write(buffer, offset, length);
	}

	/**
	 * Ends this Response by flushing the socket's output stream and closing the socket.
	 * 
	 * @throws IOException
	 *             if any I/O error occurs
	 */
	public void endResponse() throws IOException
	{
		out.flush();
		socket.close();
	}

	public HTTPStatusCode getHTTPStatusCode()
	{
		return statusCode;
	}

	public String getHeaderValue(HTTPResponseHeader header)
	{
		return headers.get(header.getHeader());
	}

	/**
	 * Adds the default headers to this Response.
	 */
	private void populateDefaultResponseHeaders()
	{
		headers.put(HTTPResponseHeader.Date.getHeader(), HTTPDateFormatter.getFormattedDate(new Date()));
		headers.put(HTTPResponseHeader.Server.getHeader(), HTTPServer.SERVER_NAME);
		headers.put(HTTPResponseHeader.Connection.getHeader(), "close");
	}
}
