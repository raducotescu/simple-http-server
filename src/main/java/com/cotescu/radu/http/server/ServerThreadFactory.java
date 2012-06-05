package com.cotescu.radu.http.server;

import java.util.concurrent.ThreadFactory;

/**
 * This factory is used to create {@link Runnable} tasks for the thread pool used by the {@link HTTPServer}.
 * 
 * @author Radu Cotescu
 * 
 */
public class ServerThreadFactory implements ThreadFactory
{

	private static ThreadGroup threadGroup;
	private static int n = 0;

	public ServerThreadFactory()
	{
		threadGroup = new ThreadGroup("HTTPServerWorkerThread");
	}

	@Override
	public Thread newThread(Runnable task)
	{
		n++;
		Thread t = new Thread(threadGroup, task, threadGroup.getName() + "-" + n);
		return t;
	}

}
