package com.server.page;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.server.common.Util;
import com.server.db.DBUtil;

public class PageHandler extends HttpServlet
{
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		if(request.getRequestURI().equals("/"))
		{
			response.sendRedirect(DBUtil.isMysql? "/zoho" : "/app");
			return;
		}
		response.setContentType("text/html; charset=UTF-8");
		String outputHtml = Util.readFileAsString(request.getRequestURI() + ".html");
		response.getWriter().println(outputHtml);
	}
}
