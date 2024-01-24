package com.server.tomcat;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.InputStream;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.JsonErrorReportValve;
import org.apache.tomcat.util.http.fileupload.IOUtils;

public class CustomErrorReportValve extends JsonErrorReportValve
{
	private static final Logger LOGGER = Logger.getLogger(CustomErrorReportValve.class.getName());
	private static final String ERROR_HTML;

	static
	{
		try
		{
			BufferedReader bufferedReader = new BufferedReader(new FileReader(System.getenv("MY_HOME") + "/tomcat_server/webapps/WEB-INF/conf/errorPage.html"));
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
			if(!response.isErrorReportRequired())
			{
				return;
			}

			int code = response.getStatus();
			String uri = request.getRequestURI();

			if(isRestAPI(uri))
			{
				LOGGER.log(Level.SEVERE, "TOMCAT ERROR REPORT VALVE :: Writing json response for request - {0} and status -{1}", new Object[] {request.getRequestURI(), response.getStatus()}); //No I18N

				String message = getErrorMessage(uri, code);
				String jsonReport = "{\n  \"error\": \"" + message + "\"\n}";
				response.setContentType("application/json");

				response.getWriter().print(jsonReport);
				response.setErrorReported();
			}
			else
			{
				LOGGER.log(Level.SEVERE, "TOMCAT ERROR REPORT VALVE :: Writing html response for request - {0}", request.getRequestURI()); //No I18N

				IOUtils.copy(getErrorHtmlInputStream(uri, code), response.getOutputStream());
				response.setErrorReported();
			}

			LOGGER.log(Level.SEVERE, "TOMCAT ERROR REPORT VALVE :: Uncaught throwable", throwable); //No I18N

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
			case 404:
				message = isRestAPI(uri) ? "The requested resource [$1] is not available".replace("$1", uri) : "Page not found";
				break;
			case 429:
				message = !isRestAPI(uri) ? "Throttle limit exceeded" : "Throttle limit exceeded. Please try again after 5 minutes.";
				break;
			default:
				message = "Something went wrong. Please try again later.";
		}
		return message;
	}

	private static boolean isRestAPI(String uri)
	{
		return uri.matches("/api/(.*)");
	}
}
