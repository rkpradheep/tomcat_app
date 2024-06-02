package com.server.framework.job;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.server.framework.common.ConfigurationTableUtil;
import com.server.framework.common.DateUtil;
import com.server.framework.common.OTPAPI;
import com.server.framework.security.SecurityUtil;

public class JobAPI extends HttpServlet
{
	private static final Logger LOGGER = Logger.getLogger(JobAPI.class.getName());

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		try
		{

			JSONObject payload = SecurityUtil.getJSONObject(request);
			boolean isRecurring = payload.optBoolean("is_recurring", false);
			int dayInterval = payload.optInt("day_interval", -1);;

			long millSeconds = payload.optLong("seconds", -1) != -1 ? payload.getLong("seconds") * 1000L : DateUtil.convertDateToMilliseconds(payload.getString("execution_date_time"), "yyyy-MM-dd HH:mm") - DateUtil.getCurrentTimeInMillis();
			if(millSeconds < -60 * 1000)
			{
				throw new Exception("Cannot schedule job for past time");
			}

			if(StringUtils.equals("mail", payload.getString("task")))
			{
				if(StringUtils.isEmpty(payload.optString("otp_reference")))
				{
					SecurityUtil.writerErrorResponse(response, "OTP is required");
					return;
				}
				OTPAPI.verifyOTP(payload.getString("otp_reference"), payload.getInt("otp"));
			}

			if(TaskEnum.getTask(payload.getString("task")).isNeedDBEntry())
			{
				Long jobID = JobUtil.scheduleJob(payload.getString("task"), payload.optString("data"), millSeconds, dayInterval, isRecurring);
				SecurityUtil.writeSuccessJSONResponse(response, "Job has been scheduled successfully with ID " + jobID.toString());
			}
			else
			{
				JobUtil.scheduleJob(TaskEnum.getRunnable(payload.getString("task")), millSeconds);
				SecurityUtil.writeSuccessJSONResponse(response, "Job has been scheduled successfully");
			}

		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
			SecurityUtil.writerErrorResponse(response, e.getMessage());
		}

	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		if(request.getRequestURI().equals("/scheduler/delete"))
		{
			String jobId = ConfigurationTableUtil.getValue(request.getParameter("scheduler_token"));
			if(StringUtils.isNotEmpty(jobId))
			{
				JobUtil.deleteJob(Long.parseLong(jobId));
				RefreshManager.removeJobFromQueue(Long.parseLong(jobId));
				SecurityUtil.writeSuccessJSONResponse(response, "Deleted successfully");
				return;
			}
			SecurityUtil.writerErrorResponse(response, "Invalid request");
			return;
		}
		Map<String, String> jobList = Arrays.stream(TaskEnum.values()).filter(taskEnum ->  !taskEnum.getTaskName().equals(TaskEnum.REMINDER.getTaskName()) || SecurityUtil.getCurrentUser().isAdmin()).collect(Collectors.toMap(TaskEnum::getTaskName, TaskEnum::getTaskDisplayName));
		SecurityUtil.writeJSONResponse(response, jobList);
	}
}
