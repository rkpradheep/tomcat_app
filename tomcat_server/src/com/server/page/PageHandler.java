package com.server.page;

import java.io.IOException;
import java.util.logging.Logger;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.server.framework.common.Configuration;
import com.server.framework.security.SecurityUtil;

public class PageHandler extends HttpServlet
{
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		if(request.getRequestURI().equals("/") || StringUtils.isEmpty(request.getRequestURI().replaceAll("/", StringUtils.EMPTY)))
		{
			response.sendRedirect(Configuration.getBoolean("production") ? "/app" : "/zoho");
			return;
		}
		response.setContentType("text/html; charset=UTF-8");
		String outputHtml = SecurityUtil.readFileAsString(request.getRequestURI() + ".html");
		outputHtml = outputHtml.replace("${PUBLIC_IP}", request.getRemoteAddr());
		response.getWriter().println(outputHtml);
	}
}
