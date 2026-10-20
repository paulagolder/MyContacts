package org.lerot.mycontacts;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

// Names and addresses are stored as { key:'value', ... }; values containing
// apostrophes or quotes must survive being saved and read back
class QuotesTest
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

	static Map<String, String> address(String street)
	{
		Map<String, String> m = new LinkedHashMap<>();
		m.put("street", street);
		m.put("city", "Testtown");
		m.put("postcode", "TT1 1AA");
		return m;
	}

	static mcKeyValueDataType addressType()
	{
		return (mcKeyValueDataType) mcDataTypes.getType("address");
	}

	// the edit panel's writer
	@ParameterizedTest
	@ValueSource(strings = { "1 High Street", "St John's Road", "The \"Old\" Mill", "Rose's \"Cottage\"",
		"'Twas Cottage", "Flat 2, Rose's House", "Flat 2; Rose's House", "C:\\path\\name", "It's \\ \"odd\"" })
	void editPanelWriterRoundTrips(String street)
	{
		String stored = mcUtilities.keyvaluesmaptoArrayString(address(street), "=");
		Map<String, String> back = addressType().getKeyValueMap(stored);
		assertEquals(street, back.get("street"), stored);
		assertEquals("Testtown", back.get("city"), stored);
		assertEquals("TT1 1AA", back.get("postcode"), stored);
	}

	// both readers: mcUtilities.parseArray is used by other parts of the app
	@ParameterizedTest
	@ValueSource(strings = { "1 High Street", "St John's Road", "The \"Old\" Mill", "Rose's \"Cottage\"",
		"'Twas Cottage", "Flat 2, Rose's House", "C:\\path\\name" })
	void otherReaderRoundTrips(String street)
	{
		String stored = mcUtilities.keyvaluesmaptoArrayString(address(street), ":");
		Map<String, String> back = mcUtilities.parseArray(stored);
		assertEquals(street, back.get("street"), stored);
		assertEquals("Testtown", back.get("city"), stored);
	}

	// values as already stored (by the backup import) read as before
	@ParameterizedTest
	@ValueSource(strings = { " { street:'1 High Street' , city:'Testtown' , postcode:'TT1 1AA' ,  }",
		"{street=1 High Street, city=Testtown, postcode=TT1 1AA, }",
		" { street:\"St John's Road\" , city:'Testtown' , postcode:'TT1 1AA' ,  }" })
	void storedValuesStillRead(String stored)
	{
		Map<String, String> back = addressType().getKeyValueMap(stored);
		assertTrue(back.get("street").endsWith("Street") || back.get("street").equals("St John's Road"), stored);
		assertEquals("Testtown", back.get("city"), stored);
		assertEquals("TT1 1AA", back.get("postcode"), stored);
	}

	// names shown in a capitalised format ("Fn Sn") keep apostrophes, hyphens
	// and deliberate capitals
	@org.junit.jupiter.params.ParameterizedTest
	@org.junit.jupiter.params.provider.CsvSource(delimiter = '|', value = {
		"o'brien | O'Brien", "O'BRIEN | O'Brien", "O'Brien | O'Brien", "smith-jones | Smith-Jones",
		"McDonald | McDonald", "mcdonald | Mcdonald", "john and mary | John And Mary",
		"Theresa & Richard | Theresa & Richard" })
	void capitalisesNames(String stored, String shown)
	{
		assertEquals(shown, mcKeyValueDataType.capitalise(stored));
	}

	// the other writers of the same format
	@ParameterizedTest
	@ValueSource(strings = { "1 High Street", "St John's Road", "The \"Old\" Mill", "Rose's \"Cottage\"",
		"'Twas Cottage", "Flat 2, Rose's House", "Flat 2; Rose's House", "C:\\path\\name", "It's \\ \"odd\"" })
	void mapWriterRoundTrips(String street)
	{
		String stored = mcKeyValueDataType.mapToArrayString(address(street));
		Map<String, String> back = addressType().getKeyValueMap(stored);
		assertEquals(street, back.get("street"), stored);
		assertEquals("Testtown", back.get("city"), stored);
	}
}
