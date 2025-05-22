package com.server.tomcat;

import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.InputStream;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.JsonErrorReportValve;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.tomcat.util.http.fileupload.IOUtils;

public class CustomErrorReportValve extends JsonErrorReportValve
{
	private static final Logger LOGGER = Logger.getLogger(CustomErrorReportValve.class.getName());
	private static final String ERROR_HTML;

	static
	{
		try
		{
			BufferedReader bufferedReader = new BufferedReader(new FileReader(System.getenv("MY_HOME") + "/tomcat_build/conf/errorPage.html"));
			ERROR_HTML = bufferedReader.lines().collect(Collectors.joining());
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void report(Request request, Response response, Throwable throwable)
	{
		try
		{
			int code = response.getStatus();
			String uri = request.getRequestURI();

			if(!response.isErrorReportRequired() || StringUtils.isEmpty(uri))
			{
				return;
			}

			if(code == HttpStatus.SC_NOT_FOUND && uri.endsWith("/"))
			{
				String redirectUri = request.getRequestURI().replaceAll("(/*)$", StringUtils.EMPTY);
				response.sendRedirect(StringUtils.defaultIfEmpty(redirectUri, "/"));
				return;
			}

			if(isRestAPI(uri))
			{
				LOGGER.log(Level.SEVERE, "TOMCAT ERROR REPORT VALVE :: Writing json response for request - {0} and status - {1}", new Object[] {request.getRequestURI(), response.getStatus()}); //No I18N

				String message = getErrorMessage(uri, code);

				Thread.currentThread().getContextClassLoader().loadClass("com.server.framework.security.SecurityUtil").getDeclaredMethod("writerErrorResponse", HttpServletResponse.class, String.class).invoke(null, response, message);
				response.setErrorReported();
			}
			else
			{
				LOGGER.log(Level.SEVERE, "TOMCAT ERROR REPORT VALVE :: Writing html response for request - {0}", request.getRequestURI()); //No I18N

				response.setContentType("text/html");
				IOUtils.copy(getErrorHtmlInputStream(uri, code), response.getOutputStream());
				response.setErrorReported();
			}

			if(Objects.nonNull(throwable))
			{
				LOGGER.log(Level.SEVERE, "TOMCAT ERROR REPORT VALVE", throwable);
			}

		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "TOMCAT ERROR REPORT VALVE :: Exception occurred: {0}", e); //No I18N
		}
	}

	private static InputStream getErrorHtmlInputStream(String uri, int code)
	{
		String errorHtml = ERROR_HTML;

		errorHtml = errorHtml.replace("${code}", code + "");
		errorHtml = errorHtml.replace("${message}", getErrorMessage(uri, code));
		errorHtml = errorHtml.replace("${canShowHomePage}", code == 404 ? "inline-block" : "none");

		return new ByteArrayInputStream(errorHtml.getBytes());
	}

	private static String getErrorMessage(String uri, int code)
	{
		String message;
		switch(code)
		{
			case HttpStatus.SC_NOT_FOUND:
				message = isRestAPI(uri) ? "The requested resource [$1] is not available".replace("$1", uri) : "Page not found";
				break;
			case HttpStatus.SC_TOO_MANY_REQUESTS:
				message = !isRestAPI(uri) ? "Throttle limit exceeded" : "Throttle limit exceeded. Please try again after 5 minutes.";
				break;
			case HttpStatus.SC_FORBIDDEN:
				message = "You are not authorized to access this page";
				break;
			default:
				message = "Something went wrong. Please try again later";
		}
		return message;
	}

	private static boolean isRestAPI(String uri)
	{
		return uri.matches("/api/(.*)");
	}
}
