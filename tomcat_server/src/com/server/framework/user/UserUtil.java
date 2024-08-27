package com.server.framework.user;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.server.framework.common.DateUtil;
import com.server.framework.persistence.Criteria;
import com.server.framework.persistence.DataAccess;
import com.server.framework.persistence.DataObject;
import com.server.framework.persistence.Join;
import com.server.framework.persistence.Row;
import com.server.framework.persistence.SelectQuery;
import com.server.framework.security.SecurityUtil;
import com.server.table.constants.AUTHTOKEN;
import com.server.table.constants.SESSIONMANAGEMENT;
import com.server.table.constants.USER;
import com.server.zoho.ZohoAPI;

public class UserUtil
{
	private static final Logger LOGGER = Logger.getLogger(UserUtil.class.getName());

	public static User getCurrentUser()
	{
		String authToken = SecurityUtil.getAuthToken();
		String sessionId = StringUtils.isBlank(authToken) ? SecurityUtil.getSessionId() : null;

		if(StringUtils.isBlank(sessionId) && StringUtils.isBlank(authToken))
		{
			return null;
		}

		SelectQuery selectQuery = new SelectQuery(USER.TABLE);
		Join join = new Join(USER.TABLE, USER.ID, SESSIONMANAGEMENT.TABLE, SESSIONMANAGEMENT.USERID);
		if(StringUtils.isNotEmpty(authToken))
		{
			join = new Join(USER.TABLE, USER.ID, AUTHTOKEN.TABLE, AUTHTOKEN.USERID);
			Criteria criteria = new Criteria(AUTHTOKEN.TABLE, AUTHTOKEN.TOKEN, authToken, Criteria.Constants.EQUAL);
			selectQuery.setCriteria(criteria);
		}
		else
		{
			Criteria criteria = new Criteria(SESSIONMANAGEMENT.TABLE, SESSIONMANAGEMENT.EXPIRYTIME, DateUtil.getCurrentTimeInMillis(), Criteria.Constants.GREATER_THAN);
			criteria = criteria.and(new Criteria(SESSIONMANAGEMENT.TABLE, SESSIONMANAGEMENT.SESSIONID, sessionId, Criteria.Constants.EQUAL));
			selectQuery.setCriteria(criteria);
		}

		selectQuery.addJoin(join, Join.Constants.INNER_JOIN);

		try
		{
			return getCurrentUser(DataAccess.get(selectQuery).getRows().get(0));
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
			return null;
		}
	}

	public static User getCurrentUser(Row row)
	{
		return Objects.nonNull(row) ? new User((long)row.get(USER.ID), (String) row.get(USER.NAME), (int)row.get(USER.ROLETYPE) == RoleEnum.ADMIN.getType()) : null;
	}

	public static void addUser(JSONObject userJSON) throws Exception
	{

		DataObject dataObject = new DataObject();
		Row row = new Row(USER.TABLE);

		row.set(USER.NAME, userJSON.getString("name"));
		row.set(USER.PASSWORD, DigestUtils.sha256Hex(userJSON.getString("password").trim()));
		row.set(USER.ROLETYPE, RoleEnum.getType(userJSON.getString("role")));

		dataObject.addRow(row);
		DataAccess.add(dataObject);
	}
}
