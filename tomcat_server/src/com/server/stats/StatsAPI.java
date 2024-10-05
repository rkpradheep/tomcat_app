package com.server.stats;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.jose4j.lang.HashUtil;
import org.json.JSONObject;

import com.server.concurrency.ConcurrencyAPIHandler;
import com.server.framework.common.AppException;
import com.server.framework.common.DateUtil;
import com.server.framework.common.Util;
import com.server.framework.http.FormData;
import com.server.framework.http.HttpAPI;
import com.server.framework.http.HttpResponse;
import com.server.framework.job.JobUtil;
import com.server.framework.security.SecurityUtil;
import com.server.stats.meta.StatsMeta;

public class StatsAPI extends HttpServlet
{

	private static final List<String> RUNNING_STATS = new ArrayList<>();

	private static final Logger LOGGER = Logger.getLogger(StatsAPI.class.getName());

	@Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		try
		{
			Map<String, FormData> formDataMap = SecurityUtil.parseMultiPartFormData(request);
			FormData configurationFile = formDataMap.get("configuration_file");
			FormData configuration = formDataMap.get("configuration");
			FormData requestData = formDataMap.get("request_data");
			if(Objects.isNull(requestData) || (Objects.isNull(configuration) && Objects.isNull(configurationFile)))
			{
				SecurityUtil.writerErrorResponse(response, "configuration or input file is missing");
				return;
			}
			if(!StringUtils.equals(requestData.getFileData().getContentType(), "text/csv"))
			{
				SecurityUtil.writerErrorResponse(response, "Request data file is not in csv format");
				return;
			}

			byte[] configurationFileBytes = Objects.nonNull(configurationFile) ? configurationFile.getFileData().getBytes() : configuration.getValue().getBytes();
			byte[] requestDataBytes = requestData.getFileData().getBytes();

			String reqId = DigestUtils.sha256Hex(ByteBuffer.allocate(configurationFileBytes.length + requestDataBytes.length).put(configurationFileBytes).put(requestDataBytes).array());

			if(RUNNING_STATS.contains(reqId))
			{
				throw new AppException("Stats already running for this configuration with ReqId : " + reqId);
			}

			StatsMeta statsMeta = StatsUtil.getStatsMeta(Objects.isNull(configuration) ? configurationFile.getFileData().getInputStream() : new ByteArrayInputStream(configuration.getValue().getBytes()), requestData.getFileData().getReader(), SecurityUtil.getUploadsPath() + "/" + reqId + ".csv");
			statsMeta.setRequestId(reqId);
			statsMeta.setRawResponseWriter(new FileWriter(SecurityUtil.getUploadsPath() + "/" + "RawResponse_" + reqId + ".txt"));
			RUNNING_STATS.add(reqId);

			JobUtil.scheduleJob(() -> startStats(statsMeta), 1);

			File statsFile = new File(SecurityUtil.getUploadsPath() + "/" + reqId + ".csv");
			long starTime = DateUtil.getCurrentTimeInMillis();
			while(!statsFile.exists() && (DateUtil.getCurrentTimeInMillis() - starTime) < DateUtil.ONE_SECOND_IN_MILLISECOND * 5)
				;

			SecurityUtil.writeSuccessJSONResponse(response, "Stats request initiated successfully.", Map.of("request_id", reqId));
		}
		catch(Exception e)
		{
			SecurityUtil.writerErrorResponse(response, ExceptionUtils.getMessage(e));
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
		}
	}

	public static void startStats(StatsMeta statsMeta) throws IOException
	{
		File outputFile = new File(statsMeta.getResponseFilePath());
		outputFile.createNewFile();
		int requestCount = 0;

		try(PrintWriter output = new PrintWriter(new BufferedWriter(new FileWriter(outputFile))))
		{
			statsMeta.setResponseWriter(output);

			List<Map<String, String>> requestList = StatsUtil.getRequestList(statsMeta);

			statsMeta.getResponseWriter().println(statsMeta.getResponseHeaders());
			output.flush();

			List<Runnable> runnableList = new ArrayList<>();

			for(Map<String, String> requestData : requestList)
			{
				try
				{
					statsMeta.incrementCount();
					requestCount++;

					int currentRequestNo = statsMeta.getRequestCount();
					runnableList.add(() -> makeCall(statsMeta, requestData, currentRequestNo));

					if(requestCount >= statsMeta.getRequestBatchSize() || statsMeta.getRequestCount() >= requestList.size())
					{
						if(statsMeta.isDisableParallelCalls())
						{
							runnableList.forEach(Runnable::run);
						}
						else
						{
							ConcurrencyAPIHandler.executeAsynchronously(runnableList);
						}

						runnableList.clear();
						if(statsMeta.getRequestCount() >= requestList.size())
						{
							break;
						}
						long waitTime = DateUtil.getCurrentTimeInMillis() + (1000L * statsMeta.getRequestIntervalSeconds());
						while(waitTime > DateUtil.getCurrentTimeInMillis());
						requestCount = 0;
					}
				}
				catch(Exception e)
				{
					LOGGER.log(Level.INFO, "Exception for Request Count : " + statsMeta.getRequestCount(), e);
				}
			}

		}
		catch(Exception e)
		{
			System.out.println("Exception :");
			System.out.println(ExceptionUtils.getStackTrace(e));
		}
		finally
		{

			RUNNING_STATS.remove(statsMeta.getRequestId());
			File desFile = new File(outputFile.getAbsolutePath().replace("_inprocess", StringUtils.EMPTY));
			outputFile.renameTo(desFile);
			IOUtils.copy(new FileInputStream(desFile), new FileOutputStream(Util.HOME_PATH + "/uploads/" + outputFile.getName()));
			IOUtils.copy(new FileInputStream(SecurityUtil.getUploadsPath() + "/" + "RawResponse_" + statsMeta.getRequestId() + ".txt"), new FileOutputStream(Util.HOME_PATH + "/uploads/RawResponse_" + statsMeta.getRequestId() + ".txt"));
			LOGGER.log(Level.INFO, "Stats completed for ReqId : {0}", statsMeta.getRequestId());

			statsMeta.getRawResponseWriter().close();
			statsMeta.getResponseWriter().close();
		}
	}

	static void makeCall(StatsMeta statsMeta, Map<String, String> requestData, int requestCount)
	{
		try
		{
			ImmutableTriple<String, Map<String, String>, JSONObject> placeHolderTriple = StatsUtil.handlerPlaceholder(statsMeta, requestData, requestCount);

			String response = connect(statsMeta, placeHolderTriple.getLeft(), placeHolderTriple.getMiddle(), placeHolderTriple.getRight());

			handleResponse(statsMeta, requestData, placeHolderTriple, response, requestCount);
		}
		catch(Exception e)
		{
			LOGGER.log(Level.INFO, "Exception for Request Count : " + requestCount, e);
		}
	}

	static void handleResponse(StatsMeta statsMeta, Map<String, String> requestData, Triple<String, Map<String, String>, JSONObject> placeHolderTriple, String response, int requestCount)
		throws Exception
	{
		StringBuilder rowBuilder = new StringBuilder();
		for(String responseColumnName : statsMeta.getResponseColumnNames())
		{
			String columnVale = StatsUtil.getColumnValue(statsMeta, requestData, responseColumnName, placeHolderTriple, response, requestCount);
			rowBuilder.append(StringUtils.contains(columnVale, ",") ? "\"" + columnVale + "\"" : columnVale).append(",");
		}
		synchronized(statsMeta.getResponseWriter())
		{
			statsMeta.getResponseWriter().println(rowBuilder.toString().replaceAll(",$", ""));
			statsMeta.getResponseWriter().flush();

			statsMeta.getRawResponseWriter().write("Request No : " + requestCount + "\n");
			statsMeta.getRawResponseWriter().write(response);
			statsMeta.getRawResponseWriter().write("\n\n\n\n");
			statsMeta.getRawResponseWriter().flush();
		}
	}

	public static String connect(StatsMeta statsMeta, String connectionUrl, Map<String, String> params, JSONObject jsonBody) throws Exception
	{
		HttpResponse httpResponse = HttpAPI.makeNetworkCall(connectionUrl, statsMeta.getMethod(), statsMeta.getRequestHeaders(), params, jsonBody);
		return httpResponse.getStringResponse();
	}
}
