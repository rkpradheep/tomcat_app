package com.server.framework.common;

import java.io.IOException;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HealthCheck extends HttpServlet
{
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		response.getWriter().print(true);
	}
}
