package org.lerot.mycontacts;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/*
 * The first start on a new computer: the settings folder (~/.mccontacts/)
 * gets an empty database and the label and address templates, from the
 * starter kit inside the program (resources/org/lerot/mycontacts/starter).
 * A folder that already holds a database is left as it is.
 */
public class mcStarter
{
	static final String KIT = "/org/lerot/mycontacts/starter/";
	static final String[] TEMPLATES = { "labelTemplates.xml", "addressTemplates.xml" };

	public static void setUp()
	{
		File folder = new File(mcSettings.folder());
		try
		{
			Files.createDirectories(folder.toPath());
			for (String name : TEMPLATES)
			{
				Path file = folder.toPath().resolve(name);
				if (!Files.exists(file))
					try (InputStream in = kit(name))
					{
						Files.copy(in, file);
					}
			}
			File[] databases = folder.listFiles((dir, name) -> name.endsWith(".sqlite"));
			if (databases != null && databases.length == 0)
			{
				Path db = Path.of(mcSettings.databasePath());
				createDatabase(db);
				System.out.println("New contacts database " + db);
			}
		} catch (IOException | SQLException e)
		{
			System.err.println("Cannot set up " + folder + " : " + e.getMessage());
		}
	}

	// a new, empty database: the tables and the settings they need
	public static void createDatabase(Path db) throws IOException, SQLException
	{
		try (InputStream in = kit("newdb.sql"))
		{
			runScript(db, new String(in.readAllBytes(), StandardCharsets.UTF_8));
		}
	}

	// run SQL statements, each ending with ";" at the end of a line
	public static void runScript(Path db, String script) throws SQLException
	{
		try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + db);
				Statement st = c.createStatement())
		{
			for (String sql : script.split(";\\s*\\n"))
			{
				String stmt = sql.replaceAll("(?m)^--.*$", "").trim();
				if (!stmt.isEmpty()) st.executeUpdate(stmt);
			}
		}
	}

	private static InputStream kit(String name) throws IOException
	{
		InputStream in = mcStarter.class.getResourceAsStream(KIT + name);
		if (in == null) throw new IOException("the program has no " + name);
		return in;
	}
}
