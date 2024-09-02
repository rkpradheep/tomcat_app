package com.server.page;

import java.io.IOException;
import java.util.logging.Logger;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONObject;

import com.server.framework.common.Configuration;
import com.server.framework.http.HttpAPI;
import com.server.framework.security.SecurityUtil;

public class PageHandler extends HttpServlet
{
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		HttpAPI.makeNetworkCall("https://pradheep-14225.csez.zohocorpin.com/_app/health", HttpPost.METHOD_NAME, null, new JSONObject().put("test",1));
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
