package org.lerot.mycontacts.lite;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.lerot.mycontacts.TestDatabase;

import java.nio.file.Path;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.*;

// myContactsLite's read-only view of the contacts
class ContactStoreTest
{
	@TempDir
	Path folder;
	ContactStore store;

	@BeforeEach
	void setUp() throws Exception
	{
		TestDatabase.create(folder).close();
		store = new ContactStore(folder.resolve("test.sqlite").toString());
		store.refresh();
	}

	@AfterEach
	void tearDown()
	{
		store.close();
	}

	@Test
	void listsEveryContactSortedByName()
	{
		assertEquals(java.util.List.of("Jones Carol", "O'Brien Bob", "Smith Alice"),
				store.all().stream().map(ContactCard::listName).toList());
	}

	@Test
	void showsThePlainFieldsFormatted()
	{
		ContactCard alice = store.find(1).orElseThrow();
		assertEquals("Alice Smith", alice.name());
		assertEquals("alice@example.com", alice.email()); // not the "old" one
		assertEquals("+441234567890", alice.phone());
		assertEquals("+447700900123", alice.mobile());
		assertEquals("2 March 1950", alice.birthday());
		assertNotNull(alice.address());
		assertTrue(alice.address().contains("1 High Street"), alice.address());
		assertFalse(alice.address().contains("Alice"), "address block should not repeat the name");
	}

	@Test
	void missingFieldsAreNull()
	{
		ContactCard bob = store.find(2).orElseThrow();
		assertNull(bob.email());
		assertNull(bob.address());
		assertNull(bob.birthday());
	}

	@Test
	void searchMatchesEveryWordAnywhereOnTheCard()
	{
		assertEquals(1, store.search("alice").size());
		assertEquals(1, store.search("high street").size());
		assertEquals(1, store.search("7700").size());
		assertEquals(0, store.search("alice jones").size());
		assertEquals(3, store.search("  ").size());
	}

	@Test
	void reloadsOnlyWhenTheDatabaseChanges() throws Exception
	{
		assertFalse(store.refresh());
		Thread.sleep(1100); // file times can be to the second
		try (var c = DriverManager.getConnection("jdbc:sqlite:" + folder.resolve("test.sqlite"));
				var st = c.createStatement())
		{
			st.executeUpdate("insert into attributeValues(cid, root, qualifier, value) values (2, 'email', '', 'bob@example.com')");
		}
		assertTrue(store.refresh());
		assertEquals("bob@example.com", store.find(2).orElseThrow().email());
	}

	@Test
	void cannotChangeTheDatabase() throws Exception
	{
		var con = new org.lerot.mycontacts.mcDataSource(folder.resolve("test.sqlite").toString(), true).getConnection();
		assertThrows(java.sql.SQLException.class,
				() -> con.createStatement().executeUpdate("delete from attributeValues"));
		con.close();
	}
}
