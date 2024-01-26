package com.server.file;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.server.common.Util;

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
			outputStream.write(Util.readAllBytes(fileInputStream));
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

		String text = Util.getJSONObject(request).getString("text");

		try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(text.getBytes());)
		{
			String fileName = "text_" + System.currentTimeMillis() + ".txt";
			FileOutputStream fileOutputStream = new FileOutputStream(fileName);
			fileOutputStream.write(Util.readAllBytes(byteArrayInputStream));

			Map<String, String> responseMap = new HashMap<>();
			responseMap.put("file_name", fileName);
			Util.writeJSONResponse(response, responseMap);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
