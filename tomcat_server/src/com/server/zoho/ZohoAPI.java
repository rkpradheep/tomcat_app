package com.server.zoho;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONException;
import org.json.JSONObject;

import com.server.framework.common.AppException;
import com.server.framework.common.Configuration;
import com.server.framework.http.HttpAPI;
import com.server.framework.http.HttpResponse;
import com.server.framework.security.SecurityUtil;
import com.server.oauth.OauthHandler;

public class ZohoAPI extends HttpServlet
{
	private static final Hex HEX = new Hex();
	private static final Logger LOGGER = Logger.getLogger(ZohoAPI.class.getName());
	private static final Map<String, String> DC_DOMAIN_MAPPING;

	static
	{
		Map<String, String> dcDomainMappingTmp = new HashMap<>();
		dcDomainMappingTmp.put("dev", "csez.zohocorpin.com");
		dcDomainMappingTmp.put("csez", "csez.zohocorpin.com");
		dcDomainMappingTmp.put("local", "localzoho.com");
		dcDomainMappingTmp.put("us", "zoho.com");
		dcDomainMappingTmp.put("in", "zoho.in");
		dcDomainMappingTmp.put("eu", "zoho.eu");
		dcDomainMappingTmp.put("au", "zoho.com.au");
		dcDomainMappingTmp.put("jp", "zoho.jp");
		dcDomainMappingTmp.put("ca", "zohocloud.ca");
		dcDomainMappingTmp.put("uk", "zoho.uk");

		DC_DOMAIN_MAPPING = Collections.unmodifiableMap(dcDomainMappingTmp);
	}

	@Override public void service(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		try
		{
			if(request.getRequestURI().equals("/api/v1/zoho/isc"))
			{
				SecurityUtil.writeSuccessJSONResponse(response, "success", generateISCSignature(request.getParameter("service"), request.getParameter("dc")));
			}
			else if(request.getRequestURI().equals("/api/v1/zoho/jobs"))
			{
				SecurityUtil.writeSuccessJSONResponse(response, "success", handleJob(SecurityUtil.getJSONObject(request)));
			}
			else if(request.getRequestURI().equals("/api/v1/zoho/repetitions"))
			{
				SecurityUtil.writeSuccessJSONResponse(response, "success", handleRepetition(SecurityUtil.getJSONObject(request)));
			}
			else if(request.getRequestURI().equals("/api/v1/zoho/ear"))
			{
				String service;
				String dc;
				String keyLabel;
				String cipherText;
				boolean isOEK;
				boolean isSearchable;

				JSONObject payload;
				if(request.getMethod().equals(HttpPost.METHOD_NAME) && (payload = SecurityUtil.getJSONObject(request)) != null)
				{
					service = payload.getString("service");
					dc = payload.getString("dc");
					keyLabel = payload.getString("key_label");
					cipherText = payload.getString("cipher_text");
					isOEK = payload.optBoolean("is_oek", false);
					isSearchable = payload.optBoolean("is_searchable", true);
				}
				else
				{
					service = request.getParameter("service");
					dc = request.getParameter("dc");
					keyLabel = request.getParameter("key_label");
					cipherText = request.getParameter("cipher_text");
					isOEK = Boolean.parseBoolean(request.getParameter("is_oek"));
					isSearchable = Objects.isNull(request.getParameter("is_searchable")) || Boolean.parseBoolean(request.getParameter("is_searchable"));
				}

				SecurityUtil.writeSuccessJSONResponse(response, "success", doEARDecryption(service, dc, keyLabel, cipherText, isOEK, isSearchable));
			}
		}
		catch(JSONException e)
		{
			LOGGER.log(Level.INFO, "Exception occurred", e);
			SecurityUtil.writerErrorResponse(response, "Invalid request. Please try again with valid input.");
		}
		catch(AppException e)
		{
			LOGGER.log(Level.INFO, "Exception occurred", e);
			SecurityUtil.writerErrorResponse(response, e.getMessage());
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
			SecurityUtil.writerErrorResponse(response, ExceptionUtils.getMessage(e));
		}
	}

	public static String getDomainUrl(String subDomain, String resourceUri, String dc)
	{
		return "https://" + subDomain + "." + DC_DOMAIN_MAPPING.get(dc) + resourceUri;
	}

	public static String generateISCSignature(String service, String dc) throws Exception
	{
		String encodedPrivateKey = Configuration.getProperty("security.private.key.".concat(service).concat(".").concat(dc));
		byte[] privateKeyBytes = HEX.decode(encodedPrivateKey.getBytes());
		PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
		String currentTimeStr = String.valueOf(System.currentTimeMillis());
		Signature signature = Signature.getInstance("MD5withRSA");
		signature.initSign(privateKey);
		signature.update(currentTimeStr.getBytes());

		return Configuration.getProperty("security." + service + ".iam.service.name") + "-" + currentTimeStr + "-" + new String(HEX.encode(signature.sign()));
	}

	public static String doEARDecryption(String serviceName, String dc, String keyLabel, String cipherText, boolean isOEK, boolean isSearchable) throws Exception
	{
		String clientId = Configuration.getProperty("ear." + serviceName + "." + dc + "." + "client.id");
		String clientSecret = Configuration.getProperty("ear." + serviceName + "." + dc + "." + "client.secret");
		String refreshToken = Configuration.getProperty("ear." + serviceName + "." + dc + "." + "refresh.token");

		String tokenUrl = getDomainUrl("accounts", "/oauth/v2/token", dc);
		JSONObject tokenGeneratePayload = new JSONObject()
			.put("client_id", clientId)
			.put("client_secret", clientSecret)
			.put("refresh_token", refreshToken)
			.put("redirect_uri", "ear://")
			.put("url", tokenUrl);

		return doEARDecryption(keyLabel, cipherText, dc, isSearchable, isOEK, tokenGeneratePayload);
	}

	static String doEARDecryption(String keyLabel, String cipherText, String dc, boolean isSearchable, boolean isOEK, JSONObject tokenGeneratePayload) throws Exception
	{

		String accessToken = new JSONObject(OauthHandler.generateOauthTokens(tokenGeneratePayload)).getString("access_token");

		String subDomain = dc.equals("csez") || dc.equals("local") ? "encryption" : "keystore";
		String resourceURI = isOEK ? "/kms/getDEK" : "/getKeyIv";
		String earURL = getDomainUrl(subDomain, resourceURI, dc);

		Map<String, String> parametersMap = new HashMap<>();
		parametersMap.put("operation", "0");
		if(isOEK)
		{
			parametersMap.put("org_id", keyLabel);
			parametersMap.put("kms_type", "1");
		}
		else
		{
			byte[] keyTokenHashedBytes = MessageDigest.getInstance("SHA-256").digest(keyLabel.getBytes());
			parametersMap.put("keytoken", new String(HEX.encode(keyTokenHashedBytes)));
		}

		HttpResponse httpResponse = HttpAPI.makeNetworkCall(earURL, "POST", Map.of("Authorization", "Bearer " + accessToken), parametersMap);

		JSONObject response = new JSONObject(httpResponse.getStringResponse());
		return doEARDecryption(cipherText, response.getString("key"), response.getString("iv"), isSearchable);
	}

	static String doEARDecryption(String cipherText, String key, String iv, boolean isSearchable) throws Exception
	{
		iv = isSearchable ? iv : cipherText.substring(0, 32);
		cipherText = isSearchable ? cipherText : cipherText.substring(32);

		SecretKeySpec secretKeySpec = new SecretKeySpec(HEX.decode(key.getBytes()), "AES");
		IvParameterSpec ivParameterSpec = new IvParameterSpec(HEX.decode(iv.getBytes()));

		Cipher decryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		decryptCipher.init(2, secretKeySpec, ivParameterSpec);

		byte[] decryptedDataBytes = decryptCipher.doFinal(HEX.decode(cipherText.getBytes()));
		return new String(decryptedDataBytes);
	}

	static Object handleJob(JSONObject payload) throws Exception
	{
		String service = payload.getString("service");
		Pair<Long, Long> userIdCustomerIdPair = JobAPI.getUserIdCustomerIdPair(payload);
		long userId = userIdCustomerIdPair.getKey();
		long customerId = userIdCustomerIdPair.getValue();

		String dc = payload.getString("dc");
		String serviceId = Configuration.getProperty("taskengine." + service + "." + dc + ".service.id");
		String queueName = payload.optString("thread_pool");
		String className = payload.optString("class_name");
		Integer delaySeconds = StringUtils.isEmpty((String) payload.opt("delay")) ? null : Integer.parseInt((String) payload.opt("delay"));
		long jobId = payload.getLong("job_id");
		String retryRepetition = payload.optString("retry_repetition");
		String repetition = payload.optString("repetition");

		String operation = payload.getString("operation");
		if(StringUtils.equals("add", operation))
		{
			return StringUtils.isEmpty(repetition) ? JobAPI.getInstance(dc, serviceId, queueName).addOrUpdateOTJ(jobId, className, retryRepetition, delaySeconds, userId, customerId) : JobAPI.getInstance(dc, serviceId, queueName).addOrUpdateRepetitiveJob(jobId, className, repetition, retryRepetition, delaySeconds, userId, customerId);
		}
		else if(StringUtils.equals("delete", operation))
		{
			return JobAPI.getInstance(dc, serviceId, queueName).deleteJob(jobId, customerId);
		}
		else
		{
			return JobAPI.getInstance(dc, serviceId, queueName).getJobDetails(jobId, customerId);
		}
	}

	static Object handleRepetition(JSONObject payload) throws Exception
	{
		String service = payload.getString("service");

		Pair<Long, Long> userIdCustomerIdPair = JobAPI.getUserIdCustomerIdPair(payload);
		long userId = userIdCustomerIdPair.getKey();
		long customerId = userIdCustomerIdPair.getValue();

		String dc = payload.getString("dc");
		String serviceId = Configuration.getProperty("taskengine." + service + "." + dc + ".service.id");
		String queueName = payload.getString("thread_pool");
		String repetitionName = payload.getString("repetition_name");
		if(StringUtils.isEmpty(repetitionName))
		{
			throw new AppException("Enter a valid value for repetition name");
		}

		boolean isCommon = payload.optBoolean("is_common");
		String operation = payload.getString("operation");
		if(StringUtils.equals("get", operation))
		{
			return JobAPI.getInstance(dc, serviceId, queueName).getRepetitionDetails(repetitionName, userId, customerId);
		}
		else if(StringUtils.equals("add_periodic", operation))
		{
			Integer periodicity = Integer.parseInt(StringUtils.defaultIfEmpty(payload.optString("periodicity"), "-1"));
			periodicity = periodicity == -1 ? null : periodicity;
			Boolean isExecutionStartTimePolicy = (Boolean) payload.opt("is_execution_start_time_policy");
			return JobAPI.getInstance(dc, serviceId, queueName).addOrUpdatePeriodicRepetition(repetitionName, userId, customerId, periodicity, isCommon, isExecutionStartTimePolicy);
		}
		else if(StringUtils.equals("add_calender", operation))
		{
			String hourMinSec = payload.optString("time");
			String frequency = payload.optString("frequency");
			String dayOfWeek = payload.optString("day_of_week", null);
			return JobAPI.getInstance(dc, serviceId, queueName).addOrUpdateCalenderRepetition(repetitionName, userId, customerId, isCommon, hourMinSec, frequency, dayOfWeek);
		}
		else if(StringUtils.equals("delete", operation))
		{
			return JobAPI.getInstance(dc, serviceId, queueName).deleteRepetition(repetitionName, userId, customerId);
		}

		return null;
	}
}
