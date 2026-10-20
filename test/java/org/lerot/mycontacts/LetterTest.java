package org.lerot.mycontacts;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.odftoolkit.simple.TextDocument;
import org.w3c.dom.Element;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

// Filling in a letter template (.odt, simple-odf / odfdom)
class LetterTest
{
	static final String TEXT_NS = "urn:oasis:names:tc:opendocument:xmlns:text:1.0";

	@TempDir
	Path folder;

	@Test
	void fillsTemplateVariables() throws Exception
	{
		// a template with one variable, "salutation", as LibreOffice saves it
		File template = folder.resolve("template.odt").toFile();
		TextDocument doc = TextDocument.newTextDocument();
		Element para = doc.getContentDom().createElementNS(TEXT_NS, "text:p");
		Element var = doc.getContentDom().createElementNS(TEXT_NS, "text:variable-set");
		var.setAttributeNS(TEXT_NS, "text:name", "salutation");
		var.appendChild(doc.getContentDom().createTextNode("placeholder"));
		para.appendChild(var);
		doc.getContentRoot().appendChild(para);
		doc.save(template);
		doc.close();

		File output = folder.resolve("letter.odt").toFile();
		mcLetter letter = new mcLetter();
		letter.setTemplateFileName(template.getPath());
		letter.setOutputFileName(output.getPath());
		letter.setVariable("salutation", "Dear Alice");
		letter.printLetter();

		assertTrue(output.exists(), "letter was not written");
		TextDocument result = TextDocument.loadDocument(output);
		String text = result.getContentRoot().getTextContent();
		result.close();
		assertTrue(text.contains("Dear Alice"), text);
		assertFalse(text.contains("placeholder"), text);
	}
}
