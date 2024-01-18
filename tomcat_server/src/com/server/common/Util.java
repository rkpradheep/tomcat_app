package com.server.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.tomcat.websocket.server.WsServerContainer;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Util
{
	public static final String HOME_PATH = System.getenv("MY_HOME");

	private static final Logger LOGGER = Logger.getLogger(Util.class.getName());

	private static ServletContext SERVLET_CONTEXT;
	private static List<String> API_END_POINTS;

	public static void init(ServletContext servletContext)
	{
		SERVLET_CONTEXT = servletContext;

		API_END_POINTS = servletContext.getServletRegistrations().keySet().stream()
			.map(SERVLET_CONTEXT::getServletRegistration)
			.map(ServletRegistration::getMappings)
			.flatMap(Collection::stream)
			.collect(Collectors.toList());
	}

	public static String encryptData(PublicKey publicKey, String plainText)
	{
		try
		{
			byte[] plainBytes = plainText.getBytes(StandardCharsets.UTF_8);
			Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			return new String(Base64.getEncoder().encode(cipher.doFinal(plainBytes)));
		}
		catch(Exception e)
		{
			return "";
		}
	}

	public static String decryptData(PrivateKey privateKey, String cipherText) throws Exception
	{
		byte[] plainBytes = Base64.getDecoder().decode(cipherText.getBytes("UTF-8"));
		Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		return new String(cipher.doFinal(plainBytes));
	}

	public static String postMessageToBot(String message)
	{
		try
		{
			URL url = new URL("https://cliq.zoho.com/company/64396901/api/v2/bots/myserver/message?zapikey=1001.cb9555d23c48ab721daae1657431b62f.5d7e4f5eabc947097d2d4fd64a235f49");

			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			httpURLConnection.setRequestMethod("POST");

			httpURLConnection.setDoOutput(true);
			httpURLConnection.setRequestProperty("Content-Type", "application/json");

			JSONObject payload = new JSONObject();
			payload.put("text", message);
			httpURLConnection.getOutputStream().write(payload.toString().getBytes(StandardCharsets.UTF_8));

			return getResponse(httpURLConnection.getResponseCode() != 200 ? httpURLConnection.getErrorStream() : httpURLConnection.getInputStream());
		}
		catch(Exception e)
		{
			return "";
		}
	}

	public static String postMessageToChat(String chatID, String message, String replyTo, String accessToken)
	{
		try
		{
			URL url = new URL("https://cliq.zoho.com/company/64396901/api/v2/chats/{CHAT_ID}/message".replace("{CHAT_ID}", chatID));

			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			httpURLConnection.setRequestMethod("POST");

			httpURLConnection.setDoOutput(true);
			httpURLConnection.setRequestProperty("Content-Type", "application/json");
			httpURLConnection.setRequestProperty("Authorization", "Bearer " + accessToken);

			JSONObject payload = new JSONObject();
			payload.put("text", message);
			payload.put("reply_to", replyTo);

			JSONObject bot = new JSONObject();
			bot.put("name", "Reminder Bot");
			bot.put("image", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRB2vYCNle16ALEpxdACttqEVBnI-2lX9fVh5o3kCxYv3rZ9xkk6DDDNKN-9VVTcdXq7Mc&usqp=CAU");

			payload.put("bot", bot);
			payload.put("sync_message", true);

			httpURLConnection.getOutputStream().write(payload.toString().getBytes(StandardCharsets.UTF_8));

			return getResponse(httpURLConnection.getResponseCode() != 200 ? httpURLConnection.getErrorStream() : httpURLConnection.getInputStream());
		}
		catch(Exception e)
		{
			return "";
		}
	}

	public static String getResponse(InputStream inputStream) throws Exception
	{
		StringBuilder output = new StringBuilder();

		try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream)))
		{
			String line;
			while((line = bufferedReader.readLine()) != null)
			{
				output.append(line);
				output.append("\n");
			}
		}

		//LOGGER.log(Level.INFO, "Internal API Response received {0}", output.toString());
		return output.toString();
	}

	public static void sendEmail(String subject, String toAddress, String message)
	{
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
		props.put("mail.smtp.ssl.protocols", "TLSv1.2");

		Session session = Session.getInstance(props, new Authenticator()
		{
			protected PasswordAuthentication getPasswordAuthentication()
			{
				return new PasswordAuthentication(Configuration.getProperty("mail.user"), Configuration.getProperty("mail.password"));
			}
		});

		try
		{
			Message mimeMessage = new MimeMessage(session);
			mimeMessage.setSubject(subject);

			Multipart mp = new MimeMultipart();
			MimeBodyPart htmlPart = new MimeBodyPart();
			htmlPart.setContent(message, "text/html");
			mp.addBodyPart(htmlPart);
			mimeMessage.setContent(mp);
			mimeMessage.setFrom(new InternetAddress(Configuration.getProperty("mail.user")));
			mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
			Transport.send(mimeMessage);

			LOGGER.info("Email sent successfully");

		}
		catch(MessagingException e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
			throw new RuntimeException(e);
		}
	}

	public static void writerErrorResponse(HttpServletResponse response, String message) throws IOException
	{
		writerErrorResponse(response, HttpStatus.SC_BAD_REQUEST, null, message);
	}

	public static void writerErrorResponse(HttpServletResponse response, int statusCode, String code, String message) throws IOException
	{
		response.setStatus(statusCode);

		Map<String, String> responseMap = new HashMap<>();
		responseMap.put("error", message);
		responseMap.put("code", code);

		writeJSONResponse(response, responseMap);
	}
	public static void writerErrorResponse(HttpServletResponse response, int statusCode, String code, String message, Map<String, String> additionalData) throws IOException
	{
		response.setStatus(statusCode);

		Map<String, String> responseMap = new HashMap<>();
		responseMap.put("error", message);
		responseMap.put("code", code);
		responseMap.putAll(additionalData);

		writeJSONResponse(response, responseMap);
	}

	public static String getFormattedCurrentTime()
	{
		return getFormattedTime(System.currentTimeMillis());
	}

	public static String getFormattedTime(Long timeInMilliseconds)
	{
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(timeInMilliseconds), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("hh : mm a"));
	}

	public static JSONObject getZohoSecrets(String dc)
	{
		JSONObject oauthCredentials = new JSONObject();

		oauthCredentials.put("client_id", Configuration.getProperty("oauth.$.client.id".replace("$", dc)));
		oauthCredentials.put("client_secret", Configuration.getProperty("oauth.$.client.secret".replace("$", dc)));
		oauthCredentials.put("redirect_uri", Configuration.getProperty("oauth.$.client.redirecturi".replace("$", dc)));

		return oauthCredentials;
	}

	public static JSONObject getJSONObject(HttpServletRequest request) throws IOException
	{
		return new JSONObject(request.getReader().lines().collect(Collectors.joining()));
	}

	public static void writeJSONResponse(HttpServletResponse response, Object responseObject) throws IOException
	{
		response.setContentType("application/json");

		ObjectMapper objectMapper = new ObjectMapper();
		response.getWriter().print(objectMapper.writeValueAsString(responseObject));
	}

	public static void writeSuccessJSONResponse(HttpServletResponse response, String responseMessage) throws IOException
	{
		Map<String, String> responseMap = new HashMap<>();
		responseMap.put("message", responseMessage);
		writeJSONResponse(response, responseMap);
	}

	public static String readFileAsString(File file) throws IOException
	{
		FileReader fileReader = new FileReader(file);
		StringWriter stringWriter = new StringWriter();

		IOUtils.copy(fileReader, stringWriter);

		return stringWriter.toString();
	}

	public static String readFileAsString(String fileName) throws IOException
	{
		InputStream inputStream = SERVLET_CONTEXT.getResourceAsStream("/WEB-INF/conf/".concat(fileName));

		StringWriter stringWriter = new StringWriter();

		IOUtils.copy(inputStream, stringWriter);

		return stringWriter.toString();
	}

	public static long convertDateToMilliseconds(String date, String format) throws ParseException
	{
		return new SimpleDateFormat(format).parse(date).getTime();
	}

	public static boolean isValidEndPoint(String endPoint) throws MalformedURLException
	{
		return API_END_POINTS.contains(endPoint) || isValidWebSocketEndPoint(endPoint) || Objects.nonNull(SERVLET_CONTEXT.getResource(endPoint));
	}

	public static boolean isValidWebSocketEndPoint(String endPoint)
	{
		Object mappingResult = ((WsServerContainer) SERVLET_CONTEXT.getAttribute("javax.websocket.server.ServerContainer")).findMapping(endPoint);
		return Objects.nonNull(mappingResult);
	}

	public static boolean isValidJSON(String value)
	{
		try
		{
			new JSONObject(value);
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}

}
