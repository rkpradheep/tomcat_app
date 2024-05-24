package com.server.framework.common;

import java.io.IOException;
import java.util.Arrays;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.server.framework.persistence.Criteria;
import com.server.framework.persistence.DataAccess;
import com.server.framework.persistence.SelectQuery;

public class HealthCheck extends HttpServlet
{
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		SelectQuery selectQuery = new SelectQuery("Users");
		selectQuery.setCriteria(new Criteria("Users", "name", Arrays.asList("admin"), Criteria.Constants.IN));

		try
		{
			DataAccess.get(selectQuery);
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
		response.getWriter().print(true);
	}
}
