package com.server.zoho;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONObject;

import com.server.framework.common.AppException;
import com.server.framework.common.Configuration;
import com.server.framework.common.DateUtil;
import com.server.framework.http.HttpAPI;
import com.server.framework.http.HttpResponse;

public class JobAPI
{
	private static final Map<String, String> OTJ_NAME_MAPPING;
	private static final Map<String, String> PERIODIC_REPETITION_NAME_MAPPING;
	private static final Map<String, String> CALENDER_REPETITION_NAME_MAPPING;
	private final String queueId;
	private final String taskEngineUrl;
	private final Map<String, String> headersMap;

	public JobAPI(String dc, String serviceId, String queueName) throws Exception
	{
		taskEngineUrl = ZohoAPI.getDomainUrl("taskengine", "/ScheduleServlet", dc);
		queueId = getQueueId(serviceId, queueName);

		headersMap = new HashMap<>();
		headersMap.put("q", queueId);
		headersMap.put("s", "y");
	}

	static Pair<Long, Long> getUserIdCustomerIdPair(JSONObject payload) throws Exception
	{
		if(StringUtils.isEmpty(payload.optString("zsid")))
		{
			return new ImmutablePair<>(payload.optLong("user_id"), payload.optLong("customer_id"));
		}

		JSONObject serviceCredentials = new JSONObject();

		String service = payload.getString("service");
		String ip = Configuration.getProperty("db.$.ip".replace("$", service));
		String user = Configuration.getProperty("db.$.user".replace("$", service));
		String password = Configuration.getProperty("db.$.password".replace("$", service));
		String server = Configuration.getProperty("db.$.server".replace("$", service));

		serviceCredentials.put("ip", ip);
		serviceCredentials.put("server", server);
		serviceCredentials.put("user", user);
		serviceCredentials.put("password", password);
		serviceCredentials.put("zsid", "admin");
		serviceCredentials.put("query", "Select SASAccounts.id, UserDomains.customerid from SASAccounts INNER JOIN UserDomains on SASAccounts.id = UserDomains.id WHERE SASAccounts.loginname = '" + payload.getString("zsid") + "'");

		Map output = (Map) ((List) SASHandler.handleSasRequest(serviceCredentials).get("query_output")).get(0);

		return new ImmutablePair<>(Long.parseLong(output.get("ID") + ""), Long.parseLong(output.get("CUSTOMERID") + ""));

	}

	static
	{
		Map<String, String> otjNameMappingTmp = new HashMap<>();
		otjNameMappingTmp.put("JOB_ID", "12");
		otjNameMappingTmp.put("ADMIN_STATUS", "74");
		otjNameMappingTmp.put("RETRY_SCHEDULE_ID", "92");
		otjNameMappingTmp.put("SCHEDULED_TIME", "62");
		otjNameMappingTmp.put("USER_ID", "112");
		otjNameMappingTmp.put("SCHEMAID", "42");
		otjNameMappingTmp.put("RETRY_SCHEDULE_NAME", "143");
		otjNameMappingTmp.put("CLASS_NAME", "133");
		otjNameMappingTmp.put("SCHEDULE_ID", "22");
		otjNameMappingTmp.put("IS_COMMISIONED", "84");
		otjNameMappingTmp.put("TRANSACTION_TIME", "51");
		otjNameMappingTmp.put("AUDIT_FLAG", "104");
		otjNameMappingTmp.put("TASK_ID", "122");

		OTJ_NAME_MAPPING = Collections.unmodifiableMap(otjNameMappingTmp);

		Map<String, String> periodicRepetitionNameMappingTmp = new HashMap<>();
		periodicRepetitionNameMappingTmp.put("SCHEDULE_ID", "62");
		periodicRepetitionNameMappingTmp.put("SCHEDULE_MODE", "101");
		periodicRepetitionNameMappingTmp.put("IS_COMMON", "34");
		periodicRepetitionNameMappingTmp.put("USER_ID", "42");
		periodicRepetitionNameMappingTmp.put("END_DATE", "82");
		periodicRepetitionNameMappingTmp.put("START_DATE", "72");
		periodicRepetitionNameMappingTmp.put("TIME_PERIOD", "91");
		periodicRepetitionNameMappingTmp.put("SCHEDULE_NAME", "23");
		periodicRepetitionNameMappingTmp.put("SCHEMAID", "52");

		PERIODIC_REPETITION_NAME_MAPPING = Collections.unmodifiableMap(periodicRepetitionNameMappingTmp);

		Map<String, String> calenderRepetitionNameMappingTmp = new HashMap<>();
		calenderRepetitionNameMappingTmp.put("TIME_OF_DAY", "81");
		calenderRepetitionNameMappingTmp.put("FIRST_DAY_OF_WEEK", "171");
		calenderRepetitionNameMappingTmp.put("TZ", "143");
		calenderRepetitionNameMappingTmp.put("IS_COMMON", "34");
		calenderRepetitionNameMappingTmp.put("USER_ID", "42");
		calenderRepetitionNameMappingTmp.put("SKIP_FREQUENCY", "151");
		calenderRepetitionNameMappingTmp.put("SCHEDULE_NAME", "23");
		calenderRepetitionNameMappingTmp.put("SCHEMAID", "52");
		calenderRepetitionNameMappingTmp.put("SCHEDULE_ID", "62");
		calenderRepetitionNameMappingTmp.put("MONTH_OF_YEAR", "121");
		calenderRepetitionNameMappingTmp.put("USE_DATE_IN_REVERSE", "164");
		calenderRepetitionNameMappingTmp.put("REPEAT_FREQUENCY", "71");
		calenderRepetitionNameMappingTmp.put("YEAR_OF_DECADE", "131");
		calenderRepetitionNameMappingTmp.put("DATE_OF_MONTH", "111");
		calenderRepetitionNameMappingTmp.put("DAY_OF_WEEK", "91");
		calenderRepetitionNameMappingTmp.put("WEEK", "102");
		calenderRepetitionNameMappingTmp.put("RUN_ONCE", "184");

		CALENDER_REPETITION_NAME_MAPPING = Collections.unmodifiableMap(calenderRepetitionNameMappingTmp);

	}

	private static String getIdForOTJ(String name)
	{
		return OTJ_NAME_MAPPING.get(name);
	}

	private static String getNameForOTJ(String id)
	{
		return OTJ_NAME_MAPPING.entrySet().stream().filter(otjEntrySet -> StringUtils.equals(id, otjEntrySet.getValue())).findFirst().get().getKey();
	}

	private static String getIdForRepetition(String name, boolean isPeriodic)
	{
		return isPeriodic ? PERIODIC_REPETITION_NAME_MAPPING.get(name) : CALENDER_REPETITION_NAME_MAPPING.get(name);
	}

	private static String getNameForRepetition(String id, boolean isPeriodic)
	{
		Map<String, String> repetitionNameMapping = isPeriodic ? PERIODIC_REPETITION_NAME_MAPPING : CALENDER_REPETITION_NAME_MAPPING;
		return repetitionNameMapping.entrySet().stream().filter(otjEntrySet -> StringUtils.equals(id, otjEntrySet.getValue())).findFirst().get().getKey();
	}

	public static JobAPI getInstance(String dc, String serviceId, String queueName) throws Exception
	{
		return new JobAPI(dc, serviceId, queueName);
	}

	public String getQueueId(String serviceId, String queueName) throws Exception
	{
		Map<String, String> headersMap = new HashMap<>();
		headersMap.put("queue", queueName);
		headersMap.put("service-id", serviceId);
		headersMap.put("opr", "get-queueid-of-queue");
		HttpResponse httpResponse = HttpAPI.makeNetworkCall(taskEngineUrl.replace("/ScheduleServlet", "/AdminServlet"), HttpPost.METHOD_NAME, headersMap);
		handleErrorResponse(httpResponse);

		return httpResponse.getResponseHeaders().get("queue-id");
	}

	public JSONObject getOTJDetails(long jobId, long customerId) throws Exception
	{
		JSONObject payload = new JSONObject()
			.put("id", "j10")
			.put("0", jobId)
			.put("1", customerId);

		HttpResponse httpResponse = HttpAPI.makeNetworkCall(taskEngineUrl, HttpPost.METHOD_NAME, headersMap, payload);
		handleErrorResponse(httpResponse);

		JSONObject jobDetails = httpResponse.getJSONResponse();
		jobDetails.remove("md");
		JSONObject jobResponse = new JSONObject();
		jobDetails.keySet().forEach(key -> jobResponse.put(getNameForOTJ(key), jobDetails.get(key)));

		return jobResponse;
	}

	public String addOrUpdateOTJ(long jobId, String className, String retryRepetition, int delaySeconds, long userId, long customerId) throws Exception
	{
		JSONObject jobDetails = new JSONObject()
			.put(getIdForOTJ("JOB_ID"), jobId)
			.put(getIdForOTJ("CLASS_NAME"), className)
			.put(getIdForOTJ("USER_ID"), userId)
			.put("md", "1")
			.put(getIdForOTJ("SCHEDULED_TIME"), DateUtil.getCurrentTimeInMillis() + (1000L * delaySeconds))
			.put(getIdForOTJ("TRANSACTION_TIME"), -1)
			.put(getIdForOTJ("RETRY_SCHEDULE_NAME"), retryRepetition)
			.put(getIdForOTJ("SCHEMAID"), customerId);

		Iterator<String> iterator = jobDetails.keys();

		while(iterator.hasNext())
		{
			String key = iterator.next();
			if(jobDetails.get(key) instanceof String && StringUtils.isEmpty(jobDetails.getString(key)))
			{
				iterator.remove();
			}
		}
		String jobMethodId = "j1";
		try
		{
			getOTJDetails(jobId, customerId);
			jobMethodId = "j5";
		}
		catch(Exception ignored)
		{
		}

		JSONObject payload = new JSONObject()
			.put("id", jobMethodId)
			.put("0", jobDetails);

		HttpResponse httpResponse = HttpAPI.makeNetworkCall(taskEngineUrl, HttpPost.METHOD_NAME, headersMap, payload);
		handleErrorResponse(httpResponse);

		return jobMethodId.equals("j5") ? "Job updated successfully" : "Job added successfully";
	}

	public void deleteOTJ(long jobId, long customerId) throws Exception
	{
		JSONObject payload = new JSONObject()
			.put("id", "j7")
			.put("0", jobId)
			.put("1", customerId);

		HttpResponse httpResponse = HttpAPI.makeNetworkCall(taskEngineUrl, HttpPost.METHOD_NAME, headersMap, payload);
		handleErrorResponse(httpResponse);
	}

	public JSONObject getRepetitionDetails(String repetitionName, long userId, long customerId) throws Exception
	{
		JSONObject payload = new JSONObject()
			.put("id", "s8")
			.put("0", repetitionName)
			.put("1", userId)
			.put("2", customerId);

		HttpResponse httpResponse = HttpAPI.makeNetworkCall(taskEngineUrl, HttpPost.METHOD_NAME, headersMap, payload);
		handleErrorResponse(httpResponse);

		JSONObject repetitionDetails = httpResponse.getJSONResponse();

		JSONObject repetitionResponse = new JSONObject();
		boolean isPeriodic = StringUtils.equals("3", repetitionDetails.getString("md"));
		repetitionDetails.remove("md");
		repetitionDetails.keySet().forEach(key -> repetitionResponse.put(getNameForRepetition(key, isPeriodic), repetitionDetails.get(key)));

		return repetitionResponse;
	}

	private void handleErrorResponse(HttpResponse httpResponse) throws Exception
	{
		if(httpResponse.getResponseHeaders().containsKey("excp"))
		{
			throw new AppException(httpResponse.getResponseHeaders().get("excp"));
		}
	}

}
