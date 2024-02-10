package com.server.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.server.security.SecurityUtil;

public class CSVParser extends HttpServlet
{
	private static final char DELIMITER = ',';

	@Override
	public void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException
	{
		try
		{

			DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
			diskFileItemFactory.setSizeThreshold(0);
			List<FileItem> items = new ServletFileUpload(diskFileItemFactory).parseRequest(httpServletRequest);

			for(FileItem item : items)
			{
				if(item.isFormField())
				{
					//				String fieldName = item.getFieldName();
					//				String fieldValue = item.getString();
				}
				else
				{
					StringBuilder tableData = new StringBuilder("<table>\n");
					tableData.append("<thead>\n");
					tableData.append("<tr>\n");

					AtomicInteger counter = new AtomicInteger(0);
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(item.getInputStream()));
					AtomicInteger headerSize = new AtomicInteger();
					bufferedReader.lines().forEach(line -> {
						if(counter.get() == 0)
						{
							Pair<String, Integer> headerPair = constructHeader(line);
							tableData.append(headerPair.getLeft());
							headerSize.set(headerPair.getValue());
							tableData.append("</thead>\n");
							tableData.append("</tr>\n");
							tableData.append("<tbody>\n");
						}
						else
						{
							tableData.append(constructBody(counter.get(), line, headerSize.get()));
						}
						counter.incrementAndGet();

					});
					tableData.append("</tbody>\n");
					Map<String, String> responseMap = new HashMap<>();
					responseMap.put("table_data", tableData.toString());
					responseMap.put("total", String.valueOf(counter.decrementAndGet()));
					item.delete();
					SecurityUtil.writeJSONResponse(httpServletResponse, responseMap);
				}
				break;
			}

		}
		catch(Exception e)
		{
			SecurityUtil.writerErrorResponse(httpServletResponse, e.getMessage());
		}
	}

	public Pair<String, Integer> constructHeader(String line)
	{
		String header = "<th>S.NO</th>\n";
		List<String> csvRow = getCSVRow(line);
		for(String columnName : csvRow)
		{
			header += "<th>" + columnName.trim() + "</th>\n";
		}
		return new ImmutablePair<>(header, csvRow.size());
	}

	public String constructBody(int count, String line, Integer headerSize)
	{
		String rowData = "<tr>\n";
		rowData += "<td>" + count + "</td>\n";
		List<String> csvRow = getCSVRow(line);
		for(String value : csvRow)
		{
			rowData += "<td>" + value.trim() + "</td>\n";
		}
		for(int i = 0; i < headerSize - csvRow.size(); i++)
		{
			rowData += "<td> </td>\n";
		}
		rowData += "</tr>\n";
		return rowData;
	}

	public List<String> getCSVRow(String line)
	{
		char[] lineSplit = line.toCharArray();
		boolean isQuoteEnclosed = false;
		List<String> values = new ArrayList<>();
		String currentValue = null;
		for(int i = 0; i < line.length(); i++)
		{
			if(lineSplit[i] == '"')
			{
				isQuoteEnclosed = !isQuoteEnclosed;
			}
			else if(lineSplit[i] == DELIMITER && !isQuoteEnclosed)
			{
				if(Objects.nonNull(currentValue))
				{
					values.add(currentValue.trim());
				}
				else
				{
					values.add(StringUtils.EMPTY);
				}
				currentValue = null;
			}
			else
			{
				if(currentValue == null)
				{
					currentValue = String.valueOf(lineSplit[i]);
				}
				else
				{
					currentValue += lineSplit[i];
				}
			}
		}
		if(Objects.nonNull(currentValue))
		{
			values.add(currentValue.trim());
		}
		return values;
	}

}
