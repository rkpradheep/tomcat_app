package com.server.framework.common;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.tomcat.jakartaee.commons.lang3.StringUtils;

import com.server.framework.security.SecurityUtil;

public class OTPAPI extends HttpServlet
{
	private static final Map<String, Integer> OTP_META = new ConcurrentHashMap<>();

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		String email = StringUtils.defaultIfEmpty(request.getParameter("email"), StringUtils.EMPTY);
		SecurityUtil.writeJSONResponse(response, Map.of("otp_reference", initiateOTP(email)));
	}

	public static String initiateOTP(String email)
	{
		String subject = "OTP Verification";
		String message = "We have received request to verify your email.";
		int otp = new Random().nextInt(90000) + 10000;
		message += "<br><br>Your OTP is <b>" + otp + "</b>";

		Util.sendEmail(subject, email, message);

		String otpReference = RandomStringUtils.randomAlphanumeric(10);

		OTP_META.put(otpReference, otp);
		return otpReference;
	}

	public static void verifyOTP(String otpReference, int otp) throws Exception
	{
		if(!OTP_META.containsKey(otpReference) || OTP_META.get(otpReference) != otp)
		{
			throw new Exception("Invalid OTP");
		}

		OTP_META.remove(otpReference);
	}
}
