package com.cotescu.radu.http.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import com.cotescu.radu.http.server.constants.HTTPMethod;
import com.cotescu.radu.http.server.constants.HTTPResponseHeader;
import com.cotescu.radu.http.server.constants.HTTPStatusCode;
import com.cotescu.radu.http.server.exceptions.HTTPException;
import com.cotescu.radu.http.server.utils.FileNameComparator;

/**
 * This class handles the listing of a directory represented by the {@link File} asked in a {@link Request}.
 * 
 * @author Radu Cotescu
 * 
 */
public class DirectoryIndexRequestHandler extends RequestHandler
{

	/**
	 * Creates a request handler for listing directories.
	 * 
	 * @param request
	 *            the {@link Request} object
	 * @param response
	 *            the {@link Response} object
	 */
	public DirectoryIndexRequestHandler(Request request, Response response)
	{
		super(request, response);
	}

	@Override
	public void processRequest() throws HTTPException, IOException
	{
		File[] files = request.getFile().listFiles();
		ArrayList<File> directories = new ArrayList<File>();
		ArrayList<File> regularFiles = new ArrayList<File>();
		for (File f : files)
		{
			if (f.isDirectory())
			{
				directories.add(f);
			}
			else
			{
				regularFiles.add(f);
			}
		}
		Comparator<File> fileNameComparator = new FileNameComparator();
		Collections.sort(directories, fileNameComparator);
		Collections.sort(regularFiles, fileNameComparator);
		String html = getHTML(request.getRequestURI(), directories, regularFiles);
		response.addHeader(HTTPResponseHeader.ContentLength, new Integer(html.getBytes().length).toString());
		response.addHeader(HTTPResponseHeader.ContentType, HTTPServer.getMimeMap().getContentType("html"));
		response.sendResponseStatus(HTTPStatusCode.HTTP_OK);
		response.sendHeaders();
		if (request.getMethod() != HTTPMethod.HEAD)
		{
			response.write(html);
		}
		response.endResponse();
	}

	/**
	 * Creates the HTML page returned for listing directories.
	 * 
	 * @param requestURI
	 *            the Request-URI
	 * @param directories
	 *            an ArrayList of directories
	 * @param files
	 *            an ArrayList of files
	 * @return a String containing the generated HTML for the response page
	 */
	private String getHTML(String requestURI, ArrayList<File> directories, ArrayList<File> files)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n");
		sb.append("<html>\n<head>\n<title>Index of ").append(requestURI).append("</title>\n</head>");
		sb.append("<body>\n<h1>Index of ").append(requestURI).append("</h1>\n");
		sb.append("<table>\n<tr><th>Name</th><th>Last modified</th><th>Size</th></tr><tr><th colspan=\"3\"><hr></th></tr>\n");
		for (File f : directories)
		{
			sb.append("<tr><td><a href=\"").append(requestURI);
			if (!requestURI.endsWith("/"))
			{
				sb.append("/");
			}
			sb.append(f.getName()).append("\">").append(f.getName()).append("</a></td><td>").append(new Date(f.lastModified()))
					.append("</td><td>-</td></tr>\n");
		}
		for (File f : files)
		{
			sb.append("<tr><td><a href=\"").append(requestURI);
			if (!requestURI.endsWith("/"))
			{
				sb.append("/");
			}
			sb.append(f.getName()).append("\">").append(f.getName()).append("</a></td><td>").append(new Date(f.lastModified()))
					.append("</td><td>").append(f.length()).append("</td></tr>\n");
		}
		sb.append("<tr><th colspan=\"3\"><hr></th></tr>\n</table>\n").append(HTTPServer.SERVER_NAME).append("\n</body>\n</html>");
		return sb.toString();
	}

}
