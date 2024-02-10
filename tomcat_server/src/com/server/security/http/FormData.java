package com.server.security.http;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class FormData
{
	boolean isFile;
	String value = StringUtils.EMPTY;
	List<FileData> fileDataList = new ArrayList<>();

	public boolean isFile()
	{
		return isFile;
	}

	public void setIsFile(boolean file)
	{
		isFile = file;
	}

	public void addFileData(FileData fileData)
	{
		fileDataList.add(fileData);
	}

	public List<FileData> getFileDataList()
	{
		return fileDataList;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public static class FileData
	{
		String fileName;
		byte[] bytes;

		public FileData(String fileName, byte[] bytes)
		{
			this.fileName = fileName;
			this.bytes = bytes;
		}

		public String getFileName()
		{
			return fileName;
		}

		public byte[] getBytes()
		{
			return bytes;
		}
	}
}
