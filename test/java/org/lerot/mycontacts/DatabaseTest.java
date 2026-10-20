package org.lerot.mycontacts;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

// Database access through the SQLite driver (sqlite-jdbc)
class DatabaseTest
{
	@TempDir
	Path folder;
	mcDataSource ds;

	@BeforeEach
	void setUp() throws Exception
	{
		ds = TestDatabase.create(folder);
	}

	@AfterEach
	void tearDown()
	{
		ds.close();
	}

	@Test
	void databaseCheckReportsContacts()
	{
		Map<String, String> checks = ds.checkmcdb();
		assertEquals("yes", checks.get("Valid"));
		assertEquals("3", checks.get("No of Contacts"));
		assertEquals("Users Contacts", checks.get("Title"));
	}

	@Test
	void loadsAContactWithItsAttributes()
	{
		mcContact alice = mcContact.getContact("1");
		assertNotNull(alice);
		assertEquals("alice@example.com", alice.getAttributes().get("email").getValue());
	}

	@Test
	void textWithApostrophesIsStoredAndFound()
	{
		assertEquals("O'Brien  Bob",
				TestDatabase.value("select value from attributeValues where cid = ? and root = 'tid'", 2));
	}

	@Test
	void transactionCommitsWhenAllStatementsSucceed()
	{
		boolean ok = ds.transaction(() -> new mcDataObject()
				.doExecute("insert into parameters(key, value) values('t', 'x')"));
		assertTrue(ok);
		assertEquals("1", TestDatabase.value("select count(*) from parameters where key = 't'"));
	}

	@Test
	void transactionRollsBackWhenAStatementFails()
	{
		boolean ok = ds.transaction(() ->
		{
			mcDataObject o = new mcDataObject();
			o.doExecute("insert into parameters(key, value) values('t', 'x')");
			o.doExecute("insert into nosuchtable values(1)");
		});
		assertFalse(ok);
		assertEquals("0", TestDatabase.value("select count(*) from parameters where key = 't'"));
	}

	@Test
	void newContactGetsTheNextUnusedId()
	{
		mcContact c = new mcContact();
		c.setTID("Test Person");
		int id = c.insertNewContact();
		assertEquals(4, id);
		assertEquals(4, c.getCID());
		assertEquals("Test Person",
				TestDatabase.value("select value from attributeValues where cid = ? and root = 'tid'", id));
	}

	@Test
	void idOfDeletedContactIsNotReused()
	{
		mcContact c = new mcContact();
		c.setTID("Short Lived");
		int id = c.insertNewContact();
		new mcDataObject().doDelete("attributeValues", "cid = ?", id);
		assertEquals(id + 1, new mcContacts().getNewID());
	}

	@Test
	void savingAContactSavesChangedAttributes()
	{
		mcContact alice = mcContact.getContact("1");
		alice.getAttributes().get("email").setValue("alice@new.example.com", "now");
		assertTrue(alice.updateContact());
		assertEquals("alice@new.example.com",
				TestDatabase.value("select value from attributeValues where cid = 1 and root = 'email'"));
	}
}
