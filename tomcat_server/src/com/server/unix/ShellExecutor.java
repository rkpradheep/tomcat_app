package com.server.unix;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.server.common.Configuration;
import com.server.common.Util;

public class ShellExecutor extends HttpServlet
{
	private static final Logger LOGGER = Logger.getLogger(ShellExecutor.class.getName());

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		String command = request.getParameter("command");
		try
		{
			if(request.getParameter("password") != null)
			{
				if(request.getParameter("password").equals("null") || request.getParameter("password").equals(""))
				{
					response.getWriter().println("Password mandatory");
					return;
				}
				else if(!request.getParameter("password").equals("111"))
				{
					response.getWriter().println("Incorrect password");
					return;
				}
			}

			if(command.contains("takePhoto"))
			{
				takePhotoAndPostToBot(request, response);
			}
			else if(command.contains("takeVideo"))
			{
				new Thread(() -> takeVideoAndPostToBot(request, response)).start();
			}
			else if(command.contains("wakeUpAlert"))
			{
				executeCommand(request, response, new String[] {command.split(" ")[0], command.substring(command.indexOf(" ") + 1)});
			}
			else
			{
				executeCommand(request, response, new String[] {"bash", "-c", command});
			}
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred ", e);
			response.getWriter().print("Failed :\n\n");
			response.getWriter().print(e.getMessage());
		}
	}

	public void takePhotoAndPostToBot(HttpServletRequest request, HttpServletResponse response) throws Exception
	{

		executeCommand(request, response, new String[] {"bash", "-c", "fswebcam --jpeg 100 myImage.jpeg"});

		String fileName = "myImage.jpeg";

		String boundary = "---" + Long.toHexString(System.currentTimeMillis());

		URL url = new URL("https://cliq.zoho.com/company/64396901/api/v2/bots/myserver/files?zapikey=1001.cb9555d23c48ab721daae1657431b62f.5d7e4f5eabc947097d2d4fd64a235f49");

		HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
		httpURLConnection.setRequestMethod("POST");

		httpURLConnection.setDoOutput(true);
		httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

		OutputStream outputStream = httpURLConnection.getOutputStream();
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);

		writer.print("--" + boundary + "\r\n");
		writer.print("Content-Disposition: form-data; name=\"file\"; filename=\"myImage.jpeg\"\r\n");
		writer.print("Content-Type: image/jpeg\r\n\r\n");
		writer.flush();

		FileInputStream inputStream = new FileInputStream(fileName);
		byte[] buffer = new byte[4096];
		int bytesRead;
		while((bytesRead = inputStream.read(buffer)) != -1)
		{
			outputStream.write(buffer, 0, bytesRead);
		}
		outputStream.flush();
		inputStream.close();

		writer.append("\r\n--" + boundary + "--\r\n");
		writer.close();
		outputStream.close();

		LOGGER.info("API Response " + Util.getResponse(httpURLConnection.getInputStream()));
		//TODO: Alternative with advanced library to achieve the same

		//		String url = "https://cliq.zoho.com/company/64396901/api/v2/bots/myserver/files?zapikey=1001.cb9555d23c48ab721daae1657431b62f.5d7e4f5eabc947097d2d4fd64a235f49";
		//		File file = new File("myImage.jpeg");
		//
		//		HttpEntity entity = MultipartEntityBuilder.create()
		//			.addBinaryBody("file", file)
		//			.build();
		//
		//		HttpPost httpPost = new HttpPost(url);
		//		httpPost.setEntity(entity);
		//
		//		HttpClient httpClient = HttpClientBuilder.create().build();
		//		HttpResponse response = httpClient.execute(httpPost);
		//		writeResponse(getResponse(response.getEntity().getContent()), true);
	}

	public void takeVideoAndPostToBot(HttpServletRequest request, HttpServletResponse response)
	{
		try
		{
			String seconds = request.getParameter("command").split(" ").length > 1 ? request.getParameter("command").split(" ")[1] : "5";

			executeCommand(request, response, new String[] {"bash", "-c", "takeVideo " + seconds});
			String fileName = "video.mp4";
			String filePath = Util.HOME_PATH + fileName;

			String boundary = "---" + Long.toHexString(System.currentTimeMillis());

			URL url = new URL("https://cliq.zoho.com/company/64396901/api/v2/bots/myserver/files?zapikey=1001.cb9555d23c48ab721daae1657431b62f.5d7e4f5eabc947097d2d4fd64a235f49");

			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			httpURLConnection.setRequestMethod("POST");

			httpURLConnection.setDoOutput(true);
			httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

			OutputStream outputStream = httpURLConnection.getOutputStream();
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);

			writer.print("--" + boundary + "\r\n");
			writer.print("Content-Disposition: form-data; name=\"file\"; filename=\"video.mp4\"\r\n");
			writer.print("Content-Type: video/mp4\r\n\r\n");
			writer.flush();

			FileInputStream inputStream = new FileInputStream(filePath);
			byte[] buffer = new byte[4096];
			int bytesRead;
			while((bytesRead = inputStream.read(buffer)) != -1)
			{
				outputStream.write(buffer, 0, bytesRead);
			}
			outputStream.flush();
			inputStream.close();

			writer.append("\r\n--").append(boundary).append("--\r\n");
			writer.close();
			outputStream.close();

			LOGGER.info("API Response " + Util.getResponse(httpURLConnection.getInputStream()));
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception ", e);
		}
	}

	private void executeCommand(HttpServletRequest request, HttpServletResponse response, String[] cmdArray) throws Exception
	{
		Process process = Runtime.getRuntime().exec(cmdArray);

		int status = process.waitFor();

		String message = status == 0 ? Util.getResponse(process.getInputStream()) : Util.getResponse(process.getErrorStream());
		if(status != 0)
		{
			postCommandFailureMessage(message, request, request.getParameter("command"));
			response.getWriter().println(message);
		}
		else
		{
			response.getWriter().println(message.isEmpty() ? "Success" : message);
		}
	}

	public void postCommandFailureMessage(String message, HttpServletRequest request, String command) throws Exception
	{
		Util.postMessageToBot("Proxy IP : *" + request.getRemoteAddr() + "*\n\n\nSource IP : *" + request.getHeader("X-FORWARDED-FOR") + "*\n\n\nCommand Executed :\n\n*" + command.replace(Configuration.getProperty("machine.password"), "*********") + "*\n\n\n" + message);
	}
}
