package org.lerot.mycontacts;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.parameter.AddressType;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.*;
import ezvcard.util.PartialDate;
import org.lerot.mywidgets.jswLabel;

import java.io.IOException;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.*;

/*
 * Imports a vCard file (Tools > Import, source Vcard), as exported by Google
 * Contacts, Apple Contacts, Outlook or a phone, read with ez-vcard.
 *
 * Each card becomes a new contact, or is merged into the contact of the same
 * name (in either word order): details the contact does not have yet are
 * added, and nothing it already has is overwritten. A dropped vCard is merged
 * into its contact the same way (mcContact.importVcard).
 *
 * New contacts are listed surname first ("Doe  Jane"), like the rest; a card
 * without a surname (a couple, a group, a company) keeps its own name.
 */
public class mcImportVcard extends mcImports
{
	// groups Google adds by itself, not ones the user made
	static final Set<String> SYSTEMGROUPS = Set.of("mycontacts", "my contacts", "starred", "starred in android");
	// structured details a contact has only one of; a card's is used only if the contact has none
	static final Set<String> SINGLE = Set.of("name", "birthday", "anniversary");

	int newcontacts, mergedcontacts;

	public mcImportVcard(String importfilename)
	{
		super(importfilename, "vcard");
	}

	public LinkedHashMap<String, mcImportexception> importall(boolean test, jswLabel message)
	{
		LinkedHashMap<String, mcImportexception> exceptions = new LinkedHashMap<>();
		newcontacts = 0;
		mergedcontacts = 0;
		List<VCard> cards;
		try
		{
			cards = Ezvcard.parse(java.nio.file.Path.of(importfilename)).all();
		} catch (IOException e)
		{
			if (message != null) message.setText("cannot read " + importfilename + " : " + e.getMessage());
			return exceptions;
		}
		mcContacts all = new mcContacts();
		all.refreshAllContacts();
		for (VCard card : cards)
		{
			noteUnused(card, exceptions);
			List<String[]> details = details(card);
			String tid = listName(card);
			if (tid == null) continue;
			mcContact existing = all.FindbyTID(tid);
			if (existing == null && card.getFormattedName() != null)
				existing = all.FindbyTID(card.getFormattedName().getValue());
			if (test)
			{
				if (existing == null) newcontacts++;
				else mergedcontacts++;
				continue;
			}
			mcContact contact = existing;
			boolean saved = ds().transaction(() ->
			{
				if (contact == null) all.put(create(tid, details));
				else merge(contact, details);
			});
			if (saved && existing == null) newcontacts++;
			else if (saved) mergedcontacts++;
		}
		String result = (test ? "test only, nothing saved: " : "") + newcontacts + " new contacts, "
				+ mergedcontacts + (test ? " to merge with" : " merged into") + " existing ones";
		System.out.println(" vcard import " + importfilename + ": " + result);
		if (message != null)
		{
			message.setText(result);
			message.repaint();
		}
		return exceptions;
	}

	// a dropped vCard, merged into this contact
	public static void mergeInto(mcContact contact, VCard card)
	{
		ds().transaction(() -> merge(contact, details(card)));
	}

	// ---- from a card to MyContacts details: {root, qualifier, value} ----

	static List<String[]> details(VCard card)
	{
		List<String[]> details = new ArrayList<>();
		Map<String, String> labels = labels(card);
		StructuredName n = card.getStructuredName();
		if (n != null)
		{
			String[] given = given(n);
			Map<String, String> name = new LinkedHashMap<>();
			name.put("fn", given[0]);
			name.put("sn", n.getFamily());
			name.put("mn", given[1]);
			name.put("title", String.join(" ", n.getPrefixes()));
			name.put("sufix", String.join(" ", n.getSuffixes()));
			add(details, "name", "", fields(name));
		}
		for (Email email : card.getEmails())
			add(details, "email", qualifier(email, email.getTypes().contains(EmailType.HOME),
					email.getTypes().contains(EmailType.WORK), labels), email.getValue());
		for (Telephone tel : card.getTelephoneNumbers())
		{
			String number = tel.getText() != null ? tel.getText() : tel.getUri() != null ? tel.getUri().getNumber() : null;
			List<TelephoneType> types = tel.getTypes();
			String root = types.contains(TelephoneType.CELL) ? "mobile" : types.contains(TelephoneType.FAX) ? "fax" : "phone";
			String qual = root.equals("phone") ? qualifier(tel, types.contains(TelephoneType.HOME),
					types.contains(TelephoneType.WORK), labels) : "";
			add(details, root, qual, number);
		}
		for (Address adr : card.getAddresses())
		{
			Map<String, String> address = new LinkedHashMap<>();
			address.put("pobox", adr.getPoBox());
			address.put("extaddr", adr.getExtendedAddress());
			address.put("street", adr.getStreetAddress() == null ? null : adr.getStreetAddress().replaceAll("\\s*\\n\\s*", ", "));
			address.put("city", adr.getLocality());
			address.put("county", adr.getRegion());
			address.put("postcode", adr.getPostalCode());
			address.put("country", adr.getCountry());
			// a home address is the contact's main one
			String qual = qualifier(adr, false, adr.getTypes().contains(AddressType.WORK), labels);
			add(details, "address", qual, fields(address));
		}
		if (card.getBirthday() != null)
			add(details, "birthday", "", date(card.getBirthday().getDate(), card.getBirthday().getPartialDate()));
		if (card.getAnniversary() != null)
			add(details, "anniversary", "", date(card.getAnniversary().getDate(), card.getAnniversary().getPartialDate()));
		for (Title title : card.getTitles())
			add(details, "position", "", title.getValue());
		for (Organization org : card.getOrganizations())
			add(details, "department", "", String.join(", ", org.getValues()));
		for (Url url : card.getUrls())
			add(details, "webpage", "", url.getValue());
		for (Note note : card.getNotes())
			add(details, "note", "", note.getValue());
		Set<String> tags = new TreeSet<>();
		for (Categories categories : card.getCategoriesList())
			for (String tag : categories.getValues())
				if (!tag.isBlank() && !SYSTEMGROUPS.contains(tag.trim().toLowerCase()))
					tags.add(tag.trim().toLowerCase());
		if (!tags.isEmpty()) add(details, "tags", "", String.join(";", tags) + ";");
		return details;
	}

	// "Surname  Forename"; a card without a surname keeps its own name
	static String listName(VCard card)
	{
		StructuredName n = card.getStructuredName();
		String family = n == null ? null : blankToNull(n.getFamily());
		String given = n == null ? null : blankToNull(given(n)[0]);
		if (family != null && given != null) return family + "  " + given;
		if (card.getFormattedName() != null && blankToNull(card.getFormattedName().getValue()) != null)
			return card.getFormattedName().getValue().trim();
		if (family != null) return family;
		if (given != null) return given;
		for (Organization org : card.getOrganizations())
			if (!org.getValues().isEmpty()) return org.getValues().get(0);
		return null;
	}

	// forename and middle names; a couple saved as "Theresa &" with "Richard"
	// as a middle name (as Google does) is one forename, "Theresa & Richard"
	static String[] given(StructuredName n)
	{
		String given = n.getGiven() == null ? "" : n.getGiven().trim();
		String middle = String.join(" ", n.getAdditionalNames()).trim();
		if (!middle.isEmpty() && given.matches("(?i).*(\\s|^)(&|and|et)$"))
			return new String[] { given + " " + middle, "" };
		return new String[] { given, middle };
	}

	// Google's custom labels: "item1.EMAIL:..." with "item1.X-ABLabel:Holiday"
	private static Map<String, String> labels(VCard card)
	{
		Map<String, String> labels = new HashMap<>();
		for (RawProperty label : card.getExtendedProperties("X-ABLABEL"))
			if (label.getGroup() != null && label.getValue() != null)
				labels.put(label.getGroup().toLowerCase(), label.getValue().replaceAll("[_$!<>]", "").trim().toLowerCase());
		return labels;
	}

	private static String qualifier(VCardProperty p, boolean home, boolean work, Map<String, String> labels)
	{
		if (p.getGroup() != null && labels.containsKey(p.getGroup().toLowerCase()))
			return labels.get(p.getGroup().toLowerCase());
		if (work) return "work";
		if (home && !(p instanceof Address)) return "home";
		return "";
	}

	// a date, or one without a year ("--11-03"), as {year, month, day}
	private static String date(Temporal date, PartialDate partial)
	{
		Map<String, String> fields = new LinkedHashMap<>();
		if (date != null)
		{
			if (date.isSupported(ChronoField.YEAR)) fields.put("year", String.valueOf(date.get(ChronoField.YEAR)));
			if (date.isSupported(ChronoField.MONTH_OF_YEAR)) fields.put("month", String.valueOf(date.get(ChronoField.MONTH_OF_YEAR)));
			if (date.isSupported(ChronoField.DAY_OF_MONTH)) fields.put("day", String.valueOf(date.get(ChronoField.DAY_OF_MONTH)));
		} else if (partial != null)
		{
			if (partial.getYear() != null) fields.put("year", String.valueOf(partial.getYear()));
			if (partial.getMonth() != null) fields.put("month", String.valueOf(partial.getMonth()));
			if (partial.getDate() != null) fields.put("day", String.valueOf(partial.getDate()));
		}
		return fields(fields);
	}

	// "{ fn:'Jane' , sn:'Doe' ,  }", the stored form of a structured value
	private static String fields(Map<String, String> fields)
	{
		String out = "{ ";
		for (Map.Entry<String, String> field : fields.entrySet())
			if (blankToNull(field.getValue()) != null)
				out += field.getKey() + ":" + mcUtilities.quoteValue(field.getValue().trim()) + " , ";
		return out.equals("{ ") ? null : out + " }";
	}

	private static void add(List<String[]> details, String root, String qualifier, String value)
	{
		if (blankToNull(value) != null) details.add(new String[] { root, qualifier, value.trim() });
	}

	private static String blankToNull(String s)
	{
		return s == null || s.isBlank() ? null : s;
	}

	// properties not imported, listed by Test Import
	private static void noteUnused(VCard card, Map<String, mcImportexception> exceptions)
	{
		for (VCardProperty p : card.getProperties())
		{
			String name = p instanceof RawProperty raw ? raw.getPropertyName().toUpperCase()
					: p.getClass().getSimpleName().toUpperCase();
			if (USED.contains(name)) continue;
			mcImportexception e = exceptions.computeIfAbsent(name, k -> new mcImportexception(0, String.valueOf(p), k));
			e.setCount(e.getCount() + 1);
		}
	}

	static final Set<String> USED = Set.of("STRUCTUREDNAME", "FORMATTEDNAME", "EMAIL", "TELEPHONE", "ADDRESS",
			"BIRTHDAY", "ANNIVERSARY", "TITLE", "ORGANIZATION", "URL", "NOTE", "CATEGORIES", "X-ABLABEL",
			"PRODUCTID", "REVISION", "UID", "KIND");

	// ---- into the database ----

	// a new contact with these details
	private static mcContact create(String tid, List<String[]> details)
	{
		mcContact contact = new mcContact();
		contact.setTID(tid);
		contact.insertNewContact();
		contact.fillContact();
		// a new contact starts with its name as plain text and tagged "newcontact"
		boolean tagged = false;
		for (String[] d : details)
		{
			if (d[0].equals("name") || d[0].equals("tags"))
			{
				mcAttribute att = contact.updateAttribute(d[0], "", d[2]);
				att.dbupsertAttribute();
				tagged |= d[0].equals("tags");
			} else
				insert(contact, d);
		}
		if (!tagged) new mcAttribute(contact.getCID(), "tags", "").dbdeleteAttribute();
		return contact;
	}

	// add the details the contact does not have yet
	private static void merge(mcContact contact, List<String[]> details)
	{
		contact.fillContact();
		for (String[] d : details)
		{
			String root = d[0], qual = d[1], value = d[2];
			mcAttributes same = contact.getAttributes().findAllbyRoot(root);
			if (root.equals("tags"))
			{
				mcAttribute tags = contact.getAttribute("tags", "");
				Set<String> merged = new TreeSet<>();
				if (tags != null) merged.addAll(mcTagListDataType.getTags(tags));
				int before = merged.size();
				merged.addAll(Arrays.asList(value.split(";")));
				merged.remove("");
				if (merged.size() > before) contact.updateAttribute("tags", "", String.join(";", merged) + ";").dbupsertAttribute();
				continue;
			}
			if (has(same, value)) continue;
			mcAttribute empty = contact.getAttribute(root, qual);
			if (empty != null && isEmpty(empty.getValue()))
			{
				empty.getAttributevalue().setValue(value, "now");
				empty.dbupsertAttribute();
			} else if (!(SINGLE.contains(root) && hasAny(same)))
				insert(contact, d);
		}
	}

	private static void insert(mcContact contact, String[] d)
	{
		mcAttribute att = contact.createAttribute(d[0], d[1], true);
		att.getAttributevalue().setValue(d[2], "now");
		att.dbinsertAttribute();
	}

	private static boolean hasAny(mcAttributes atts)
	{
		for (Map.Entry<String, mcAttribute> e : atts.entrySet())
			if (!isEmpty(e.getValue().getValue())) return true;
		return false;
	}

	// the contact already has this value (phone numbers by their digits,
	// structured values by their fields)
	private static boolean has(mcAttributes atts, String value)
	{
		for (Map.Entry<String, mcAttribute> e : atts.entrySet())
			if (same(e.getValue().getValue(), value)) return true;
		return false;
	}

	static boolean same(String a, String b)
	{
		if (a == null || b == null) return false;
		if (a.trim().startsWith("{") || b.trim().startsWith("{")) return structured(a).equals(structured(b));
		String da = a.replaceAll("\\D", ""), db = b.replaceAll("\\D", "");
		if (da.length() >= 9 && db.length() >= 9 && da.length() > a.length() / 2)
			return da.substring(da.length() - 9).equals(db.substring(db.length() - 9));
		return a.trim().equalsIgnoreCase(b.trim());
	}

	// the non-empty fields of "{ k:'v' , ... }", lower case
	private static Map<String, String> structured(String value)
	{
		Map<String, String> fields = new TreeMap<>();
		java.util.regex.Matcher m = java.util.regex.Pattern
				.compile("(\\w+)\\s*:\\s*(\"[^\"]*\"|'(?:[^'\\\\]|\\\\.)*'|[^,;}]*)").matcher(value);
		while (m.find())
		{
			String v = m.group(2).trim();
			if (v.length() >= 2 && (v.startsWith("'") || v.startsWith("\""))) v = v.substring(1, v.length() - 1);
			v = v.trim().toLowerCase();
			if (!v.isEmpty()) fields.put(m.group(1).toLowerCase(), v.replaceFirst("^0+(?=\\d)", ""));
		}
		return fields;
	}

	private static boolean isEmpty(String value)
	{
		return value == null || value.isBlank() || value.equals("null") || value.trim().equals("{ }")
				|| structured(value).isEmpty() && value.trim().startsWith("{");
	}
}
