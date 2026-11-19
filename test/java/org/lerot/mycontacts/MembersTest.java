package org.lerot.mycontacts;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Add group as members on the Edit page
class MembersTest
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
	void addsHasMemberLinksLeavingOutItselfAndExistingMembers()
	{
		// Alice already has Carol as a member
		mcContact alice = mcContact.getContact("1");
		List<mcContact> group = List.of(alice, mcContact.getContact("2"), mcContact.getContact("3"));
		int[] added = { 0 };
		assertTrue(ds.transaction(() -> added[0] = alice.addMembers(group)));
		assertEquals(1, added[0]);
		assertEquals(List.of("000002=000002", "000003=000003"), members());
	}

	private static List<String> members()
	{
		return mcDataObject.doQuery(
				"select qualifier || '=' || value as link from attributeValues where cid = 1 and root = 'hasmember'"
						+ " order by qualifier").stream().map(row -> row.get("link")).toList();
	}
}
