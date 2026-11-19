package org.lerot.mycontacts;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StarterTest
{
	@TempDir
	Path home;

	@Test
	void aNewComputerStartsWithTheAuthorsEntry() throws Exception
	{
		String saved = System.getProperty("user.home");
		System.setProperty("user.home", home.toString());
		try
		{
			mcStarter.setUp();
			Path db = Path.of(mcSettings.databasePath());
			assertTrue(Files.exists(db));
			assertEquals(List.of("email=paul.a.golder@gmail.com", "name={ fn:'Paul' , sn:'Golder' ,  }",
					"tid=Golder Paul"), values(db));
		} finally
		{
			System.setProperty("user.home", saved);
		}
	}

	@Test
	void anotherNewDatabaseIsEmpty() throws Exception
	{
		Path db = home.resolve("another.sqlite");
		mcStarter.createDatabase(db);
		assertEquals(List.of(), values(db));
	}

	private static List<String> values(Path db) throws Exception
	{
		List<String> found = new ArrayList<>();
		try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + db);
				ResultSet rs = c.createStatement().executeQuery(
						"select cid, root, value from attributeValues order by root"))
		{
			while (rs.next())
			{
				assertEquals(1, rs.getInt("cid"));
				found.add(rs.getString("root") + "=" + rs.getString("value"));
			}
		}
		return found;
	}
}
