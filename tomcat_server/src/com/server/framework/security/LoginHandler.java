package com.server.framework.security;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.server.framework.common.Configuration;
import com.server.framework.user.User;

public class LoginHandler extends HttpServlet
{
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		JSONObject jsonObject = SecurityUtil.getJSONObject(request);

		String tokenName = "iam_token";

		User user = LoginUtil.validateCredentials(jsonObject.getString("name"), jsonObject.getString("password"));
		if(Objects.nonNull(user))
		{
			String sessionID = user.getId() + String.valueOf(System.currentTimeMillis());
			LoginUtil.addSession(sessionID, user.getId(), user.isAdmin());

			String maxAge = user.isAdmin() ? "Max-Age=86400;" : "Max-Age=1800;";
			StringBuilder tokenHeader = new StringBuilder()
				.append(tokenName + "=")
				.append(sessionID)
			    .append("; Path=/;")
			    .append(maxAge);

			StringBuilder productionHeader = new StringBuilder()
				.append("production" + "=")
				.append(Configuration.getBoolean("production"))
				.append("; Path=/;")
				.append(maxAge);

			String origin = request.getHeader("Origin");
			String referer = request.getHeader("Referer");
			if(StringUtils.isNotEmpty(origin) && StringUtils.isNotEmpty(referer) & !new URL(origin).getHost().equals(new URL(referer).getHost()))
			{
				tokenHeader.append("; Secure").append("; SameSite=None");
				productionHeader.append("; Secure").append("; SameSite=None");
			}

			response.setHeader("Set-Cookie", tokenHeader.toString());

			response.addHeader("Set-Cookie", productionHeader.toString());

			SecurityUtil.writeSuccessJSONResponse(response, "success");
			return;
		}
		SecurityUtil.writerErrorResponse(response, "failed");
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		String maxAge = "Max-Age=0";
		StringBuilder header = new StringBuilder()
			.append("iam_token" + "=")
			.append(SecurityUtil.getSessionId())
			//.append("; Secure")
			//.append("; SameSite=None")
			.append("; Path=/;")
			.append(maxAge);

		response.setHeader("Set-Cookie", header.toString());
		response.sendRedirect("/");
	}

}
