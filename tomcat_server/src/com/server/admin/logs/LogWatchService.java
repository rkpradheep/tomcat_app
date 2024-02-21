package com.server.admin.logs;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.server.common.Util;

public class LogWatchService
{
	private static WatchService watchService;
	private static final String logDir = Util.HOME_PATH + "/tomcat_build/logs/";
	private static final Path path = Paths.get(logDir);

	private static final Logger LOGGER = Logger.getLogger(LogWatchService.class.getName());

	private static ExecutorService executorService;

	public static void start()
	{
		if(Objects.isNull(watchService))
		{
			executorService = Executors.newFixedThreadPool(1);
			executorService.submit(LogWatchService::run);
			LOGGER.log(Level.INFO, "Started watching for logs");
		}
	}

	private static void run()
	{
		try
		{
			watchService = FileSystems.getDefault().newWatchService();

			path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

			String fileName = "app_log.txt";

			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(logDir + fileName)));
			while(reader.readLine() != null)
				;

			WatchKey key;
			while((key = watchService.take()) != null)
			{
				for(WatchEvent<?> event : key.pollEvents())
				{
					final Path changed = (Path) event.context();
					if(changed.toString().contains((fileName)))
					{
						String log = StringUtils.EMPTY;
						String temp;
						while(Objects.nonNull(temp = reader.readLine()))
						{
							log = StringUtils.join(log, temp, System.lineSeparator());
						}
						LiveLogHandler.broadcast(log);
					}
				}
				key.reset();
			}
		}
		catch(ClosedWatchServiceException closedWatchServiceException)
		{
			LOGGER.log(Level.INFO, "Stopped watching for logs");
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
		}
	}

	public static void stop()
	{
		try
		{
			watchService.close();
			watchService = null;
			executorService.shutdownNow();
			LOGGER.log(Level.INFO, "Thread shutdown for Log WatchService");
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
		}
	}
}
