package com.server.framework.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ObjectUtils;

import com.server.framework.security.SecurityUtil;

public class FileManager extends HttpServlet
{
	PublicKey publicKey;
	PrivateKey privateKey;
	Cipher cipher;

	private static final Logger LOGGER = Logger.getLogger(FileManager.class.getName());

	public FileManager()
	{
		try
		{
			KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
			SecureRandom random = SecureRandom.getInstanceStrong();
			keyPairGen.initialize(2048, random);
			KeyPair keyPair = keyPairGen.generateKeyPair();
			publicKey = keyPair.getPublic();
			privateKey = keyPair.getPrivate();

			cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
		}
		catch(Exception e)
		{

		}

	}

	@Override public void service(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		try
		{
			String breaker = "<br/>";
			String path = request.getParameter("path") != null ? Util.decryptData(privateKey, request.getParameter("path").replaceAll("true_|false_", "")) : Util.HOME_PATH + "/..";

			String html = "<html>\n"
				+ "<head>\n"
				+ "    <meta  name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
				+ "</head>"
				+ "<form id=\"myForm\" method=\"post\" action=\"/files\">\n"
				+ "<input type=\"hidden\" name=\"path\" id=\"path\">\n"
				+ "<input type=\"hidden\" name=\"password\" id=\"password\">\n"
				+ "</form>\n"
				+ "<!-- JavaScript code to submit the form when the link is clicked -->\n"
				+ "<script>\n"
				+ "function submitForm(path) {\n"
				+ "if(path.startsWith(\"true\"))"
				+ "document.getElementById(\"password\").value = prompt(\"Enter password to download\");\n"
				+ "document.getElementById(\"path\").value = path;\n"
				+ " document.getElementById(\"myForm\").submit();\n"
				+ "}\n"
				+ "</script>";

			if(new File(path).isFile())
			{
				if(request.getParameter("password") == null || request.getParameter("password").equals(""))
				{
					response.getWriter().println("<html>\n"
						+ "<head>\n"
						+ "    <meta  name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
						+ "</head>");
					response.getWriter().println("<p style=\"color:red;\">Password Mandatory");
					return;
				}
				else if(!request.getParameter("password").equals("1166"))
				{
					response.getWriter().println("<html>\n"
						+ "<head>\n"
						+ "    <meta  name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
						+ "</head>");
					response.getWriter().println("<p style=\"color:red;\">Incorrect Password");
					return;
					//				path = new File(path).getParentFile().getPath();
					//				html = html.concat("\n<script>alert(\"Incorrect password.\");</script>");

				}
				else
				{
					downloadFile(new File(path), response);
					return;
				}
			}

			response.setContentType("text/html");
			File[] files = new File(path).listFiles();
			Arrays.sort(files);

			List<File> fileList = Arrays.asList(files);
			response.getWriter().println(html);

			for(File file : fileList)
			{
				String link = "<a href=\"#\" onclick=\"submitForm('" + file.isFile() + "_" + Util.encryptData(publicKey, file.getPath()) + "')\">" + file.getName() + "</a>";
				response.getWriter().println(link.concat(String.join("", Collections.nCopies(3, breaker))));
			}
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
			SecurityUtil.writerErrorResponse(response, e.getMessage());
		}
	}

	public void downloadFile(File file, HttpServletResponse response)
	{

		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");

		try(InputStream inputStream = new FileInputStream(file);
			OutputStream outputStream = response.getOutputStream())
		{
			byte[] buffer = new byte[4096];
			int length;
			while((length = inputStream.read(buffer)) > 0)
			{
				outputStream.write(buffer, 0, length);
			}
			outputStream.flush();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

	}

	public static void copyUploads()
	{
		try
		{
			new File(Util.HOME_PATH + "/tomcat_build/webapps/ROOT/uploads").mkdirs();
			File[] fileList = ObjectUtils.defaultIfNull(new File(Util.HOME_PATH + "/uploads").listFiles(), new File[0]);
			for(File file : fileList)
			{
				try
				{
					FileInputStream fileInputStream = new FileInputStream(file);
					byte[] b = new byte[2048];
					FileOutputStream fileOutputStream = new FileOutputStream(Util.HOME_PATH + "/tomcat_build/webapps/ROOT/uploads/" + file.getName());
					int len = 0;
					while((len = fileInputStream.read(b)) != -1)
					{
						fileOutputStream.write(b, 0, len);
					}
					fileInputStream.close();
				}
				catch(Exception e)
				{
					LOGGER.log(Level.SEVERE, "Exception occurred", e);
				}
			}
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
		}
	}

}
