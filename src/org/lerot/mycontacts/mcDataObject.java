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
	protected static  Connection con = null;
	public static String errorMessage = "";

	public mcDataObject()
	{
       datasource = MyContacts.topgui.currentcon;
	}
	
	public  void setConnection(mcDataSource source)
	{
		con = source.con;
	}
	
	
	public static void getConnection()
	{
		con = datasource.getConnection();
	}
	
	public static  void disconnect()
	{
		try
		{
			con.close();
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void doDelete(String table, String sqlstr, Object... params)
	{
		String sql = "DELETE FROM " + table + " WHERE " + sqlstr;
		PreparedStatement preparedStatement = null;
		try
		{
			 getConnection();
			preparedStatement = con.prepareStatement(sql);
			for (int i = 0; i < params.length; i++)
				preparedStatement.setObject(i + 1, params[i]);
			preparedStatement.execute();
			preparedStatement.close();
			disconnect();

		} catch (SQLException e)
		{
			errorMessage = e.getClass().getName() + ": " + e.getMessage();
			System.err.println(sql);
			System.err.println(errorMessage);
		}
	}

	

	public void doExecute(String sqlstr)
	{
		PreparedStatement preparedStatement = null;
		try
		{
			getConnection();
			preparedStatement = con.prepareStatement(sqlstr);
			preparedStatement.execute();
			preparedStatement.close();
			disconnect();

		} catch (SQLException e)
		{
			errorMessage = e.getClass().getName() + ": " + e.getMessage();
			System.err.println(sqlstr);
			System.err.println(errorMessage);
		}
	}

	
	
	public static ArrayList<Map<String, String>> doQuery(String sqlstr, Object... params)
	{
		ArrayList<Map<String, String>> Rowlist = new ArrayList<Map<String, String>>();
		ResultSetMetaData rsmd = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try
		{
			con =datasource.getConnection();
			if (con == null)
			{
				errorMessage = datasource.getErrorMessage();
				System.err.println(errorMessage + " [" + sqlstr + "]");
				return Rowlist;
			}
			stmt = con.prepareStatement(sqlstr);
			for (int i = 0; i < params.length; i++)
				stmt.setObject(i + 1, params[i]);
			rs = stmt.executeQuery();
			rsmd = rs.getMetaData();
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
			rs.close();
			stmt.close();
		} catch (SQLException e)
		{
			errorMessage = e.getClass().getName() + ": " + e.getMessage();
			System.err.println(errorMessage + " [" + sqlstr + "]");
		} finally
		{
			if (con != null) datasource.disconnect();
		}
		return Rowlist;

	}

	public void doSql(String sqlstr)
	{
		String sql = sqlstr;
		PreparedStatement preparedStatement = null;
		try
		{
			preparedStatement = con.prepareStatement(sql);
			preparedStatement.execute();
			preparedStatement.close();

		} catch (SQLException e)
		{
			errorMessage = e.getClass().getName() + ": " + e.getMessage();
			System.err.println(errorMessage);
		}

	}

	public void doUpdate(String table, String sqlstr)
	{
		String sql = "UPDATE  " + table + " SET " + sqlstr;
		PreparedStatement preparedStatement = null;
		try
		{
			preparedStatement = con.prepareStatement(sql);
			preparedStatement.execute();
			preparedStatement.close();

		} catch (SQLException e)
		{
			errorMessage = e.getClass().getName() + ": " + e.getMessage();
			errorMessage += " [" + sql + "]";
			System.err.println(errorMessage);
		}
	}
	
	

	public int doInsert(String table, String sqlstr)
	{
		int newid = -1;
		String sql = "INSERT INTO " + table + sqlstr;
		PreparedStatement preparedStatement = null;
		try
		{
			preparedStatement = con.prepareStatement(sql);
			preparedStatement.execute();
			ResultSet resultSet = preparedStatement.getGeneratedKeys();
			if (resultSet != null && resultSet.next())
			{
				newid = resultSet.getInt(1);
			}
			preparedStatement.close();
		} catch (SQLException e)
		{
			errorMessage = e.getClass().getName() + ": " + e.getMessage();
			System.err.println(sql);
			System.err.println(errorMessage);
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
