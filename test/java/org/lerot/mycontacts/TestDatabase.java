package org.lerot.mycontacts;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/*
 * Builds a fresh test database (from test/resources/testdb.sql: the real
 * schema and settings, plus three made-up contacts) and points the data
 * layer and settings folder at it, without starting the GUI.
 */
public class TestDatabase
{
	public static mcDataSource create(Path folder) throws IOException, SQLException
	{
		Path db = folder.resolve("test.sqlite");
		String script = new String(TestDatabase.class.getResourceAsStream("/testdb.sql")
				.readAllBytes(), StandardCharsets.UTF_8);
		try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + db);
				Statement st = c.createStatement())
		{
			// the old driver runs one statement per call
			for (String sql : script.split(";\\s*\\n"))
			{
				String stmt = sql.replaceAll("(?m)^--.*$", "").trim();
				if (!stmt.isEmpty()) st.executeUpdate(stmt);
			}
		}
		MyContacts.settingsdir = settingsFolder() + File.separator;
		mcDataSource ds = new mcDataSource(db.toString());
		mcDataObject.datasource = ds;
		mcDataTypes.loadTypes();
		mcAttributeTypes.loadAttributeTypes();
		return ds;
	}

	// the folder holding the test copies of labelTemplates.xml etc.
	public static String settingsFolder()
	{
		try
		{
			return new File(TestDatabase.class.getResource("/labelTemplates.xml").toURI())
					.getParent();
		} catch (URISyntaxException e)
		{
			throw new IllegalStateException(e);
		}
	}

	public static String value(String sql, Object... params)
	{
		var rows = mcDataObject.doQuery(sql, params);
		return rows.isEmpty() ? null : rows.get(0).values().iterator().next();
	}
}
