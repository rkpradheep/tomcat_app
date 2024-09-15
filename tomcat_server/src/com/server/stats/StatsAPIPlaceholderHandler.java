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

			JSONArray userDetails = (JSONArray) responseColumnValue;
			int userDetailsLength = userDetails.length();

			String emailID = null;
			String zuid = null;
			String name = null;
			for(int i = 0; i < userDetailsLength; i++)
			{
				JSONObject userObject = userDetails.optJSONObject(i);
				if(StringUtils.equals(userObject.optString("role"), "Account Owner"))
				{
					emailID = userObject.optString("email");
					zuid = userObject.optString("zuid");
					name = userObject.optString("name");
					break;
				}
			}
			if(responseColumnName.equals("email"))
			{
				return emailID;
			}
			if(responseColumnName.equals("zuid"))
			{
				return zuid;
			}
			if(responseColumnName.equals("name"))
			{
				return name;
			}
			return null;
		};
	}

	public static BiFunction<String, Object, String> getBooksPlaceholderHandler()
	{
		return (responseColumnName, responseColumnValue) -> {
			JSONArray custom = (JSONArray) responseColumnValue;
			return custom.getJSONObject(0).get("index").toString();
		};
	}
}
