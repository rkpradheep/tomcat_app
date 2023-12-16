package com.server.page;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.server.common.Util;

public class PageHandler extends HttpServlet
{
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		if(request.getRequestURI().equals("/"))
		{
			response.sendRedirect("/zoho");
			return;
		}
		response.setContentType("text/html; charset=UTF-8");
		String outputHtml = Util.readFileAsString(request.getRequestURI() + ".html");
		response.getWriter().println(outputHtml);
	}
}
