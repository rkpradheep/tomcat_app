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

	public List<Row> getRows(Criteria criteria)
	{
		List<Row> filteredRowList = new ArrayList<>();
		for(Row row : rowList)
		{
			if(criteria.matches(row))
			{
				filteredRowList.add(row);
			}
		}
		return filteredRowList;
	}

	public boolean isEmpty()
	{
		return rowList.isEmpty();
	}

	public String toString()
	{
		StringBuilder dataObjectBuilder = new StringBuilder();
		dataObjectBuilder.append("<Rows>").append(System.lineSeparator());
		for(Row row : rowList)
		{
			dataObjectBuilder.append(row).append(System.lineSeparator());
		}
		dataObjectBuilder.append("</Rows>").append(System.lineSeparator());
		return dataObjectBuilder.toString();
	}
}
