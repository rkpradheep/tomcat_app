package com.server.job;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.server.common.Util;
import com.server.security.SecurityUtil;

public class JobAPI extends HttpServlet
{
	private static final Logger LOGGER = Logger.getLogger(JobAPI.class.getName());

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		try
		{

			JSONObject payload = SecurityUtil.getJSONObject(request);
			long millSeconds = payload.optLong("seconds", -1) != -1 ? payload.getLong("seconds") * 1000L : Util.convertDateToMilliseconds(payload.getString("date_time"), "yyyy-MM-dd HH:mm") - System.currentTimeMillis();
			if(millSeconds < -60 * 1000)
			{
				throw new Exception("Cannot schedule job for past time");
			}

			TaskEnum.getHandler(payload.getString("task"));

			Long jobID = JobUtil.scheduleJob(payload.getString("task"), payload.optString("data"), millSeconds);

			SecurityUtil.writeSuccessJSONResponse(response, "Job has been scheduled successfully with ID " + jobID.toString());
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
		Map<String, String> jobList = Arrays.stream(TaskEnum.values()).filter(taskEnum -> request.getRequestURL().toString().contains("pradheep") || !taskEnum.getTaskName().equals(TaskEnum.REMINDER.getTaskName())).collect(Collectors.toMap(TaskEnum::getTaskName, TaskEnum::getTaskDisplayName));
		SecurityUtil.writeJSONResponse(response, jobList);
	}
}
