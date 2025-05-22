package com.server.framework.user;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import com.server.framework.security.SecurityUtil;

public class UserHandler extends HttpServlet
{
	@Override
	public void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException
	{
		try
		{
			if(httpServletRequest.getRequestURI().equals("/api/v1/admin/users"))
			{
				UserUtil.addUser(SecurityUtil.getJSONObject(httpServletRequest));
				SecurityUtil.writeSuccessJSONResponse(httpServletResponse, "User added successfully");
			}
		}
		catch(Exception e)
		{
			SecurityUtil.writerErrorResponse(httpServletResponse, e.getMessage());
		}
	}
}
