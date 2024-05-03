package com.server.proxy;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class ProxyServer
{
	private static final Logger LOGGER = Logger.getLogger(ProxyServer.class.getName());

	private static ExecutorService executorService;
	private static ExecutorService executorServiceForRequestHandling;
	private static final int MAX_WAIT_SECONDS = 5;
	private static final int MAX_WORKER_SIZE = 50;
	private static int THREAD_NUMBER = 1;
	private static final String TEN_HASH = Collections.nCopies(10, "#").stream().collect(Collectors.joining());
	private static boolean stopProxy = false;

	public static void init()
	{
		ThreadFactory tf = run -> {
			Thread thread = new Thread(run, "proxy-server");
			return thread;
		};

		ThreadFactory requestTF = run -> {
			Thread thread = new Thread(run, "proxy-worker-thread-" + THREAD_NUMBER++);
			return thread;
		};

		executorService = Executors.newFixedThreadPool(1, tf);
		executorServiceForRequestHandling = Executors.newFixedThreadPool(MAX_WORKER_SIZE, requestTF);
		executorService.submit(ProxyServer::start);
	}

	public static void shutDown()
	{
		stopProxy = true;
		executorService.shutdownNow();
		executorServiceForRequestHandling.shutdownNow();
	}

	public static void start()
	{
		try
		{
			if(isProxyAlreadyRunning())
			{
				LOGGER.log(Level.INFO, "Proxy is already running ");
				return;
			}
			ServerSocket serverSocket = new ServerSocket(8092);
			LOGGER.info("Proxy server started");
			while(!stopProxy)
			{
				Socket clientSocket = serverSocket.accept();
				executorServiceForRequestHandling.submit(() -> {
					try
					{
						LOGGER.info(TEN_HASH + " Got proxy request : " + Thread.currentThread().getName()  + " " + TEN_HASH);
						handleClientRequest(clientSocket);
					}
					catch(IOException e)
					{
						LOGGER.log(Level.SEVERE, "Exception occurred " + e);
					}
				});
			}
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred " + e);
		}
	}

	private static boolean isProxyAlreadyRunning()
	{
		try
		{
			new Socket("127.0.0.1", 8092);
			return true;
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Proxy is not running. Exception message : {0}", e);
			return false;
		}
	}

	private static void handleClientRequest(Socket clientSocket) throws IOException
	{
		try
		{
			InputStream clientIn = clientSocket.getInputStream();
			OutputStream clientOut = clientSocket.getOutputStream();

			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(clientOut);

			LOGGER.info("Input Stream Available size before waiting " + clientIn.available());

			long maxWaitLimit = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(MAX_WAIT_SECONDS);

			while(clientIn.available() == 0 && System.currentTimeMillis() < maxWaitLimit);

			byte[] bytes = new byte[clientIn.available()];

			LOGGER.info("Input Stream Available size after waiting " + clientIn.available());

			int read = clientIn.read(bytes);

			LOGGER.info("Headers received \n\n" + new String(bytes, 0, read));
			String targetUrl = new String(bytes, 0, read).split("\\r\\n")[0].split(" ")[1];
			String targetHost = new BufferedReader(new StringReader(new String(bytes, 0, read))).lines().filter(s -> s.toLowerCase().startsWith("host")).findFirst().orElse(targetUrl);
			String[] hostAndPort = getHostAndPort(targetHost, targetUrl);

			LOGGER.log(Level.INFO, "Fetched Host {0} and Port {1}", new Object[] {hostAndPort[0], hostAndPort[1]});

			Socket targetSocket = new Socket();
			targetSocket.connect(new InetSocketAddress(hostAndPort[0], Integer.parseInt(hostAndPort[1])), 5000);

			if(new String(bytes, 0, read).contains("CONNECT"))
			{
				outputStreamWriter.write("HTTP/1.0" + " 200 Connection established\r\n");
				outputStreamWriter.write("Proxy-agent: Simple/0.1\r\n");
				outputStreamWriter.write("\r\n");
				outputStreamWriter.flush();

				ThreadFactory forwarderThread = run -> {
					Thread thread = new Thread(run, "forwarder-" + Thread.currentThread().getName());
					return thread;
				};
				ExecutorService executorService = Executors.newFixedThreadPool(2, forwarderThread);

				Future<?> clientThread = executorService.submit(() -> forwardData(clientSocket, targetSocket, "Proxy's Client Socket"));
				Future<?> targetThread = executorService.submit(() -> forwardData(targetSocket, clientSocket,  "Proxy's Client's Target Socket"));

				try
				{
					clientThread.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
				}
				catch(Exception e)
				{
					LOGGER.log(Level.SEVERE, "Exception occurred " + e);
				}
				targetSocket.shutdownOutput();
				try
				{
					targetThread.get(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
				}
				catch(Exception e)
				{
					LOGGER.log(Level.SEVERE, "Exception occurred " + e);
				}

				executorService.shutdownNow();
			}
			else
			{
				String data = new String(bytes, 0, read);
				forwardData(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)), targetSocket);

				maxWaitLimit = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(MAX_WAIT_SECONDS);
				while(targetSocket.getInputStream().available() < 1 && System.currentTimeMillis() < maxWaitLimit) ;

				byte[] targetOutputBytes = new byte[targetSocket.getInputStream().available()];
				targetSocket.getInputStream().read(targetOutputBytes);
				forwardData(new ByteArrayInputStream(targetOutputBytes), clientSocket);

				targetSocket.shutdownOutput();
			}

			targetSocket.close();
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred " + e);
		}
		finally
		{
			clientSocket.close();
			LOGGER.info(TEN_HASH + " Completed proxy request " + TEN_HASH);
		}
	}

	static String[] getHostAndPort(String targetHost, String targetURL) throws Exception
	{

		String port = targetURL.split(":").length > 1 ? targetURL.split(":")[1] : "80";
		if(targetURL.startsWith("http"))
		{
			int portFromUrl = new URL(targetURL).getPort();
			port = (portFromUrl == -1 ? new URL(targetURL).getDefaultPort() : portFromUrl) + StringUtils.EMPTY;
		}
		Pattern pattern = Pattern.compile("([\\w.-]+):?(\\d*)");
		Matcher matcher = pattern.matcher(targetHost.toLowerCase().replaceFirst("host: ", ""));
		matcher.matches();

		LOGGER.info("Target Host " + matcher.group(1));
		return new String[] {matcher.group(1), matcher.group(2).length() > 0 ? matcher.group(2) : port};

	}

	private static void forwardData(Socket inputSocket, Socket outputSocket, String partyName)
	{
		try
		{
			forwardData(inputSocket.getInputStream(), outputSocket);
			LOGGER.info(partyName + " Stream Closed");
		}
		catch(IOException e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred " + e);
		}
	}

	private static void forwardData(InputStream inputStream, Socket outputSocket)
	{
		try
		{
			IOUtils.copy(inputStream, outputSocket.getOutputStream());
		}
		catch(IOException e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred " + e);
		}
	}

//		private static void forwardData(Socket inputSocket, Socket outputSocket)
//		{
//			try
//			{
//				InputStream inputStream = inputSocket.getInputStream();
//				try
//				{
//					OutputStream outputStream = outputSocket.getOutputStream();
//					try
//					{
//						byte[] buffer = new byte[4096];
//						int read;
//						do
//						{
//							if(inputSocket.isClosed())
//							{
//								break;
//							}
//							read = inputStream.read(buffer);
//							if(read > 0)
//							{
//								outputStream.write(buffer, 0, read);
//								if(inputStream.available() < 1)
//								{
//									outputStream.flush();
//								}
//							}
//						} while(read >= 0);
//					}
//					finally
//					{
//						if(!outputSocket.isClosed() && !outputSocket.isOutputShutdown())
//						{
//							outputSocket.shutdownOutput();
//						}
//					}
//				}
//				finally
//				{
//					if(!inputSocket.isClosed() && !inputSocket.isInputShutdown())
//					{
//						inputSocket.shutdownInput();
//					}
//					LOGGER.info("Stream Closed");
//				}
//			}
//			catch(IOException e)
//			{
//				LOGGER.log(Level.SEVERE, "Exception occurred", e);
//			}
//		}

}
