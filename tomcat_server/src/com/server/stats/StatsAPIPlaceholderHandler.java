package com.server.stats;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.server.stats.meta.StatsMeta;

public class StatsAPIPlaceholderHandler
{
	public static BiFunction<String, Object, String> getPayoutPlaceholderHandler()
	{
		return (responseColumnName, responseColumnValue) -> {

//			JSONArray userDetails = (JSONArray) responseColumnValue;
//			int userDetailsLength = userDetails.length();
//
//			String emailID = null;
//			String zuid = null;
//			String name = null;
//			for(int i = 0; i < userDetailsLength; i++)
//			{
//				JSONObject userObject = userDetails.getJSONObject(i);
//
//				JSONArray integ = userObject.optJSONArray("integration_details");
//				if(integ == null)
//					continue;
//
//				for(int j = 0; j < integ.length(); j++)
//				{
//					if(integ.getJSONObject(j).getString("bank_name").equals("sbi_bank") && StringUtils.isNotEmpty(integ.getJSONObject(j).optString("identifier")))
//					{
//						return "true";
//					}
//				}
//
//			}
//
//			return "false";

			JSONObject requestData = new JSONObject(responseColumnValue.toString());
			return requestData.getJSONObject("beneMaintV2").getJSONArray("BeneList").getJSONObject(0).getString("BeneAccountNo");






			//			if(responseColumnName.equals("ISBIIntegrated"))
			//			{
			//				return emailID;
			//			}
			//			if(responseColumnName.equals("zuid"))
			//			{
			//				return zuid;
			//			}
			//			if(responseColumnName.equals("name"))
			//			{
			//				return name;
			//			}
		};
	}

	public static BiFunction<String, Object, String> getBooksPlaceholderHandler()
	{
		return (responseColumnName, responseColumnValue) -> {
			JSONArray custom = (JSONArray) responseColumnValue;
			return custom.toString();
		};
	}
}
