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
	void aTypeInUseIsNotDeleted() throws Exception
	{
		mcAttributeTypes.addType("Skype", "text", 44, "EB");
		new mcContacts().doExecute("insert into attributeValues(cid, root, qualifier, value) values (1, 'skype', '', 'ally.s')");
		assertEquals(1, mcAttributeTypes.usage().get("skype"));
		mcAttributeTypes.deleteType("skype");
		assertNotNull(mcAttributeTypes.findType("skype"));
	}
}
