package com.server.security;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.server.common.Util;

public class LoginHandler extends HttpServlet
{
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		JSONObject jsonObject = Util.getJSONObject(request);

		boolean isAdminLogin = request.getRequestURI().startsWith("/api/v1/admin");
		String tokenName = isAdminLogin ? "iam_admin_token" : "iam_token";

		Long userID = LoginUtil.validateCredentials(jsonObject.getString("name"), jsonObject.getString("password"), isAdminLogin);
		String sessionID = userID + String.valueOf(System.currentTimeMillis());
		if(Objects.nonNull(userID) && LoginUtil.addSession(sessionID, userID))
		{
			StringBuilder header = new StringBuilder();
			header.append(tokenName + "=").append(sessionID);
			header.append("; Secure");
			header.append("; SameSite=None");
			header.append("; Path=/");
			header.append("; Max-Age=1800");

			response.setHeader("Set-Cookie", header.toString());

			Util.writeSuccessJSONResponse(response, "success");
			return;
		}
		Util.writerErrorResponse(response, "failed");
	}

}
