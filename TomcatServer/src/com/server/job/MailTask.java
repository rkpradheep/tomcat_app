package com.server.job;

import org.json.JSONObject;

import com.server.common.Util;

public class MailTask implements Task
{
	@Override public void run(String data) throws Exception
	{
		JSONObject jsonObject = new JSONObject(data);
		Util.sendEmail("Mail Reminder", jsonObject.getString("to"), jsonObject.optString("message"));
	}
}
