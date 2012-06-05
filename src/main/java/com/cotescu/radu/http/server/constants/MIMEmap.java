package com.cotescu.radu.http.server.constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.cotescu.radu.commons.StringUtils;

/**
 * This class stores a MIMEs map useful to send the Content-Type header for an HTTP response.
 * 
 * @author Radu Cotescu
 * 
 */
public class MIMEmap
{

	private static final Map<String, String> mimeTypes = new HashMap<String, String>();
	private static final Logger log = Logger.getLogger(MIMEmap.class);
	private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

	/**
	 * Creates a MIMEmap from the mime.types file supplied with the server.
	 */
	public MIMEmap()
	{
		try
		{
			InputStream is = ClassLoader.getSystemClassLoader().getResource("mime.types").openStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = reader.readLine()) != null)
			{
				StringTokenizer st = new StringTokenizer(line);
				if (st.countTokens() < 2 || line.startsWith("#"))
				{
					continue;
				}
				String mime = st.nextToken();
				while (st.hasMoreTokens())
				{
					mimeTypes.put(st.nextToken(), mime);
				}
			}
			reader.close();
		}
		catch (IOException e)
		{
			log.error("Unable to read from input stream.", e);
		}
	}

	/**
	 * Returns the content type associated with the extension of a file.
	 * 
	 * @param extension
	 *            the extension for which the content type is needed
	 * @return a String containing the content type
	 */
	public String getContentType(String extension)
	{
		String contentType = mimeTypes.get(extension);
		if (StringUtils.isEmpty(contentType))
		{
			contentType = DEFAULT_CONTENT_TYPE;
		}
		return contentType;
	}
}
