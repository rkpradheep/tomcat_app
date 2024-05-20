package com.server.framework.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataObject
{
	List<Row> rowList = new ArrayList<>();

	private transient Map<String, List<String>> tableUniqueRowList = new HashMap<>();

	public void addRow(Row row)
	{
		rowList.add(row);
	}

	public List<Row> getRows()
	{
		return rowList;
	}
}
