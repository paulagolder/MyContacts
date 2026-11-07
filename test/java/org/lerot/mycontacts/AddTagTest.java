package org.lerot.mycontacts;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

// mcContact.addTag, used by the search page's "Tag selection"
class AddTagTest
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

	private static Set<String> tags(int cid)
	{
		String value = TestDatabase.value("select value from attributeValues where cid = ? and root = 'tags'", cid);
		Set<String> tags = new TreeSet<>();
		if (value != null)
			for (String t : value.split(";"))
				if (!t.isBlank()) tags.add(t.trim());
		return tags;
	}

	@Test
	void addsATagBesideTheOthers()
	{
		assertTrue(mcContact.getContact("3").addTag("la rochelle"));
		assertEquals(Set.of("work", "la rochelle"), tags(3));
	}

	@Test
	void doesNotAddATagTwice()
	{
		assertTrue(mcContact.getContact("3").addTag("bristol"));
		assertFalse(mcContact.getContact("3").addTag("bristol"));
		assertFalse(mcContact.getContact("3").addTag("work"));
		assertEquals(Set.of("work", "bristol"), tags(3));
	}

	@Test
	void tagsAContactWithoutTags()
	{
		new mcContacts().doExecute("delete from attributeValues where cid = 2 and root = 'tags'");
		assertTrue(mcContact.getContact("2").addTag("bristol"));
		assertEquals(Set.of("bristol"), tags(2));
	}
}
