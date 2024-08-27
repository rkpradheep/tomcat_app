package com.server.framework.security;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;

import com.server.framework.common.DateUtil;
import com.server.framework.persistence.Criteria;
import com.server.framework.persistence.DataAccess;
import com.server.framework.persistence.Row;
import com.server.framework.persistence.SelectQuery;
import com.server.framework.user.User;
import com.server.framework.user.UserUtil;
import com.server.table.constants.SESSIONMANAGEMENT;
import com.server.table.constants.USER;

public class LoginUtil
{
	private static final Logger LOGGER = Logger.getLogger(LoginUtil.class.getName());

	public static User validateCredentials(String name, String password)
	{
		name = name.toUpperCase().trim();

		try
		{
			SelectQuery selectQuery = new SelectQuery(USER.TABLE);
			Criteria criteria = new Criteria(USER.TABLE, USER.NAME, name, Criteria.Constants.EQUAL);
			criteria = criteria.and(new Criteria(USER.TABLE, USER.PASSWORD,  DigestUtils.sha256Hex(password.trim()), Criteria.Constants.EQUAL));
			selectQuery.setCriteria(criteria);

			return UserUtil.getCurrentUser(DataAccess.get(selectQuery).getRows().get(0));
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
			return null;
		}
	}

	public static void addSession(String sessionId, Long userId, boolean isAdminLogin)
	{

		try
		{
			long expiryTime = isAdminLogin ? (1000 * 60 * 60 * 24) : (1000 * 60 * 30);

			Row row = new Row(SESSIONMANAGEMENT.TABLE);
			row.set(SESSIONMANAGEMENT.SESSIONID, sessionId);
			row.set(SESSIONMANAGEMENT.EXPIRYTIME, DateUtil.getCurrentTimeInMillis() + expiryTime);
			row.set(SESSIONMANAGEMENT.USERID, userId);

			DataAccess.add(row);

		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
		}
	}

	public static void deleteExpiredSessions() throws Exception
	{
		try
		{
			Criteria criteria = new Criteria(SESSIONMANAGEMENT.TABLE, SESSIONMANAGEMENT.EXPIRYTIME, DateUtil.getCurrentTimeInMillis(), Criteria.Constants.LESS_THAN);
			DataAccess.delete(SESSIONMANAGEMENT.TABLE, criteria);
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
		}
	}
}
