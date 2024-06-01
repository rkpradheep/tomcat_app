package com.server.framework.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class Util
{
	public static final String HOME_PATH = System.getenv("MY_HOME");

	private static final Logger LOGGER = Logger.getLogger(Util.class.getName());

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
			URL url = new URL("https://cliq.zoho.com/company/64396901/api/v2/bots/myserver/message?zapikey=" + Configuration.getProperty("cliq.zapi.key"));

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
		sendEmail(subject, toAddress, null, message);
	}

	public static void sendEmail(String subject, String toAddress, String cc, String message)
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
			if(StringUtils.isNotEmpty(cc))
			{
				mimeMessage.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
			}
			Transport.send(mimeMessage);

			LOGGER.info("Email sent successfully");

		}
		catch(MessagingException e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
			throw new RuntimeException(e);
		}
	}

	public static JSONObject getZohoSecrets(String dc)
	{
		dc = Arrays.asList("local","dev").contains(dc) ? dc : "us";

		JSONObject oauthCredentials = new JSONObject();

		oauthCredentials.put("client_id", Configuration.getProperty("oauth.$.client.id".replace("$", dc)));
		oauthCredentials.put("client_secret", Configuration.getProperty("oauth.$.client.secret".replace("$", dc)));
		oauthCredentials.put("redirect_uri", Configuration.getProperty("oauth.$.client.redirecturi".replace("$", dc)));

		return oauthCredentials;
	}

	public static String readFileAsString(File file) throws IOException
	{
		FileReader fileReader = new FileReader(file);
		StringWriter stringWriter = new StringWriter();

		IOUtils.copy(fileReader, stringWriter);

		return stringWriter.toString();
	}

	public static Object getJSONFromString(String value)
	{
		try
		{
			return new JSONObject(value);
		}
		catch(Exception e)
		{
			try
			{
				return new JSONArray(value);
			}
			catch(Exception e1)
			{
				return null;
			}
		}
	}

}
