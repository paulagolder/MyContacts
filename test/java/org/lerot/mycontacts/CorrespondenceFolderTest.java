package org.lerot.mycontacts;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Where letters and labels are saved (mcSettings)
class CorrespondenceFolderTest
{
	@Test
	void contactFileNameHasNoSpacesOrPunctuation()
	{
		mcContact c = new mcContact();
		c.setTID("O'Brien Mary & Jo");
		assertEquals("OBrienMaryJo", mcSettings.fileName(c));
	}

	@Test
	void contactFolderIsInsideTheCorrespondenceFolder()
	{
		MyContacts.docsfolder = "/home/someone/Documents/MyCorrespondance";
		mcContact c = new mcContact();
		c.setTID("Golder Paul");
		assertEquals("/home/someone/Documents/MyCorrespondance/GolderPaul",
				mcSettings.contactFolder(c));
	}

	@Test
	void labelsGoInAllLabels()
	{
		assertTrue(mcSettings.labelsFolder().endsWith("/AllLabels"));
		assertTrue(mcSettings.labelsFolder().startsWith(mcSettings.correspondenceFolder()));
	}
}
