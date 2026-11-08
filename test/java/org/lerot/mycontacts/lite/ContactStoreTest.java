package org.lerot.mycontacts.lite;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.lerot.mycontacts.TestDatabase;
import org.lerot.mycontacts.mcDataObject;
import org.lerot.mycontacts.mcDataSource;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

// the Contacts viewer's cards, read through the program's connection
class ContactStoreTest
{
	@TempDir
	Path folder;
	mcDataSource ds;
	ContactStore store;

	@BeforeEach
	void setUp() throws Exception
	{
		ds = TestDatabase.create(folder);
		store = new ContactStore();
		store.refresh();
	}

	@AfterEach
	void tearDown()
	{
		ds.close();
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
	void namesKeepTheirApostrophes()
	{
		assertEquals("Bob O'Brien", store.find(2).orElseThrow().name());
	}

	@Test
	void missingFieldsAreNull()
	{
		ContactCard bob = store.find(2).orElseThrow();
		assertNull(bob.email());
		assertNull(bob.birthday());
		assertNull(store.find(1).orElseThrow().addressFrom(), "Alice has her own address");
	}

	@Test
	void aMemberWithoutAnAddressShowsTheirGroupsAddress()
	{
		// Bob: memberof Alice; Carol: listed by Alice with hasmember
		for (int id : new int[] { 2, 3 })
		{
			ContactCard member = store.find(id).orElseThrow();
			assertEquals(store.find(1).orElseThrow().address(), member.address());
			assertEquals("Alice Smith", member.addressFrom());
		}
		assertEquals(3, store.search("high street").size());
	}

	@Test
	void searchMatchesEveryWordAnywhereOnTheCard()
	{
		assertEquals(1, store.search("alice").size());
		assertEquals(3, store.search("testtown").size()); // Bob and Carol share Alice's address
		assertEquals(1, store.search("7700").size());
		assertEquals(0, store.search("alice jones").size());
		assertEquals(3, store.search("  ").size());
	}

	@Test
	void reloadsWhenAContactIsSaved()
	{
		assertFalse(store.refresh());
		new mcDataObject().doExecute(
				"insert into attributeValues(cid, root, qualifier, value) values (2, 'email', '', 'bob@example.com')");
		assertTrue(store.refresh());
		assertEquals("bob@example.com", store.find(2).orElseThrow().email());
	}

	@Test
	void doesNotReloadForOtherTables()
	{
		assertFalse(store.refresh());
		// the full window rebuilds its allcontacts summary table often
		new mcDataObject().doExecute("create table allcontacts as select cid from attributeValues");
		assertFalse(store.refresh());
	}

	@Test
	void reloadsWhenAnotherDatabaseIsOpened()
	{
		assertFalse(store.refresh());
		mcDataObject.useDataSource(ds);
		assertTrue(store.refresh());
	}
}
