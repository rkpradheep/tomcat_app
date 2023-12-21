package com.server.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import com.server.common.Configuration;
import com.server.common.Util;
import com.server.user.User;
import com.server.user.UserUtil;

public class SecurityFilter implements Filter
{
	public static final ThreadLocal<User> CURRENT_USER_TL = new ThreadLocal<>();
	private static final Logger LOGGER = Logger.getLogger(SecurityFilter.class.getName());

	@Override public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
	{
		try
		{
			if(!Boolean.parseBoolean(Configuration.getProperty("login.needed")))
			{
				filterChain.doFilter(servletRequest, servletResponse);
				return;
			}

			HttpServletRequest httpServletRequest = ((HttpServletRequest) servletRequest);
			HttpServletResponse httpServletResponse = ((HttpServletResponse) servletResponse);

			String requestURI = httpServletRequest.getRequestURI();
			String requestURL = httpServletRequest.getRequestURL().toString();

			String sessionId = SecurityUtil.getSessionId(httpServletRequest);

			CURRENT_USER_TL.set(UserUtil.getUser(sessionId));

			if(SecurityUtil.CAN_SKIP_AUTHENTICATION.apply(requestURI))
			{
				if(requestURI.matches("/(admin)?login") && SecurityUtil.isLoggedIn())
				{
					httpServletResponse.sendRedirect("/");
					return;
				}

				filterChain.doFilter(servletRequest, servletResponse);
				return;
			}

			if(SecurityUtil.isLoggedIn())
			{
				LOGGER.log(Level.INFO, "Request received for uri {0} for session {1}", new Object[] {requestURI, CURRENT_USER_TL.get().getSessionId()});
				filterChain.doFilter(servletRequest, servletResponse);
			}
			else
			{
				if(SecurityUtil.IS_REST_API.apply(requestURI))
				{
					String errorMessage = StringUtils.isNotEmpty(httpServletRequest.getHeader("Authorization")) ? "Invalid value passed for Authorization header" : "Session expired. Please login again and try.";
					Util.writerErrorResponse(httpServletResponse, HttpStatus.SC_UNAUTHORIZED, "authentication_needed", errorMessage);
				}
				else
				{
					String loginPage = httpServletRequest.getRequestURI().contains("admin") ? "/adminlogin" : "/login";
					loginPage += "?redirect_uri=" + URLEncoder.encode(httpServletRequest.getRequestURL().toString(), StandardCharsets.UTF_8);

					httpServletResponse.sendRedirect(loginPage);
				}
			}
		}
		finally
		{
			CURRENT_USER_TL.remove();
		}
	}

}
