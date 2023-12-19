package com.server.user;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.server.common.Util;

public class UserHandler extends HttpServlet
{
	@Override
	public void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException
	{
		try
		{
			if(httpServletRequest.getRequestURI().equals("/api/v1/admin/users"))
			{
				UserUtil.addUser(Util.getJSONObject(httpServletRequest));
				Util.writeSuccessJSONResponse(httpServletResponse, "User added successfully");
			}
		}
		catch(Exception e)
		{
			Util.writerErrorResponse(httpServletResponse, e.getMessage());
		}
	}
}
