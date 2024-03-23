package com.server.security;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.server.security.user.User;

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

			String maxAge = user.isAdmin() ? "Max-Age=86400" : "Max-Age=1800";
			StringBuilder header = new StringBuilder()
				.append(tokenName + "=")
				.append(sessionID)
			    //.append("; Secure")
			    //.append("; SameSite=None")
			    .append("; Path=/;")
			    .append(maxAge);

			response.setHeader("Set-Cookie", header.toString());

			header = new StringBuilder()
				.append("production" + "=")
				.append(Configuration.getBoolean("production"))
				//.append("; Secure")
				//.append("; SameSite=None")
				.append("; Path=/;")
				.append(maxAge);

			response.addHeader("Set-Cookie", header.toString());

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
			.append(SecurityUtil.getSessionId(request))
			//.append("; Secure")
			//.append("; SameSite=None")
			.append("; Path=/;")
			.append(maxAge);

		response.setHeader("Set-Cookie", header.toString());
		response.sendRedirect("/");
	}

}
