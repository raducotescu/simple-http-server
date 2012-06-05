package com.cotescu.radu.http.server.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * This class provides a useful map that associates {@link HTTPStatusCode}s with more descriptive error messages, used for displaying error
 * pages.
 * 
 * @author Radu Cotescu
 * 
 */
public class HTTPErrorStatusCodesMap
{
	private static final Map<HTTPStatusCode, String> statusCodesMap = new HashMap<HTTPStatusCode, String>();

	static
	{
		statusCodesMap.put(HTTPStatusCode.HTTP_NOT_FOUND,
				formatDescription(HTTPStatusCode.HTTP_NOT_FOUND.getStatusMessage(), "Resource not found."));
		statusCodesMap.put(HTTPStatusCode.HTTP_FORBIDDEN,
				formatDescription(HTTPStatusCode.HTTP_FORBIDDEN.getStatusMessage(), "You are not allowed to access this resource."));
		statusCodesMap
				.put(HTTPStatusCode.HTTP_BAD_REQUEST,
						formatDescription(HTTPStatusCode.HTTP_BAD_REQUEST.getStatusMessage(),
								"The request cannot be fulfilled due to bad syntax."));
		statusCodesMap.put(
				HTTPStatusCode.HTTP_INTERNAL_SERVER_ERROR,
				formatDescription(HTTPStatusCode.HTTP_INTERNAL_SERVER_ERROR.getStatusMessage(),
						"The server encountered an internal error and cannot fulfill the request."));
		statusCodesMap.put(HTTPStatusCode.HTTP_NOT_IMPLEMENTED,
				formatDescription(HTTPStatusCode.HTTP_NOT_IMPLEMENTED.getStatusMessage(), "The request method is not implemented."));
	}

	/**
	 * Provide a HTML formatted description.
	 * 
	 * @param shortStatus
	 *            the short status of the HTTPStatusCode
	 * @param description
	 *            the description of the HTTPStatus error code
	 * @return a HTML formatted string
	 */
	private static String formatDescription(String shortStatus, String description)
	{
		return String.format("<h1>%s</h1>\n<p>%s</p>", shortStatus, description);
	}

	/**
	 * Creates a HTML formatted string with the error message for a HTTPStatus error code.
	 * 
	 * @param statusCode
	 *            the HTTPStatusCode for which the error will be generated
	 * @return a String containing the HTML formatted error message
	 */
	public static String getErrorMessage(HTTPStatusCode statusCode)
	{
		return statusCodesMap.get(statusCode);
	}
}
