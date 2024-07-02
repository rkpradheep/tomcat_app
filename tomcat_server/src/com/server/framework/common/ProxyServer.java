package com.server.framework.common;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProxyServer
{
	private static final Logger LOGGER = Logger.getLogger(ProxyServer.class.getName());

	private static ExecutorService executorService;
	private static ExecutorService executorServiceForRequestHandling;
	private static final int MAX_WAIT_SECONDS = 30;
	private static int MAX_WORKER_SIZE = 50;
	private static final AtomicInteger THREAD_NUMBER = new AtomicInteger(0);
	private static final AtomicInteger WORKER_THREAD_NUMBER = new AtomicInteger(0);
	private static final String TEN_HASH = String.join("", Collections.nCopies(10, "#"));
	private static boolean stopProxy = false;
	private static int PROXY_PORT;
	private static String PROXY_CREDENTIAL;

	public static void main(String[] args)
	{
		String userName = null, password = null;
		String defaultPort = "3128";

		Map<String, String> customPropertyMap = new HashMap<>();

		for(String arg : args)
		{
			if(arg.split("=").length == 0)
			{
				LOGGER.log(Level.SEVERE, "Invalid property specified. Format :: key=value");
				return;
			}
			customPropertyMap.put(arg.split("=")[0].trim(), arg.substring(arg.indexOf("=") + 1));
		}

		if(customPropertyMap.get("port") != null)
		{
			try
			{
				defaultPort = String.valueOf(Integer.parseInt(customPropertyMap.get("port")));
			}
			catch(Exception e)
			{
				LOGGER.log(Level.SEVERE, "Invalid port number");
				return;
			}
		}
		if(customPropertyMap.get("user") != null && customPropertyMap.get("password") != null)
		{
			userName = customPropertyMap.get("user");
			password = customPropertyMap.get("password");
		}

		if(customPropertyMap.get("threads") != null)
		{
			try
			{
				MAX_WORKER_SIZE = Integer.parseInt(customPropertyMap.get("threads"));
				if(MAX_WORKER_SIZE < 1)
				{
					throw new Exception();
				}
			}
			catch(Exception e)
			{
				LOGGER.log(Level.SEVERE, "Invalid value specified for threads");
				return;
			}
		}

		init(defaultPort, userName, password);
	}

	public static void init(String port, String userName, String password)
	{
		PROXY_PORT = Integer.parseInt(port);

		if(isProxyAlreadyRunning())
		{
			LOGGER.log(Level.INFO, "Another process with port {0} is already running", new Object[] {PROXY_PORT});
			return;
		}

		PROXY_CREDENTIAL = (userName == null || password == null) ? null : (userName + ":" + password);

		ThreadFactory tf = run -> new Thread(run, "proxy-server");

		ThreadFactory requestTF = run -> new Thread(run, "proxy-worker-thread-" + THREAD_NUMBER.incrementAndGet());

		executorService = Executors.newFixedThreadPool(1, tf);
		executorServiceForRequestHandling = Executors.newFixedThreadPool(MAX_WORKER_SIZE, requestTF);
		executorService.submit(ProxyServer::start);

	}

	public static void shutDown()
	{
		stopProxy = true;
		if(isProxyAlreadyRunning())
		{
			executorService.shutdownNow();
			executorServiceForRequestHandling.shutdownNow();
		}
	}

	public static void start()
	{
		if(isProxyAlreadyRunning())
		{
			LOGGER.log(Level.INFO, "Proxy is already running ");
			return;
		}

		try(ServerSocket serverSocket = new ServerSocket(PROXY_PORT))
		{
			LOGGER.info("Proxy server started");
			while(!stopProxy)
			{
				Socket clientSocket = serverSocket.accept();
				executorServiceForRequestHandling.submit(() -> {
					try
					{
						LOGGER.info(TEN_HASH +  " Remote IP :: "  + ((InetSocketAddress)clientSocket.getRemoteSocketAddress()).getHostName() + " Got proxy request : " + Thread.currentThread().getName() + " " + TEN_HASH);
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
		try(Socket socket = new Socket())
		{
			socket.connect(new InetSocketAddress("127.0.0.1", PROXY_PORT), 3000);
			return true;
		}
		catch(Exception e)
		{
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

			//LOGGER.info("Input Stream Available size before waiting " + clientIn.available());

			long maxWaitLimit = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(3);

			while(clientIn.available() == 0 && System.currentTimeMillis() < maxWaitLimit)
				;

			byte[] bytes = new byte[clientIn.available()];

			//LOGGER.info("Input Stream Available size after waiting " + clientIn.available());

			int read = clientIn.read(bytes);

			String header = new String(bytes, 0, read);
			//LOGGER.info("Headers received \n\n" + header);

			if(!handleAuthorization(header, outputStreamWriter))
			{
				return;
			}

			String targetUrl = header.split("\\r\\n")[0].split(" ")[1];
			String targetHost = getHeader(header, "host");
			String[] hostAndPort = getHostAndPort(targetHost, targetUrl);

			LOGGER.log(Level.INFO, "Fetched Host {0} and Port {1}", new Object[] {hostAndPort[0], hostAndPort[1]});

			Socket targetSocket = new Socket();
			targetSocket.connect(new InetSocketAddress(hostAndPort[0], Integer.parseInt(hostAndPort[1])), 5000);

			if(header.contains("CONNECT"))
			{
				outputStreamWriter.write("HTTP/1.0" + " 200 Connection established\r\n");
				outputStreamWriter.write("Proxy-agent: Simple/0.1\r\n");
				outputStreamWriter.write("\r\n");
				outputStreamWriter.flush();

				ThreadFactory forwarderThread = run -> new Thread(run, "forwarder-" + WORKER_THREAD_NUMBER.incrementAndGet() + "-" + Thread.currentThread().getName());
				ExecutorService executorService = Executors.newFixedThreadPool(2, forwarderThread);

				Future<?> clientThread = executorService.submit(() -> forwardData(clientSocket, targetSocket, "Proxy's Client Socket"));
				Future<?> targetThread = executorService.submit(() -> forwardData(targetSocket, clientSocket, "Proxy's Client's Target Socket"));

				try
				{
					maxWaitLimit = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(MAX_WAIT_SECONDS);
					while(System.currentTimeMillis() < maxWaitLimit && !clientThread.isDone())
						;
					targetSocket.shutdownOutput();
					clientSocket.shutdownInput();
					clientThread.get(1, TimeUnit.SECONDS);
					targetThread.get(1, TimeUnit.SECONDS);
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
				while(targetSocket.getInputStream().available() < 1 && System.currentTimeMillis() < maxWaitLimit)
					;

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
			if(!clientSocket.isClosed())
			{
				clientSocket.close();
			}
			LOGGER.info(TEN_HASH + " Completed proxy request " + TEN_HASH);
		}
	}

	static String getHeader(String header, String headerName)
	{
		String headerLine = new BufferedReader(new StringReader(header)).lines().filter(s -> s.toLowerCase().startsWith(headerName.toLowerCase())).findFirst().orElse("").trim();
		return headerLine.split(":").length > 1 ? headerLine.substring(headerLine.indexOf(":") + 1).trim() : "";
	}

	static String[] getHostAndPort(String targetHost, String targetURL) throws Exception
	{
		if(targetURL.startsWith("http"))
		{
			URL url = new URL(targetURL);
			String port = (url.getPort() == -1 ? url.getDefaultPort() : url.getPort()) + "";
			return new String[] {url.getHost(), port};
		}

		Pattern pattern = Pattern.compile("([\\w.-]+):?(\\d*)");
		Matcher matcher = pattern.matcher(targetHost.toLowerCase().replaceFirst("host: ", ""));
		matcher.matches();

		return new String[] {matcher.group(1), matcher.group(2).trim().length() == 0 ? "443" : matcher.group(2).trim()};

	}

	private static boolean handleAuthorization(String header, Writer outputStreamWriter) throws IOException
	{
		if(PROXY_CREDENTIAL == null)
		{
			return true;
		}
		String credentialReceived = getHeader(header, "Proxy-Authorization");
		Pattern authorizationHeaderPattern = Pattern.compile("Basic\\s+(.*)"); //No I18N
		Matcher authorizationHeaderMatcher = authorizationHeaderPattern.matcher(credentialReceived);
		if(authorizationHeaderMatcher.matches())
		{
			credentialReceived = new String(Base64.getDecoder().decode(authorizationHeaderMatcher.group(1)));
		}
		if(!PROXY_CREDENTIAL.equals(credentialReceived))
		{
			outputStreamWriter.write("HTTP/1.1 407 Proxy Authentication Required\r\n");
			outputStreamWriter.write("Proxy-Authenticate: Basic realm=\"Proxy\"\r\n");

			outputStreamWriter.write("\r\n");
			outputStreamWriter.flush();

			outputStreamWriter.close();

			LOGGER.info("Authorization failed for Proxy request");
			return false;
		}
		return true;
	}

	private static void forwardData(Socket inputSocket, Socket outputSocket, String partyName)
	{
		try
		{
			forwardData(inputSocket.getInputStream(), outputSocket);
			//LOGGER.info(partyName + " Stream Closed");
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
			//IOUtils.copy(inputStream, outputSocket.getOutputStream());
			OutputStream outputStream = outputSocket.getOutputStream();
			int n;
			byte[] buffer = new byte[8192];
			while((n = inputStream.read(buffer)) != -1)
			{
				outputStream.write(buffer, 0, n);
			}
		}
		catch(IOException e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred " + e);
		}
	}

}
