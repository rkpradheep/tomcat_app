package com.server.framework.common;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.server.framework.http.FormData;
import com.server.framework.security.SecurityUtil;

public class SSLCertificateGenHandler extends HttpServlet
{
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		try
		{
			if(request.getRequestURI().contains("/ssl/sign/initiate"))
			{
				Map<String, FormData> formDataMap = SecurityUtil.parseMultiPartFormData(request);
				String domain = formDataMap.get("domain").getValue();
				byte[] csr = formDataMap.get("csr_file").getFileDataList().get(0).getBytes();
				boolean isHTTPChallenge = Objects.nonNull(formDataMap.get("is_http_challenge")) && Boolean.parseBoolean(formDataMap.get("is_http_challenge").getValue());
				SecurityUtil.writeJSONResponse(response, ACMEClientUtil.initiate(domain, csr, isHTTPChallenge));
			}
			else
			{
				String fileName = ACMEClientUtil.verify(request.getParameter("domain"));
				Map<String, String> responseMap = new HashMap<>();
				responseMap.put("message", "Certificate signed successfully. Download it using url provided in cert_url node");
				responseMap.put("cert_url", "/uploads/" + fileName);
				SecurityUtil.writeJSONResponse(response, responseMap);
			}
		}
		catch(Exception e)
		{
			SecurityUtil.writerErrorResponse(response, e.getMessage());
		}
	}
}
