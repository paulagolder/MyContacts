package org.lerot.mycontacts;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

// Escaping text for XML backups (commons-lang3) and decoding
// quoted-printable vCard values (commons-codec)
class TextEncodingTest
{
	@TempDir
	Path folder;

	@Test
	void escapesSpecialCharactersForXml()
	{
		assertEquals("  <value>Tom &amp; &quot;Jerry&quot; &lt;tag&gt;</value>",
				new mcTextDataType().toXML("Tom & \"Jerry\" <tag>"));
	}

	@Test
	void decodesQuotedPrintableVcardValues() throws Exception
	{
		Path vcf = folder.resolve("qp.vcf");
		Files.writeString(vcf, String.join("\r\n",
				"BEGIN:VCARD",
				"VERSION:2.1",
				"NOTE;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:Caf=C3=A9 au lait",
				"END:VCARD", ""), StandardCharsets.UTF_8);
		vcardTokenReader reader = new vcardTokenReader(vcf.toString());
		mcImporttoken note = null;
		while (reader.HasNext())
		{
			mcImporttoken t = reader.getNextToken();
			if (t != null && "NOTE".equals(t.getToken())) note = t;
		}
		assertNotNull(note, "NOTE line not found");
		assertEquals("Café au lait", note.getValue());
	}
}
