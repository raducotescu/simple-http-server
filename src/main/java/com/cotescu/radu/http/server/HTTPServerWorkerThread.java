package com.cotescu.radu.http.server;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;

import org.apache.log4j.Logger;

import com.cotescu.radu.http.server.constants.HTTPErrorStatusCodesMap;
import com.cotescu.radu.http.server.constants.HTTPMethod;
import com.cotescu.radu.http.server.constants.HTTPRequestHeader;
import com.cotescu.radu.http.server.constants.HTTPResponseHeader;
import com.cotescu.radu.http.server.constants.HTTPStatusCode;
import com.cotescu.radu.http.server.exceptions.HTTPException;
import com.cotescu.radu.http.server.utils.HTTPDateFormatter;

/**
 * This class implements a server worker thread used to serve {@link Request}s.
 * 
 * @author Radu Cotescu
 * 
 */
public class HTTPServerWorkerThread implements Runnable
{

	private Socket socket;
	private Logger log;
	private Request request;
	private Response response;

	/**
	 * Creates a worker thread for a {@code Socket}.
	 * 
	 * @param socket
	 *            the {@code Socket} for this worker thread
	 */
	public HTTPServerWorkerThread(Socket socket)
	{
		this.socket = socket;
		log = Logger.getLogger(getClass());
	}

	/**
	 * The thread's main function.
	 */
	public void run()
	{
		try
		{
			socket.setSoTimeout(30000);
			handleRequest();
		}
		catch (HTTPException e)
		{
			sendErrorPage(e);
		}
		catch (IOException e)
		{
			log.error("I/O error while handling request", e);
		}
	}

	/**
	 * Handles a request by creating the the {@link Request} and {@link Response} objects, which are then passed to a Request Handler.
	 * 
	 * @throws IOException
	 *             if any I/O error occurs
	 * @throws HTTPException
	 *             for any HTTP error
	 */
	private void handleRequest() throws IOException, HTTPException
	{
		request = new Request(socket);
		response = new Response(request);
		request.validateRequest();
		RequestHandler rh = getRequestHandler();
		if (rh != null)
		{
			rh.processRequest();
			logRequestInfo();
		}
		else
		{
			response.endResponse();
		}
	}

	/**
	 * Finds the appropriate {@link RequestHandler} for the {@link Request} associated with this thread's socket.
	 * 
	 * @return the {@link RequestHandler} which will process the {@link Request}
	 * @throws HTTPException
	 *             for any HTTP error
	 */
	private RequestHandler getRequestHandler() throws HTTPException
	{
		if (request.getRequestURI().equals("/server-status") && Configuration.INSTANCE.getBooleanValueFor(Configuration.SERVER_STATUS)) {
			return new DefaultRequestHandler(request, response);
		}
		if (request.getFile().isDirectory())
		{
			for (String fileName : HTTPServer.getDefaultDocuments())
			{
				File indexFile = new File(request.getFile(), fileName);
				if (indexFile.exists() && !indexFile.isDirectory())
				{
					request.setFile(indexFile);
					break;
				}
			}
		}
		if (!request.getFile().exists())
		{
			throw new HTTPException(HTTPStatusCode.HTTP_NOT_FOUND);
		}
		else
		{
			if (request.getFile().isDirectory())
				if (Configuration.INSTANCE.getBooleanValueFor(Configuration.SERVER_BROWSEABLE_DIRECTORIES))
				{
					return new DirectoryIndexRequestHandler(request, response);
				}
				else
				{
					throw new HTTPException(HTTPStatusCode.HTTP_FORBIDDEN);
				}

			else
			{
				return new DefaultRequestHandler(request, response);
			}
		}
	}

	/**
	 * Based on a {@link HTTPException}, this method creates the HTML for the error page that will be served to the client.
	 * 
	 * @param httpException
	 *            the {@link HTTPException} that will be used to generate the error page
	 */
	private void sendErrorPage(HTTPException httpException)
	{
		try
		{
			StringBuilder sb = new StringBuilder();
			sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n");
			sb.append("<html>\n<head>\n<title>").append(httpException.getIntStatusCode()).append(" ").append(httpException.getMessage())
					.append("</title>\n</head>\n");
			sb.append("<body>\n").append(HTTPErrorStatusCodesMap.getErrorMessage(httpException.getHTTPStatusCode())).append("\n");
			sb.append("<hr />").append(HTTPServer.SERVER_NAME).append("\n</body>\n</html>");
			String htmlErrorMessage = sb.toString();
			response.sendResponseStatus(httpException.getHTTPStatusCode());
			response.addHeader(HTTPResponseHeader.ContentLength, new Integer(htmlErrorMessage.getBytes().length).toString());
			response.addHeader(HTTPResponseHeader.ContentType, HTTPServer.getMimeMap().getContentType("html"));
			response.addHeader(HTTPResponseHeader.LastModified, HTTPDateFormatter.getFormattedDate(new Date()));
			response.sendHeaders();
			if (request.getMethod() != HTTPMethod.HEAD)
			{
				response.write(htmlErrorMessage);
			}
			response.endResponse();
			logRequestInfo();
		}
		catch (IOException e)
		{
			log.error("Unable to send error page due to I/O error", e);
			logRequestError();
			log.error("HTTP exception", httpException);
		}

	}

	/**
	 * Creates a logger entry for a request using the INFO level.
	 */
	private void logRequestInfo()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(request.getSocket().getInetAddress().getHostAddress()).append(" - \"").append(request.getFirstRequestLine())
				.append("\" ").append(response.getHTTPStatusCode().getStatusCode()).append(" ")
				.append(response.getHeaderValue(HTTPResponseHeader.ContentLength)).append(" \"")
				.append(request.getHeaders().get(HTTPRequestHeader.UserAgent.getHeader())).append("\"");
		log.info(sb);
	}

	/**
	 * Creates a logger entry for a request using the ERROR level.
	 */
	private void logRequestError()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(request.getSocket().getInetAddress().getHostAddress()).append(" - \"").append(request.getFirstRequestLine())
				.append("\" ").append(response.getHTTPStatusCode().getStatusCode()).append(" ")
				.append(response.getHeaderValue(HTTPResponseHeader.ContentLength)).append(" \"")
				.append(request.getHeaders().get(HTTPRequestHeader.UserAgent.getHeader())).append("\"");
		log.error(sb);
	}

}
