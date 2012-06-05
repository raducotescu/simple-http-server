package com.cotescu.radu.http.server;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.ThreadPoolExecutor;

import com.cotescu.radu.http.server.constants.HTTPMethod;
import com.cotescu.radu.http.server.constants.HTTPResponseHeader;
import com.cotescu.radu.http.server.constants.HTTPStatusCode;
import com.cotescu.radu.http.server.exceptions.HTTPException;
import com.cotescu.radu.http.server.utils.HTTPDateFormatter;

/**
 * This class implements the default request handler for GET/POST/HEAD.
 * 
 * @author Radu Cotescu
 * 
 */
public class DefaultRequestHandler extends RequestHandler
{

	/**
	 * Creates a request handler for GET/POST/HEAD.
	 * 
	 * @param request
	 *            the {@link Request} object
	 * @param response
	 *            the {@link Response} object
	 */
	public DefaultRequestHandler(Request request, Response response)
	{
		super(request, response);
	}

	@Override
	public void processRequest() throws HTTPException, IOException
	{
		if (request.getRequestURI().equals("/server-status") && Configuration.INSTANCE.getBooleanValueFor(Configuration.SERVER_STATUS)) {
			String htmlMessage = getServerStatus();
			response.sendResponseStatus(HTTPStatusCode.HTTP_OK);
			response.addHeader(HTTPResponseHeader.ContentLength, Integer.toString(htmlMessage.getBytes().length));
			response.addHeader(HTTPResponseHeader.ContentType, "html");
			response.addHeader(HTTPResponseHeader.LastModified, HTTPDateFormatter.getFormattedDate(new Date()));
			response.addHeader(HTTPResponseHeader.Connection, "close");
			response.sendHeaders();
			if (request.getMethod() == HTTPMethod.HEAD) {
				response.endResponse();
				return;
			}
			response.write(htmlMessage);
			response.endResponse();
			return;
		}
		checkRequestedFile();
		response.sendResponseStatus(HTTPStatusCode.HTTP_OK);
		response.addHeader(HTTPResponseHeader.ContentLength, new Long(request.getFile().length()).toString());
		response.addHeader(HTTPResponseHeader.ContentType, request.getContentType());
		response.addHeader(HTTPResponseHeader.LastModified, HTTPDateFormatter.getFormattedDate(new Date(request.getFile().lastModified())));
		response.addHeader(HTTPResponseHeader.Connection, "close");
		response.sendHeaders();
		if (request.getMethod() == HTTPMethod.HEAD)
		{
			response.endResponse();
			return;
		}
		InputStream reader = new BufferedInputStream(new FileInputStream(request.getFile()));
		byte[] buffer = new byte[4096];
		int bytesRead;
		while ((bytesRead = reader.read(buffer)) != -1)
		{
			response.write(buffer, 0, bytesRead);
		}
		response.endResponse();
	}
	
	private String getServerStatus() {
		ThreadPoolExecutor e = (ThreadPoolExecutor) HTTPServer.getExecutorService();
		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n");
		sb.append("<html>\n<head>\n<title>Server Status</title>\n</head>\n");
		sb.append("<body>\n");
		sb.append("Server max threads: ").append(e.getMaximumPoolSize()).append("<br/>\n");
		sb.append("Server min threads: ").append(e.getCorePoolSize()).append("<br/>\n");
		sb.append("Server current threads: ").append(e.getPoolSize()).append("<br/>\n");
		sb.append("Server active threads: ").append(e.getActiveCount()).append("<br/>\n");
		sb.append("Requests served: ").append(e.getCompletedTaskCount()).append("<br/>\n");
		sb.append("\n");
		sb.append("<hr />").append(HTTPServer.SERVER_NAME).append("\n</body>\n</html>");
		return sb.toString();
	}

}
