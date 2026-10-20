package org.lerot.mycontacts;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

// Printing address labels to PDF (iText)
class PdfLabelsTest
{
	@TempDir
	Path folder;

	@Test
	void printsAnAddressLabel() throws Exception
	{
		MyContacts.settingsdir = TestDatabase.settingsFolder() + File.separator;
		MyContacts.labeltemplates = mcPDF.readTemplates();
		File out = folder.resolve("labels.pdf").toFile();

		mcPDF pdf = new mcPDF(out, "test labels");
		pdf.setLayout("3by7");
		pdf.makeLabelPage("Alice Smith\n1 High Street\nTesttown TT1 1AA", 1);

		PdfReader reader = new PdfReader(out.getPath());
		try
		{
			assertTrue(reader.getNumberOfPages() >= 1);
			String text = PdfTextExtractor.getTextFromPage(reader, reader.getNumberOfPages());
			assertTrue(text.contains("1 High Street"), text);
		} finally
		{
			reader.close();
		}
	}
}
