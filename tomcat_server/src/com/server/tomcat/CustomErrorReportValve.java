package com.server.tomcat;

import java.io.FileInputStream;
import java.io.InputStream;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.JsonErrorReportValve;
import org.apache.tomcat.util.http.fileupload.IOUtils;

public class CustomErrorReportValve extends JsonErrorReportValve
{
	private static final Logger LOGGER = Logger.getLogger(CustomErrorReportValve.class.getName());

	@Override
	protected void report(Request request, Response response, Throwable throwable)
	{
		try
		{
			if(!response.isErrorReportRequired())
			{
				return;
			}
			if(request.getRequestURI().matches("/api/(.*)"))
			{
				LOGGER.log(Level.SEVERE, "TOMCAT ERROR REPORT VALVE :: Writing json response for request - {0} and status -{1}", new Object[] {request.getRequestURI(), response.getStatus()}); //No I18N

				String message = response.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR ? "Something went wrong. Please try again later" : "The requested resource [$1] is not available".replace("$1", request.getRequestURI());
				String jsonReport = "{\n  \"error\": \"" + message + "\"\n}";
				response.setContentType("application/json");

				response.getWriter().print(jsonReport);
				response.setErrorReported();
			}
			else
			{
				LOGGER.log(Level.SEVERE, "TOMCAT ERROR REPORT VALVE :: Writing html response for request - {0}", request.getRequestURI()); //No I18N
				InputStream inputStream = new FileInputStream(System.getenv("MY_HOME") + "/tomcat_server/webapps/WEB-INF/conf/errorPage.html");
				IOUtils.copy(inputStream, response.getOutputStream());
				response.setErrorReported();
			}

			LOGGER.log(Level.SEVERE, "TOMCAT ERROR REPORT VALVE :: Uncaught throwable", throwable); //No I18N

		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "TOMCAT ERROR REPORT VALVE :: Exception occurred: {0}", e); //No I18N
		}
	}
}
