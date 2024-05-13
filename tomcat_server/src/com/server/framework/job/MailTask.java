package com.server.framework.job;

import org.json.JSONObject;

import com.server.framework.common.Util;

public class MailTask implements Task
{
	@Override public void run(String data) throws Exception
	{
		JSONObject jsonObject = new JSONObject(data);
		String message = jsonObject.getString("message");
		message += "<br><br><b>Note : This email is sent on behalf of " + jsonObject.getString("from_address") + "</b>";
		Util.sendEmail(jsonObject.getString("subject"), jsonObject.getString("to_address"), jsonObject.getString("from_address"), message);
	}
}
