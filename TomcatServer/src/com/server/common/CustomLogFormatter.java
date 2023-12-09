package com.server.common;

import java.util.Collections;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class CustomLogFormatter extends SimpleFormatter
{
	@Override
	public String format(LogRecord record)
	{
		return System.lineSeparator()  + getLineSeparator() + System.lineSeparator() + System.lineSeparator() + Thread.currentThread().getName()  + " ----> " + super.format(record) + System.lineSeparator() + getLineSeparator();
	}

	String getLineSeparator()
	{
		return String.join("", Collections.nCopies(200, "-"));
	}
}
