package org.lerot.mycontacts.gui;

import org.lerot.mycontacts.gui.widgets.jswDropDownContactBox;
import org.lerot.mycontacts.*;
import org.lerot.mywidgets.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Vector;

public class importPanel extends jswVerticalPanel implements ActionListener
{

	private static final long serialVersionUID = 1L;

	mcImportAttributes attributes;
	private jswHorizontalPanel buttonpanel;
	mcImportContact currentimport;
	private jswCheckbox[] doupdate = new jswCheckbox[40];
	private mcContact forcedselect = null;

	mcMappings mappings;

	private boolean noaction;

	ActionListener plistener = null;

	jswDropDownContactBox selectcontact;

	private mcContact selectedcontact;

	private jswVerticalPanel showcontact;

	private mcImports imports;

	private int importrownumber;

	private int importcount;

	private boolean autoaccept;

	public importPanel()
	{
        super("import",false,false);
        imports = new mcImports();
		buttonpanel = new jswHorizontalPanel();

	}

	@Override
	public void actionPerformed(ActionEvent evt)
	{
		String action = evt.getActionCommand().toUpperCase();
		if (action.startsWith("ADDTO:"))
		{
			String type = "whitelist";
			switch (action)
			{
			case ("ADDTO:GRAYLIST"):
				type = "graylist";
				break;
			case ("ADDTO:BLACKLIST"):
				type = "blacklist";
				break;
			}
			forcedselect = createContact(type);
			if (forcedselect == null) return; // not saved; user already told
			String seltid = forcedselect.getTID();
			mcImportAttribute tidattribute = new mcImportAttribute("TID",
					seltid, "HIDE");
			attributes.addImportAttribute(tidattribute);
			// mcImportAttribute statusattribute = new mcImportAttribute(
			// "IMPORTSTATUS", "contactfound", "HIDE");
			// attributes.addImportAttribute(statusattribute);
			currentimport.setTID(seltid);
			// currentimport.setImportstatus("contactfound");
			currentimport.update();

			action = "NEXT PROBLEM";
		}
		if (action.equals("NEXT"))
		{
			incrementrownumber();
			forcedselect = null;
			currentimport = imports.get(importrownumber);
		} else if (action.equals("NEXT PROBLEM"))
		{
			int importrow = importrownumber;
			boolean next = true;
			autoaccept = true;
			while (importrow < importcount && next)
			{
				importrow++;
				currentimport = imports.get(importrow);
				if (currentimport == null) continue;
				String icstatus = currentimport.getImportstatus();
				if (icstatus != null && icstatus.equalsIgnoreCase("ignore"))
					continue;
				if (!icstatus.equalsIgnoreCase("contactfound"))
				{
					next = false;
					continue;
				}
				String foundtid = currentimport.getTID();
				mcContact fcontact = MyContacts.selbox.FindbyTID(foundtid);
				if (fcontact != null)
				{

					int nonmatches = currentimport
							.checkforfieldmatchesAll(fcontact);
					if (nonmatches == 0) continue;
					else
					{
						next = false;
					}
				}
			}
			importrownumber = importrow;
		}else if (action.equals("ACCEPT and NEXT"))
		{

			mcContact foundcontact;
			if (forcedselect != null) foundcontact = forcedselect;
			else
				foundcontact = MyContacts.selbox.getSelcontact();
			for (jswCheckbox anupdate : doupdate)
			{
				if (anupdate != null && anupdate.isSelected())
				{
					String attkey = anupdate.getTag();
					mcImportAttribute editatt = attributes.get(attkey);
					if (editatt != null)
					{
						String attvalue = editatt.getValue();
						System.out.println(" update " + foundcontact + "("
								+ attkey + ")" + " to " + attvalue);
						foundcontact.updateAttribute(editatt.getRoot(),editatt.getQualifier() ,attvalue);
					} else
					{
						System.out.println(" not found " + attkey);
					}
				}
			}
			foundcontact.getAttributes();
			MyContacts.selbox.put(foundcontact);
			showcontact = showContact(foundcontact);
			int importrow = importrownumber;
			boolean next = true;
			while (importrow < importcount && next)
			{
				importrow++;
				currentimport = imports.get(importrow);
				if (currentimport == null) continue;
				String icstatus = currentimport.getImportstatus();
				if (icstatus != null && icstatus.equalsIgnoreCase("ignore"))
					continue;
				if (!icstatus.equalsIgnoreCase("contactfound"))
				{
					next = false;
					continue;
				}
				String foundtid = currentimport.getTID();
				mcContact fcontact = MyContacts.selbox.FindbyTID(foundtid);
				if (fcontact != null)
				{

					int nonmatches = currentimport
							.checkforfieldmatchesAll(fcontact);
					if (nonmatches == 0) continue;
					else
					{
						next = false;
					}
				}
			}
			importrownumber = importrow;
		}  else if (action.equals("PREVIOUS"))
		{
			decrementrownumber();
			currentimport = imports.get(importrownumber);
			forcedselect = null;
		} else if (action.equals("UNLINK"))
		{
			currentimport.setTID(null);
			currentimport.setImportstatus("notfound");
			currentimport.update();
		} else if (action.equals("UPDATE"))
		{
			mcContact foundcontact;
			if (forcedselect != null) foundcontact = forcedselect;
			else
				foundcontact = MyContacts.selbox.getSelcontact();
			for (jswCheckbox anupdate : doupdate)
			{
				if (anupdate != null && anupdate.isSelected())
				{
					String attkey = anupdate.getTag();
					mcImportAttribute editatt = attributes.get(attkey);
					if (editatt != null)
					{
						String attvalue = editatt.getValue();
						String root = editatt.getRoot();
						String qual =editatt.getQualifier();
						System.out.println(" update " + foundcontact + "("
								+ attkey + ")" + " to " + attvalue);
						foundcontact.updateAttribute(root,qual, attvalue);
					} else
					{
						System.out.println(" not found " + attkey);
					}
				}
			}
			foundcontact.getAttributes();
			MyContacts.selbox.put(foundcontact);
			showcontact = showContact(foundcontact);
		} else if (action.startsWith("UPDATELIST:"))
		{
			String attkey = action.substring(11).toLowerCase();
			mcContact foundcontact;
			if (forcedselect != null) foundcontact = forcedselect;
			else
				foundcontact = MyContacts.selbox.getSelcontact();
			mcAttribute targetatt = foundcontact.getAttributebyKey(attkey);
			for (jswCheckbox anupdate : doupdate)
			{
				if (anupdate != null && anupdate.isSelected())
				{
					String upkey = anupdate.getTag();
					mcImportAttribute editatt = attributes.get(upkey);
					if (editatt != null)
					{
						if (upkey.equalsIgnoreCase("name"))
						{
							String attvalue = editatt.getValue();
							targetatt.updateQualifier(attvalue);
							System.out.println(" update  name to " + attvalue);
						} else
						{
							System.out.println(" not found " + upkey);
						}
					}
				}
			}
			foundcontact.getAttributes();
			MyContacts.selbox.put(foundcontact);
			showcontact = showContact(foundcontact);
		} else if (action.equals("IGNOREATTRIBUTE"))
		{

			for (jswCheckbox anupdate : doupdate)
			{
				if (anupdate != null && anupdate.isSelected())
				{
					String attkey = anupdate.getTag();
					mcImportAttribute editatt = attributes.get(attkey);
					if (editatt != null)
					{
						editatt.setImportstatus("IGNORE");
						editatt.updateStatus("IGNORE");
					} else
					{
						System.out.println(" not found " + attkey);
					}
				}
			}
		} else if (action.equals("ADDIMPORT"))
		{
			mcContact foundcontact;

			String foundtid = currentimport.getTID();
			foundcontact = MyContacts.selbox.FindbyTID(foundtid);
			for (jswCheckbox anupdate : doupdate)
			{
				if (anupdate != null && anupdate.isSelected())
				{
					String attkey = anupdate.getTag();
					System.out.println(" to update  " + attkey);
					mcImportAttribute editatt = attributes.get(attkey);
					String newkey = attkey;
					if (editatt != null)
					{
						String oldatt = foundcontact.getAttributeValuebyKey(attkey);
						String root = mcUtilities.parseKey(attkey);
						int k = 1;
						String newqual="";
						while (oldatt != null)
						{
							newqual= "alt" + k;
							oldatt = foundcontact.getAttributeValue(root, newqual);
							k++;
						}
						String attvalue = editatt.getValue();
						System.out.println(" update  " + newkey + " to "
								+ editatt.getValue());
						foundcontact.updateAttribute(root, newqual , attvalue);
					} else
					{
						System.out.println(" not found " + attkey);
					}
				}
			}
			foundcontact.getAttributes();
			MyContacts.selbox.put(foundcontact);
			showcontact = showContact(foundcontact);
		} else if (action.equals("CREATECONTACT"))
		{
			forcedselect = createContact("person");
			if (forcedselect == null) return; // not saved; user already told
			String seltid = forcedselect.getTID();
			mcImportAttribute tidattribute = new mcImportAttribute("TID",
					seltid, "HIDE");
			attributes.addImportAttribute(tidattribute);
			mcImportAttribute statusattribute = new mcImportAttribute(
					"IMPORTSTATUS", "contactfound", "HIDE");
			attributes.addImportAttribute(statusattribute);
			currentimport.setTID(seltid);
			currentimport.setImportstatus("contactfound");
			currentimport.update();
		} else if (action.startsWith("SELECT:"))
		{
			String selID = action.substring(7).toLowerCase();
			System.out.println(" forced to select " + selID);
			forcedselect = MyContacts.selbox.getAllcontactlist().FindbyTID(selID);
			String seltid = forcedselect.getTID();
			// System.out.println(" SELECTED >>" + currentimport);
			// int importrownumber = currentimport.getRownumber();
			mcImportAttribute tidattribute = new mcImportAttribute("TID",
					seltid, "HIDE");
			attributes.addImportAttribute(tidattribute);
			mcImportAttribute statusattribute = new mcImportAttribute(
					"IMPORTSTATUS", "contactfound", "HIDE");
			attributes.addImportAttribute(statusattribute);
			currentimport.setTID(seltid);
			currentimport.setImportstatus("contactfound");
			currentimport.update();
		} else if (action.equals("SELCONTACT"))
		{
			mcContact selcont = selectcontact.getSelectedValue();
			forcedselect = selcont;
			if (forcedselect != null)
			{
				String seltid = forcedselect.getTID();
				// int importrownumber = currentimport.getRownumber();
				mcImportAttribute tidattribute = new mcImportAttribute("TID","",
						seltid, "HIDE");
				attributes.addImportAttribute(tidattribute);
				mcImportAttribute statusattribute = new mcImportAttribute(
						"IMPORTSTATUS","", "contactfound", "HIDE");
				attributes.addImportAttribute(statusattribute);
				currentimport.setTID(seltid);
				currentimport.setImportstatus("contactfound");
				currentimport.update();
			} else
			{
				System.out.println("forced select fails "
						+ selectcontact.getSelectedValue());
			}
		} else if (action.equals("IMPORTCONTACTSELECTED"))
		{
			// System.out.println("action in importpanel " + action + " "
			// + noaction);
			if (!noaction)
			{
				selectedcontact = selectcontact.getSelectedValue();
				// System.out.println("action in importpanel " +
				// selectedcontact);
			}
		} else if (action.equals("IGNOREIMPORT"))
		{
			System.out.println(" set to ignore ");
			currentimport.setImportstatus("ignore");
			currentimport.update();
		} else
			System.out.println("action in importpanel " + action);
		// MyContacts.selbox.filterContacts();
		showImportPanel();
	}

	public jswVerticalPanel showContact(mcContact selcontact)
	{
		jswVerticalPanel mainpanel = new jswVerticalPanel("title",false,false);;
		mcAttributes attributes = selcontact.getAttributes();
		jswHorizontalPanel idbox = new jswHorizontalPanel("idbox", false);
		add(idbox);
		if (attributes != null)
		{
			mcAttribute photoatt = attributes.find("photo");
			if (photoatt != null)
			{
				jswImage animage = new jswImage(photoatt.getValue());
				idbox.add(animage.DisplayImage());
			}
		}
		jswLabel idpanel1 = new jswLabel(" ");
		idbox.add(idpanel1);
		idpanel1.setText(selcontact.getIDstr());
		jswLabel idpanel2;
		jswLabel idpanel3;
		idpanel2 = new jswLabel(" ");
		idbox.add(idpanel2);
		idpanel2.setText(selcontact.getName());
		idpanel3 = new jswLabel(" ");
		idbox.add("RIGHT", idpanel3);
		idpanel3.setText("MYTAGS");
		jswTable attributepanel = new jswTable(this,"attributes",
				MyContacts.getTableStyles());
		add(attributepanel);
		attributepanel.removeAll();
		selcontact.fillContact();
		int row = 0;
		for (Entry<String, mcAttribute> anentry : attributes.entrySet())
		{
			mcAttribute anattribute = anentry.getValue();
			if (anattribute != null)
			{
				{
					String atributekey = anattribute.getKey();
					jswLabel alabel = new jswLabel(atributekey);
					attributepanel.addCell(alabel, row, 0);
					if (anattribute.isImage())
					{
						jswImage animage = new jswImage(anattribute.getValue());
						attributepanel.addCell(animage.DisplayImage(), row, 1);
					} else
					{
						String value = anattribute.getFormattedValue();
						attributepanel.addCell(new jswLabel(value), row, 1);
					}
				}
			}
			row++;

		}
		return mainpanel;
	}

	

	public HashSet<mcAttribute> getMatchingAttributes(mcContact selcontact,
			mcImportContact impcontact)
	{
		Vector<String> filters = impcontact.AttributeValues();
		mcAttributes attributes = selcontact.getAttributes();
		HashSet<mcAttribute> list = new HashSet<mcAttribute>();
		for (Entry<String, mcAttribute> anentry : attributes.entrySet())
		{
			mcAttribute anattribute = anentry.getValue();
			if (anattribute != null)
			{
				for (String filter : filters)
				{
					String qualifier = anattribute.getQualifier().toLowerCase();
					String lcvalue = anattribute.getValue().toLowerCase();
					if (mcUtilities.containedBy(filter, qualifier)
							|| mcUtilities.containedBy(filter, lcvalue))
					{
						if (!list.contains(anattribute)) list.add(anattribute);
					}
					if (mcUtilities.containedBy(qualifier, filter)
							|| mcUtilities.containedBy(lcvalue, filter))
					{
						if (!list.contains(anattribute)) list.add(anattribute);
					}

				}
			}
		}
		return list;
	}

	public void showImportPanel()
	{
		// imports = importfile;
		// importrownumber = rownumber;
		buttonpanel = new jswHorizontalPanel();
		if (imports != null)
		{
			try
			{
				this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				imports.selectAllImports();
				// imported.scanImports();
				imports.selectAllImports();
			} finally
			{
				this.setCursor(Cursor.getDefaultCursor());
			}
			importcount = imports.size();

		}
		Vector<String> foundcontacts = null;
		// jswVerticalPanel mainpanel = this;
		removeAll();
		jswHorizontalPanel header = new jswHorizontalPanel();
		jswLabel heding = new jswLabel(" ");
		heding.setText(" Data imported from :" + imports.getImportfilename()
				+ " as " + imports.getImporttype());
		header.add(heding);
		add(header);
		add(buttonpanel);
		new jswHorizontalPanel();
		mcImportContact currentimport = imports.get(importrownumber);
		jswHorizontalPanel idbox = new jswHorizontalPanel("idbox", false);
		add(idbox);
		jswButton backward = new jswButton(this, "Previous");
		idbox.add(" LEFT ", backward);
		jswLabel idpanel1 = new jswLabel("");
		idpanel1.setText("importing:" + (importrownumber + 1) + " of:"
				+ imports.size());
		idbox.add(" MIDDLE ", idpanel1);
		if (currentimport != null)
		{
			jswLabel idpanel2 = new jswLabel(" ");
			idbox.add(" MIDDLE ", idpanel2);
			idpanel2.setText(currentimport.getName());
		}
		jswButton acceptnextproblem = new jswButton(this, "Accept and Next");
		idbox.add(" RIGHT ", acceptnextproblem);
		jswButton nextproblem = new jswButton(this, "Next Problem");
		idbox.add(" RIGHT ", nextproblem);
		jswButton forward = new jswButton(this, "Next");
		idbox.add(" RIGHT ", forward);
		jswHorizontalPanel impcontactbox = new jswHorizontalPanel();
		jswLabel status = new jswLabel(" ");
		if (currentimport != null)
		{
			foundcontacts = null;
			String icstatus = currentimport.getImportstatus();
			if (icstatus == null || icstatus.isEmpty()
					|| icstatus.equalsIgnoreCase("notfound"))
			{
				foundcontacts = currentimport.findMatchingContacts();
				if (foundcontacts.size() == 1)
				{
					mcContact fcontact = MyContacts.selbox.FindbyID(foundcontacts
							.get(0));
					currentimport.setImportstatus("contactfound");
					currentimport.setTID(fcontact.getTID());
					currentimport.update();
				}
			}

			if (icstatus.equalsIgnoreCase("contactfound"))
			{
				icstatus = " Matched to:" + currentimport.getTID();
				String foundtid = currentimport.getTID();
				mcContact fcontact = MyContacts.selbox.FindbyTID(foundtid);
				status.setText(icstatus);
				impcontactbox.add(status);
				jswButton forget = new jswButton(this, "Unlink");
				impcontactbox.add(" LEFT ", forget);
				if (fcontact != null)
				{
					// currentimport.checkforduplicatevalues();
					currentimport.checkforfieldmatches(fcontact);
				}
			} else
			{
				status.setText(icstatus);
				impcontactbox.add(status);
			}

			// foundcontacts = currentimport.findMatchingContacts();
			add(impcontactbox);
			attributes = currentimport.getAttributes();
			if (attributes != null)
			{
				jswTable attributepanel = new jswTable(this,"attributes",
						MyContacts.getTableStyles());
				add(attributepanel);
				attributepanel.removeAll();
				int row = 0;
				for (Entry<String, mcImportAttribute> anentry : attributes
						.entrySet())
				{
					anentry.getKey();
					mcImportAttribute anattribute = anentry.getValue();

					if (anattribute != null)
					{
						String quallabel = anattribute.getAttkey();
						String attstatus = anattribute.getImportstatus();
						// temp
						// if (!anattribute.ignore())
						{
							jswLabel alabel3 = new jswLabel(quallabel);
							alabel3.applyStyle(MyContacts.panelstyles.getStyle("greenfont"));
							attributepanel.addCell(alabel3, row, 0);
							String value = anattribute.getValue();
							if (value == null) continue;
							if(quallabel.startsWith("photo"))
							{
								jswImage animage = new jswImage(value);
								attributepanel.addCell(animage.DisplayImage(),
										row, 1);
							}
							else 
								{
								if (value.length() > 200)
								{
									value = value.substring(0,50)+" ....";
								}
								attributepanel.addCell(new jswLabel(value),
										row, 1);
								}
							if (attstatus.equals("OK")
									|| attstatus.equals("DONE")
									|| attstatus.equals("HIDE")
									|| attstatus.equals("IGNORE"))
							{
								attributepanel.addCell(new jswLabel(attstatus),
										row, 2);
							} else
							{
								doupdate[row] = new jswCheckbox(this,"");
								doupdate[row].setTag(quallabel);
								if(autoaccept)doupdate[row].setSelected(true);
								attributepanel.addCell(doupdate[row], row, 2);
							}
						}
					}
					row++;
				}
			}
			autoaccept=false;
		}
		String foundtid = "";
		if (currentimport != null)
		{
			foundtid = currentimport.getTID();
			mcContact fcontact = MyContacts.selbox.FindbyTID(foundtid);
			if (fcontact != null)
			{
				// MyContacts.selbox.setSelcontact(fcontact);
				jswHorizontalPanel actionbox = new jswHorizontalPanel();
				jswButton actionbutton1 = new jswButton(this, "Add Selected",
						"addimport");
				actionbox.add(" RIGHT ", actionbutton1);
				jswButton actionbutton = new jswButton(this, "Update Selected",
						"update");
				actionbox.add(" RIGHT ", actionbutton);

				jswButton actionbutton2 = new jswButton(this,
						"Ignore Selected", "ignoreattribute");
				actionbox.add(" RIGHT ", actionbutton2);
				add(" FILLW ", actionbox);

				showcontact = showContact(fcontact);
				add(showcontact);

			} else
			{

				// Vector<String> foundcontacts = currentimport
				// .findMatchingContacts();
				jswHorizontalPanel actionbox = new jswHorizontalPanel();
				jswButton selbutton = new jswButton(this, "Select>",
						"selcontact");
				actionbox.add(" LEFT ", selbutton);
				selectcontact = new jswDropDownContactBox(null,"Select Contact",	false);
				selectcontact.addList(MyContacts.selbox.makeContactsVector());
				selectcontact.setSelected(selectedcontact);
				// selectcontact.addActionListener(this,
				// "importcontactselected");
				actionbox.add(" LEFT ", selectcontact);
				jswButton actionbutton = new jswButton(this,
						"Create New Contact", "createcontact");
				actionbox.add(" RIGHT ", actionbutton);
				jswButton igbutton = new jswButton(this, "Ignore import",
						"ignoreimport");
				actionbox.add(" RIGHT ", igbutton);

				add(" FILLW ", actionbox);
				jswHorizontalPanel actionbox2 = new jswHorizontalPanel();

				jswButton wlbutton = new jswButton(this, "Add to Whitelist",
						"addto:whitelist");
				actionbox2.add(" RIGHT ", wlbutton);
				jswButton glbutton = new jswButton(this, "Add to Graylist",
						"addto:graylist");
				actionbox2.add(" RIGHT ", glbutton);
				jswButton blbutton = new jswButton(this, "Add to Blacklist",
						"addto:blacklist");
				actionbox2.add(" RIGHT ", blbutton);

				add(" FILLW ", actionbox2);
				if (foundcontacts != null)
				{
					for (String acontactid : foundcontacts)
					{

						fcontact = MyContacts.selbox.FindbyID(acontactid);
						jswHorizontalPanel actionbox3 = new jswHorizontalPanel();
						jswLabel aflabel = new jswLabel(fcontact.getTID());
						actionbox3.add(" LEFT ", aflabel);
						jswButton actionbutton2 = new jswButton(this, "Select",
								"select:" + fcontact.getTID());
						actionbox3.add(" RIGHT ", actionbutton2);
						add(" FILLW ", actionbox3);
					}
				}
			}
		}
		Dimension d = getMinimumSize();
		Rectangle fred = getBounds();
		fred.width = d.width;
		fred.height = d.height + 60;
		Rectangle actual = MyContacts.topgui.getBounds();
		if (fred.width > actual.width) actual.width = fred.width;
		if (fred.height + 60 > actual.height) actual.height = fred.height + 60;
		// MyContacts.topgui.setBounds(actual);
		MyContacts.topgui.setVisible(true);
		repaint();
		MyContacts.topgui.getContentPane().validate();
	}

	public mcContact createContact(String group)
	{
		String newname = null;
		String email = null;
		for (jswCheckbox anupdate : doupdate)
		{
			if (anupdate != null)
			{
				String attkey = anupdate.getTag();
				if (attkey.startsWith("name"))
				{
					mcImportAttribute editatt = attributes.get(attkey);

					if (editatt != null)
					{
						newname = editatt.getValue();
					}
				}
				if (attkey.startsWith("email"))
				{
					mcImportAttribute editatt = attributes.get(attkey);

					if (editatt != null)
					{
						email = editatt.getValue();
					}
				}
			}
		}
		if (newname == null) newname = email;
		if (newname == null) newname = "anewimportedcontact";
		newname = mcUtilities.tidyValue(newname);
		mcContact newcontact = new mcContact();
		//newcontact.setKind(group);
		newcontact.setTID(newname);
		final String contactname = newname;
		// the new contact and its import status are saved together or not at all
		boolean saved = MyContacts.dbTransaction("Creating contact " + newname, () ->
		{
			newcontact.insertNewContact();
			newcontact.updateAttributebyKey("name", contactname);
			newcontact.updateAttributebyKey("tags", "imported");
			// int importrownumber = currentimport.getRownumber();
			mcImportAttribute tidattribute = new mcImportAttribute("TID", contactname,
					"HIDE");
			attributes.addImportAttribute(tidattribute);
			mcImportAttribute statusattribute = new mcImportAttribute(
					"IMPORTSTATUS", "contactfound", "HIDE");
			attributes.addImportAttribute(statusattribute);
			currentimport.setTID(contactname);
			currentimport.setImportstatus("contactfound");
			currentimport.update();

			for (jswCheckbox anupdate : doupdate)
			{
				if (anupdate != null && anupdate.isSelected())
				{
					String attkey = anupdate.getTag();
					mcImportAttribute editatt = attributes.get(attkey);
					if (editatt != null)
					{
						String attvalue = editatt.getValue();
						System.out.println(" update to " + editatt.getValue());
						newcontact.updateAttributebyKey(attkey, attvalue);
					} else
					{
						System.out.println(" not found " + attkey);
					}
				}
			}
		});
		return saved ? newcontact : null;

	}

	public void decrementrownumber()
	{
		importrownumber = importrownumber - 1;
		if (importrownumber < 0) importrownumber = 0;

	}

	public void incrementrownumber()
	{
		importrownumber = importrownumber + 1;
		if (importrownumber > importcount) importrownumber = 0;

	}

	public void refresh()
	{
		// TODO Auto-generated method stub
		
	}

}
