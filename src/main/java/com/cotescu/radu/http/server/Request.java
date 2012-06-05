package com.cotescu.radu.http.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import com.cotescu.radu.commons.FileUtils;
import com.cotescu.radu.commons.StringUtils;
import com.cotescu.radu.http.server.constants.HTTPMethod;
import com.cotescu.radu.http.server.constants.HTTPStatusCode;
import com.cotescu.radu.http.server.exceptions.HTTPException;

/**
 * This class represents an HTTP request.
 * 
 * @author Radu Cotescu
 * 
 */
public class Request
{
	private final Socket socket;
	private final BufferedReader reader;
	private final Map<String, String> headers;
	private String requestURI;
	private HTTPMethod method;
	private float httpVersion;
	private String firstRequestLine;

	private File file;
	private String fileExtension;
	private String contentType;

	/**
	 * Creates a Request object based on a {@link Socket}.
	 * 
	 * @param socket
	 *            the {@code Socket} which is used for this Request
	 * @throws IOException
	 *             if any I/O error occurs
	 */
	public Request(Socket socket) throws IOException
	{
		this.socket = socket;
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		headers = new HashMap<String, String>();
	}

	/**
	 * Validates a request.
	 * 
	 * @throws IOException
	 *             if any I/O error occurs
	 * @throws HTTPException
	 *             in case the server is unable to fulfil this Request
	 */
	public void validateRequest() throws IOException, HTTPException
	{
		readFirstRequestLine();
		populateHeaders();
		analyseRequestedFile();
	}

	/**
	 * Analyses this Request's first line.
	 * 
	 * @throws IOException
	 *             if any I/O error occurs
	 * @throws HTTPException
	 *             in case the server is unable to fulfil this Request
	 */
	private void readFirstRequestLine() throws IOException, HTTPException
	{
		String line = reader.readLine();
		if (!StringUtils.isEmpty(line))
		{
			firstRequestLine = line;
			String[] requestLine = firstRequestLine.split(" ");
			if (requestLine.length != 3)
			{
				throw new HTTPException(HTTPStatusCode.HTTP_BAD_REQUEST);
			}
			String _method = requestLine[0];
			String _requestURI = requestLine[1];
			String _httpVersion = requestLine[2];

			try
			{
				method = HTTPMethod.valueOf(_method);
			}
			catch (IllegalArgumentException e)
			{
				throw new HTTPException(HTTPStatusCode.HTTP_NOT_IMPLEMENTED);
			}

			requestURI = URLDecoder.decode(_requestURI, "UTF-8");

			if (_httpVersion.endsWith("/1.1"))
			{
				httpVersion = 1.1f;
			}
			else
				if (_httpVersion.endsWith("/1.0"))
				{
					httpVersion = 1.0f;
				}
				else
				{
					httpVersion = 1.0f;
				}
		}
		else
		{
			throw new HTTPException(HTTPStatusCode.HTTP_BAD_REQUEST);
		}
	}

	public Map<String, String> getHeaders()
	{
		return headers;
	}

	public String getRequestURI()
	{
		return requestURI;
	}

	public HTTPMethod getMethod()
	{
		return method;
	}

	public float getHttpVersion()
	{
		return httpVersion;
	}

	public Socket getSocket()
	{
		return socket;
	}

	public File getFile()
	{
		return file;
	}

	public String getFileExtension()
	{
		return fileExtension;
	}

	public String getContentType()
	{
		return contentType;
	}

	public String getFirstRequestLine()
	{
		return firstRequestLine;
	}

	public void setFile(File file)
	{
		this.file = file;
		this.fileExtension = FileUtils.getExtension(file);
		contentType = HTTPServer.getMimeMap().getContentType(fileExtension);
	}

	/**
	 * Reads the headers sent by the client for this Request.
	 * 
	 * @throws IOException
	 *             if any I/O error occurs
	 */
	private void populateHeaders() throws IOException
	{
		String line = reader.readLine();
		while (!StringUtils.isEmpty(line))
		{
			String header = line.split(":", 2)[0];
			StringBuilder sb = new StringBuilder(line.substring(line.indexOf(header) + header.length() + 1).trim());
			line = reader.readLine();
			while (!StringUtils.isEmpty(line) && Character.isWhitespace(line.charAt(0)))
			{
				sb.append(line);
				line = reader.readLine();
			}
			String headerValue = sb.toString();
			headers.put(header, headerValue);
		}
	}

	/**
	 * Retrieves basic information about the requested file.
	 * 
	 * @throws IOException
	 *             if any I/O error occurs
	 */
	private void analyseRequestedFile() throws IOException
	{
		file = new File(HTTPServer.getRootDirectory(), requestURI).getCanonicalFile();
		fileExtension = FileUtils.getExtension(file);
		contentType = HTTPServer.getMimeMap().getContentType(fileExtension);
	}
}
