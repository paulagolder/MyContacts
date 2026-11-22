package org.lerot.mycontacts.lite;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.lerot.mycontacts.TestDatabase;
import org.lerot.mycontacts.mcAttributeTypes;
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
		assertEquals("+447700900123", alice.whatsapp());
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
	void savesAWhatsAppNumberFromThePhoneBox()
	{
		assertNull(store.find(2).orElseThrow().whatsapp());
		assertTrue(store.setWhatsApp(2, "07700 900456"));
		assertTrue(store.refresh());
		assertEquals("+447700900456", store.find(2).orElseThrow().whatsapp());
	}

	@Test
	void aNewWhatsAppNumberReplacesTheOld()
	{
		store.setWhatsApp(1, "+33621250190");
		store.refresh();
		assertEquals("+33621250190", store.find(1).orElseThrow().whatsapp());
		assertEquals("1", TestDatabase.value(
				"select count(*) from attributeValues where cid = 1 and root = 'whatsapp'"));
	}

	@Test
	void olderDatabasesGainTheWhatsAppType()
	{
		new mcDataObject().doExecute("delete from attribute where attributeKey = 'whatsapp'");
		mcAttributeTypes.loadAttributeTypes();
		assertNotNull(mcAttributeTypes.findType("whatsapp"));
		assertEquals("cellphone", TestDatabase.value(
				"select dataType from attribute where attributeKey = 'whatsapp'"));
	}

	@Test
	void showsAPhotoWhenThereIsOne()
	{
		assertNull(store.photo(1));
		// a 1x1 JPEG, folded over two lines as some imports leave it
		String jpeg = "/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////wgALCAABAAEBAREA/8QAFBABAAAAAAAAAAAAAAAAAAAAAP/aAAgBAQABPxA=";
		new mcDataObject().doExecute("insert into attributeValues(cid, root, qualifier, value) values (1, 'photo', '', '"
				+ jpeg.substring(0, 60) + "\n" + jpeg.substring(60) + "')");
		assertNotNull(store.photo(1));
	}

	@Test
	void aCutShortPhotoIsNotShown()
	{
		new mcDataObject().doExecute("insert into attributeValues(cid, root, qualifier, value) values (1, 'photo', '', "
				+ "'/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAMCAgMCAgMDAwMEAwMEBQgFBQQEBQoHBwYIDAoMDAs')");
		assertNull(store.photo(1));
	}

	@Test
	void reloadsWhenAnotherDatabaseIsOpened()
	{
		assertFalse(store.refresh());
		mcDataObject.useDataSource(ds);
		assertTrue(store.refresh());
	}

	@Test
	void showsTheTypesTickedForLiteInDisplayOrder()
	{
		assertEquals(java.util.List.of("address", "phone", "mobile", "whatsapp", "email", "birthday"),
				store.find(1).orElseThrow().details().stream().map(ContactCard.Detail::key).toList());
	}

	@Test
	void aTypeTickedForLiteShowsWithItsLabel()
	{
		new mcDataObject().doExecute("insert into attributeValues(cid, root, qualifier, value) values (1, 'position', '', 'Head gardener')");
		new mcDataObject().doExecute("insert into attributeValues(cid, root, qualifier, value) values (1, 'note', 'old', 'qualified')");
		mcAttributeTypes.updateType("position", "Job", 16, "EBSL");
		mcAttributeTypes.updateType("note", "Note", 90, "EBL");
		assertTrue(store.refresh(), "a changed type reloads the cards");
		ContactCard alice = store.find(1).orElseThrow();
		assertEquals("Head gardener", alice.value("position"));
		assertEquals("Job", alice.details().stream().filter(d -> d.key().equals("position")).findFirst().orElseThrow().label());
		// before the address: position's order is 16, address's 20
		assertEquals("position", alice.details().get(0).key());
		assertNull(alice.value("note"), "only the plain detail shows");
		assertEquals(1, store.search("gardener").size());
	}

	@Test
	void aTypeUntickedForLiteIsNotShown()
	{
		mcAttributeTypes.updateType("email", "Email", 61, "EBS");
		store.refresh();
		assertNull(store.find(1).orElseThrow().email());
	}

	@Test
	void olderDatabasesGainLiteOnTheUsualTypesOnce()
	{
		new mcDataObject().doExecute("update attribute set displaygroups = replace(displaygroups, 'L', '')");
		mcAttributeTypes.loadAttributeTypes();
		assertTrue(mcAttributeTypes.findType("email").getDisplaygroup().contains("L"));
		assertFalse(mcAttributeTypes.findType("fax").getDisplaygroup().contains("L"));
		// unticked later, it stays unticked
		mcAttributeTypes.updateType("email", "Email", 61, "EBS");
		mcAttributeTypes.loadAttributeTypes();
		assertFalse(mcAttributeTypes.findType("email").getDisplaygroup().contains("L"));
	}
}
