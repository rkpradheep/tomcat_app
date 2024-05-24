import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public class TableConstantGenerator
{

	public static void main(String[] args) throws Exception
	{
		String dbServer = args[0];
		String driverName = StringUtils.equals(dbServer, "mariadb") ? "org.mariadb.jdbc.Driver" : "com.mysql.jdbc.Driver";
		String dbPort = args[1];
		String userName = args[2];
		String password = args[3];
		String schemaName = args[4];

		Class.forName(driverName);
		Connection connection = DriverManager.getConnection("jdbc:" + dbServer + "://localhost:" + dbPort + "/" + schemaName, userName, password);

		DatabaseMetaData databaseMetaData = connection.getMetaData();

		List<TableMeta> tableMetaList = new ArrayList<>();

		List<String> tableList = new ArrayList<>();

		ResultSet rs = connection.createStatement().executeQuery("Show tables");
		while(rs.next())
		{
			tableList.add(rs.getString(1));
		}

		for(String tableName : tableList)
		{
			List<String> columnList = new ArrayList<>();
			ResultSet columnResultSet = databaseMetaData.getColumns(null, schemaName, tableName, null);
			while(columnResultSet.next())
			{
				String columnName = columnResultSet.getString("COLUMN_NAME");
				columnList.add(columnName);
			}

			tableMetaList.add(new TableMeta(tableName, columnList));
		}

		VelocityEngine velocityEngine = new VelocityEngine();
		velocityEngine.setProperty("file.resource.loader.path", System.getenv("MY_HOME") + "/tomcat_server/build-tools/conf");
		velocityEngine.init();

		String packageName = "com.server.table.constants";
		for(TableMeta tableMeta : tableMetaList)
		{
			VelocityContext context = new VelocityContext();
			context.put("tableMeta", tableMeta);
			context.put("pkg", packageName);

			Template template = velocityEngine.getTemplate("ClassTemplate.vtl");

			File file = new File(System.getenv("MY_HOME") + "/tomcat_server/build-tools/tmp/" + packageName.replaceAll("\\.", "/"));
			file.mkdirs();

			Writer writer = new FileWriter(file.getAbsolutePath() + "/" + tableMeta.getFormattedTableName() + ".java");
			template.merge(context, writer);
			writer.flush();
			writer.close();
		}

		System.out.println("Generated");
	}
}
