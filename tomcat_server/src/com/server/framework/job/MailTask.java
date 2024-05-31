package com.server.framework.job;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.server.framework.common.Configuration;
import com.server.framework.common.Util;

public class MailTask
{
	public static void run() throws Exception
	{
		JobMeta jobMeta = JobUtil.SCHEDULER_TL.get();

		JSONObject jsonObject = new JSONObject(jobMeta.getData());
		String message = jsonObject.getString("message");
		message += "<br><br><b>Note : This email is sent on behalf of " + jsonObject.getString("from_address") + "</b>";
		if(jobMeta.isRecurring())
		{
			String appUrl = Configuration.getProperty("app.url");
			if(StringUtils.isNotEmpty(appUrl))
			{
				message += "<br><br> Click <a href=\"" + appUrl + "/scheduler/delete?scheduler_token=" + DigestUtils.sha1Hex(String.valueOf(jobMeta.getId())) + "\">here</a> to unsubscribe";
			}
		}
		Util.sendEmail(jsonObject.getString("subject"), jsonObject.getString("to_address"), jsonObject.getString("from_address"), message);
	}
}
