package com.server.oauth;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.server.framework.common.Util;
import com.server.framework.security.SecurityUtil;

public class OauthHandler extends HttpServlet
{
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		try
		{
			JSONObject credentials = SecurityUtil.getJSONObject(request);
			String responseJSON;

			if(request.getRequestURI().contains("/api/v1/oauth/code"))
			{
				responseJSON = getURIForOauthCodeGeneration(credentials);
			}
			else
			{
				responseJSON = credentials.has("client_id") ? generateOauthTokens(credentials) : generateOauthTokensForDefaultCredentials(credentials);
			}

			response.setContentType("application/json");
			response.getWriter().println(responseJSON);
		}
		catch(Exception e)
		{
			response.getOutputStream().print("Failed :\n\n");
			response.getOutputStream().print(e.getMessage());
		}
	}

	public String generateOauthTokens(JSONObject credentials) throws Exception
	{
		StringBuilder stringBuilder = new StringBuilder(credentials.getString("url") + "?");
		stringBuilder.append("client_id=" + credentials.getString("client_id"));
		stringBuilder.append("&client_secret=" + credentials.getString("client_secret"));

		if(credentials.has("refresh_token"))
		{
			stringBuilder.append("&refresh_token=" + credentials.getString("refresh_token"));
			stringBuilder.append("&grant_type=" + "refresh_token");
		}
		else
		{
			stringBuilder.append("&grant_type=authorization_code");
			stringBuilder.append("&code=" + credentials.getString("code"));
			stringBuilder.append("&redirect_uri=" + credentials.getString("redirect_uri"));
		}

		URL url = new URL(stringBuilder.toString());

		HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
		httpURLConnection.setRequestMethod("POST");
		httpURLConnection.setDoOutput(true);
		httpURLConnection.getOutputStream().write(StringUtils.EMPTY.getBytes());

		return (Util.getResponse(httpURLConnection.getResponseCode() != 200 ? httpURLConnection.getErrorStream() : httpURLConnection.getInputStream()));
	}

	public String generateOauthTokensForDefaultCredentials(JSONObject credentials) throws Exception
	{
		Map<String, Object> credentials1 = credentials.toMap();
		Map<String, Object> credentials2 = Util.getZohoSecrets(getDC(credentials.getString("url"))).toMap();
		credentials2.putAll(credentials1);

		return generateOauthTokens(new JSONObject(credentials2));
	}

	public String getURIForOauthCodeGeneration(JSONObject credentials)
	{
		StringBuilder queryString = new StringBuilder(credentials.getString("url"));

		JSONObject secretJson = Util.getZohoSecrets(getDC(credentials.getString("url")));

		queryString.append("?scope=" + credentials.getString("scope"))
			.append("&client_id=" + secretJson.getString("client_id"))
			.append("&prompt=consent")
			.append("&redirect_uri=" + secretJson.getString("redirect_uri"))
			.append("&response_type=code")
			.append("&access_type=offline");

		return new JSONObject().put("redirect_uri", queryString.toString()).toString();
	}

	public String getDC(String url)
	{
		if(url.contains("csez"))
			return "dev";
		if(url.contains("localzoho"))
			return "local";
		if(url.contains(".in"))
			return "in";
		return "us";
	}
}
