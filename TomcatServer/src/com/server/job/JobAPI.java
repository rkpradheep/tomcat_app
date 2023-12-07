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

public class JobAPI extends HttpServlet
{
	private static final Logger LOGGER = Logger.getLogger(JobAPI.class.getName());

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		try
		{
			String taskName = request.getParameter("task");
			if(taskName != null)
			{
				String data = request.getParameter("data");
				int time = Integer.parseInt(request.getParameter("time"));

				RefreshManager.addJob(TaskNameEnum.getClazz(taskName), data, time);

			}
			else
			{
				JSONObject payload = Util.getJSONObject(request);
				RefreshManager.addJob(TaskNameEnum.getClazz(payload.getString("task")), payload.optString("data"), payload.getInt("time"));
			}

			response.getWriter().println("success");
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
			Util.writerErrorResponse(response, e.getMessage());
		}

	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		Map<String, String> jobList = Arrays.stream(TaskNameEnum.values()).collect(Collectors.toMap(TaskNameEnum::getTaskName, TaskNameEnum::getTaskDisplayName));
		Util.writeJSONResponse(response, jobList);
	}
}
