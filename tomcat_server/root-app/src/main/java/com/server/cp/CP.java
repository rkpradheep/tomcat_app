package com.server.cp;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServlet;

import java.io.IOException;
import java.util.logging.Logger;


public class CP extends HttpServlet
{
	private static final Logger LOGGER = Logger.getLogger(CP.class.getName());

	@Override public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException
	{
	}
}
