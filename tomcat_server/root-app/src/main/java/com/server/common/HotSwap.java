package com.server.common;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.server.framework.common.DateUtil;
import com.server.framework.security.SecurityUtil;
import com.server.unix.ShellExecutor;

public class HotSwap extends HttpServlet
{
	private static final Logger LOGGER = Logger.getLogger(HotSwap.class.getName());

	@Override
	protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException
	{
		JSONObject jsonObject = SecurityUtil.getJSONObject(httpServletRequest);
		JSONObject hotswapDetails = jsonObject.getJSONObject("hotswap_details");
		List<String> classNames = hotswapDetails.keySet().stream().collect(Collectors.toList());

		String fileName = "hotswap_commands_" + DateUtil.getCurrentTimeInMillis() + ".txt";
		File tempFile = new File(System.getProperty("java.io.tmpdir"), fileName);
		String host = jsonObject.optString("host");
		String port = jsonObject.optString("port");
		host = StringUtils.defaultIfEmpty(host, "localhost");
		port = StringUtils.defaultIfEmpty(port, "8001");

		FileWriter fileWriter = new FileWriter(tempFile);
		for(String className : classNames)
		{
			fileWriter.write("redefine " + className + " " + hotswapDetails.getString(className));
			fileWriter.flush();
		}
		fileWriter.close();
		String[] command = new String[] {"bash", "-c", System.getenv("JAVA_HOME") + "/bin/jdb -attach " + host + ":" + port + "< " + tempFile.getAbsolutePath()};
		String message = "Something went wrong!";
		try
		{
			message = ShellExecutor.execute(command);
			int errorMessageStartIndex = StringUtils.contains(message, "Initializing jdb ...") ? message.indexOf("Initializing jdb ...") + "Initializing jdb ...".length() : 0;
			message = StringUtils.substring(message, errorMessageStartIndex);
			message = StringUtils.equals(StringUtils.replace(message, "\n", StringUtils.EMPTY), "> > Input stream closed.") ? "Hotswap completed" : message;
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
		}
		tempFile.delete();

		SecurityUtil.writeSuccessJSONResponse(httpServletResponse, message);

	}
}
