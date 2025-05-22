package com.server.framework.common;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class HealthCheck extends HttpServlet
{

	protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		response.getWriter().print(true);
	}
}
