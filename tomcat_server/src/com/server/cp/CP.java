package com.server.cp;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServlet;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import com.server.zoho.JobAPI;

public class CP extends HttpServlet
{
	private static final Logger LOGGER = Logger.getLogger(CP.class.getName());

	@Override public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException
	{
		try
		{
			String dc = "in";
			String serviceId = "90778201";
			String queueName = "com.zoho.payout.default";
			long jobId = 2207000000006029L;
			long userId = 400L;
			long customerId = 3082L;
			String className = "com.zoho.pay.payouts.framework.jobs.PaymentSearchOTJ";

//			int delaySeconds = 5;
//
			JobAPI jobAPI = JobAPI.getInstance(dc, serviceId, queueName);

			LOGGER.info(jobAPI.getRepetitionDetails("InProgressTransaction", userId, customerId).toString());
//
//			jobAPI.addOrUpdateOTJ(jobId, className, 50, userId, customerId);
//
//			JSONObject jsonObject = jobAPI.getOTJDetails(jobId, customerId);
//			LOGGER.info(jsonObject.toString());
//
//			jobAPI.addOrUpdateOTJ(jobId, className, 5, userId, customerId);
//
//			jsonObject = jobAPI.getOTJDetails(jobId, customerId);
//			LOGGER.info(jsonObject.toString());

//			jobAPI.deleteOTJ(jobId, customerId);
//
//			jsonObject = jobAPI.getOTJDetails(jobId, customerId);
//			LOGGER.info(jsonObject.toString());
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
		}
	}
}
