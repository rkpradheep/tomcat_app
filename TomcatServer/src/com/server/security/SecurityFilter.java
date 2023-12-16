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

import com.server.common.Configuration;
import com.server.common.Util;

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

			CURRENT_USER_TL.set(LoginUtil.getUser(sessionId));

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
					Util.writerErrorResponse((HttpServletResponse) servletResponse, "Invalid value passed for Authorization header");
				}
				else
				{
					String loginPage = requestURI.contains("admin") ? "/adminlogin" : "/login";
					httpServletResponse.sendRedirect(loginPage + "?redirect_uri=" + URLEncoder.encode(requestURL, StandardCharsets.UTF_8));
				}
			}
		}
		finally
		{
			CURRENT_USER_TL.remove();
		}
	}

}
