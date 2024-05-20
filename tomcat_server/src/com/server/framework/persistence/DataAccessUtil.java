package com.server.framework.persistence;

import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;

public class DataAccessUtil
{
	private static final AtomicLong BATCH_END = new AtomicLong(-1L);
	private static final AtomicLong CURRENT_PK = new AtomicLong(-1L);

	public static long getNextPK() throws Exception
	{

		if(Objects.equals(CURRENT_PK.get(), -1L) || Objects.equals(CURRENT_PK.get(), BATCH_END.get()))
		{
			SelectQuery selectQuery = new SelectQuery("BatchDetails");
			DataObject dataObject = DataAccess.get(selectQuery);
			if(dataObject.getRows().isEmpty())
			{
				Row row = new Row("BatchDetails");
				row.set("sas_id", 1);
				row.set("batch_start", 1000000001000L);

				dataObject.addRow(row);

				DataAccess.add(dataObject);

				CURRENT_PK.set(1000000000003L);
				BATCH_END.set(1000000001000L - 1L);
			}

			Long batchStart = (Long) dataObject.getRows().get(0).get("batch_start");
			Long updatedBatchEnd = batchStart + 1000L;

			CURRENT_PK.set(batchStart);
			BATCH_END.set(updatedBatchEnd - 1L);

			UpdateQuery updateQuery = new UpdateQuery("BatchDetails");
			updateQuery.setValue("batch_start", updatedBatchEnd);

			DataAccess.update(updateQuery);
		}

		return CURRENT_PK.getAndIncrement();
	}

	public static String getInsertQueryString(Row row)
	{
		Map<String, Object> rowMap = row.getRowMap();
		StringBuilder insertQuery = new StringBuilder("INSERT INTO " + row.getTableName());
		insertQuery.append(" ( ");
		StringBuilder valuesPart = new StringBuilder(" (");
		for(String columnName : rowMap.keySet())
		{
			insertQuery.append(columnName);
			insertQuery.append(",");

			valuesPart.append("?,");
		}

		insertQuery.deleteCharAt(insertQuery.length() - 1);
		valuesPart.deleteCharAt(valuesPart.length() - 1);

		insertQuery.append(")");
		insertQuery.append(" VALUES ");
		insertQuery.append(valuesPart);
		insertQuery.append(")");

		return insertQuery.toString();

	}

	public static String getCriteriaString(Criteria criteria, List<Object> criteriaPlaceHolderList)
	{
		if(criteria.leftCriteria == null)
		{
			Criteria.Criterion criterion = criteria.criterion;
			if(criterion == null)
			{
				return StringUtils.EMPTY;
			}

			if(criteria.criterion.columnValue instanceof List<?> placeHolderValueList)
			{
				criteriaPlaceHolderList.addAll(placeHolderValueList);
				String placeHolders = String.join(",", "?".repeat(placeHolderValueList.size()).split(StringUtils.EMPTY));
				return "(" + criterion.tableName + "." + criterion.columnName + StringUtils.SPACE + criterion.comparator + StringUtils.SPACE + "(" + placeHolders + "))";
			}

			criteriaPlaceHolderList.add(criteria.criterion.columnValue);
			return "(" + criterion.tableName + "." + criterion.columnName + StringUtils.SPACE + criterion.comparator + StringUtils.SPACE + "?" + ")";
		}

		String left = getCriteriaString(criteria.leftCriteria, criteriaPlaceHolderList);

		if(criteria.rightCriteria == null)
		{
			return left;
		}

		left += criteria.operator;

		String right = getCriteriaString(criteria.rightCriteria, criteriaPlaceHolderList);

		return "(" + left + right + ")";
	}

	public static String getSelectQueryString(SelectQuery selectQuery, List<Object> valuePlaceHolderList)
	{
		StringBuilder selectQueryBuilder = new StringBuilder("SELECT * FROM " + selectQuery.tableName + " ");

		List<SelectQuery.Join> joinList = selectQuery.joinList;

		for(SelectQuery.Join join : joinList)
		{
			selectQueryBuilder.append(join.joinType).append(" ").append(join.referenceTableName).append(" ON ");
			selectQueryBuilder.append(join.baseTableName).append(".").append(join.baseTableColumnName);
			selectQueryBuilder.append("=").append(join.referenceTableName).append(".").append(join.referenceTableColumnName);
		}

		if(Objects.nonNull(selectQuery.criteria))
		{
			selectQueryBuilder.append(" WHERE ").append(getCriteriaString(selectQuery.criteria, valuePlaceHolderList));
		}

		if(selectQuery.needLock)
		{
			selectQueryBuilder.append(" FOR UPDATE");
		}

		return selectQueryBuilder.toString();
	}

	public static String getUpdateQueryString(UpdateQuery updateQuery, List<Object> valuePlaceHolderList)
	{
		StringBuilder updateQueryBuilder = new StringBuilder("UPDATE " + updateQuery.tableName + " ");

		for(Map.Entry<String, Object> objectEntry : updateQuery.columnNameValueMap.entrySet())
		{
			updateQueryBuilder.append("SET ").append(objectEntry.getKey()).append(" = ").append("?");
			updateQueryBuilder.append(",");
			valuePlaceHolderList.add(objectEntry.getValue());
		}

		updateQueryBuilder.deleteCharAt(updateQueryBuilder.length() - 1);
		if(updateQuery.criteria != null)
		{
			updateQueryBuilder.append(" WHERE ").append(getCriteriaString(updateQuery.criteria, valuePlaceHolderList));
		}

		return updateQueryBuilder.toString();
	}

	public static String getDeleteQueryString(String tableName, Criteria criteria, List<Object> valuePlaceHolderList)
	{
		StringBuilder updateQueryBuilder = new StringBuilder("DELETE FROM " + tableName + " ");

		if(criteria != null)
		{
			updateQueryBuilder.append(" WHERE ").append(getCriteriaString(criteria, valuePlaceHolderList));
		}

		return updateQueryBuilder.toString();
	}

	public static void populatePK(List<Row> rowList) throws Exception
	{
		Map<UVH, Long> uvhLongMap = new HashMap<>();

		for(Row row : rowList)
		{
			for(Map.Entry<String, Object> objectEntry : row.rowMap.entrySet())
			{
				if(objectEntry.getValue() instanceof UVH)
				{
					Long generatedValue = uvhLongMap.getOrDefault(objectEntry.getValue(), getNextPK());
					uvhLongMap.put((UVH) objectEntry.getValue(), generatedValue);
					row.set(objectEntry.getKey(), generatedValue);
				}
			}
		}
	}

	public static void handleExceptionForTxn(Connection connection) throws Exception
	{
		if(Objects.isNull(DataAccess.Transaction.getActiveTxnFromTL()))
		{
			if(Objects.nonNull(connection) && !connection.isClosed())
			{
				connection.rollback();
			}
		}
	}

	public static void handlePostProcessForTxn(Connection connection) throws Exception
	{
		if(Objects.isNull(DataAccess.Transaction.getActiveTxnFromTL()))
		{
			if(Objects.nonNull(connection) && !connection.isClosed())
			{
				connection.close();
			}
		}
	}
}
