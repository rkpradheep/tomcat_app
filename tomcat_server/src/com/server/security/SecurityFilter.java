package com.server.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import com.server.security.user.User;
import com.server.security.user.UserUtil;

public class SecurityFilter implements Filter
{
	static final ThreadLocal<User> CURRENT_USER_TL = new ThreadLocal<>();
	static final ThreadLocal<HttpServletRequest> CURRENT_REQUEST_TL = new ThreadLocal<>();
	static final ThreadLocal<ServletContext> SERVLET_CONTEXT_TL = new ThreadLocal<>();
	private static final Logger LOGGER = Logger.getLogger(SecurityFilter.class.getName());

	@Override public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
	{
		try
		{
			SERVLET_CONTEXT_TL.set(servletRequest.getServletContext());

			HttpServletRequest httpServletRequest = ((HttpServletRequest) servletRequest);
			HttpServletResponse httpServletResponse = ((HttpServletResponse) servletResponse);

			CURRENT_REQUEST_TL.set(httpServletRequest);

			httpServletResponse.setHeader("Server_Build_Label", Configuration.getProperty("build.label"));

			String requestURI = httpServletRequest.getRequestURI().replaceFirst(httpServletRequest.getContextPath(), StringUtils.EMPTY);
			String requestURL = httpServletRequest.getRequestURL().toString();

			if(!SecurityUtil.isValidEndPoint(requestURI))
			{
				httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			if(!SecurityUtil.isResourceUri(servletRequest.getServletContext(), requestURI) && !ThrottleHandler.handleThrottling(httpServletRequest))
			{
				httpServletResponse.sendError(429);
				return;
			}

			String authToken = SecurityUtil.getAuthToken();
			String sessionId = StringUtils.isBlank(authToken) ? SecurityUtil.getSessionId() : null;

			CURRENT_USER_TL.set(UserUtil.getUser(sessionId, authToken));

			if(Configuration.getBoolean("production") && (requestURI.equals("/dbtool.jsp") || requestURI.equals("/zoho")))
			{
				httpServletResponse.sendRedirect("/");
				return;
			}

			if(Configuration.getBoolean("skip.authentication") || SecurityUtil.canSkipAuthentication(requestURI))
			{
				if(requestURI.matches("/login") && SecurityUtil.isLoggedIn())
				{
					httpServletResponse.sendRedirect("/");
					return;
				}

				filterChain.doFilter(servletRequest, servletResponse);
				return;
			}

			if(SecurityUtil.isLoggedIn())
			{
				if(!SecurityUtil.getCurrentUser().isAdmin() && SecurityUtil.isAdminCall(requestURI))
				{
					httpServletResponse.sendError(HttpStatus.SC_FORBIDDEN);
					return;
				}
				LOGGER.log(Level.INFO, "Request received for uri {0} Public IP {1}  Session {2} Originating IP {3}", new Object[] {requestURI, httpServletRequest.getRemoteAddr(), sessionId, SecurityUtil.getOriginatingUserIP()});
				filterChain.doFilter(servletRequest, servletResponse);
			}
			else
			{
				if(SecurityUtil.IS_REST_API.apply(requestURI))
				{
					String errorMessage = StringUtils.isNotEmpty(httpServletRequest.getHeader("Authorization")) ? "Invalid value passed for Authorization header" : "Session expired. Please login again and try.";
					Map<String, String> additionalData = new HashMap<>();
					additionalData.put("redirect_uri", SecurityUtil.getRedirectURI(httpServletRequest));
					SecurityUtil.writerErrorResponse(httpServletResponse, HttpStatus.SC_UNAUTHORIZED, "authentication_needed", errorMessage, additionalData);
				}
				else
				{
					String loginPage = httpServletRequest.getContextPath() + "/login";
					loginPage += "?redirect_uri=" + URLEncoder.encode(requestURL, StandardCharsets.UTF_8);

					httpServletResponse.sendRedirect(loginPage);
				}
			}

			SecurityUtil.sendVisitorNotification();
		}
		finally
		{
			CURRENT_USER_TL.remove();
			SERVLET_CONTEXT_TL.remove();
			CURRENT_REQUEST_TL.remove();
		}
	}

}
