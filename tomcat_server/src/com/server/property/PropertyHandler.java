package com.server.property;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.server.common.Configuration;
import com.server.common.Util;
import com.server.db.DBUtil;

public class PropertyHandler extends HttpServlet
{
	@Override
	protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException
	{
		String propertyName = httpServletRequest.getParameter("property_name");
		if(StringUtils.isBlank(propertyName))
		{
			Util.writeJSONResponse(httpServletResponse, Configuration.getPropertyList());
		}
		else
		{
			Map<String, String> responseMap = new HashMap<>();
			responseMap.put("property_value", Configuration.getProperty(propertyName));
			Util.writeJSONResponse(httpServletResponse, responseMap);
		}
	}

	@Override
	protected void doPut(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException
	{
		JSONObject payload = Util.getJSONObject(httpServletRequest);

		Configuration.setProperty(payload.getString("property_name"), payload.getString("property_value"));

		if(StringUtils.equals("db.server.ip", payload.getString("property_name").trim()))
		{
			DBUtil.initialiseDataSource();
		}

		Util.writeSuccessJSONResponse(httpServletResponse, "updated");
	}
}
