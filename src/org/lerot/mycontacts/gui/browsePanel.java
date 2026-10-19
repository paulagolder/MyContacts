package org.lerot.mycontacts.gui;

import org.lerot.mycontacts.gui.widgets.jswDropPane;
import org.lerot.mycontacts.*;
import org.lerot.mywidgets.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import static org.lerot.mycontacts.MyContacts.panelstyles;

public class browsePanel extends jswVerticalPanel implements ActionListener
{

	private static final long serialVersionUID = 1L;
	private static jswStyles tablestyles;
	private static jswStyles linktablestyles;
	mcContact selcontact;
	private String vcarddirectory = "";


    private Vector<mcCorrespondance> letters;

    public browsePanel()
	{
        super("browse",false,false);
		this.setTrace(true);
        vcarddirectory = MyContacts.topgui.desktop;
		jswStyles defaultstyles = jswStyles.getDefaultStyles();
		tablestyles = StylesMakeAttributeTable();
		tablestyles = searchTableStyles();
		//tablestyles = defaultstyles;
		//linktablestyles = StylesMakeLinkTable();
		linktablestyles =searchTableStyles();
		//jswStyles mystyles = mystyles;
	}

	@Override
	public void actionPerformed(ActionEvent evt)
	{

		String cmd = evt.getActionCommand();
		System.out.println(" here we are bp " + cmd);
		//HashMap<String, String> cmdmap = jswUtils.parsecsvstring(cmd);
	//	String action= cmdmap.get("command");
        String action= cmd;
		if (action.startsWith("VCARD"))
		{
			System.out.println(" export vcard " + selcontact.getTID());
			printVcard();
		} else if (action.startsWith("VIEW:"))
		{
			String selid = action.substring(5);
			mcContact selcon = MyContacts.selbox.getAllcontactlist()
					.FindbystrID(selid);
			MyContacts.selbox.contactselectbox.setSelected(selcon);
			MyContacts.selbox.contactselectbox.contactddbox.setSelectedItem(selcon);
			// MyContacts.selbox.setSelcontact(selcon); // paul fiddling here
			MyContacts.topgui.refreshView();
		} else if (action.startsWith("USE:"))
		{
			System.out.println(" use address " + selcontact.getTID());
			String address = selcontact.makeBlockAddress("\n", true);

			String[] options = new String[] { "Letter", "Label", "Copy",
					"Cancel" };
			TextTransfer textTransfer = new TextTransfer();
			textTransfer.setClipboardContents(address);
			int n = JOptionPane.showOptionDialog(this,
					"Use this address\n" + address, "Address",
					JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
					options, options[0]);
			System.out.println("action is " + n);
			if (n != 3)
			{
				if (n == 1)
				{
					printlabel(address);
				} else if (n == 0)
				{
					printletter(selcontact, address);
				} else if (n == 2)
				{
					textTransfer.setClipboardContents(address);
				}
			}

		} else if (action.startsWith("MAKEGROUPEMAIL"))
		{
			String sendermail = "paul.a.golder@lerot.org";
			if (sendermail != null)
			{
				mcContacts memberlist = selcontact.getMembers("member");
				String emailaddresses = "";
				for (Entry<String, mcContact> anentry : memberlist.entrySet())
				{
					mcContact acontact = anentry.getValue();
					String email = acontact.getEmail();
					if (email != null) emailaddresses += email + "%2C%20";
				}
				System.out.println(emailaddresses);
				email(sendermail, emailaddresses);
			}

		} else if (action.startsWith("MAIL:"))
		{
			String atkey = action.substring(5);
			mcAttribute selatt = selcontact.getAttributebyKey(atkey);
			String emailaddress = selatt.getValue();
			;
			System.out.println(" use email " + selcontact.getTID());

			String[] options = new String[] { "EMAIL", "Copy", "Cancel" };
			TextTransfer textTransfer = new TextTransfer();
			textTransfer.setClipboardContents(emailaddress);
			int n = JOptionPane.showOptionDialog(this,
					"Use this email\n" + emailaddress, "EMail",
					JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
					options, options[0]);
			System.out.println("action is " + n);
			if (n != 2)
			{
				if (n == 0)
				{

					email(emailaddress, null);
				} else if (n == 1)
				{
					textTransfer.setClipboardContents(emailaddress);
				}
			}

		} else if (action.startsWith("COPY:"))
		{
			String atkey = action.substring(5);
			mcAttribute selatt = selcontact.getAttributebyKey(atkey);
			String info = selatt.getValue();
			TextTransfer textTransfer = new TextTransfer();
			textTransfer.setClipboardContents(info);
		} else
		{
			System.out
					.println(" unknown action " + action + " in browsepanel ");
		}

	}

	void email(String emailaddress, String bcc)
	{
		// mcAttribute selatt = selcontact.getAttributebyKey(atkey);
		// String emailaddress = selatt.getValue();

		Desktop desktop;
		if (Desktop.isDesktopSupported() && (desktop = Desktop.getDesktop())
				.isSupported(Desktop.Action.MAIL))
		{
			URI mailto;
			String subject = "email from " + MyContacts.topgui.user;
			subject = subject.replace(" ", "%20");
			String body = mcLetter.getSalutation(selcontact) + "%0D%0A";
			body = body.replace(" ", "%20");
			body = body.replace("&", "%20");
			try
			{
				String target = "mailto:" + emailaddress + "?subject=" + subject
						+ "&body=" + body;
				if (bcc != null) target += "&bcc=" + bcc;
				// String converted = URLDecoder.encode(target, "UTF-8");
				mailto = new URI(target);
				desktop.mail(mailto);
			} catch (URISyntaxException | IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else
		{
			// TODO fallback to some Runtime.exec(..) voodoo?
			throw new RuntimeException(
					"desktop doesn't support mailto; mail is dead anyway ;)");
		}
	}

	private jswTable makeAttributePanel(mcContact selcontact, String selector)
	{

		jswTable attributepanel = new jswTable(this,"attributes", tablestyles);
		if (selcontact != null)
		{
			mcAttributes attributes = selcontact.getAttributes();
			if (attributes.size() < 1)
			{
				selcontact.fillContact();
				attributes = selcontact.getAttributes();
			}
			int row = 0;
		//	mcAttributeTypes attributetypes = mcAttributeTypes;
			if (mcAttributeTypes.getAll() == null)
			{
				attributepanel.addCell(new jswLabel("no attributes"), 0, 1);
			}
			int attcount = 0;
			for (Entry<String, mcAttributeType> anentry : mcAttributeTypes.entrySet())
			{
				String attkey = anentry.getKey();
				mcAttributeType attype = anentry.getValue();
				if (attype.getDisplaygroup().contains(selector))
				{
					Vector<mcAttribute> allattributes = attributes
							.filterAttributes(attkey);
					for (mcAttribute anattribute : allattributes)
					{
						if (anattribute != null)
						{
							attcount = attcount + 1;
							String attributekey = anattribute.getKey();
							jswLabel alabel = new jswLabel(attributekey);
							attributepanel.addCell(alabel, row, 0);
							if (anattribute.isImage())
							{
								jswImage animage = new jswImage(
										anattribute.getValue());
								attributepanel.addCell(animage.DisplayImage(),
										row, 1);
							} else
							{
								if (anattribute.isType("date"))
								{
									// System.out.println(" in bday");
								}
								String value = anattribute.getFormattedValue();// paul
								jswLabel slabel = new jswLabel(value);
								slabel.setBorder(jswStyle.makeLineBorder(Color.blue, 2));
								slabel.getLabel().setBackground(Color.green);
								attributepanel.addCell(slabel, " LEFT ", row,
										1);
							}
							if (anattribute.isType("address")
									&& selector.contains("B"))
							{
								jswButton usecontact = new jswButton(this,
										"USE", "USE:" + anattribute.getKey());
								attributepanel.addCell(usecontact, row, 2);

							} else if (anattribute.isType("email")
									&& selector.contains("B"))
							{
								jswButton usecontact = new jswButton(this,
										"MAIL", "MAIL:" + anattribute.getKey());
								attributepanel.addCell(usecontact, row, 2);

							} else if (anattribute.isType("phone")
									&& selector.contains("B"))
							{
								jswButton usecontact = new jswButton(this,
										"COPY", "COPY:" + anattribute.getKey());
								attributepanel.addCell(usecontact, row, 2);

							} else if (anattribute.isType("cellphone")
									&& selector.contains("B"))
							{
								jswButton usecontact = new jswButton(this,
										"COPY", "COPY:" + anattribute.getKey());
								attributepanel.addCell(usecontact, row, 2);

							} else if (anattribute.getRoot().equals("note")
									&& selector.contains("B"))
							{
								jswButton usecontact = new jswButton(this,
										"COPY", "COPY:" + anattribute.getKey());
								attributepanel.addCell(usecontact, row, 2);

							} else
							{
								jswLabel dummylabel = new jswLabel("");
								attributepanel.addCell(dummylabel, row, 2);
							}
						}
						row++;
					}
				}
			}
			if (attcount == 0)
			{
				attributepanel.addCell(new jswLabel(""), 0, 1);
			}

		} else
		{
			attributepanel.addCell(new jswLabel("disconnected membership"), 0,
					1);
		}
		return attributepanel;
	}

	public void makeBrowsePanel()
	{
		jswVerticalPanel browsepanel = this;

		selcontact = MyContacts.selbox.getSelcontact();
		browsepanel.removeAll();
		browsepanel.setName("Browsepanel");
		browsepanel.setTag("Browsepanel");
		//browsepanel.setLayout(new jswVerticalLayout());
        browsepanel.setTrace(true);
		if (selcontact != null)
		{
			mcAttributes attributes = selcontact.getAttributes();
			jswHorizontalPanel idbox = new jswHorizontalPanel("idbox", false);
			idbox.setPadding(10);
			browsepanel.add(" FILLW ",idbox);
			idbox.setStyleAttribute("height",50);
			idbox.applyStyle();
			if (attributes != null)
			{
				mcAttribute photoatt = attributes.find("photo");
				if (photoatt != null)
				{
					jswImage animage = new jswImage(photoatt.getValue());
					animage.setTargetheight(40);
					idbox.add(animage.DisplayImage());
				}
			}
			jswLabel idpanel1 = new jswLabel(" ");
			idbox.add(idpanel1);
			idpanel1.setText(selcontact.getIDstr());
			idpanel1.applyStyle(panelstyles.getStyle("mediumLabel"));
			jswLabel idpanel2 = new jswLabel(" ");		
			idbox.add(idpanel2);
			idpanel2.setText(selcontact.getName());
			idpanel2.applyStyle(panelstyles.getStyle("mediumLabel"));
			//idpanel2.applyStyle();
			jswButton vcardexport = new jswButton(this, "VCard");
			idbox.add("RIGHT", vcardexport);
			selcontact.fillContact();
			jswTable contactattributes = makeAttributePanel(selcontact, "B");
			if (contactattributes != null) browsepanel.add( " FILLW ",contactattributes);
		//	mcContacts relationlist = makeLinkToList(selcontact, "related");
		//	mcContacts memberoflist = makeLinkToList(selcontact, "memberof");
		//	mcContacts hasmemberslist = makeLinkToList(selcontact, "hasmember");

			mcAttributes relationlist = selcontact.selectLinkToAttributes("related");
			mcAttributes memberoflist = selcontact.selectLinkToAttributes( "memberof");
			mcAttributes hasmemberslist = selcontact.selectLinkToAttributes( "hasmember");


			if (relationlist != null)
			{
				jswVerticalPanel arelationslist = makeLinkToPanel(relationlist,	"related", "Related to");
				browsepanel.add(" ",arelationslist);
			}
			if (hasmemberslist != null)
			{
				//hasmemberslist.printlist("hm");
				jswHorizontalPanel memberheading = new jswHorizontalPanel();
				jswLabel label = new jswLabel("Has Members");
				memberheading.add(" MIDDLE ", label);
				jswButton groupemail = new jswButton(this, "MAKE GROUP EMAIL","MAKEGROUPEMAIL");
				memberheading.add(groupemail);
				browsepanel.add(" ",memberheading);
				jswTable memberstable = makeLinkToTable(hasmemberslist,"members");
				if (hasmemberslist.size() < 6)
				{
					browsepanel.add(" FILLW ",memberstable);
				} else
				{
					jswScrollPane scrollableTextArea = new jswScrollPane(memberstable, -10, -10);
					scrollableTextArea.setName("resultscroll");
					scrollableTextArea.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					scrollableTextArea.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
					browsepanel.add(" FILLH ", scrollableTextArea);
					scrollableTextArea.setVisible(true);
				}
			}
			if (memberoflist != null)
			{
				jswVerticalPanel aorglist = makeLinkToPanel(memberoflist, "memberof","Member Of");
				browsepanel.add(" FILLW ",aorglist);
				//memberoflist.printlist("mo");
			}
			jswLabel correspondancelabel = new jswLabel("Correspondance");
			browsepanel.add("  ", correspondancelabel);

			jswVerticalPanel correspondance = new correspondancePanel(selcontact, "letters");
           correspondance.setPreferredSize(new Dimension(500,500));
            this.add(" FILLH FILLW ", correspondance);
		}

	}



	private jswVerticalPanel makeLinkToPanel(mcAttributes contacts,String selector, String title)
	{
		jswVerticalPanel vpanel = new jswVerticalPanel("LinkTo",false,false);
		if (title != null)
		{
			jswLabel memberheading = new jswLabel(title);
			vpanel.add(memberheading);
		}
		jswTable memberpanel = new jswTable(this,selector, linktablestyles);
		int row = 0;
		for (Entry<String, mcAttribute> anentry : contacts.entrySet())
		{
			mcAttribute linkedcontact = anentry.getValue();
			memberpanel.addCell(linkedcontact.getTag(), row, 0);
			jswButton viewcontact = new jswButton(this, "VIEW",
					"VIEW:" + linkedcontact.getValue());
			memberpanel.addCell(linkedcontact.getQualifier(), row, 1);
			memberpanel.addCell(" ", row, 2);
			memberpanel.addCell(viewcontact, row, 3);
			row++;
		}
		if (row == 0)
		{
			return null;
		} else if (row < 6)
		{
			vpanel.add(memberpanel);
			return vpanel;
		} else
		{
			jswScrollPane scrollableTextArea = new jswScrollPane(memberpanel,
					-10, -10);
			scrollableTextArea.setName("resultscroll");
			scrollableTextArea.setHorizontalScrollBarPolicy(
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrollableTextArea.setVerticalScrollBarPolicy(
					ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			vpanel.add(" FILLH ", scrollableTextArea);
			scrollableTextArea.setVisible(true);
			return vpanel;
		}

	}

/*	private jswTable makeLinktotable(mcContacts hasmemberslist)
	{
		// TODO Auto-generated method stub
		return null;
	}*/

	private jswTable makeLinkToTable(mcAttributes contacts, String selector)
	{
		jswTable memberpanel = new jswTable(this,selector, linktablestyles);
		int row = 0;
		for (Entry<String, mcAttribute> anentry : contacts.entrySet())
		{
			mcAttribute linkedcontact = anentry.getValue();
			memberpanel.addCell(linkedcontact.getTag(), row, 0);
			jswButton viewcontact = new jswButton(this, "VIEW",
					"VIEW:" + linkedcontact.getQualifier());
			memberpanel.addCell("", row, 1);
			memberpanel.addCell(viewcontact, row, 2);
			row++;
		}
		return memberpanel;
	}

	private void printlabel(String address)
	{

		JFileChooser fc = new JFileChooser(MyContacts.letterfolder);
		fc.setDialogTitle("Specify a file to save label");
		String labelname = mcLetter.makeFileName(selcontact);
		fc.setSelectedFile(
				new File(MyContacts.letterfolder + "/" + labelname + ".pdf"));
		;
		FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF",
				"pdf");
		fc.setFileFilter(filter);
		;
		int returnVal = fc.showSaveDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			mcContacts thiscontactlist = new mcContacts();
			// String address = selcontact.makeBlockAddress("\n");
			thiscontactlist.put(selcontact);
			File fileToSave = fc.getSelectedFile();
			String filepath = fileToSave.getPath();
			File afile = new File(filepath);
			jswVerticalPanel panel = new jswVerticalPanel("options",false,false);
			jswOptionset options = new jswOptionset(this,"options",false,false);
			options.addNewOption("OK");
			options.addNewOption("Cancel");

			jswTextArea addressarea = new jswTextArea("address", true);
			addressarea.setText(address);
			jswDropDownBox pagelayout = new jswDropDownBox(this,"Layout");
			for (Entry<String, Map<String, String>> entry : MyContacts.labeltemplates
					.entrySet())
			{
				pagelayout.addItem(entry.getKey());
			}
			jswThumbwheel startpos = new jswThumbwheel(this,"Starting Position", 1,
					10);
			startpos.setValue(1);
			pagelayout.setPreferredSize(new Dimension(100, 50));
			panel.add(new jswLabel("Address"));
			panel.add(addressarea);
			panel.add(startpos);
			panel.add(pagelayout);
			JDialog dialog = null;
			JOptionPane optionPane = new JOptionPane();
			optionPane.setMessage("");
			optionPane.setMessageType(JOptionPane.PLAIN_MESSAGE);
			optionPane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
			optionPane.add(panel);
			dialog = optionPane.createDialog(null, "Print Label");
			dialog.setVisible(true);
			int pane = (Integer) optionPane.getValue();
			if (pane == 0)
			{
				int sp = startpos.getValue();
				address = addressarea.getText();
				String sellayout = pagelayout.getSelectedValue();
				System.out.println("selectedlayout " + sellayout);
				mcPDF labelpages = new mcPDF(afile, "Lerot Contacts Labels");
				labelpages.setLayout(sellayout);
				int ncount = labelpages.makeLabelPage(address, sp);
			}

		} else
		{
			System.out.println("Open command cancelled by user.");
		}
	}

	private void printletter(mcContact selcontact, String address)
	{
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Specify a file to save Letter");
		String lettername = mcLetter.makeFileName(selcontact);
		fc.setSelectedFile(new File(MyContacts.docsfolder + "/" + selcontact.getCID()
				+ "/" + lettername + ".odt"));
		FileNameExtensionFilter filter = new FileNameExtensionFilter("ODT",
				"odt");
		fc.setFileFilter(filter);
		int returnVal = fc.showSaveDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			String template = templateselector();
			if (template != null)
			{
				File fileToSave = fc.getSelectedFile();
				String filepath = fileToSave.getPath();
				lettername = fileToSave.getName();
				// MyContacts.docsfolder = fileToSave.getParent();
				try
				{
					// String address = selcontact.makeBlockAddress("\n");
					String salutation = mcLetter.getSalutation(selcontact);
					String printdate = mcDateDataType.getNow("dd MMM yyyy");
					String filedate = mcDateDataType.getNow("yyyy-MM-dd");
					mcLetter letter = new mcLetter();
					letter.setOutputFileName(filepath);
					letter.setTemplateFileName(
							MyContacts.topgui.dotcontacts + template);
					letter.setVariable("address", address);
					letter.setVariable("salutation", salutation);
					letter.setVariable("printdate", printdate);
					letter.printLetter();

				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		} else
		{
			System.out.println("Open command cancelled by user.");
		}
	}

	private void printVcard()
	{
		String name = selcontact.getName("sn fn");
		name = name.replace(" ", "");
		String date = mcDateDataType.getNow("_yyyyMMdd");
		String fname = vcarddirectory + "/" + name + date + ".vcf";
		File vfile = new File(fname);
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Specify a file for Vcard");
		FileNameExtensionFilter filter = new FileNameExtensionFilter("vcf",
				"vcf");
		fc.setFileFilter(filter);
		fc.setSelectedFile(vfile);
		int returnVal = fc.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			String vout = selcontact.toVcard();
			File fileToSave = fc.getSelectedFile();
			String filepath = fileToSave.getPath();

			try
			{
				vcarddirectory = fileToSave.getParentFile().getCanonicalPath();
				Writer writer = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(filepath), "utf-8"));
				writer.write(vout);
				writer.close();
			} catch (UnsupportedEncodingException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private jswStyles StylesMakeAttributeTable()
	{	
		jswStyles tablestyles = jswStyles.clone("AttributeTableStyles",MyContacts.getTableStyles());
		
		jswStyle tablestyle = tablestyles.makeStyle("table");
		tablestyle.putAttribute("backgroundColor", "#C0C0C0");
		tablestyle.putAttribute("foregroundColor", "Green");
		tablestyle.putAttribute("borderWidth", "2");
		tablestyle.putAttribute("borderColor", "blue");
		tablestyle.putAttribute("padding", "10");
		
		jswStyle rowstyle = tablestyles.makeStyle("row");
		rowstyle.putAttribute("height", "40");
		
		jswStyle cellstyle = tablestyles.makeStyle("cell");
		cellstyle.putAttribute("minheight", "40");
		cellstyle.putAttribute("cellbordercolor", "red");
		cellstyle.putAttribute("cellborderwidth", "1");
		cellstyle.putAttribute("padding", "10");

		jswStyle col0style = tablestyles.makeStyle("col_0");
		col0style.putAttribute("fontStyle", Font.BOLD);
		col0style.putAttribute("forecolor", "red");
		col0style.setHorizontalAlign("RIGHT");
		col0style.putAttribute("minwidth", "true");

		jswStyle col1style = tablestyles.makeStyle("col_1");
		col1style.putAttribute("fontStyle", Font.PLAIN);
		col1style.setHorizontalAlign("LEFT");

		jswStyle col2style = tablestyles.makeStyle("col_2");
		col2style.putAttribute("horizontalAlignment", "LEFT");
		col2style.putAttribute("minwidth", "true");

		jswStyle col3style = tablestyles.makeStyle("col_3");
		col3style.putAttribute("minwidth", "true");
		col3style.setHorizontalAlign("RIGHT");
		
		return tablestyles;
	}

	private jswStyles StylesMakeLinkTable()
	{
		jswStyles tablestyles = jswStyles.clone("LinkTableStyles",MyContacts.getTableStyles());

		jswStyle tablestyle = tablestyles.makeStyle("table");
		tablestyle.putAttribute("backgroundColor", "#C0C0C0");
		tablestyle.putAttribute("foregroundColor", "Green");
		tablestyle.putAttribute("borderWidth", "2");
		tablestyle.putAttribute("borderColor", "blue");

		jswStyle col0style = tablestyles.makeStyle("col_0");
		col0style.putAttribute("fontStyle", Font.BOLD);
		col0style.setHorizontalAlign("RIGHT");
		col0style.putAttribute("minwidth", "true");

		jswStyle col1style = tablestyles.makeStyle("col_1");
		col1style.putAttribute("fontStyle", Font.BOLD);
		col1style.setHorizontalAlign("LEFT");
		col1style.putAttribute("horizontalAlignment", "LEFT");
		
		jswStyle col2style = tablestyles.makeStyle("col_2");
		// col2style.putAttribute("horizontalAlignment", "RIGHT");
		col2style.putAttribute("minwidth", "true");

		jswStyle col3style = tablestyles.makeStyle("col_3");
		col3style.putAttribute("horizontalAlignment", "RIGHT");
		col3style.putAttribute("minwidth", "true");

		return tablestyles;
	}

	String templateselector()
	{
		jswDropDownBox templatelist = new jswDropDownBox((ActionListener)this,"template");
		for (String text : mcLetter.getTemplateList())
		{
			templatelist.addItem(text);
		}
		JDialog dialog = null;
		JOptionPane optionPane = new JOptionPane();
		optionPane.setMessage("");
		optionPane.setMessageType(JOptionPane.PLAIN_MESSAGE);
		optionPane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
		optionPane.add(templatelist);
		dialog = optionPane.createDialog(null, "Select Template");
		dialog.setVisible(true);
		int pane = (Integer) optionPane.getValue();
		if (pane == 0)
			return templatelist.getSelectedValue();
		else
			return null;
	}

	public static jswStyles searchTableStyles()
	{
		jswStyles tablestyles = new jswStyles();
		tablestyles.name = "defaulttable";
		jswStyle tablestyle = tablestyles.makeStyle("table");
		//tablestyle.putAttribute("backgroundColor", "#c0d6f2");
		tablestyle.putAttribute("borderwidth", "1");
		tablestyle.putAttribute("borderColor", "green");
		jswStyle cellstyle = tablestyles.makeStyle("cell");
		//	cellstyle.putAttribute("backgroundColor", "#c0d6f2");
		cellstyle.putAttribute("fontSize", "12");
		cellstyle.putAttribute("fontStyle", Font.PLAIN);
		cellstyle.putAttribute("borderColor", "black");
		cellstyle.putAttribute("borderWidth", 1);
		cellstyle.putAttribute("padding", 4);

		jswStyle rowstyle = tablestyles.makeStyle("row");
		//row0style.putAttribute("fontStyle", Font.BOLD + Font.ITALIC);
		rowstyle.putAttribute("minheight", 50);

		jswStyle cell00style = tablestyles.makeStyle("cell");
		//cell00style.putAttribute("foregroundColor", "transparent");
		cell00style.putAttribute("minheight", 50);
		jswStyle colstyle = tablestyles.makeStyle("col");
		colstyle.setHorizontalAlign("RIGHT");
		jswStyle col0style = tablestyles.makeStyle("col_0");
		col0style.putAttribute("fontStyle", Font.BOLD);
		col0style.putAttribute("fontSize", "16");
		col0style.putAttribute("backgroundColor", "PINK");
		col0style.putAttribute("minwidth", true);
		col0style.setHorizontalAlign("LEFT");

		jswStyle col1style = tablestyles.makeStyle("col_1");
		//col1style.putAttribute("fontStyle", Font.BOLD);
		//col1style.putAttribute("fontSize", "16");
		//col1style.putAttribute("backgroundColor", "BLUE");

		jswStyle col2style = tablestyles.makeStyle("col_2");
		//col1style.putAttribute("fontStyle", Font.BOLD);
		//col2style.putAttribute("fontSize", "16");
		col2style.putAttribute("minwidth", true);
		col2style.putAttribute("backgroundColor", "GREEN");
		jswStyle col3style = tablestyles.makeStyle("col_3");
		//col1style.putAttribute("fontStyle", Font.BOLD);
		//col2style.putAttribute("fontSize", "16");
		col3style.putAttribute("minwidth", true);
		col3style.putAttribute("backgroundColor", "GREEN");

		return tablestyles;
	}
}
