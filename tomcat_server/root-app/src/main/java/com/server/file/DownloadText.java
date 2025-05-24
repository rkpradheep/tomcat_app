package com.server.file;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.server.framework.common.DateUtil;
import com.server.framework.security.SecurityUtil;

public class DownloadText extends HttpServlet
{
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		String fileName = request.getParameter("file_name");
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename=\"response.txt\"");

		FileInputStream fileInputStream = new FileInputStream(fileName);

		try(OutputStream outputStream = response.getOutputStream())
		{
			outputStream.write(SecurityUtil.readAllBytes(fileInputStream));
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			new File(fileName).delete();
		}
	}
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
	{

		String text = SecurityUtil.getJSONObject(request).getString("text");

		try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(text.getBytes());)
		{
			String fileName = "text_" + DateUtil.getCurrentTimeInMillis() + ".txt";
			FileOutputStream fileOutputStream = new FileOutputStream(fileName);
			fileOutputStream.write(SecurityUtil.readAllBytes(byteArrayInputStream));

			Map<String, String> responseMap = new HashMap<>();
			responseMap.put("file_name", fileName);
			SecurityUtil.writeJSONResponse(response, responseMap);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
