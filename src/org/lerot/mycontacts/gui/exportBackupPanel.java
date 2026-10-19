package org.lerot.mycontacts.gui;

import org.lerot.mycontacts.*;
import org.lerot.mywidgets.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

public class exportBackupPanel extends jswVerticalPanel
		implements ActionListener
{
	private static final long serialVersionUID = 1L;
	JFileChooser fc;
	jswHorizontalPanel importbar;
	jswButton backupbutton;
	private jswVerticalPanel exportlog;
	File exportfile;
	String exporttype;
	jswDropDownBox exporttypebox;
	jswButton selbutton;
	jswTextBox selectedfile;

	String extension = "csv";
	private String exportfilename;
	private jswLabel imptrace;
	private int crows, orows;
	private jswLabel countlabel;
	jswHorizontalPanel exportresult;
	private jswOptionset optionset;
	private mcContacts exportsource;
	private jswOption allcontacts;
	private jswOption browsecontacts;
	private jswOption selectedcontacts;
	private jswButton testbutton;
	private File fileToSave;

	public exportBackupPanel()
	{
        super("backup",false,false);
        // this.setBackground(Color.green);
		jswHorizontalPanel header = new jswHorizontalPanel();
		jswLabel heading = new jswLabel(" Backing Up Contacts ");
		header.add(" FILLW ", heading);
		this.add(header);
		jswHorizontalPanel filterbar = new jswHorizontalPanel();
		optionset = new jswOptionset(this,"source", false, false);
		allcontacts = optionset
				.addNewOption("All Contacts " + MyContacts.selbox.countAll(), "allcontacts");
		allcontacts.setTag("all");
		allcontacts.setStyleAttribute("mywidth",300);
		allcontacts.applyStyle();
		browsecontacts = optionset.addNewOption(
				"Browse Contacts " + MyContacts.selbox.getBrowsecontactlist().size(),"Browse");
		browsecontacts.setTag("browse");
		browsecontacts.setStyleAttribute("mywidth",300);
		browsecontacts.applyStyle();
		selectedcontacts = optionset.addNewOption(
				"Selected Contacts " + MyContacts.selbox.getSearchResultList().size(),
				"selected");
		selectedcontacts.setTag("selected");
		selectedcontacts.setStyleAttribute("mywidth",300);
		selectedcontacts.applyStyle();
		filterbar.add(" LEFT ", allcontacts);
		filterbar.add(" MIDDLE ", browsecontacts);
		browsecontacts.setSelected(true);

		filterbar.add(" RIGHT ", selectedcontacts);
		this.add(" FILLW ", filterbar);
		jswHorizontalPanel filebar = new jswHorizontalPanel();
		selbutton = new jswButton(this, "Select");
		filebar.add(" LEFT ", selbutton);
		String bufilename = MyContacts.topgui.budir + "/contacts_bu_"
				+ mcDateDataType.getNow("yyyyMMdd") + ".xml";
		selectedfile = new jswTextBox(this,bufilename,300,"6789");
		//selectedfile = new jswTextBox(this,bufilename);
		selectedfile.setText(bufilename);
		selectedfile.setEnabled(true);
		System.out.println(bufilename);
		filebar.add(" LEFT WIDTH=200 ", selectedfile);
		exporttypebox = new jswDropDownBox(this, "type");
		Vector<String> varry = new Vector<String>();
		varry.add("XML");
		exporttypebox.addList(varry);
		filebar.add(" LEFT WIDTH=200 ", exporttypebox);
		this.add(filebar);
		importbar = new jswHorizontalPanel();
		backupbutton = new jswButton(this, "BackUp");
		importbar.add(" MIDDLE  ", backupbutton);
		backupbutton.setVisible(true);
		this.add(importbar);
		exportresult = new jswHorizontalPanel();
		countlabel = new jswLabel("freddy");
		exportresult.add(" middle ", countlabel);
		countlabel.setVisible(false);
		exportresult.setVisible(true);
		this.add(exportresult);
		exportlog = new jswVerticalPanel("title",false,false);;
		imptrace = new jswLabel("");
		exportlog.add(imptrace);
		imptrace.setVisible(true);
		imptrace.setText(" Starting Backup ");
		exportlog.setVisible(false);
		this.add(" FILLH ", exportlog);
		importbar.setVisible(true);
		this.setVisible(true);
		refresh();
		this.refresh();
	}

	@Override
	public void actionPerformed(ActionEvent evt)
	{
		String cmd = evt.getActionCommand();
		System.out.println(" here we are ep  " + cmd);
		//HashMap<String, String> cmdmap = jswUtils.parsecsvstring(cmd);
		//String command = cmdmap.get("command");
        String command= cmd;
		if (command.equalsIgnoreCase("Select"))
		{
			JFileChooser fc = new JFileChooser();
			fc.setDialogTitle("Specify a file to save");
			String bufilename = selectedfile.getText();
			File file = new File(bufilename);
			fc.setCurrentDirectory(file.getParentFile());
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					"Backup", "xml", "XML");
			fc.setFileFilter(filter);
			fc.setSelectedFile(file);
			int returnVal = fc.showSaveDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				File fileToSave = fc.getSelectedFile();
				extension = "";
				int i = fileToSave.getName().lastIndexOf('.');
				if (i > 0)
				{
					extension = fileToSave.getName().substring(i + 1);
				}
				if (extension.equalsIgnoreCase("xml"))
					exporttypebox.setSelected("XML");
				exporttypebox.repaint();
				selectedfile.setText(fileToSave.getPath());
				exportfile = fileToSave;
			} else
			{
				System.out.println("Open command cancelled by user.");
			}
		} else if (command.equalsIgnoreCase("BackUp"))
		{

			Vector<String> attkeys = new Vector<String>();
			String selection = optionset.getSelected();
			System.out.println(selection);
			if (selection.equals("all"))
				exportsource = MyContacts.selbox.getAllcontactlist();
			else if (selection.equals("selected"))
			{
				exportsource = MyContacts.selbox.getSearchResultList();
			} else
				exportsource = MyContacts.selbox.getBrowsecontactlist();
			try
			{
				Vector<mcContact> sortedcontacts = exportsource
						.makeOrderedContactsVector();
				exportfilename = selectedfile.getText();
				new File(exportfilename);
				imptrace.setVisible(true);
				int outcount = backupXML(sortedcontacts, exportfilename);
				countlabel.setText(outcount + " records exported ");
				this.repaint();
			} catch (Exception e2)
			{

			}
			countlabel.setVisible(true);
			exportresult.setVisible(true);
			exportresult.repaint();
			countlabel.setVisible(true);
		} else if (command.equalsIgnoreCase("optionselected"))
		{
			exporttypebox.repaint();
			MyContacts.topgui.refreshView();
		}
		else
		{
			System.out.println(" export panel not found command " + command);
		}
		refresh();
		this.repaint();
	}

	public int backupXML(Vector<mcContact> outlist, String exportfilename)
	{
		HashMap<String, Integer> taglist = new HashMap<String, Integer>();
		mcAttribute anattrinbute = new mcAttribute(0);
		Vector<String> attkeylist = anattrinbute.dbloadAttributeKeyList();
		PrintWriter printWriter;
		int k = 0;

		try
		{
			printWriter = new PrintWriter(exportfilename);
			printWriter.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			printWriter.println(
					"<?xml-stylesheet type='text/xsl' href='./Stylesheets/ContactsStyler.xsl' ?>");
			printWriter.println("<contacts source=\"mycontacts\" version=\"" + MyContacts.version+"\" >\n");
			for (mcContact acontact : outlist)
			{
				k++;
				String tid = acontact.getTID();
				if(acontact.getCID() == 19)
				{
					System.out.println ( "found "+ tid);
				}
				String nameatt = acontact.toXML(attkeylist);
				if (nameatt != null) printWriter.println(nameatt);
				addTags(taglist, acontact);
			}

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

	public void refresh()
	{
		int ncontacts = MyContacts.selbox.countAll();
		int nsearchcontacts = MyContacts.selbox.getSearchResultList().size();
		int nbrowsecontacts = MyContacts.selbox.getBrowsecontactlist().size();
		allcontacts.setText("All Contacts (" + ncontacts + ")");
		browsecontacts.setText("Browse Contacts (" + nbrowsecontacts + ")");
		selectedcontacts.setText("Selected Contacts (" + nsearchcontacts + ")");
	}
}
