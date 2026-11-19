package org.lerot.mycontacts.gui.widgets;

import org.junit.jupiter.api.Test;
import org.lerot.mycontacts.mcContact;

import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;

// the contact drop-down at the top of the main window follows View buttons
class DropDownContactBoxTest
{
	private static mcContact contact(int id, String tid)
	{
		mcContact c = new mcContact(id);
		c.setTID(tid);
		return c;
	}

	@Test
	void findsAContactInARefilledListEvenAsAReloadedCopy()
	{
		jswDropDownContactBox box = new jswDropDownContactBox(null, "contacts");
		Vector<mcContact> list = new Vector<>();
		list.add(contact(1, "Smith Alice"));
		list.add(contact(2, "O'Brien Bob"));
		list.add(contact(3, "Jones Carol"));
		box.setList(list);
		// a copy, as reloading the contacts makes
		mcContact bob = contact(2, "O'Brien Bob");
		assertTrue(box.contains(bob));
		box.setSelected(bob);
		assertEquals(2, box.getSelectedValue().getCID());
		assertFalse(box.contains(contact(9, "Nobody")));
	}
}
