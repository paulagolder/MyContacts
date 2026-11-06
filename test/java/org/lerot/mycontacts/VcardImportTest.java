package org.lerot.mycontacts;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

// Importing a vCard file (Tools > Import, source Vcard) as Google Contacts
// exports it ("vCard for iOS Contacts")
class VcardImportTest
{
	@TempDir
	Path folder;
	mcDataSource ds;
	String file;

	static final String GOOGLE = String.join("\r\n",
			"BEGIN:VCARD", "VERSION:3.0", "FN:Jane Doe", "N:Doe;Jane;;;",
			"EMAIL;TYPE=INTERNET;TYPE=HOME:jane.doe@example.com",
			"EMAIL;TYPE=INTERNET;TYPE=WORK:jane@work.example.com",
			"TEL;TYPE=CELL:07700 900456", "TEL;TYPE=HOME:0117 496 0123",
			"ADR;TYPE=HOME:;;12 Oak Road;Bristol;;BS1 4AA;United Kingdom",
			"item1.ADR:;;5 Mill Lane\\nFlat 2;Bath;;BA1 1AA;United Kingdom", "item1.X-ABLabel:Holiday",
			"BDAY:1980-05-17", "ORG:Acme Ltd", "TITLE:Engineer", "NOTE:Met at the sailing club",
			"CATEGORIES:myContacts,Family", "END:VCARD",
			"BEGIN:VCARD", "VERSION:3.0", "FN:Seán O'Neill", "N:O'Neill;Seán;;;",
			"TEL;TYPE=CELL:+353 87 123 4567", "BDAY:--11-03", "CATEGORIES:myContacts", "END:VCARD",
			"BEGIN:VCARD", "VERSION:3.0", "FN:Alice Smith", "N:Smith;Alice;;;",
			"TEL;TYPE=CELL:+447700900123", "EMAIL;TYPE=INTERNET:alice.new@example.com",
			"CATEGORIES:myContacts,Friends", "END:VCARD",
			"BEGIN:VCARD", "VERSION:3.0", "FN:Theresa & Richard Allan", "N:Allan;Theresa &;Richard;;", "END:VCARD", "");

	@BeforeEach
	void setUp() throws Exception
	{
		ds = TestDatabase.create(folder);
		file = folder.resolve("contacts.vcf").toString();
		Files.writeString(Path.of(file), GOOGLE, StandardCharsets.UTF_8);
	}

	@AfterEach
	void tearDown()
	{
		ds.close();
	}

	@Test
	void importsANewContactWithAllItsDetails()
	{
		new mcImportVcard(file).importall(false, null);
		int jane = cid("Doe  Jane");
		assertEquals(List.of(
				"address/ = { street:'12 Oak Road' , city:'Bristol' , postcode:'BS1 4AA' , country:'United Kingdom' ,  }",
				"address/holiday = { street:'5 Mill Lane, Flat 2' , city:'Bath' , postcode:'BA1 1AA' , country:'United Kingdom' ,  }",
				"birthday/ = { year:'1980' , month:'5' , day:'17' ,  }",
				"department/ = Acme Ltd",
				"email/home = jane.doe@example.com",
				"email/work = jane@work.example.com",
				"mobile/ = 07700 900456",
				"name/ = { fn:'Jane' , sn:'Doe' ,  }",
				"note/ = Met at the sailing club",
				"phone/home = 0117 496 0123",
				"position/ = Engineer",
				"tags/ = family;",
				"tid/ = Doe  Jane"), details(jane));
	}

	@Test
	void keepsABirthdayWithoutAYear()
	{
		new mcImportVcard(file).importall(false, null);
		int sean = cid("O'Neill  Seán");
		assertTrue(details(sean).contains("birthday/ = { month:'11' , day:'3' ,  }"), details(sean).toString());
		// Google's own group is not a tag
		assertFalse(details(sean).stream().anyMatch(d -> d.startsWith("tags/")), details(sean).toString());
	}

	@Test
	void keepsBothNamesOfACouple()
	{
		new mcImportVcard(file).importall(false, null);
		int couple = cid("Allan  Theresa & Richard");
		assertTrue(details(couple).contains("name/ = { fn:'Theresa & Richard' , sn:'Allan' ,  }"), details(couple).toString());
	}

	@Test
	void mergesIntoAnExistingContactWithoutOverwriting()
	{
		new mcImportVcard(file).importall(false, null);
		List<String> alice = details(1);
		assertTrue(alice.contains("email/ = alice@example.com"), alice.toString());
		assertTrue(alice.stream().anyMatch(d -> d.startsWith("email/") && d.endsWith("alice.new@example.com")), alice.toString());
		assertEquals(1, alice.stream().filter(d -> d.startsWith("mobile/")).count(), alice.toString());
		assertTrue(alice.contains("tid/ = Smith  Alice"), alice.toString());
		assertTrue(alice.contains("tags/ = friends;"), alice.toString());
		assertEquals(6, contacts());
	}

	@Test
	void importingTheSameFileAgainChangesNothing()
	{
		new mcImportVcard(file).importall(false, null);
		List<String> before = everything();
		new mcImportVcard(file).importall(false, null);
		assertEquals(before, everything());
	}

	@Test
	void testImportSavesNothing()
	{
		List<String> before = everything();
		new mcImportVcard(file).importall(true, null);
		assertEquals(before, everything());
	}

	private static int cid(String tid)
	{
		return Integer.parseInt(TestDatabase.value("select cid from attributeValues where root = 'tid' and value = ?", tid));
	}

	private static int contacts()
	{
		return Integer.parseInt(TestDatabase.value("select count(distinct cid) from attributeValues"));
	}

	private static List<String> details(int cid)
	{
		TreeSet<String> out = new TreeSet<>();
		for (Map<String, String> row : mcDataObject.doQuery(
				"select root, qualifier, value from attributeValues where cid = ?", cid))
			out.add(row.get("root") + "/" + row.get("qualifier") + " = " + row.get("value"));
		return List.copyOf(out);
	}

	private static List<String> everything()
	{
		TreeSet<String> out = new TreeSet<>();
		for (Map<String, String> row : mcDataObject.doQuery("select cid, root, qualifier, value from attributeValues"))
			out.add(row.get("cid") + " " + row.get("root") + "/" + row.get("qualifier") + " = " + row.get("value"));
		return List.copyOf(out);
	}
}
