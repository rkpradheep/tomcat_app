package com.server.instrumentation;

import java.lang.instrument.Instrumentation;

public class InstrumentationAPI
{
	public static void premain(String agentArgs, Instrumentation inst)
	{
		String className = "com.server.framework.security.SecurityFilter";
		String methodName = "doFilter";
		transform(className, methodName, inst);
	}

	private static void transform(String className, String methodName, Instrumentation instrumentation)
	{
		InstrumentationUtil dt = new InstrumentationUtil(className, methodName);
		instrumentation.addTransformer(dt, true);
	}
}
