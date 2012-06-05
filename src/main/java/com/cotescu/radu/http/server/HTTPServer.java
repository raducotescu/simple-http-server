package com.cotescu.radu.http.server;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.cotescu.radu.commons.StringUtils;
import com.cotescu.radu.http.server.constants.MIMEmap;

/**
 * This is the main class of the HTTP server.
 * 
 * @author Radu Cotescu
 * 
 */
public class HTTPServer implements Runnable
{

	public static final String HTTP_VERSION = "HTTP/1.1";
	public static final String SERVER_NAME = "Radu's HTTP Server";
	public static final String SERVER_VERSION = "0.0.1";
	public static final String CONFIG_FILE = "shs.conf";
	
	private static final Logger log = Logger.getLogger(HTTPServer.class);

	private static String configurationFilePath;
	private static int port;
	private static InetAddress address;
	private static ServerSocket serverSocket;
	private Socket socket;
	private static File rootDirectory;
	private static MIMEmap mimeMap;
	private static List<String> defaultDocuments;
	private static boolean isRunning = true;

	private static ExecutorService executorService;
	private static BlockingQueue<Runnable> tasksQueue;

	/**
	 * Creates the main server thread. If no configuration file is supplied to the server, a default file will be used with the following
	 * content:
	 * 
	 * server.listen.port=8080
	 * server.listen.address=0.0.0.0
	 * server.root.folder=./www/
	 * server.default.documents=index.html
	 * server.browseable.directories=true
	 * server.threads.min=5
	 * server.threads.max=10
	 * server.threads.keepalive=60
	 * server.threads.queued.requests=10
	 * server.status=true
	 * 
	 * @param _configurationFilePath
	 *            the path for the configuration file
	 */
	public HTTPServer(String _configurationFilePath)
	{

		if (!StringUtils.isEmpty(_configurationFilePath))
		{
			configurationFilePath = _configurationFilePath;
		}
		validateConfiguration();
		try
		{
			mimeMap = new MIMEmap();
			serverSocket = new ServerSocket(port, 0, address);
			log.info("Started server on " + serverSocket.getInetAddress().toString().replace("/", "") + ":" + serverSocket.getLocalPort());
			log.info("Root directory is " + rootDirectory);
		}
		catch (IOException e)
		{
			log.error("Unable to open socket for address " + address.getHostName() + ", port " + port);
			System.exit(1);
		}
	}

	/**
	 * Reads the Configuration properties and validates them.
	 */
	private void validateConfiguration()
	{
		try
		{
			port = Configuration.INSTANCE.getIntegerValueFor(Configuration.SERVER_LISTEN_PORT, 8080);
			if (port < 0 || port > 65535)
				throw new NumberFormatException("Invalid value for listen port.");
			address = InetAddress.getByName(Configuration.INSTANCE.getValueFor(Configuration.SERVER_LISTEN_ADDRESS));
			rootDirectory = new File(Configuration.INSTANCE.getValueFor(Configuration.SERVER_ROOT_FOLDER)).getCanonicalFile();
			if (!rootDirectory.exists())
			{
				throw new IOException("Root directory " + rootDirectory + " does not exist.");
			}
			defaultDocuments = new ArrayList<String>();
			if (Configuration.INSTANCE.getValueFor(Configuration.SERVER_DEFAULT_DOCUMENTS) != null)
			{
				String[] ddocs = Configuration.INSTANCE.getValueFor(Configuration.SERVER_DEFAULT_DOCUMENTS).split(",");
				defaultDocuments = new ArrayList<String>(Arrays.asList(ddocs));
			}
			for (String d : defaultDocuments)
			{
				defaultDocuments.set(defaultDocuments.indexOf(d), d.trim());
			}
			if (Configuration.INSTANCE.getIntegerValueFor(Configuration.SERVER_THREADS_MIN, 5) > Configuration.INSTANCE.getIntegerValueFor(
					Configuration.SERVER_THREADS_MAX, 10))
			{
				throw new IllegalArgumentException("The number of min threads cannot be smaller than the number of max threads.");
			}
			tasksQueue = new ArrayBlockingQueue<Runnable>(Configuration.INSTANCE.getIntegerValueFor(
					Configuration.SERVER_THREADS_QUEUED_REQUESTS, 10));
			executorService = new ThreadPoolExecutor(Configuration.INSTANCE.getIntegerValueFor(Configuration.SERVER_THREADS_MIN, 5),
					Configuration.INSTANCE.getIntegerValueFor(Configuration.SERVER_THREADS_MAX, 10),
					Configuration.INSTANCE.getIntegerValueFor(Configuration.SERVER_THREADS_KEEPALIVE, 30), TimeUnit.SECONDS, tasksQueue,
					new ServerThreadFactory(), new RejectedExecution());
		}
		catch (Exception e)
		{
			log.error("Invalid configuration values detected", e);
			System.exit(1);
		}

	}

	/**
	 * Adapter used to implement the behaviour of the thread pool when no more tasks can be queued. In this case all additional requests
	 * will be refused and the connections will be closed.
	 * 
	 * @author Radu Cotescu
	 * 
	 */
	private class RejectedExecution implements RejectedExecutionHandler
	{
		@Override
		public void rejectedExecution(Runnable task, ThreadPoolExecutor executor)
		{
			try
			{
				socket.close();
			}
			catch (IOException e)
			{
				log.error("Unable to close socket", e);
			}
		}

	}

	public void run()
	{
		while (isRunning)
		{
			try
			{
				socket = serverSocket.accept();
				executorService.execute(new HTTPServerWorkerThread(socket));
			}
			catch (IOException e)
			{
				log.error("I/o error while waiting for connection", e);
			}
		}

	}

	public static int getListenPort()
	{
		return port;
	}

	public static InetAddress getListenAddress()
	{
		return address;
	}

	public static File getRootDirectory()
	{
		return rootDirectory;
	}

	public static MIMEmap getMimeMap()
	{
		return mimeMap;
	}

	public static List<String> getDefaultDocuments()
	{
		return defaultDocuments;
	}

	public static String getConfigurationFilePath()
	{
		return configurationFilePath;
	}

	public static Logger getLog()
	{
		return log;
	}

	public static ExecutorService getExecutorService()
	{
		return executorService;
	}

	public static void main(String[] args)
	{
		String configurationFilePath = null;
		if (args.length == 1)
		{
			configurationFilePath = args[0];
		}
		HTTPServer server = new HTTPServer(configurationFilePath);
		new Thread(server).start();
	}

}
