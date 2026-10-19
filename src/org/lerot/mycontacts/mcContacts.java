package org.lerot.mycontacts;

import org.lerot.mywidgets.jswCheckbox;

import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

public class mcContacts extends mcDataObject
{


	public static mcContacts createList(TreeSet<mcContact> searchresults)
	{
		mcContacts newlist = new mcContacts();
		for (mcContact entry : searchresults)
		{
			newlist.put(entry);
		}
		return newlist;
	}

	public mcContacts createCopy()
	{
		mcContacts newlist = new mcContacts();
		for (Entry<String, mcContact> entry : contactlist.entrySet())
		{
			newlist.put(entry.getValue());
		}
		return newlist;
	}
	
	public static mcContact createNewContact()
	{
		mcContact newcontact = new mcContact();
		newcontact.setTID("new contact");
		newcontact.setTags("newcontact");
		newcontact.setUpdate();
		int nid = newcontact.insertNewContact();
		newcontact.setID(nid);
		MyContacts.selbox.put(newcontact);
		return newcontact;
	}

	public  int getNewID()
	{
		PreparedStatement st;
		String query = "SELECT max(cid) as ncid  FROM attributeValues";
		int ncid = -1;
		try
		{
			getConnection();
			st = con.prepareStatement(query);
			ResultSet resset = st.executeQuery();

			if (resset.next())
			{
				ncid = resset.getInt("ncid") + 1;

			}
			st.close();
			disconnect();
			return ncid;
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		return -1;
	}

	public static boolean updateContactfromVcardImport(String imptid, mcImportContact impcontact)
	{
		mcContact foundcontact = MyContacts.selbox.getAllcontactlist()
				.FindbyTID(imptid);
		boolean isupdated =false;
		if (foundcontact != null)
		{
			foundcontact.refreshAttributes();
			isupdated = foundcontact.updateFromVcardImport(impcontact);
			if(isupdated)
			{
				System.out.println("updated " + foundcontact.toString());
			    foundcontact.updateContact();
			}
		} else
		{
			System.out.println("create " + imptid);
			mcContact newcontact = new mcContact(impcontact);
			newcontact.updateContact();
			isupdated= true;
		}
		return isupdated;

	}

	private TreeMap<String, mcContact> contactlist;

	private Vector<String> groupfilter;

	private String textfilter = "";

	public boolean textfiltererror;

	private String browsefilter;

	public mcContacts()
	{
		contactlist = new TreeMap<String, mcContact>(new MyContactComparator());
		groupfilter = new Vector<String>();
	}

	private void addTags(Map<String, Integer> taglist, mcContact acontact)
	{
		Set<String> tags = acontact.getTagList();
		if (tags != null)
		{
			for (String atag : tags)
			{
				if (taglist.containsKey(atag))
				{
					int count = taglist.get(atag);
					taglist.put(atag, count + 1);
				} else
					taglist.put(atag, 1);
			}
		}
	}

	public void addtoGroupfilter(String filter)
	{
		groupfilter.add(filter);
	}

	

	public void clear()
	{
		contactlist.clear();
	}

	public void clearError()
	{
		textfiltererror = false;
	}

	public void clearGroupfilter()
	{
		groupfilter.removeAllElements();
	}

	public void clearTextfilter()
	{
		textfilter = "";
	}

	public void delete(mcContact acontact)
	{
		if (contactlist.containsKey(acontact.getIDstr()))
		{
			contactlist.remove(acontact.getIDstr());
			acontact.deleteContact();
		}

	}

	public Set<Entry<String, mcContact>> entrySet()
	{
		return contactlist.entrySet();
	}

	
	

	public mcContact findAddress(String sname)
	{

		String normaddress = mcUtilities.normaliseaddress(sname);
		mcContact acontact;
		for (Entry<String, mcContact> entry : entrySet())
		{
			acontact = entry.getValue();
			Vector<String> addresses = acontact.getAttributeValues("address");
			for (String address : addresses)
			{
				if (mcUtilities.normaliseaddress(address)
						.equals(normaddress)) { return acontact; }
			}
		}
		return null;
	}

	public mcContact FindbyID(int seekid)
	{

		for (Entry<String, mcContact> anentry : contactlist.entrySet())
		{
			mcContact acontact = anentry.getValue();
			int cid = acontact.getID();
			if (cid == seekid) return acontact;
		}
		return null;
	}
	
	public boolean isPresent(mcContact acontact)
	{
        String strid = acontact.getIDstr();
        if(contactlist.containsKey(strid)) return true;
        else
		return false;
	}

	public mcContact FindbyID(String selcontactID)
	{
		for (Entry<String, mcContact> anentry : contactlist.entrySet())
		{
			mcContact acontact = anentry.getValue();
			//String strid = acontact.getSimpleIDstr();
			String strid2 = acontact.getIDstr();
			//int id3 = acontact.getID();
			if (strid2.equalsIgnoreCase(selcontactID))
				return acontact;
		}
		return null;
	}
	
	public mcContact FindbystrID(String selcontactID)
	{
		String idstr = mcContact.makeIDstr(selcontactID);
		for (Entry<String, mcContact> anentry : contactlist.entrySet())
		{
			mcContact acontact = anentry.getValue();
			if (acontact.getIDstr().equalsIgnoreCase(idstr))
				return acontact;
		}
		return null;
	}

	public mcContact FindbyTID(String selcontactTID)
	{
		if (selcontactTID == null || selcontactTID.equals("?")) return null;
		String ntid = mcUtilities.normalisename(selcontactTID);
		for (Entry<String, mcContact> anentry : contactlist.entrySet())
		{
			mcContact acontact = anentry.getValue();
			String ctid = acontact.getTID();
			if(ctid ==  null)
			{
//				System.out.println(" not found looking for " + selcontactTID);
				return null;
			}
			
			String nctid = mcUtilities.normalisename(ctid);
			if (ctid.equalsIgnoreCase(selcontactTID)) return acontact;
			if (nctid.equalsIgnoreCase(ntid)) return acontact;
		}
		return null;
	}

	public mcContact findFilterContact(String filter)
	{
		for (Entry<String, mcContact> entry : contactlist.entrySet())
		{
			mcContact acontact = entry.getValue();
			if (acontact.TID.toLowerCase().contains(filter))
			{
				//mcContact ancontact = new mcContact(acontact.CID);
				//return ancontact;
				return acontact;
			}
		}
		return null;
	}
	
	public mcContact findFilterContact(mcContact currentcontact,String filter)
	{
		boolean foundcurrent=false;
		if(currentcontact==null) foundcurrent = true;
		for (Entry<String, mcContact> entry : contactlist.entrySet())
		{
			mcContact acontact = entry.getValue();
			if(foundcurrent)
			{
		
			if (acontact.TID.toLowerCase().contains(filter))
			{
				//mcContact ancontact = new mcContact(acontact.CID);
				//return ancontact;
				return acontact;
			}
			}
			else
			{
				if(acontact.getCID() == currentcontact.getCID())foundcurrent= true; 
			}
		}

		return currentcontact;
	}

	public mcContact findFirst()
	{
		return get(null);
	}

	public mcContact findName(String sname)
	{
		mcContact acontact = MyContacts.selbox.FindbyTID(sname);
		if (acontact != null) return acontact;
		String normname = mcUtilities.normalisename(sname);
		if (normname.isEmpty()) return null;
		acontact = MyContacts.selbox.FindbyTID(normname);
		if (acontact != null) return acontact;

		for (Entry<String, mcContact> entry : entrySet())
		{
			acontact = entry.getValue();
			Vector<String> contactvalue = acontact.getAttributeValues("name");
			if (contactvalue != null)
			{
				for (String avalue : contactvalue)
				{
					if (avalue != null)
					{
						String nvalue = mcUtilities.normalisename(avalue);
						if (nvalue.contains(normname)) { return acontact; }
					}
				}
			}
		}
		return null;
	}

	public mcContact findName2(String sname)
	{
		// System.out.println(" looking for " + sname);
		String snname = sname.toLowerCase();
		for (Entry<String, mcContact> entry : entrySet())
		{
			mcContact acontact = entry.getValue();
			Vector<String> contactvalue = acontact.getAttributeValues("name");
			if (contactvalue != null)
			{
				for (String avalue : contactvalue)
				{
					if (avalue != null)
					{
						String nvalue = avalue.toLowerCase();
						if (nvalue.contains(snname)) { return acontact; }
					}
				}
			}
		}
		return null;
	}

	public mcContact findName3(String sname)
	{
		String snname = sname.toLowerCase();
		for (Entry<String, mcContact> entry : entrySet())
		{
			mcContact acontact = entry.getValue();
			Vector<String> contactvalue = acontact.getAttributeValues("name");
			if (contactvalue != null)
			{
				for (String avalue : contactvalue)
				{
					if (avalue != null)
					{
						// check hits by parsing name
						String lcname = avalue.toLowerCase();
						int k = lcname.indexOf(snname);
						if (k > -1) return acontact;
						k = sname.indexOf(lcname);
						if (k > -1) return acontact;
					}
				}
			}
		}
		return null;
	}

	public mcContact findPhone(String attkey, String svalue)
	{
		String snvalue = mcUtilities.normalisephonenumber(svalue);
		for (Entry<String, mcContact> entry : entrySet())
		{
			mcContact acontact = entry.getValue();
			Vector<String> contactvalue = acontact.getAttributeValues(attkey);
			if (contactvalue != null)
			{
				for (String avalue : contactvalue)
				{
					if (avalue != null)
					{
						String nvalue = mcUtilities
								.normalisephonenumber(avalue);
						if (nvalue
								.equalsIgnoreCase(snvalue)) { return acontact; }
					}
				}
			}
		}
		return null;
	}

	public mcContact findValue(String attkey, String svalue)
	{
		svalue = svalue.trim().toLowerCase();
		for (Entry<String, mcContact> entry : entrySet())
		{
			mcContact acontact = entry.getValue();
			Vector<String> contactvalue = acontact.getAttributeValues(attkey);
			if (contactvalue != null)
			{
				for (String avalue : contactvalue)
				{
					if (avalue != null)
					{
						String nvalue = avalue.trim().toLowerCase();
						if (nvalue
								.equalsIgnoreCase(svalue)) { return acontact; }
					}
				}
			}
		}
		return null;
	}

	public mcContact get(String ID)
	{
		if (ID == null)
		{
			mcContact acontact = makeOrderedContactsVector().get(0);
			return acontact;
		}
		mcContact acontact = contactlist.get(ID);
		if (acontact == null) return makeOrderedContactsVector().get(0);
		return acontact;
	}

	public mcContact getContactbyTID(String key)
	{
		mcContact lastContact = null;
		for (Entry<String, mcContact> anentry : contactlist.entrySet())
		{
			mcContact acontact = anentry.getValue();
			String tid = acontact.getTID();
			if (tid.compareToIgnoreCase(key) > 0)
			{
				return lastContact;
			} else
				lastContact = acontact;
		}
		return null;
	}

	public Vector<String> getContactKeyVector()
	{

		Vector<String> contactvector = new Vector<String>();
		if (contactlist.size() > 0)
		{

			for (Entry<String, mcContact> entry : entrySet())
			{
				contactvector.add(entry.getKey());
			}
		}
		return contactvector;
	}

	public TreeMap<String, mcContact> getContactlist()
	{
		return contactlist;
	}

	public mcContact getNextContact(String key)
	{

		for (Entry<String, mcContact> anentry : contactlist.entrySet())
		{
			mcContact acontact = anentry.getValue();

			if (acontact.getIDstr()
					.compareToIgnoreCase(key) > 0) { return acontact; }
		}
		return null;
	}

	public mcContact getPreviousContact(String key)
	{
		mcContact lastContact = null;
		for (Entry<String, mcContact> anentry : contactlist.entrySet())
		{
			mcContact acontact = anentry.getValue();
			String tid = acontact.getTID();
			if (tid.compareToIgnoreCase(key) >= 0)
			{
				return lastContact;
			} else
				lastContact = acontact;
		}
		return null;
	}

	public boolean isEmpty()
	{
		return contactlist.isEmpty();
	}

	public TreeMap<String, mcContact> makeOrderedContactsMap()
	{
		TreeMap<String, mcContact> sortedcontacts = new TreeMap<String, mcContact>(
				new MyContactComparator());
		if (contactlist.size() > 0)
		{
			for (Entry<String, mcContact> entry : entrySet())
			{
				mcContact acontact = entry.getValue();
				if(acontact.getTID() != null)
				{
				sortedcontacts.put(acontact.getTID().trim(), acontact);
				}
			}
		}
		return sortedcontacts;
	}

	public Vector<mcContact> makeOrderedContactsVector()
	{
		TreeMap<String, mcContact> sortedcontacts = new TreeMap<String, mcContact>(
				new MyContactComparator());
	    Vector<mcContact> orderedcontactvector = new Vector<mcContact>();
		//mcContacts orderedcontactvector = new mcContacts();
		if (contactlist.size() > 0)
		{
			for (Entry<String, mcContact> entry : entrySet())
			{
				mcContact acontact = entry.getValue();
				sortedcontacts.put(acontact.getTID(), acontact);
			}

			for (Entry<String, mcContact> entry : sortedcontacts.entrySet())
			{
				orderedcontactvector.add(entry.getValue());
			}
		}
		return orderedcontactvector;
	}
	
	
	public mcContacts makeOrderedContacts()
	{
		TreeMap<String, mcContact> sortedcontacts = new TreeMap<String, mcContact>(
				new MyContactComparator());
	//	Vector<mcContact> orderedcontactvector = new Vector<mcContact>();
		mcContacts orderedcontactvector = new mcContacts();
		if (contactlist.size() > 0)
		{
			for (Entry<String, mcContact> entry : entrySet())
			{
				mcContact acontact = entry.getValue();
				sortedcontacts.put(acontact.getTID(), acontact);
			}

			for (Entry<String, mcContact> entry : sortedcontacts.entrySet())
			{
				orderedcontactvector.putLC(entry.getValue());
			}
		}
		return orderedcontactvector;
	}

	public void putLC(mcContact foundcontact)
	{
		put(foundcontact.getTID().toLowerCase(), foundcontact);

	}
	
	public void put(mcContact foundcontact)
	{
		put(foundcontact.getIDstr().toLowerCase(), foundcontact);

	}


	public void put(String id, mcContact acontact)
	{
		contactlist.put(id, acontact);
	}

	public void remove(mcContact acontact)
	{
		contactlist.remove(acontact.getIDstr());
	}

	public void remove(mcContacts alist)
	{
		if (alist == null) return;
		for (Entry<String, mcContact> anentry : alist.entrySet())
		{
			remove(anentry.getValue());
		}

	}

	public void printlist(String flag)
	{
		if(this.isEmpty())
		{
			System.out.println( flag+" isempty");
			return;
		}
		for (Entry<String, mcContact> anentry : entrySet())
		{
			System.out.println( flag+" "+anentry.getValue().toString());
		}

	}

	public void removeAll()
	{
		contactlist.clear();
		;
	}

	public int savecontacts(String exportfilename, String exporttype,
			Vector<String> attkeys,Vector<String> optlist)
	{
		int k = 0;
		if (exporttype.equalsIgnoreCase("ldif"))
			k = saveLdif(exportfilename, attkeys);
		else if (exporttype.equalsIgnoreCase("vcard"))
			k = saveVcard(exportfilename, attkeys,optlist);
		else if (exporttype.equalsIgnoreCase("ical"))
			k = saveIcal(exportfilename, attkeys);
		else if (exporttype.equalsIgnoreCase("xml"))
			k = saveXML(exportfilename, attkeys);
		else if (exporttype.equalsIgnoreCase("csv"))
			k = saveCSV(exportfilename, attkeys);
		else
			System.out.println(" Not recognised " + exporttype);
		return k;
	}
	
	public int backupcontacts(String exportfilename)
	{
			int k= backupXML(exportfilename);
			return k;
	}

	public int saveCSV(String exportfilename, Vector<String> attkeys)
	{

		PrintWriter printWriter;
		int k = 0;
		try
		{
			printWriter = new PrintWriter(exportfilename);
			for (Entry<String, mcContact> anentry : contactlist.entrySet())
			{
				mcContact acontact = anentry.getValue();
				mcAttributes attributes = acontact.getAttributes();
				boolean isok = false;
				for (String attkey : attkeys)
				{
					mcAttribute anattribute = attributes.get(attkey);
					if (anattribute != null
							&& !anattribute.getFormattedValue().isEmpty())
						isok = true;
				}

				if (isok)
				{
					k++;
					String name = acontact.getTID();
					printWriter.print('"' + name + '"');

					for (String attkey : attkeys)
					{
						mcAttribute anattribute = attributes.get(attkey);
					//	if (anattribute != null) printWriter.print(","
					//			+ anattribute.getKey() + ": \""
					//			+ anattribute.getFormattedValue() + "\"");
						if (anattribute != null) printWriter.print(",\""
								+ anattribute.getFormattedValue() + "\"");
					}
					printWriter.println("");
				}
			}
			printWriter.close();
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return k;

	}

	public int saveIcal(String exportfilename, Vector<String> attkeys)
	{

		mcMappings mappings = MyContacts.topgui.currentcon.createMappings("export",
				"Ical");
		PrintWriter printWriter;
		int k = 0;
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
		String tstamp = dateFormat.format(new Date());
		try
		{
			printWriter = new PrintWriter(exportfilename);
			printWriter.println("BEGIN:VCALENDAR");
			printWriter.println("PRODID:lerot.org/org.lerot.mycontacts");
			printWriter.println("VERSION:2.0");
			printWriter.println("BEGIN:VTIMEZONE");
			printWriter.println("TZID:Europe/London");
			printWriter.println("BEGIN:DAYLIGHT");
			printWriter.println("TZOFFSETFROM:+0000");
			printWriter.println("TZOFFSETTO:+0100");
			printWriter.println("TZNAME:BST");
			printWriter.println("DTSTART:19700329T010000");
			printWriter.println("RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3");
			printWriter.println("END:DAYLIGHT");
			printWriter.println("END:VTIMEZONE");

			for (Entry<String, mcContact> anentry : contactlist.entrySet())
			{
				mcContact acontact = anentry.getValue();
				mcAttributes attributes = acontact.getAttributes();
				String name = acontact.getTID();
				mcAttribute nameatt = acontact.getAttributebyKey("name");

				if (nameatt != null) name = nameatt.getFormattedValue(" ");
				name = name.replace(" & ", "_");
				name = name.replace("&", "_");
				name = name.replace(" ", "_");
				name = name.replace("__", "_");

				for (Entry<String, mcAttribute> atentry : attributes.entrySet())
				{
					mcAttribute anattribute = atentry.getValue();
					if (attkeys.contains(anattribute.getRoot()))
					{
						String bday = anattribute.getFormattedValue("");

						if (bday != null)
						{
							String year = bday.substring(0, 4);
							printWriter.println("BEGIN:VEVENT");
							printWriter.println(
									"UID:LEROT.ORG:" + name + ":" + bday);
							printWriter.println("SEQUENCE:" + year);
							printWriter.println("DTSTAMP:" + tstamp);
							printWriter.println("SUMMARY: " + "Birthday:" + name
									+ " (" + year + ")");
							printWriter.println("RRULE:FREQ=YEARLY");
							printWriter.println("CATEGORIES:Birthday ");
							printWriter.println("ATTENDEE:" + name);
							printWriter.println("ORGANIZER:" + name);
							printWriter.println("DTSTART;VALUE=DATE:" + bday);
							printWriter.println("DTEND;VALUE=DATE:" + bday);
							printWriter.println("TRANSP:TRANSPARENT");
							printWriter.println("END:VEVENT");
							printWriter.println("");
							k++;
						}
					}

				}
			}
			printWriter.println("END:VCALENDAR");
			printWriter.close();
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return k;
	}

	public int saveLdif(String exportfilename, Vector<String> attkeys)
	{

		mcMappings mappings = MyContacts.topgui.currentcon.createMappings("export",
				"Ldif");
		PrintWriter printWriter;
		int k = 0;
		try
		{
			printWriter = new PrintWriter(exportfilename);
			for (Entry<String, mcContact> anentry : contactlist.entrySet())
			{
				mcContact acontact = anentry.getValue();
				mcAttributes attributes = acontact.getAttributes();

				boolean isok = false;
				for (String attkey : attkeys)
				{
					mcAttribute anattribute = attributes.get(attkey);
					if (anattribute != null
							&& anattribute.getFormattedValue() != null
							&& !anattribute.getFormattedValue().isEmpty())
					{
						isok = true;
						continue;
					}
				}

				if (isok)
				{
					k++;
					String name = acontact.getTID();
					printWriter.println("dn: " + name);
					printWriter.println("objectClass: " + "person");
					printWriter.println("objectClass: " + "inetOrgPerson");

					mcAttribute nameatt = acontact.getAttributebyKey("name");
					if (nameatt != null) name = nameatt.getFormattedValue();

					printWriter.println("cn: " + name);
					for (String attkey : attkeys)
					{
						if (!attkey.equals("cn"))
						{
							mcAttribute anattribute = attributes.get(attkey);
							String foriegnlabel = mappings.get(attkey);
							if (foriegnlabel != null && anattribute != null)
								printWriter.println(foriegnlabel + ": "
										+ anattribute.getFormattedValue());
						}
					}
					printWriter.println("");
				}
			}
			printWriter.close();
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return k;

	}

	public int saveVcard(String exportfilename, Vector<String> attkeys,Vector<String> optlist)
	{

		mcMappings mappings = MyContacts.topgui.currentcon.createMappings("export",
				"Vcard");
		PrintWriter printWriter;
		int k = 0;
		boolean owndrive = false;
		if(optlist.size() >0) owndrive=true;
				
		try
		{
			TreeMap<String, mcContact> orderedcontactmap = this
					.makeOrderedContactsMap();
			printWriter = new PrintWriter(exportfilename);
			for (Entry<String, mcContact> anentry : orderedcontactmap
					.entrySet())
			{
				mcContact acontact = anentry.getValue();
				mcAttributes attributes = acontact.getAttributes();

				boolean isok = false;
				for (String attkey : attkeys)
				{
					mcAttribute anattribute = attributes.get(attkey);
					if (anattribute != null
							&& anattribute.getFormattedValue() != null
							&& !anattribute.getFormattedValue().isEmpty()
							&& !attkey.equalsIgnoreCase("name"))
					{
						isok = true;
						
					}
				}

				if (isok)
				{
					// System.out.println(" Printing " + acontact + " "
					// + attributes.size());
					k++;

					String vout = acontact.toVcard(attkeys, mappings,owndrive);
					printWriter.println(vout);

					printWriter.println("");
				} else
				{
					System.out.println("Not  Printing " + acontact + " "
							+ attributes.size());
				}
			}
			printWriter.close();
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return k;
	}

	public int saveXML(String exportfilename, Vector<String> attkeys)
	{

		mcMappings mappings = MyContacts.topgui.currentcon.createMappings("export",
				"XML");
		PrintWriter printWriter;
		int k = 0;
		HashMap<String, Integer> taglist = new HashMap<String, Integer>();
		try
		{
			printWriter = new PrintWriter(exportfilename);
			printWriter.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			printWriter.println(
					"<?xml-stylesheet type='text/xsl' href='./Stylesheets/ContactsStyler.xsl' ?>");
			printWriter.println("<contacts>" + "\n");
			for (Entry<String, mcContact> anentry : contactlist.entrySet())
			{
				mcContact acontact = anentry.getValue();
				mcAttributes attributes = acontact.getAttributes();

				boolean isok = false;
				for (String attkey : attkeys)
				{
					mcAttribute anattribute = attributes.get(attkey);
					if (anattribute != null
							&& anattribute.getFormattedValue() != null
			                && (anattribute.getFormattedValue().trim().length()>1)
							&& !anattribute.getFormattedValue().isEmpty()
							&& !attkey.equalsIgnoreCase("name"))
						isok = true;
				}

				if (isok)
				{
					addTags(taglist, acontact);
					// System.out.println(" Printing " + acontact + " "
					// + attributes.size());
					k++;
					acontact.getTID();
					String nameatt = acontact.toXCard(attkeys, mappings);
					if (nameatt != null) printWriter.println(nameatt);
				}
			}

			if (attkeys.contains("tag list"))
			{
				HashMap<String, Integer> sortedtags = mcUtilities
						.sortMapByValues(taglist);
				printWriter.println("<taglist>");
				for (Entry<String, Integer> anentry : sortedtags.entrySet())
				{
					String atag = anentry.getKey();
					int count = anentry.getValue();
					printWriter.println(
							"<tag key='" + atag + "' count='" + count + "'/>");
				}
				printWriter.println("</taglist>");
			}
			printWriter.println("</contacts>");
			printWriter.close();
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return k;
	}
	
	public int backupXML(String exportfilename)
	{

		mcAttribute anattribute = new mcAttribute(0);
		Vector<String> attkeylist = anattribute .dbloadAttributeKeyList();
	//	mcMappings mappings = MyContacts.topgui.currentcon.createMappings("export",
	//			"XML");
		PrintWriter printWriter;
		int k = 0;
		HashMap<String, Integer> taglist = new HashMap<String, Integer>();
		try
		{
			printWriter = new PrintWriter(exportfilename);
			printWriter.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			printWriter.println(
					"<?xml-stylesheet type='text/xsl' href='./Stylesheets/ContactsStyler.xsl' ?>");
			printWriter.println("<contacts>" + "\n");
			for (Entry<String, mcContact> anentry : contactlist.entrySet())
			{
				mcContact acontact = anentry.getValue();
				//mcAttributes attributes = acontact.getAttributes();
				
					addTags(taglist, acontact);
					// System.out.println(" Printing " + acontact + " "
					// + attributes.size());
					k++;
					acontact.getTID();
					String nameatt = acontact.toXML(attkeylist);
					if (nameatt != null) printWriter.println(nameatt);
				
			}

			// add attribute type, mappings out ? paul fix
			printWriter.println("</contacts>");
			printWriter.close();
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return k;
	}


	public mcContacts searchAttribute(String searchterm)
	{
		mcContacts found = new mcContacts();
		String ntid = mcUtilities.tidyValue(searchterm);
		System.out.println("searching "+searchterm);
		TreeSet<String> foundids = searchAttributes(searchterm);
		for (String id : foundids)
		{
			mcContact fcontact = this.FindbystrID(id);
			if (fcontact!=null)
				found.put(fcontact.getIDstr(), fcontact);
		}
		return found;
	}

	public static TreeSet<String> searchAttributes(String searchterm)
	{
		TreeSet<String> searchresults = new TreeSet<String>();
		String query = " select cid from attributeValues where value LIKE ? AND NOT ( root LIKE ? ) AND NOT ( root LIKE ? )";
		PreparedStatement st;
		try
		{
			getConnection();
			String qterm =  "%" + searchterm + "%";
			st = con.prepareStatement(query);
			st.setString(1,qterm );
			st.setString(2, "%photo%");
			st.setString(3, "%tag%");
			ResultSet resset = st.executeQuery();
			while (resset.next())
			{
				int cid = resset.getInt("cid");
				String idstr = String.valueOf(resset.getInt("cid"));
				searchresults.add(idstr);
			}
			st.close();
			return searchresults;
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		return searchresults;
	}

	

	public TreeSet<String> searchTags(String searchterm)
	{
		TreeSet<String> searchresults = new TreeSet<String>();
		String query = " select cid from attributeValues where value LIKE ? AND ( root = ? ) ";
		PreparedStatement st;
		try
		{
			getConnection();
			st = con.prepareStatement(query);
			st.setString(1, "%" + searchterm + "%");
			st.setString(2, "tags");
			ResultSet resset = st.executeQuery();
			while (resset.next())
			{
				String idstr = mcContact.makeIDstr(String.valueOf(resset.getInt("cid")));
				//public static String makeIDstr(String id)
				if (contactlist.containsKey(idstr))
				{
					searchresults.add(idstr);
				}
				//searchresults.add(idstr.toLowerCase().trim());
			}
			st.close();
			disconnect();
			return searchresults;
		} catch (SQLException e)
		{
			e.printStackTrace();
		}

		return searchresults;
	}

	public static TreeSet<String> searchAllTags(String searchterm)
	{
		TreeSet<String> searchresults = new TreeSet<String>();
		String query = " select cid from attributeValues where value LIKE ? AND ( root LIKE ? ) ";
		PreparedStatement st;
		try
		{
			getConnection();
			st = con.prepareStatement(query);
			st.setString(1, "%" + searchterm + "%");
			st.setString(2, "%tag%");
			ResultSet resset = st.executeQuery();
			while (resset.next())
			{
				String idstr = mcContact.makeIDstr(String.valueOf(resset.getInt("cid")));
				searchresults.add(idstr);
			}
			st.close();
			disconnect();
			return searchresults;
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return searchresults;
	}

	
	public void refreshAllContacts()
	{
		//System.out.println("reloading all contacts ");
		contactlist = new TreeMap<String, mcContact>(new MyContactComparator());
		String query = "drop  table if exists allcontacts";
		doExecute(query);
		doExecute(query);
		String query2 = "create table allcontacts  as SELECT  DISTINCT  t1.cid as 'cid' ,'tid' as 'tid' , 'done'  as 'tags' FROM attributeValues as t1 ; "; 
		doExecute(query2);
		String query3 = "update allcontacts  set tid = (select value from attributevalues av where allcontacts.cid = av.cid and av.root = 'tid');";
		doExecute(query3);
		String query4 = "update allcontacts  set tags = (select value from attributevalues av where allcontacts.cid = av.cid and av.root = 'tags');";
		doExecute(query4);
		//String query5 = "select * from allcontacts where not tags like '%.%' ;";
		String query5 = "select * from allcontacts where tags is null or tags not like '%.%' ;";
		 ArrayList<Map<String, String>> rows = doQuery(query5);
		   int n=0;
			for ( Map<String, String> row : rows)
			{
				mcContact acontact = new mcContact();
				String strid = row.get("cid");
				acontact.setID(Integer.parseInt(strid));
			
				if (acontact.getCID() > 0)
				{
					acontact.setTID(row.get("tid"));
					acontact.fillContact();
					contactlist.put(acontact.getIDstr(), acontact);  //paul experiment fix
				}
				n++;
			}
			//System.out.println("loaded "+n+" contacts");
	}
	
	public  static mcContact retrieveContact(int id)
	{
				mcContact acontact = new mcContact();
                acontact.setID(id);;
				acontact.fillContact();
				return acontact;
	}

	public void relinkcontacts()
	{
		for (Entry<String, mcContact> anentry : contactlist.entrySet())
		{
			mcContact acontact = anentry.getValue();
			acontact.relinkcontact();
		}

	}
	
	public static mcContact retrieveContact(String strid)
	{
		
	           int id = Integer.parseInt(strid);
				mcContact acontact = new mcContact();
                acontact.setID(id);;
				acontact.fillContact();
					return acontact;
	}
	

	public void setContactlist(TreeMap<String, mcContact> contactlist)
	{
		this.contactlist = contactlist;
	}

	public void setGroupFilter(jswCheckbox[] cb)
	{
		groupfilter.clear();
		for (jswCheckbox acb : cb)
		{
			if (acb.isSelected())
			{
				String label = acb.getLabel();
				groupfilter.add(label);
			}
		}
	}
	
	

	public void setTextfilter(String text)
	{
		textfilter = text.toLowerCase();
	}

	public int size()
	{
		return contactlist.size();
	}

	public void deleteAllContacts()
	{
		for (Entry<String, mcContact> anentry : contactlist.entrySet())
		{
			mcContact acontact = anentry.getValue();
       	acontact.deleteContact();
		}

	}

	
	

}
