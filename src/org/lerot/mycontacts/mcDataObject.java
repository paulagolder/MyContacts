package org.lerot.mycontacts;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class mcDataObject
{
	static mcDataSource datasource;
	public static String errorMessage = "";

	public mcDataObject()
	{
       ds();
	}

	// for programs without the main window (myContactsLite, tests): use this database
	public static void useDataSource(mcDataSource source)
	{
		datasource = source;
	}

	// the current database; follows a switch made in dbEditPanel
	static mcDataSource ds()
	{
		if (MyContacts.topgui != null && MyContacts.topgui.currentcon != null)
			datasource = MyContacts.topgui.currentcon;
		return datasource;
	}

	// the session connection, opened on first use
	protected static Connection conn() throws SQLException
	{
		Connection c = ds().getConnection();
		if (c == null) throw new SQLException(ds().getErrorMessage());
		return c;
	}

	public static void getConnection()
	{
		ds().connect();
	}

	public static  void disconnect()
	{
	}

	// report an SQL failure; inside a transaction this forces a rollback
	protected static void sqlError(SQLException e, String sql)
	{
		errorMessage = e.getClass().getName() + ": " + e.getMessage();
		if (sql != null) errorMessage += " [" + sql + "]";
		System.err.println(errorMessage);
		ds().markFailed();
	}

	protected static void sqlError(SQLException e)
	{
		sqlError(e, null);
	}

	public void doDelete(String table, String sqlstr, Object... params)
	{
		String sql = "DELETE FROM " + table + " WHERE " + sqlstr;
		try (PreparedStatement preparedStatement = conn().prepareStatement(sql))
		{
			for (int i = 0; i < params.length; i++)
				preparedStatement.setObject(i + 1, params[i]);
			preparedStatement.execute();
		} catch (SQLException e)
		{
			sqlError(e, sql);
		}
	}

	public void doExecute(String sqlstr)
	{
		try (PreparedStatement preparedStatement = conn().prepareStatement(sqlstr))
		{
			preparedStatement.execute();
		} catch (SQLException e)
		{
			sqlError(e, sqlstr);
		}
	}

	public static ArrayList<Map<String, String>> doQuery(String sqlstr, Object... params)
	{
		ArrayList<Map<String, String>> Rowlist = new ArrayList<Map<String, String>>();
		try (PreparedStatement stmt = conn().prepareStatement(sqlstr))
		{
			for (int i = 0; i < params.length; i++)
				stmt.setObject(i + 1, params[i]);
			try (ResultSet rs = stmt.executeQuery())
			{
				ResultSetMetaData rsmd = rs.getMetaData();
				int columnCount = rsmd.getColumnCount();
				while (rs.next())
				{
					Map<String, String> arow = new HashMap<String, String>();
					// The column count starts from 1
					for (int i = 1; i < columnCount + 1; i++)
					{
						String name = rsmd.getColumnName(i);
						String value = rs.getString(i);
						if (name != null) arow.put(name, value);
					}
					Rowlist.add(arow);
				}
			}
		} catch (SQLException e)
		{
			sqlError(e, sqlstr);
		}
		return Rowlist;
	}

	public void doSql(String sqlstr)
	{
		doExecute(sqlstr);
	}

	public void doUpdate(String table, String sqlstr)
	{
		doExecute("UPDATE  " + table + " SET " + sqlstr);
	}

	public int doInsert(String table, String sqlstr)
	{
		int newid = -1;
		String sql = "INSERT INTO " + table + sqlstr;
		try (PreparedStatement preparedStatement = conn().prepareStatement(sql))
		{
			preparedStatement.execute();
			ResultSet resultSet = preparedStatement.getGeneratedKeys();
			if (resultSet != null && resultSet.next())
			{
				newid = resultSet.getInt(1);
			}
		} catch (SQLException e)
		{
			sqlError(e, sql);
		}
		return newid;
	}

	public static String getDateTime()
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}

}
