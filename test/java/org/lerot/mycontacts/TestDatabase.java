package org.lerot.mycontacts;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.SQLException;

/*
 * Builds a fresh test database (a new database from the starter kit, plus
 * the made-up contacts in test/resources/testcontacts.sql) and points the
 * data layer and settings folder at it, without starting the GUI.
 */
public class TestDatabase
{
	public static mcDataSource create(Path folder) throws IOException, SQLException
	{
		Path db = folder.resolve("test.sqlite");
		createFile(db);
		MyContacts.settingsdir = settingsFolder() + File.separator;
		mcDataSource ds = new mcDataSource(db.toString());
		mcDataObject.datasource = ds;
		mcDataTypes.loadTypes();
		mcAttributeTypes.loadAttributeTypes();
		return ds;
	}

	// write the test database file only
	public static void createFile(Path db) throws IOException, SQLException
	{
		mcStarter.createDatabase(db);
		mcStarter.runScript(db, new String(TestDatabase.class.getResourceAsStream("/testcontacts.sql")
				.readAllBytes(), StandardCharsets.UTF_8));
	}

	// the starter kit's folder, holding labelTemplates.xml etc.
	public static String settingsFolder()
	{
		try
		{
			return new File(TestDatabase.class.getResource(mcStarter.KIT + "labelTemplates.xml").toURI())
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
