package org.lerot.mycontacts;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

// Tools > Other > Attribute Types: adding, changing and deleting a type
class AttributeTypesTest
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
	void tidAndKindAreNotInTheEditTable()
	{
		assertFalse(mcAttributeTypes.findType("tid").getDisplaygroup().contains("E"));
		assertFalse(mcAttributeTypes.findType("kind").getDisplaygroup().contains("E"));
		assertFalse(mcAttributeTypes.editableList().contains("tid"));
		assertTrue(mcAttributeTypes.editableList().contains("email"));
	}

	@Test
	void addChangeAndDeleteAType()
	{
		assertEquals("skype", mcAttributeTypes.keyFor("Skype!"));
		mcAttributeTypes.addType("Skype", "text", 44, "EB");
		mcAttributeType skype = mcAttributeTypes.findType("skype");
		assertNotNull(skype);
		assertEquals("Skype", skype.getAttributeLabel());
		assertEquals("text", skype.getDataTypeKey());
		assertEquals(44, skype.getOrder());
		assertEquals("EB", skype.getDisplaygroup());

		mcAttributeTypes.updateType("skype", "Skype name", 50, "E");
		skype = mcAttributeTypes.findType("skype");
		assertEquals("Skype name", skype.getAttributeLabel());
		assertEquals(50, skype.getOrder());
		assertEquals("E", skype.getDisplaygroup());

		mcAttributeTypes.deleteType("skype");
		assertNull(mcAttributeTypes.findType("skype"));
	}

	@Test
	void newDetailStartsOnTheMostCommonMissingType()
	{
		java.util.List<String> keys = java.util.List.of("photo", "name", "position", "address", "phone", "email", "tags");
		java.util.Map<String, Integer> usage = java.util.Map.of("name", 138, "tags", 138, "address", 100, "email", 56, "phone", 48);
		assertEquals("address", mcAttributeTypes.suggestedNew(keys, java.util.Set.of("name", "tags"), usage));
		assertEquals("email", mcAttributeTypes.suggestedNew(keys, java.util.Set.of("name", "tags", "address"), usage));
		// nobody has a position: it comes after every type someone has
		assertEquals("position", mcAttributeTypes.suggestedNew(keys,
				java.util.Set.of("name", "tags", "address", "phone", "email"), usage));
		// all there: the first type
		assertEquals("photo", mcAttributeTypes.suggestedNew(keys, java.util.Set.copyOf(keys), usage));
	}

	@Test
	void tagUsersTagsExactlyTheContactsWithTheType() throws Exception
	{
		mcContacts db = new mcContacts();
		db.doExecute("insert into attributeValues(cid, root, qualifier, value) values (1, 'fax', '', '01543 000000')");
		// a contact tagged earlier that no longer has a fax loses the tag
		assertTrue(mcContact.getContact("2").addTag("has fax"));
		assertEquals(1, mcAttributeTypes.tagUsers("fax"));
		assertTrue(tags(1).contains("has fax"));
		assertFalse(tags(2).contains("has fax"));
		// again: no change, no duplicate
		assertEquals(1, mcAttributeTypes.tagUsers("fax"));
		assertEquals(1, tags(1).split("has fax", -1).length - 1);
	}

	private static String tags(int cid)
	{
		return TestDatabase.value("select value from attributeValues where cid = ? and root = 'tags'", cid);
	}

	@Test
	void aTypeInUseIsNotDeleted() throws Exception
	{
		mcAttributeTypes.addType("Skype", "text", 44, "EB");
		new mcContacts().doExecute("insert into attributeValues(cid, root, qualifier, value) values (1, 'skype', '', 'ally.s')");
		assertEquals(1, mcAttributeTypes.usage().get("skype"));
		mcAttributeTypes.deleteType("skype");
		assertNotNull(mcAttributeTypes.findType("skype"));
	}
}
