package org.lerot.mycontacts;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// + New contact: saved only once it has a name, as if typed on the Edit screen
class NewContactTest
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
	void aNewContactHasItsNameAndTheDetailsToFillIn()
	{
		mcContact dora = mcContacts.createNewContact(" Dora ", "Brown");
		assertNotNull(dora);
		List<String> rows = mcDataObject.doQuery("select root || '=' || value as row from attributeValues"
				+ " where cid = ? order by root", dora.getCID()).stream().map(r -> r.get("row")).toList();
		assertEquals(List.of("address={ }", "email=", "mobile=", "name={title:, fn:Dora, mn:, sn:Brown, }",
				"tags=newcontact", "tid=" + dora.getTID()), rows);
		assertTrue(dora.getTID().startsWith("Brown"), dora.getTID());
		assertTrue(dora.getTID().contains("Dora"), dora.getTID());
	}
}
