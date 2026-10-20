package org.lerot.mycontacts.lite;

import org.lerot.mycontacts.*;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/*
 * The contacts, read from the database through a read-only connection (so
 * myContactsLite can never change or lock it), turned into ContactCards.
 * Reloads when the database file has changed, e.g. after editing in the
 * main app.
 */
public class ContactStore
{
	private static final DateTimeFormatter DAYMONTHYEAR = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK);
	private static final DateTimeFormatter DAYMONTH = DateTimeFormatter.ofPattern("d MMMM", Locale.UK);

	private final File database;
	private final mcDataSource datasource;
	private List<ContactCard> cards = List.of();
	private long loadedVersion = -1;
	private boolean typesLoaded;

	public ContactStore(String databasePath)
	{
		database = new File(databasePath);
		datasource = new mcDataSource(databasePath, true);
	}

	// load the contacts if the database has changed since last time;
	// true if it did
	public boolean refresh()
	{
		long version = database.lastModified();
		if (version == loadedVersion) return false;
		load();
		loadedVersion = version;
		return true;
	}

	public List<ContactCard> all()
	{
		return cards;
	}

	public List<ContactCard> search(String query)
	{
		if (query == null || query.isBlank()) return cards;
		return cards.stream().filter(c -> c.matches(query)).toList();
	}

	public Optional<ContactCard> find(int id)
	{
		return cards.stream().filter(c -> c.id() == id).findFirst();
	}

	public void close()
	{
		datasource.close();
	}

	private void load()
	{
		mcDataObject.useDataSource(datasource);
		if (!typesLoaded)
		{
			mcDataTypes.loadTypes();
			mcAttributeTypes.loadAttributeTypes();
			typesLoaded = true;
		}
		List<ContactCard> loaded = new ArrayList<>();
		for (Map<String, String> row : mcDataObject.doQuery(
				"select distinct cid from attributeValues where root = 'tid'"))
		{
			mcContact contact = mcContact.getContact(row.get("cid"));
			if (contact != null) loaded.add(card(contact));
		}
		loaded.sort(Comparator.comparing(ContactCard::listName, String.CASE_INSENSITIVE_ORDER));
		cards = List.copyOf(loaded);
	}

	static ContactCard card(mcContact c)
	{
		// the stored tid ("Surname  Forename"); getTID() is not filled in by loading
		String listName = plain(c, "tid");
		String name = tidy(c.getName("Title Fn Mn Sn "));
		if (name == null) name = listName;
		return new ContactCard(c.getCID(), listName != null ? listName : name, name, address(c),
				plain(c, "phone"), plain(c, "mobile"), plain(c, "email"), birthday(c));
	}

	// the value of the plain attribute (no qualifier such as "old" or "alt1")
	private static String plain(mcContact c, String root)
	{
		mcAttribute att = c.getAttribute(root, "");
		return att == null ? null : tidy(att.getValue());
	}

	private static String address(mcContact c)
	{
		mcAttribute att = c.getAttribute("address", "");
		if (att == null || att.isEmpty() || !(att.getDataType() instanceof mcAddressDataType type))
			return null;
		String block = type.getBlockFormattedValue(att.getValue(), "\n", false);
		if (block == null) return null;
		// drop blank lines and stray spaces
		StringJoiner lines = new StringJoiner("\n");
		for (String line : block.split("\n"))
			if (!line.isBlank()) lines.add(line.trim().replaceAll("\\s{2,}", " "));
		return lines.length() == 0 ? null : lines.toString();
	}

	// e.g. "2 March 1950", "2 March" (year unknown) or "1950" (only the year)
	private static String birthday(mcContact c)
	{
		mcAttribute att = c.getAttribute("birthday", "");
		if (att == null) return null;
		String ymd = att.getFormattedValue("-"); // yyyy-mm-dd, 00 where unknown
		if (ymd == null) return null;
		String[] p = ymd.split("-");
		if (p.length != 3) return tidy(ymd);
		try
		{
			int year = Integer.parseInt(p[0].trim()), month = Integer.parseInt(p[1].trim()),
					day = Integer.parseInt(p[2].trim());
			if (month >= 1 && month <= 12 && day >= 1)
			{
				if (year > 0) return LocalDate.of(year, month, day).format(DAYMONTHYEAR);
				return LocalDate.of(2000, month, day).format(DAYMONTH);
			}
			return year > 0 ? String.valueOf(year) : null;
		} catch (RuntimeException e)
		{
			return tidy(ymd);
		}
	}

	// trimmed, single-spaced, or null if empty
	private static String tidy(String s)
	{
		if (s == null) return null;
		s = s.trim().replaceAll("[ \\t]{2,}", " ");
		return s.isEmpty() || s.equalsIgnoreCase("null") ? null : s;
	}
}
