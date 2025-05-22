package com.server.framework.common;

import java.util.Collections;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class CustomLogFormatter extends SimpleFormatter
{
	@Override
	public String format(LogRecord record)
	{
		return getLineSeparator() + Thread.currentThread().getName()  + " ----> " + super.format(record) + getLineSeparator();
	}

	String getLineSeparator()
	{
		return String.join("", Collections.nCopies(2, System.lineSeparator()));
	}
}
