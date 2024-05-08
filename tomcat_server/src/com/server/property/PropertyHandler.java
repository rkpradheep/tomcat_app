package com.server.property;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.server.framework.common.Configuration;
import com.server.framework.security.SecurityUtil;

public class PropertyHandler extends HttpServlet
{
	@Override
	protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException
	{
		String propertyName = httpServletRequest.getParameter("property_name");
		if(StringUtils.isBlank(propertyName))
		{
			SecurityUtil.writeJSONResponse(httpServletResponse, Configuration.getPropertyList());
		}
		else
		{
			Map<String, String> responseMap = new HashMap<>();
			String sensitiveKeyRegex = "(.*)(password|credential|secret)(.*)";

			responseMap.put("property_value", propertyName.matches(sensitiveKeyRegex) ? "*****" : Configuration.getProperty(propertyName));
			SecurityUtil.writeJSONResponse(httpServletResponse, responseMap);
		}
	}

	@Override
	protected void doPut(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException
	{
		JSONObject payload = SecurityUtil.getJSONObject(httpServletRequest);

		Configuration.setProperty(payload.getString("property_name"), payload.getString("property_value"));

		SecurityUtil.writeSuccessJSONResponse(httpServletResponse, "updated");
	}
}
