package com.server.common;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HealthCheck extends HttpServlet
{
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		response.getWriter().print(true);
	}
}
