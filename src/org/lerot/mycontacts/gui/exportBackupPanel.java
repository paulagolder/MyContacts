package org.lerot.mycontacts.gui;

import org.lerot.mycontacts.*;
import org.lerot.mycontacts.gui.widgets.ContactSource;
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
	private mcContacts exportsource;
	private ContactSource source;
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
		source = new ContactSource();
		this.add(" FILLW ", source);
		jswHorizontalPanel filebar = new jswHorizontalPanel();
		selbutton = new jswButton(this, "Select");
		filebar.add(" LEFT ", selbutton);
		String bufilename = MyContacts.topgui.budir + "/contacts_bu_"
				+ mcDateDataType.getNow("yyyyMMdd") + ".xml";
		selectedfile = new jswTextBox(this,bufilename,350,"6789");
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
			exportsource = source.contacts();
			try
			{
				Vector<mcContact> sortedcontacts = exportsource
						.makeOrderedContactsVector();
				exportfilename = selectedfile.getText();
				new File(exportfilename);
				imptrace.setVisible(true);
				int outcount = mcContacts.writeBackupXML(sortedcontacts, exportfilename);
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

	public void refresh()
	{
		source.refresh();
	}
}
