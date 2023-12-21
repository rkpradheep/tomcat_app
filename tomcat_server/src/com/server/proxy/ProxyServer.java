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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import com.server.common.Util;

public class ProxyServer
{
	private static final Logger LOGGER = Logger.getLogger(ProxyServer.class.getName());

	public static void start()
	{
		try
		{
			Process process = Runtime.getRuntime().exec(new String[] {"bash", "-c", "netstat -nlp | grep 8092"});
			int status = process.waitFor();
			String message = status == 0 ? Util.getResponse(process.getInputStream()) : Util.getResponse(process.getErrorStream());
			if(message.contains("LISTEN"))
			{
				LOGGER.log(Level.INFO, "Proxy is already running " + message);
				return;
			}
			ServerSocket serverSocket = new ServerSocket(8092);

			while(true)
			{
				Socket clientSocket = serverSocket.accept();
				LOGGER.info("Got proxy request");
				new Thread(() -> {
					try
					{
						handleClientRequest(clientSocket);
					}
					catch(Exception e)
					{
						LOGGER.log(Level.SEVERE, "Exception occurred ", e);
					}
				}).start();
			}
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
		}
	}

	private static void handleClientRequest(Socket clientSocket) throws IOException
	{
		try
		{
			InputStream clientIn = clientSocket.getInputStream();
			OutputStream clientOut = clientSocket.getOutputStream();

			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(clientOut);

			LOGGER.info("Input Stream Available size " + clientIn.available());

			while(clientIn.available() == 0)
				;

			byte[] bytes = new byte[clientIn.available()];

			LOGGER.info("Input Stream Available size after waiting " + clientIn.available());

			int read = clientIn.read(bytes);

			LOGGER.info("Headers received ");
			LOGGER.info(new String(bytes, 0, read));
			String targetUrl = new String(bytes, 0, read).split("\\r\\n")[0].split(" ")[1];
			String targetHost = new BufferedReader(new StringReader(new String(bytes, 0, read))).lines().filter(s -> s.toLowerCase().startsWith("host")).findFirst().orElse(targetUrl);
			String[] hostAndPort = getHostAndPort(targetHost, targetUrl);

			LOGGER.log(Level.INFO, "Fetched Host {0} and Port {1}", new Object[] {hostAndPort[0], hostAndPort[1]});

			Socket targetSocket = new Socket();
			targetSocket.connect(new InetSocketAddress(hostAndPort[0], Integer.parseInt(hostAndPort[1])), 5000);

			Thread t1 = new Thread(() -> forwardData(new ByteArrayInputStream(bytes, 0, read), targetSocket));
			Thread t2 = new Thread(() -> forwardData(targetSocket, clientSocket));

			if(new String(bytes, 0, read).contains("CONNECT"))
			{
				outputStreamWriter.write("HTTP/1.0" + " 200 Connection established\r\n");
				outputStreamWriter.write("Proxy-agent: Simple/0.1\r\n");
				outputStreamWriter.write("\r\n");
				outputStreamWriter.flush();

				t1 = new Thread(() -> forwardData(clientSocket, targetSocket));
			}

			t1.start();
			t2.start();

			t1.join();
			t2.join();

			targetSocket.close();
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
		}
		finally
		{
			clientSocket.close();
			LOGGER.info("Connection closed successfully");
		}
	}

	static String[] getHostAndPort(String targetHost, String targetURL)
	{

		String port = targetURL.split(":").length > 1 ? targetURL.split(":")[1] : "80";
		Pattern pattern = Pattern.compile("([\\w.-]+):?(\\d*)");
		Matcher matcher = pattern.matcher(targetHost.toLowerCase().replaceFirst("host: ", ""));
		matcher.matches();

		LOGGER.info("Target Host " + matcher.group(1));
		return new String[] {matcher.group(1), matcher.group(2).length() > 0 ? matcher.group(2) : port};

	}

	private static void forwardData(Socket inputSocket, Socket outputSocket)
	{
		try
		{
			forwardData(inputSocket.getInputStream(), outputSocket);
		}
		catch(IOException e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
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
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
		}
	}

	//	private static void forwardData(Socket inputSocket, Socket outputSocket)
	//	{
	//		try
	//		{
	//			InputStream inputStream = inputSocket.getInputStream();
	//			try
	//			{
	//				OutputStream outputStream = outputSocket.getOutputStream();
	//				try
	//				{
	//					byte[] buffer = new byte[4096];
	//					int read;
	//					do
	//					{
	//						read = inputStream.read(buffer);
	//						if(read > 0)
	//						{
	//							outputStream.write(buffer, 0, read);
	//							if(inputStream.available() < 1)
	//							{
	//								outputStream.flush();
	//							}
	//						}
	//					} while(read >= 0);
	//				}
	//				finally
	//				{
	//					if(!outputSocket.isClosed() && !outputSocket.isOutputShutdown())
	//					{
	//						outputSocket.shutdownOutput();
	//					}
	//				}
	//			}
	//			finally
	//			{
	//				if(!inputSocket.isClosed() && !inputSocket.isInputShutdown())
	//				{
	//					inputSocket.shutdownInput();
	//				}
	//			}
	//		}
	//		catch(IOException e)
	//		{
	//			LOGGER.log(Level.SEVERE, "Exception occurred", e);
	//		}
	//	}

}
