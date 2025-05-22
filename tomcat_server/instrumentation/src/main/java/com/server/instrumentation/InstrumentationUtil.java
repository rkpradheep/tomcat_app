package com.server.instrumentation;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InstrumentationUtil implements ClassFileTransformer
{
	private static final Logger LOGGER = Logger.getLogger(InstrumentationUtil.class.getName());

	private final String targetClassName;
	private final String targetMethodName;

	public InstrumentationUtil(String targetClassName, String targetMethodName)
	{
		this.targetClassName = targetClassName;
		this.targetMethodName = targetMethodName;
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
	{
		byte[] byteCode = classfileBuffer;
		if(!className.equals(this.targetClassName.replaceAll("\\.", "/")))
		{
			return byteCode;
		}

		LOGGER.info("[Agent] Transforming class " + this.targetClassName);
		try
		{
			ClassPool cp = ClassPool.getDefault();
			CtClass cc = cp.makeClass(new ByteArrayInputStream(classfileBuffer));
			CtMethod m = cc.getDeclaredMethod(this.targetMethodName);
			m.addLocalVariable("startTime", CtClass.longType);
			m.insertBefore("startTime = com.server.framework.common.DateUtil.getCurrentTimeInMillis();");

			StringBuilder endBlock = new StringBuilder();

			m.addLocalVariable("endTime", CtClass.longType);
			endBlock.append("endTime = com.server.framework.common.DateUtil.getCurrentTimeInMillis();");

			endBlock.append("jakarta.servlet.http.HttpServletRequest httpServletRequest = ((jakarta.servlet.http.HttpServletRequest) servletRequest);");
			endBlock.append("jakarta.servlet.http.HttpServletResponse httpServletResponse = ((jakarta.servlet.http.HttpServletResponse) servletResponse);");
			endBlock.append("String requestURI = httpServletRequest.getRequestURI().replaceFirst(httpServletRequest.getContextPath(), \"\");");
			endBlock.append("if(!com.server.framework.security.SecurityUtil.isResourceUri($1.getServletContext(), requestURI)){");
			endBlock.append("httpServletResponse.setHeader(\"TOTAL-TIME-TAKEN\", String.valueOf((endTime-startTime)/1000f) + \" second(s)\");");
			endBlock.append("LOGGER.info(\"Request completed in " + "\" + (endTime-startTime)/1000f + \" second(s)\");}");

			m.insertAfter(endBlock.toString());

			byteCode = cc.toBytecode();
			cc.detach();

			LOGGER.info("[Agent] Transforming class completed for " + this.targetClassName);
		}
		catch(Throwable e)
		{
			LOGGER.log(Level.SEVERE, "Exception", e);
		}

		return byteCode;
	}
}
