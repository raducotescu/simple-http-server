package com.cotescu.radu.http.server.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.log4j.Logger;

public class HTTPDateFormatter {
	
	private static Logger log = Logger.getLogger(HTTPDateFormatter.class);
	
	public static String getFormattedDate(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss zz");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		return sdf.format(date);
	}
	
	public static Date getDateFromString(String date) {
		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.applyPattern("E, dd MMM yyyy HH:mm:ss zz");
		Date resultedDate = null;
		try {
			resultedDate = sdf.parse(date);
		} catch (ParseException e) {
			sdf.applyPattern("EEEE, dd-MMM-yy HH:mm:ss zz");
			try {
				resultedDate = sdf.parse(date);
			} catch (ParseException e1) {
				sdf.applyPattern("E MMM d HH:mm:ss yyyy");
				try {
					resultedDate = sdf.parse(date);
				} catch (ParseException e2) {
					log.error("Unable to parse date string received as " + date, e);
				}
			}
		}
		return resultedDate;
	}
}
