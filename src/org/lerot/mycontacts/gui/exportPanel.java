package org.lerot.mycontacts.gui;

import org.lerot.mycontacts.mcContacts;
import org.lerot.mycontacts.mcDateDataType;
import org.lerot.mycontacts.mcMappings;
import org.lerot.mycontacts.MyContacts;
import org.lerot.mywidgets.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Map.Entry;
import java.util.Vector;

public class exportPanel extends jswVerticalPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;
	JFileChooser fc;
	jswHorizontalPanel importbar;
	jswButton exportbutton;
	private jswVerticalPanel exportlog;
	File exportfile;
	String exporttype;
	jswDropDownBox exporttypebox;
	jswButton selbutton;
	jswTextBox selectedfile;
	String extension = "xml";
	private String exportfilename;
	private jswVerticalPanel selectors;
	private jswVerticalPanel options;
	private jswLabel imptrace;
	private jswCheckbox[] checkbox;
	private jswCheckbox[] option;
	private int crows,orows;
	private jswLabel countlabel;
	jswHorizontalPanel exportresult;
	private mcContacts exportsource;
	private jswButton testbutton;
	private jswLabel browselabel;
	private jswButton savebutton;

	public exportPanel()
	{
        super("export",false,false);
        // this.setBackground(Color.green);
		jswHorizontalPanel header = new jswHorizontalPanel();
		jswLabel heading = new jswLabel(" Exporting Data ");
		header.add(" FILLW ", heading);
		this.add(header);
		jswHorizontalPanel filterbar = new jswHorizontalPanel();
        browselabel = new jswLabel(" ");
        browselabel.setText("Browse Contacts "+ MyContacts.selbox.getBrowsecontactlist().size());
		filterbar.add(" MIDDLE ", browselabel);
		this.add(filterbar);
		jswHorizontalPanel filebar = new jswHorizontalPanel();
		selbutton = new jswButton(this, "Select");
		filebar.add(" LEFT ", selbutton);
		selectedfile = new jswTextBox(this," ");
		String date = mcDateDataType.getNow("_yyyyMMdd");
		String  filename = "Export_contacts_" + date;
		selectedfile.setText(filename);
		selectedfile.setEnabled(true);
		filebar.add(" LEFT WIDTH=200 ", selectedfile);
		exporttypebox = new jswDropDownBox(this,"type");
		Vector<String> varry = new Vector<String>();
		varry.add("xml");
		varry.add("CSV");
		varry.add("gOutlookExport");
		varry.add("gGoogleExport");
		varry.add("Vcard");
		varry.add("Ldif");
		varry.add("Ical");
		varry.add("XML");
		exporttypebox.addList(varry);
		filebar.add(" LEFT WIDTH=50 ", exporttypebox);
		checkbox = new jswCheckbox[20];
		for (int i = 0; i < 20; i++)
		{
			checkbox[i] = new jswCheckbox(this,"*");
		}
		option = new jswCheckbox[20];
		for (int i = 0; i < 20; i++)
		{
			option[i] = new jswCheckbox(this,"*");
		}
		savebutton = new jswButton(this, "Save");
		filebar.add(" RIGHT ", savebutton);
		this.add(filebar);
		importbar = new jswHorizontalPanel();
		exportbutton = new jswButton(this, "Export File");
		importbar.add(" MIDDLE  ", exportbutton);
		exportbutton.setVisible(false);
		testbutton = new jswButton(this, "Davtest");
		importbar.add(" RIGHT  ", testbutton);
		testbutton.setVisible(true);
		this.add(importbar);
	
		exportresult = new jswHorizontalPanel();
		countlabel = new jswLabel("freddy");
		exportresult.add(" middle ", countlabel);
		countlabel.setVisible(false);
		exportresult.setVisible(true);
		this.add(exportresult);
		jswHorizontalPanel optionpanel = new jswHorizontalPanel();
		selectors = new jswVerticalPanel("title",false,false);;
		selectors.setVisible(false);
		optionpanel.add(" FILLH ", selectors);
		options = new jswVerticalPanel("title",false,false);;
		options.setVisible(false);
		optionpanel.add(" MIDDLE ", options);
	
		this.add(" FILLH ",optionpanel);
		
		exportlog = new jswVerticalPanel("title",false,false);;
		imptrace = new jswLabel("");
		exportlog.add(imptrace);
		imptrace.setVisible(true);
		imptrace.setText(" Starting Export");
		exportlog.setVisible(false);
		this.add(" FILLH ", exportlog);
		importbar.setVisible(true);

		this.setVisible(true);

	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		String command = e.getActionCommand();
		if (command == "Select")
		{
			exporttype = exporttypebox.getSelectedValue();		
			JFileChooser fc = new JFileChooser();
			fc.setDialogTitle("Specify a file to save");		
		    fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					"Contacts", "csv", "vcf", "ldif", "kdif", "ics", "xml");
			fc.setFileFilter(filter);
			String bufilename = selectedfile.getText()+"."+exporttype;
			File file = new File(bufilename);	
			fc.setSelectedFile(file);
			fc.setCurrentDirectory(new File(MyContacts.topgui.dotcontacts));
			int returnVal = fc.showSaveDialog(this);
			System.out.println(" return value =" + returnVal); 
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				File fileToSave = fc.getSelectedFile();
				extension = "";
				int i = fileToSave.getName().lastIndexOf('.');
				if (i > 0)
				{
					extension = fileToSave.getName().substring(i + 1);
				}
				if (extension.equalsIgnoreCase("csv"))
					exporttypebox.setSelected("CSV");
				else if (extension.equalsIgnoreCase("vcf"))
					exporttypebox.setSelected("Vcard");
				else if (extension.equalsIgnoreCase("ics"))
					exporttypebox.setSelected("Ical");
				else if (extension.equalsIgnoreCase("ldif")
						|| extension.equalsIgnoreCase("kdif"))
					exporttypebox.setSelected("Ldif");
				else if (extension.equalsIgnoreCase("xml"))
					exporttypebox.setSelected("XML");
				exporttypebox.repaint();
				selectedfile.setText(fileToSave.getName());
				exportfile = fileToSave;
				System.out.println(" export =" + exportfile ); 
				exportbutton.setVisible(true);			
			} else
			{
				System.out.println("Open command cancelled by user.");
			}
		} else if (command == "Save")
		{
			exportfilename = exportfile.getPath();
			exporttype = exporttypebox.getSelectedValue();		
			System.out.println(" et=" + exporttype); 	
			new File(exportfilename +"."+ exporttype);
			exportbutton.setVisible(true);
			countlabel.setVisible(false);
			selectors.removeAll();
			selectors.setVisible(true);
			options.removeAll();
			options.setVisible(true);
			mcMappings mappings = MyContacts.topgui.currentcon
					.createMappings("export", exporttype);
			if (exporttype.equalsIgnoreCase("ical"))
			{
				checkbox[0] = new jswCheckbox(this,"Birthday");
				selectors.add(" INDENT=20 ", checkbox[0]);
				checkbox[1] = new jswCheckbox(this,"Anniversary");
				selectors.add(" INDENT=20 ", checkbox[1]);
				crows = 2;
			} else if (exporttype.equalsIgnoreCase("csv"))
			{
				System.out.println(" et=" + exporttype);
				crows = 0;
				for (Entry<String, String> entry : mappings.entrySet())
				{
					String localstring = entry.getKey();
					checkbox[crows] = new jswCheckbox(this,localstring);
					selectors.add(" INDENT=20 ", checkbox[crows]);
					checkbox[crows].setVisible(true);
					checkbox[crows].setSelected(true);
					crows++;
				}
			} else if (exporttype.equalsIgnoreCase("vcard"))
			{
				System.out.println(" et=" + exporttype);
				crows = 0;
				for (Entry<String, String> entry : mappings.entrySet())
				{
					String localstring = entry.getKey();
					checkbox[crows] = new jswCheckbox(this,localstring);
					selectors.add(" INDENT=20 ", checkbox[crows]);
					checkbox[crows].setVisible(true);
					checkbox[crows].setSelected(true);
					crows++;
				}
				
				option[0] = new jswCheckbox(this,"owncloud");
				options.add(" INDENT=20 ", option[0]);
				option[0].setVisible(true);
				option[0].setSelected(false);
				orows = 1;
			}
			else
			{
				crows = 0;
				for (Entry<String, String> entry : mappings.entrySet())
				{
					String localstring = entry.getKey();
					checkbox[crows] = new jswCheckbox(this,localstring);
					selectors.add(" INDENT=20 ", checkbox[crows]);
					checkbox[crows].setVisible(true);
					checkbox[crows].setSelected(true);
					crows++;
				}
				if (exporttype.equalsIgnoreCase("xml"))
				{
					checkbox[crows] = new jswCheckbox(this,"Tag List");
					selectors.add(" INDENT=20 ", checkbox[crows]);
					checkbox[crows].setVisible(true);
					checkbox[crows].setSelected(true);
					crows++;
				}
			}
			selectors.setVisible(true);
			selectors.repaint();

		} else if (command == "Export File")
		{

			Vector<String> attkeys = new Vector<String>();
			for (int i = 0; i < crows; i++)
			{
				if (checkbox[i].isSelected())
				{
					String localstring = checkbox[i].getLabel();
					attkeys.add(localstring.toLowerCase());
				}
			}
			Vector<String> optlist= new Vector<String>();
			for (int i = 0; i < orows; i++)
			{
				if (option[i].isSelected())
				{
					String localstring = option[i].getLabel();
					// System.out.println(" mappings selected =" + localstring);
					optlist.add(localstring.toLowerCase());
				}
			}
			selectors.setVisible(true);
			selectors.repaint();
			exportsource = MyContacts.selbox.getBrowsecontactlist();
			try
			{
				new File(exportfilename);
				imptrace.setVisible(true);
				int outcount = exportsource.savecontacts(exportfilename,
						exporttype, attkeys, optlist);
				countlabel.setText(outcount + " records exported ");
				this.repaint();
			} catch (Exception e2)
			{

			}
			countlabel.setVisible(true);
			exportresult.setVisible(true);
			exportresult.repaint();
			countlabel.setVisible(true);
		} else if (command == "Davtest")
		{
			// webdav dav = new webdav();
			// dav.getfile();
		}

	}

	public void refresh()
	{
		//int nbrowsecontacts = MyContacts.selbox.getBrowsecontactlist().size();
		int nbrowsecontacts = 1234;
		browselabel.setText("Browse Contacts (" + nbrowsecontacts + ")");
	}
}
