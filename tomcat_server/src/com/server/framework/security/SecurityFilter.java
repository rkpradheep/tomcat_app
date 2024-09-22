package com.server.framework.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import com.server.framework.common.Configuration;
import com.server.framework.user.User;
import com.server.framework.user.UserUtil;

public class SecurityFilter implements Filter
{
	static final ThreadLocal<User> CURRENT_USER_TL = new ThreadLocal<>();
	static final ThreadLocal<HttpServletRequest> CURRENT_REQUEST_TL = new ThreadLocal<>();
	static final ThreadLocal<ServletContext> SERVLET_CONTEXT_TL = new ThreadLocal<>();
	private static final Logger LOGGER = Logger.getLogger(SecurityFilter.class.getName());

	@Override public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
	{
		String oldThreadName = Thread.currentThread().getName();

		try
		{
			SERVLET_CONTEXT_TL.set(servletRequest.getServletContext());

			HttpServletRequest httpServletRequest = ((HttpServletRequest) servletRequest);
			HttpServletResponse httpServletResponse = ((HttpServletResponse) servletResponse);

			CURRENT_REQUEST_TL.set(httpServletRequest);

			httpServletResponse.setHeader("Server_Build_Label", Configuration.getProperty("build.label"));

			String requestURI = httpServletRequest.getRequestURI().replaceFirst(httpServletRequest.getContextPath(), StringUtils.EMPTY);
			String requestURL = httpServletRequest.getRequestURL().toString();

			boolean isResourceURI = SecurityUtil.isResourceUri(servletRequest.getServletContext(), requestURI);

			if(!isResourceURI)
			{
				LOGGER.log(Level.INFO, "Request received for uri {0} Public IP {1} Originating IP {2}", new Object[] {httpServletRequest.getRequestURI(), httpServletRequest.getRemoteAddr(), SecurityUtil.getOriginatingUserIP()});
			}

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


			CURRENT_USER_TL.set(UserUtil.getCurrentUser());

			if(!isResourceURI && !requestURI.equals("/api/v1/admin/db/execute"))
			{
				if(SecurityUtil.isLoggedIn())
				{
					Thread.currentThread().setName(Thread.currentThread().getName().concat("/".concat(SecurityUtil.getCurrentUser().getName()).concat("-").concat(SecurityUtil.getCurrentUser().getId().toString())));
				}
				SecurityUtil.addHTTPLog();
			}

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
			Thread.currentThread().setName(oldThreadName);
		}
	}

}
