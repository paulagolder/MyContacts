package org.lerot.mycontacts.lite;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * What myContactsLite shows of one contact: its name, then the plain
 * (unqualified) details of the types ticked for Lite in Tools > Other >
 * Attribute Types, in their display order, ready to display. A contact with
 * no address of its own shows that of a group it belongs to, and addressFrom
 * names the group.
 */
public record ContactCard(int id, String listName, String name, String addressFrom, List<Detail> details)
{
	// one detail: its type's key, label and kind of data, and the text shown
	public record Detail(String key, String label, String datatype, String text) {}

	// the text of the detail of this type, or null if the card has none
	public String value(String key)
	{
		for (Detail d : details)
			if (d.key().equals(key)) return d.text();
		return null;
	}

	public String address() { return value("address"); }
	public String phone() { return value("phone"); }
	public String mobile() { return value("mobile"); }
	public String whatsapp() { return value("whatsapp"); }
	public String email() { return value("email"); }
	public String birthday() { return value("birthday"); }

	// all the text of the card, lower case, for searching
	public String searchText()
	{
		return Stream.concat(Stream.of(listName, name), details.stream().map(Detail::text))
				.filter(s -> s != null)
				.map(s -> s.toLowerCase(Locale.ROOT))
				.collect(Collectors.joining("\n"));
	}

	// true if every word of the query appears somewhere on the card
	public boolean matches(String query)
	{
		String text = searchText();
		for (String word : query.toLowerCase(Locale.ROOT).trim().split("\\s+"))
			if (!word.isEmpty() && !text.contains(word)) return false;
		return true;
	}
}
