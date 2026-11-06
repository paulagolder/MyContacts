package org.lerot.mycontacts;

import org.lerot.mywidgets.*;
import org.lerot.mywidgets.jswLabel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Vector;

public class mcImportXML extends mcImports
{
	boolean test = true;
	int maxcid = 0;
	int newobjects=0;
	int changedobjects =0;
	int failedobjects = 0;
	jswLabel message;
	static String[] Nfields = { "sn", "fn", "mn", "title", "sufix" };
	static String[] ADRfields = { "pobox", "extaddr", "street", "city",
			"county", "postcode", "country" };

	public mcImportXML(String importfilename)
	{
		super(importfilename, "xml");
		
	}

	public LinkedHashMap<String, mcImportexception> importall(boolean test,
			jswLabel inmessage)
	{
		System.out.println(" import xml "+ importfilename);
		message = inmessage;
		newobjects = 0;
		changedobjects = 0;
		failedobjects = 0;
		if (message != null)
			message.setText(" importing xml " + importfilename);
		mappings = ds().createMappings("import", "XML");
		// the contacts already in the database, matched by tid
		mcContacts all = new mcContacts();
		all.refreshAllContacts();
		if (!test)
		{
		//	initialiseImportDataTable(importfilename, importtype);
		}
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		LinkedHashMap<String, mcImportexception> exceptions = new LinkedHashMap<String, mcImportexception>();
		mcAttribute anattribute = new mcAttribute(0);
		Vector<String> attkeylist = anattribute .dbloadAttributeKeyList();
		try
		{
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.parse(importfilename);
			// get the root element
			Element docEle = dom.getDocumentElement();

			// get a nodelist of elements
			NodeList nl = docEle.getElementsByTagName("contact");
			int k = 1;
			if (nl != null && nl.getLength() > 0)
			{
				for (int i = 0; i < nl.getLength(); i++)
				{
					Element el = (Element) nl.item(i);
					mcContact importedcontact = new mcContact();
					importedcontact.loadXML(el, i);
				//	String ct = icontact.toXML(attkeylist);
					// the same contact: the one with its old id, if that still
					// has its name (restoring into the same database), else
					// the one with its name
					mcContact existingcontact = all.FindbyID(importedcontact.getBackupCID());
					if (existingcontact == null || existingcontact.getTID() == null
							|| !mcUtilities.normalisename(existingcontact.getTID())
									.equals(mcUtilities.normalisename(importedcontact.getTID())))
						existingcontact = all.FindbyTID(importedcontact.getTID());
					if (existingcontact == null)
					{
						System.out.println((test ? " would make new contact :" : " make new  contact :") + importedcontact);
						if (test)
						{
							newobjects++; // test run: count it but save nothing
							continue;
						}
						// each contact is saved completely or not at all
						boolean saved = ds().transaction(() ->
						{
							mcContact newcontact = new mcContact(importedcontact);
							newcontact.insertNewContact();
							newcontact.updateContact();
							// a new contact starts tagged "newcontact"; a restored
							// one without tags should stay without
							if (importedcontact.getAttributebyKey("tags") == null)
								new mcAttribute(newcontact.getCID(), "tags", "").dbdeleteAttribute();
						});
						if (saved)
							newobjects++;
						else
							failedobjects++;
					} else
					{
	//		            System.out.println(" found "+existingcontact);
						if (importedcontact.matches(existingcontact))
						{
	//						System.out.println(" ignoring contact :"
	//						 +icontact+" no changes");
						} else
						{
							System.out.println(" importing contact :"
									 +importedcontact+" with changes");
							if (!test)
							{
								int changes = existingcontact
										.updateContact(importedcontact);
								 if(changes>0)
								 {
								 if (existingcontact.updateContact())
								 {
									 System.out.println(
											" updated contact  :" + importedcontact + " "+changes);
									 changedobjects++;
								 } else
									 failedobjects++;
								 }
							} else
								changedobjects++; // test run: differs, may need updating
							k++;
						}
					}
				}
				if (!test)
				{
					// links in the file name the linked contact; point them at
					// its id in this database
					all.refreshAllContacts();
					all.relinkcontacts();
				}
				System.out
						.println(test ? " TEST ONLY, nothing saved. New contacts: " + newobjects
								+ ", existing contacts that differ: " + changedobjects
								: " imported: " + newobjects+ " Contacts  and updated :"+changedobjects
								+ (failedobjects > 0 ? "  FAILED: " + failedobjects : ""));
			}

		} catch (ParserConfigurationException pce)
		{
			pce.printStackTrace();
		} catch (SAXException se)
		{
			se.printStackTrace();
		} catch (IOException ioe)
		{
			ioe.printStackTrace();
		}

		if (message != null)
		{
			message.setText((test ? "test only, nothing saved: " + newobjects + " new, "
					+ changedobjects + " existing contacts differ"
					: "loaded  " + " contacts " + getCurrentposition())
					+ (failedobjects > 0 ? "  (" + failedobjects + " failed, see console)" : ""));
			message.repaint();
		}
		return exceptions;
	}

}
