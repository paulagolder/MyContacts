package org.lerot.mycontacts;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

// Adding a dropped vCard to a contact (ez-vcard)
class VcardTest
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

	private static VCard parse(String... lines)
	{
		return Ezvcard.parse(String.join("\r\n", lines) + "\r\n").first();
	}

	@Test
	void savesTheEmailAddressFromAVcard()
	{
		VCard vcard = parse("BEGIN:VCARD", "VERSION:3.0", "N:Jones;Carol;;;", "FN:Carol Jones",
				"EMAIL;TYPE=INTERNET:carol@example.com",
				"ADR;TYPE=HOME:;;2 Low Road;Testtown;;TT2 2BB;UK", "END:VCARD");
		mcContact carol = mcContact.getContact("3");
		carol.importVcard(vcard);
		assertEquals("carol@example.com", TestDatabase.value(
				"select value from attributeValues where cid = 3 and root = 'email'"));
	}

	@Test
	void acceptsAVcardWithoutAnAddress()
	{
		VCard vcard = parse("BEGIN:VCARD", "VERSION:3.0", "N:Jones;Carol;;;", "FN:Carol Jones",
				"EMAIL:carol@example.com", "END:VCARD");
		mcContact carol = mcContact.getContact("3");
		assertDoesNotThrow(() -> carol.importVcard(vcard));
	}
}
