package org.lerot.mycontacts;

import org.lerot.mywidgets.jswLabel;

import java.util.LinkedHashMap;

public class mcImportVcard extends mcImports
{

	boolean test = true;
	jswLabel message;

	public mcImportVcard(String importfilename)
	{
		super(importfilename, "vcard");
	}

	public LinkedHashMap<String, mcImportexception> importall(boolean test,
			jswLabel inmessage)
	{
		mcImportContact lastcontact = null;
		int countupdates = 0;
		message = inmessage;
		if (message != null)
			message.setText(" importing vcard " + importfilename);

		mappings = MyContacts.topgui.currentcon.createMappings("import", "Vcard");
	//	System.out.println(" importing vcard mappings  " + mappings.size());
	//	System.out.println(" mappings " + mappings.toString());
		if (!test)
		{
			initialiseImportDataTable(importfilename, importtype);
		}
		vcardContactReader cr = new vcardContactReader(importfilename, mappings);
		if (cr != null)
		{
			try
			{
				mcImportContact nextcontact = cr.getContact();
				
				while (nextcontact != null)
				{
					lastcontact = nextcontact;
					if (message != null)
					{
						if (test)
							message.setText(" testing contact details no:"
									+ nextcontact.getRownumber());
						else
							message.setText(" loading contact details no:"
									+ nextcontact.getRownumber());
						message.repaint();
					}
					if (!test)
					{
						String imptid= nextcontact.getTID();	
						if(mcContacts.updateContactfromVcardImport(imptid,nextcontact)) countupdates ++;;
					}
					nextcontact = cr.getContact();
				}
			} catch (mcGetContactException e)
			{
				if (lastcontact != null)
					message.setText(" error after contact details no:"
							+ lastcontact.getRownumber());
			}
			if (message != null)
			{
				message.setText("loaded  " + " contacts "
						+ getCurrentposition());
				message.repaint();
				System.out.println(" Loaded... " + cr.contactnumber);
			}
		}
		System.out.println(" returning... " + cr.getExceptions().size());
		System.out.println(" updated... " +countupdates);
		return cr.getExceptions();
	}

}
