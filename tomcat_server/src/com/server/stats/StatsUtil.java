package com.server.stats;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.server.framework.common.Util;
import com.server.framework.security.SecurityUtil;
import com.server.stats.meta.RequestMeta;
import com.server.stats.meta.StatsMeta;

public class StatsUtil
{
	static List<Map<String, String>> getRequestList(StatsMeta statsMeta) throws Exception
	{
		CSVParser inputDataParser;

		if(StringUtils.isNotEmpty(statsMeta.getRequestFilePath()))
		{
			inputDataParser = new CSVParser(new FileReader(statsMeta.getRequestFilePath()));
		}
		else
		{
			inputDataParser = new CSVParser(statsMeta.getRequestDataReader());
		}
		String[] lineArray;
		while((lineArray = inputDataParser.getLine()) != null)
		{
			if(StringUtils.isEmpty(Arrays.toString(lineArray)))
			{
				continue;
			}
			Map<String, String> requestMap = new HashMap<>();
			for(int i = 0; i < lineArray.length; i++)
			{
				requestMap.put("\\$\\{Col_" + i + "\\}", lineArray[i]);
			}
			statsMeta.getRequestMeta().addRequest(requestMap);
		}
		return statsMeta.getRequestMeta().getRequestList();
	}

	static ImmutableTriple<String, Map<String,String>, JSONObject> handlerPlaceholder(StatsMeta statsMeta, Map<String, String> requestData, int requestCount)
	{
		String connectionUrl = statsMeta.getRequestMeta().getConnectionUrl();
		Map<String,String> params = new HashMap<>();
		JSONObject jsonObject = statsMeta.getRequestMeta().getJsonPayload();
		for(Map.Entry<String, String> requestDataEntrySet : requestData.entrySet())
		{
			long currentTime = System.nanoTime();
			Map<String,String> phMeta = new HashMap<>();
			phMeta.put("\\$\\{RequestNo\\}",requestCount + "");
			phMeta.put("\\$\\{CurrentTime\\}",currentTime + "");
			phMeta.put(requestDataEntrySet.getKey(),requestDataEntrySet.getValue());
			connectionUrl = replacePH(connectionUrl, phMeta);
			for(Map.Entry<String, String> paramsEntrySet : statsMeta.getRequestMeta().getParamsMap().entrySet())
			{
				params.put(paramsEntrySet.getKey(), replacePH(paramsEntrySet.getValue(), phMeta));
			}
			modifyJSONPayload(jsonObject, requestDataEntrySet.getKey(),  requestDataEntrySet.getValue(), phMeta);
		}

		return new ImmutableTriple<>(connectionUrl, params, jsonObject);
	}

	static String replacePH(String value, Map<String,String> phMeta)
	{
		for(Map.Entry<String, String> phMetaEntrySet : phMeta.entrySet())
		{
			value = value.replaceAll(phMetaEntrySet.getKey(), phMetaEntrySet.getValue());
		}
		return value;
	}

	static void modifyJSONPayload(JSONObject jsonObject, String phKey, String phValue, Map<String,String> phMeta)
	{
		if(Objects.isNull(jsonObject))
		{
			return;
		}
		for(String key : jsonObject.keySet())
		{
			if(jsonObject.get(key) instanceof JSONObject)
			{
				modifyJSONPayload(jsonObject, phKey, phValue, phMeta);
			}
			else if(jsonObject.get(key) instanceof JSONArray)
			{
				modifyJSONArray(jsonObject.getJSONArray(key), phKey, phValue, phMeta);
			}
			else
			{
				jsonObject.put(key, replacePH(jsonObject.getString(key), phMeta));
			}
		}
	}

	static void modifyJSONArray(JSONArray jsonArray, String phKey, String phValue, Map<String,String> phMeta)
	{
		for(int i = 0; i < jsonArray.length(); i++)
		{
			if(jsonArray.get(i) instanceof JSONObject)
			{
				modifyJSONPayload(jsonArray.getJSONObject(i), phKey, phValue, phMeta);
			}
			else if(jsonArray.get(i) instanceof JSONArray)
			{
				modifyJSONArray(jsonArray, phKey, phValue, phMeta);
			}
			else
			{
				jsonArray.put(i, replacePH(jsonArray.getString(i), phMeta));
			}
		}
	}

	static String getColumnValue(StatsMeta statsMeta, Map<String, String> requestData, String responseColumnName, Triple<String, Map<String,String>, JSONObject> placeHolderTriple, String response, int requestCount)
	{
		try
		{
			JSONObject responseJSON = new JSONObject(response);
			String responseColumnValue = statsMeta.getResponseColumnValue(responseColumnName);

			Object columnValue;

			Pattern pattern = Pattern.compile("\\$\\{params\\[(\\w+)\\]\\}");
			Matcher paramPHMatcher = pattern.matcher(responseColumnValue);

			Pattern jsonPattern = Pattern.compile("\\$\\{json_body\\[(\\w+)\\]\\}");
			Matcher jsonPHMatcher = jsonPattern.matcher(responseColumnValue);

			Pattern columnPattern = Pattern.compile("\\$\\{Col_\\d+\\}");
			Matcher columnPHMatcher = columnPattern.matcher(responseColumnValue);

			if(paramPHMatcher.matches() && placeHolderTriple.getMiddle() != null)
			{
				columnValue = placeHolderTriple.getMiddle().get(paramPHMatcher.group(1));
			}
			else if(columnPHMatcher.matches())
			{
				columnValue = requestData.get(responseColumnValue);
			}
			else if(jsonPHMatcher.matches())
			{
				columnValue = getJSONPHValue(placeHolderTriple.getRight(), jsonPHMatcher.group(1));
			}
			else
			{
				String[] columnValuePHArray = responseColumnValue.split("/");
				if(columnValuePHArray.length == 1)
				{
					columnValue = columnValuePHArray[0].equals("${RequestNo}") ? requestCount : responseJSON.opt(columnValuePHArray[0]);
				}
				else
				{
					columnValue = getJSONPHValue(responseJSON, responseColumnValue);
				}
			}
			return statsMeta.isPHResponseColumn(responseColumnName) ? statsMeta.getPlaceholderHandlerFunction().apply(responseColumnName, columnValue) : String.valueOf(columnValue);
		}
		catch(Exception e)
		{
			return null;
		}
	}

	static Object getJSONPHValue(JSONObject jsonObject, String jsonPH)
	{
		JSONObject temp = new JSONObject(jsonObject.toString());

		String[] columnValuePHArray = jsonPH.split("/");

		for(String columnValuePH : columnValuePHArray)
		{
			Matcher jsonArrayPatternMatcher = Pattern.compile("(\\w+)\\[(\\d+)\\]").matcher(columnValuePH);
			if(columnValuePHArray[columnValuePHArray.length - 1].equals(columnValuePH))
			{
				return temp.opt(columnValuePH);
			}
			temp = !jsonArrayPatternMatcher.matches() ? temp.getJSONObject(columnValuePH) : (JSONObject) temp.getJSONArray(jsonArrayPatternMatcher.group(1)).get(Integer.valueOf(jsonArrayPatternMatcher.group(2)));
		}
		return null;
	}

	static Node getNode(Element element, String name)
	{
		//return element.getElementsByTagName("rkp:" + name).item(0);
		return element.getElementsByTagName(name).item(0);
	}

	static String getTextContent(Node node)
	{
		//return element.getElementsByTagName("rkp:" + name).item(0);
		return Objects.isNull(node) ? null : node.getTextContent();
	}

	static Element getElement(Document document, String name)
	{
		//return (Element) document.getElementsByTagName("rkp:" + name).item(0);
		return (Element) document.getElementsByTagName(name).item(0);
	}

	static RequestMeta getRequestMeta(String connectionUrl, Element paramsElement, String jsonBody) throws Exception
	{
		NodeList paramNodeList = paramsElement.getChildNodes();

		RequestMeta requestMeta = new RequestMeta();
		requestMeta.setConnectionUrl(connectionUrl);
		for(int i = 0; i < paramNodeList.getLength(); i++)
		{
			Node paramNode = paramNodeList.item(i);
			if(paramNode.getNodeType() == Node.ELEMENT_NODE)
			{
				requestMeta.addParam(paramNode.getNodeName(), paramNode.getTextContent());
			}
		}
		requestMeta.setJsonPayload(jsonBody);
		return requestMeta;
	}

	static StatsMeta getStatsMeta(InputStream configurationFile, Reader requestFileReader, String customResponseFilePath) throws Exception
	{
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Source schemaFile = new StreamSource(new File(Util.HOME_PATH + "/tomcat_build/webapps/ROOT/WEB-INF/conf/stats-meta.xsd"));
		Schema schema = factory.newSchema(schemaFile);
		Validator validator = schema.newValidator();
		validator.validate(new StreamSource(configurationFile));

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		configurationFile.reset();
		Document doc = db.parse(configurationFile);
		doc.getDocumentElement().normalize();

		Element configuration = getElement(doc, "configuration");

		String connectionUrl = getNode(configuration, "url").getTextContent();
		String method = getNode(configuration, "method").getTextContent();
		int requestBatchSize = Integer.parseInt(getNode(configuration, "request-batch-size").getTextContent());
		int requestIntervalSeconds = Integer.parseInt(getNode(configuration, "request-batch-interval").getTextContent());
		String jsonPayload = getTextContent(getNode(configuration, "json-body"));
		String requestFilePath = getTextContent(getNode(configuration, "input-file-path"));
		Node responseFilePathNode = getNode(configuration, "output-file-path");
		String responseFilePath = Objects.isNull(responseFilePathNode) ? customResponseFilePath : responseFilePathNode.getTextContent();
		Node processResponseHandlerMethodNode = getNode(configuration, "placeholder-handler");
		String processResponseHandlerMethod = getTextContent(processResponseHandlerMethodNode);
		BiFunction<String, Object, String> placeholderHandlerFunction = StringUtils.isEmpty(processResponseHandlerMethod) ? null : (BiFunction<String, Object, String>) Class.forName("com.server.stats.StatsAPIPlaceholderHandler").getDeclaredMethod(processResponseHandlerMethod).invoke(null);

		StatsMeta statsMeta = new StatsMeta();
		statsMeta.setMethod(method);
		statsMeta.setRequestMeta(getRequestMeta(connectionUrl, (Element) getNode(configuration, "params"), jsonPayload));
		statsMeta.setRequestBatchSize(requestBatchSize);
		statsMeta.setRequestIntervalSeconds(requestIntervalSeconds);
		statsMeta.setPlaceholderHandlerFunction(placeholderHandlerFunction);
		statsMeta.setRequestFilePath(requestFilePath);
		statsMeta.setResponseFilePath(responseFilePath);
		statsMeta.setRequestDataReader(requestFileReader);

		Element headersElement = (Element) getNode(configuration, "headers");
		if(Objects.nonNull(headersElement))
		{
			NodeList headerNodeList = headersElement.getChildNodes();

			for(int i = 0; i < headerNodeList.getLength(); i++)
			{
				if(headerNodeList.item(i).getNodeType() == Node.ELEMENT_NODE)
				{
					statsMeta.addRequestHeader(headerNodeList.item(i).getNodeName(), headerNodeList.item(i).getTextContent());
				}
			}
		}

		Element responseElement = (Element) getNode(configuration, "response");
		NodeList responseChildNodeList = responseElement.getChildNodes();

		StringBuilder responseHeaders = new StringBuilder();

		for(int i = 0; i < responseChildNodeList.getLength(); i++)
		{
			if(responseChildNodeList.item(i).getNodeType() == Node.ELEMENT_NODE)
			{
				String headerName = responseChildNodeList.item(i).getAttributes().getNamedItem("name").getTextContent();
				Node phNode = responseChildNodeList.item(i).getAttributes().getNamedItem("placeholder");
				boolean isPlaceholder = phNode != null && Boolean.valueOf(phNode.getTextContent());
				responseHeaders.append(headerName).append(",");
				statsMeta.addResponseColumn(headerName, responseChildNodeList.item(i).getTextContent(), isPlaceholder);
			}
		}

		statsMeta.setResponseHeaders(responseHeaders.toString().replaceAll(",$", ""));
		return statsMeta;
	}
}
