package com.server.common;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.server.security.SecurityUtil;
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

		String fileName = "hotswap_commands_" + System.currentTimeMillis() + ".txt";
		File tempFile = new File(System.getProperty("io.tempdir"), fileName);
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
		String[] command = new String[] {"bash", "-c", System.getenv("JAVA_HOME") + "/bin/jdb -attach " + host + ":" + port + "< " + fileName};
		String message = "Something went wrong!";
		try
		{
			message = ShellExecutor.execute(command);
			message = StringUtils.contains(message, "Initializing jdb") ? "Hotswap completed" : message;
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
		}
		tempFile.delete();

		SecurityUtil.writeSuccessJSONResponse(httpServletResponse, message);

	}
}
