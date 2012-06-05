package com.cotescu.radu.http.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.cotescu.radu.commons.PropertiesLoader;
import com.cotescu.radu.commons.StringUtils;

/**
 * This enum holds a singleton cache with the server's configuration settings.
 * 
 * @author Radu Cotescu
 * 
 */
public enum Configuration
{
	INSTANCE; // the single instance of this class which is instantiated automatically
	private final Logger log = Logger.getLogger(Configuration.class);
	private final Map<String, String> settingsMap;
	private final Map<String, Boolean> settingsOptionalityMap;
	public static final String SERVER_LISTEN_PORT = "server.listen.port";
	public static final String SERVER_LISTEN_ADDRESS = "server.listen.address";
	public static final String SERVER_ROOT_FOLDER = "server.root.folder";
	public static final String SERVER_DEFAULT_DOCUMENTS = "server.default.documents";
	public static final String SERVER_BROWSEABLE_DIRECTORIES = "server.browseable.directories";
	public static final String SERVER_THREADS_MIN = "server.threads.min";
	public static final String SERVER_THREADS_MAX = "server.threads.max";
	public static final String SERVER_THREADS_KEEPALIVE = "server.threads.keepalive";
	public static final String SERVER_THREADS_QUEUED_REQUESTS = "server.threads.queued.requests";
	public static final String SERVER_STATUS = "server.status";

	/**
	 * Private constructor for this enum.
	 */
	private Configuration()
	{
		settingsMap = new HashMap<String, String>();
		settingsOptionalityMap = new HashMap<String, Boolean>();
		init();
		readConfigurationFile();
	}

	/**
	 * Declares each setting parameter as mandatory or optional.
	 */
	private void init()
	{
		optional(SERVER_BROWSEABLE_DIRECTORIES);
		optional(SERVER_DEFAULT_DOCUMENTS);
		optional(SERVER_STATUS);
		mandatory(SERVER_LISTEN_ADDRESS);
		mandatory(SERVER_LISTEN_PORT);
		mandatory(SERVER_ROOT_FOLDER);
		mandatory(SERVER_THREADS_KEEPALIVE);
		mandatory(SERVER_THREADS_MAX);
		mandatory(SERVER_THREADS_MIN);
		mandatory(SERVER_THREADS_QUEUED_REQUESTS);
	}

	/**
	 * Marks a setting as optional.
	 * 
	 * @param settingName
	 *            the setting's name to be marked as optional
	 */
	private void optional(String settingName)
	{
		settingsOptionalityMap.put(settingName, true);
	}

	/**
	 * Marks a setting as mandatory.
	 * 
	 * @param settingName
	 *            the setting's name to be marked as mandatory
	 */
	private void mandatory(String settingName)
	{
		settingsOptionalityMap.put(settingName, false);
	}

	/**
	 * Reads and validates the properties from the configuration file, in the sense that it checks their presence related to their
	 * optionality degree.
	 */
	private void readConfigurationFile()
	{
		try
		{
			Properties configuration;
			if (StringUtils.isEmpty(HTTPServer.getConfigurationFilePath())) {
				configuration = PropertiesLoader.getPropertiesFromFileInClasspath(HTTPServer.CONFIG_FILE);
				HTTPServer.getLog().info("Using the server's default embedded configuration file.");
			}
			else {
				configuration = PropertiesLoader.getPropertiesFromFile(HTTPServer.getConfigurationFilePath());
				HTTPServer.getLog().info("Using configuration file " + HTTPServer.getConfigurationFilePath());
			}
			for (String settingName : settingsOptionalityMap.keySet())
			{
				String value = configuration.getProperty(settingName);
				if (StringUtils.isEmpty(value) && settingsOptionalityMap.get(settingName) == false)
				{
					log.error("Missing value for mandatory setting " + settingName);
					System.exit(1);
				}
				settingsMap.put(settingName, value);
			}
		}
		catch (IOException e)
		{
			log.error("Unable to read configuration file " + HTTPServer.getConfigurationFilePath());
			System.exit(1);
		}
	}

	/**
	 * Returns the value of a setting parameter as String.
	 * 
	 * @param settingName
	 *            the name of the setting for which the value is requested
	 * @return a String containing the setting's value
	 */
	public String getValueFor(String settingName)
	{
		return Configuration.INSTANCE.settingsMap.get(settingName);
	}

	/**
	 * Returns the value of a setting parameter as integer.
	 * 
	 * @param settingName
	 *            the name of the setting for which the value is requested
	 * @param defaultValue
	 *            the default value to be returned in case the parsing of the String value from the configuration file fails
	 * @return an int containing the setting's value
	 */
	public int getIntegerValueFor(String settingName, int defaultValue)
	{
		int value = defaultValue;
		try
		{
			value = Integer.parseInt(Configuration.INSTANCE.getValueFor(settingName));
		}
		catch (NumberFormatException nfe)
		{
			log.warn("Could not read " + settingName + " as Integer. Defaulting to " + value);
		}
		return value;
	}

	/**
	 * Returns the value of a setting parameter as long.
	 * 
	 * @param settingName
	 *            the name of the setting for which the value is requested
	 * @param defaultValue
	 *            the default value to be returned in case the parsing of the String value from the configuration file fails
	 * @return a long containing the setting's value
	 */
	public long getLongValueFor(String settingName, long defaultValue)
	{
		long value = defaultValue;
		try
		{
			value = Long.parseLong(Configuration.INSTANCE.getValueFor(settingName));
		}
		catch (NumberFormatException nfe)
		{
			log.warn("Could not read " + settingName + " as Long. Defaulting to " + value);
		}
		return value;
	}

	/**
	 * Returns the value of a setting parameter as boolean.
	 * 
	 * @param settingName
	 *            the name of the setting for which the value is requested
	 * @return a boolean containing the setting's value
	 */
	public boolean getBooleanValueFor(String settingName)
	{
		return Boolean.parseBoolean(Configuration.INSTANCE.getValueFor(settingName));
	}
}
