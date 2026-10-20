package org.lerot.mycontacts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;

// Reading the label and address template files (dom4j with jaxen XPath)
class TemplatesTest
{
	@BeforeEach
	void setUp()
	{
		MyContacts.settingsdir = TestDatabase.settingsFolder() + File.separator;
	}

	@Test
	void readsLabelLayouts()
	{
		Map<String, Map<String, String>> labels = mcPDF.readTemplates();
		assertNotNull(labels);
		assertEquals("3", labels.get("3by7").get("ncols"));
		assertEquals("7", labels.get("3by7").get("nrows"));
	}

	@Test
	void readsAddressFormats()
	{
		Map<String, Vector<String>> addresses = new mcAddressDataType().readTemplates();
		assertNotNull(addresses);
		assertTrue(addresses.containsKey("UK"), addresses.keySet().toString());
		assertTrue(addresses.get("UK").contains("POSTCODE"));
	}
}
