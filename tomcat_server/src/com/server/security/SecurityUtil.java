package com.server.security;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

public class SecurityUtil
{
	public static final List<String> SKIP_AUTHENTICATION_ENDPOINTS = List.of("/_app/health", "/api/v1/(admin/)?authenticate", "/(admin/)?login", "(/(resources|css|js)/.*)", "/api/v1/jobs", "/api/v1/run");
	public static final Function<String, Boolean> IS_REST_API = requestURI -> requestURI.matches("/api/(.*)");
	public static final Function<String, Boolean> CAN_SKIP_AUTHENTICATION = requestURI -> requestURI.matches(String.join("|", SKIP_AUTHENTICATION_ENDPOINTS));

	public static String getSessionId(HttpServletRequest request)
	{
		String requestURI = request.getRequestURI();
		Cookie[] cookies = request.getCookies();
		String authorizationHeader = ObjectUtils.defaultIfNull(request.getHeader("Authorization"), StringUtils.EMPTY);
		Pattern pattern = Pattern.compile("Bearer (\\d+)");
		Matcher matcher = pattern.matcher(authorizationHeader);

		String token = StringUtils.isNotEmpty(authorizationHeader) && matcher.matches() ? matcher.group(1) : StringUtils.EMPTY;

		if(StringUtils.isNotEmpty(token))
		{
			return token;
		}

		String tokenName = requestURI.matches("^(/api/v1)?/admin(.*)") ? "iam_admin_token" : "iam_token";

		if(Objects.nonNull(cookies))
		{
			return Arrays.stream(cookies).filter(cookie -> cookie.getName().equals(tokenName)).map(Cookie::getValue).findFirst().orElse(StringUtils.EMPTY);
		}

		return StringUtils.EMPTY;
	}

	public static boolean isLoggedIn()
	{
		return Objects.nonNull(SecurityFilter.CURRENT_USER_TL.get());
	}
}
