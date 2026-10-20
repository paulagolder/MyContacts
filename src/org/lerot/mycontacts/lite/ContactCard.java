package org.lerot.mycontacts.lite;

import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * What myContactsLite shows of one contact: the plain (unqualified) name,
 * address, phone, mobile, email and birthday, ready to display. Any of the
 * fields may be null.
 */
public record ContactCard(int id, String listName, String name, String address, String phone,
		String mobile, String email, String birthday)
{
	// all the text of the card, lower case, for searching
	public String searchText()
	{
		return Stream.of(listName, name, address, phone, mobile, email, birthday)
				.filter(s -> s != null)
				.map(s -> s.toLowerCase(Locale.ROOT))
				.collect(Collectors.joining("\n"));
	}

	// a second line for the contact list: how to reach them
	public String reach()
	{
		return email != null ? email : mobile != null ? mobile : phone;
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
