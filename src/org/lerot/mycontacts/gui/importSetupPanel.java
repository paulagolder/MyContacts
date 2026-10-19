package org.lerot.mycontacts.gui;

import org.lerot.mycontacts.*;
import org.lerot.mywidgets.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Vector;

public class importSetupPanel extends jswVerticalPanel implements
		ActionListener
{

	class ImportData implements Runnable
	{
		importSetupPanel parent;

		public ImportData(importSetupPanel aparetn)
		{
			parent = aparetn;
		}

		@Override
		public void run()
		{
			exceptions = MyContacts.topgui.imported.makeImport(false, messagelabel);
			if (exceptions.size() > 0) exceptionbutton.setVisible(true);
			importbutton.setVisible(false);
			showexceptions(exceptions);
			parent.repaint();
			System.out.println(" import  ended");
			MyContacts.topgui.startup();
		}
	}

	class TestData implements Runnable
	{
		importSetupPanel parent;

		public TestData(importSetupPanel aparetn)
		{
			parent = aparetn;
		}

		@Override
		public void run()
		{
			System.out.println(" test import  started");
			exceptions = testimports.makeImport(true, messagelabel);
			importtested = true;
			if (exceptions.size() > 0) exceptionbutton.setVisible(true);
			importbutton.setVisible(true);
			showexceptions(exceptions);
			importerrors.setVisible(true);
			importerrors.repaint();
			parent.repaint();
			System.out.println(" test import finished");
		}
	}

	private static final long serialVersionUID = 1L;
	jswButton exceptionbutton;
	LinkedHashMap<String, mcImportexception> exceptions;
	private File exportfile;
	JFileChooser fc;
	jswHorizontalPanel importbar;
	jswButton importbutton;
	private jswVerticalPanel importerrors;
	jswLabel messagelabel;
	File importfile;
	jswDropDownBox importsource;
	boolean importtested;
	// private boolean incard;
	jswButton selbutton;

	jswTextBox selectedfile;
	private jswButton testbutton;
	private String importfilename;
	private String importtype;
	private mcMappings importmappings;
	private mcImports testimports;

	public importSetupPanel()
	{
        super("import",false,false);
        importtested = false;
		showImportSetupPanel();
	}

	public void showImportSetupPanel()
	{

		this.removeAll();
		jswHorizontalPanel header = new jswHorizontalPanel();
		jswLabel heading = new jswLabel(" Setting up data import ");
		header.add(" FILLW ", heading);
		this.add(header);
		jswHorizontalPanel filebar = new jswHorizontalPanel();
		selbutton = new jswButton(this, "Select");
		filebar.add(" LEFT ", selbutton);
		selectedfile = new jswTextBox(this,"File name");
		selectedfile.setText("Import File");
		selectedfile.setEnabled(true);
		filebar.add(" LEFT WIDTH=200  ", selectedfile);
		selectedfile.setStyleAttribute(" mywidth ", 300);
		selectedfile.applyStyle();
		importsource = new jswDropDownBox(this,"source");
		Vector<String> varry = new Vector<String>();
		varry.add("csv");
		varry.add("gOutlookExport");
		varry.add("gGoogleExport");
		varry.add("Vcard");
		varry.add("Ldif");
		varry.add("XML");
		importsource.addList(varry);
		filebar.add(" WIDTH=200 ", importsource);

		jswButton savebutton = new jswButton(this, "Save");
		filebar.add(" RIGHT ", savebutton);
		this.add(filebar);
		importbar = new jswHorizontalPanel();
		testbutton = new jswButton(this, "Test Import");
		importbar.add(" LEFT  ", testbutton);
		importbar.setVisible(false);

		exceptionbutton = new jswButton(this, "Print Exceptions");
		importbar.add(" MIDDLE ", exceptionbutton);
		exceptionbutton.setVisible(false);

		importbutton = new jswButton(this, "Import File");
		importbar.add(" RIGHT ", importbutton);
		importbutton.setVisible(false);

		this.add(importbar);
		importerrors = new jswVerticalPanel("title",false,false);;
		jswLabel imptrace = new jswLabel("");
		importerrors.add(imptrace);
		imptrace.setVisible(true);
		imptrace.setText(" Starting Import ");
		importerrors.setVisible(false);
		this.add(" FILLH ", importerrors);
		try
		{
			if (importfile.canRead()) importbar.setVisible(true);

		} catch (Exception e)
		{

		}
		// filebar.setPreferredSize(new Dimension(0, 40));

		this.setVisible(true);
		// panel1.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		String command = e.getActionCommand();
		if (command == "Select")
		{
			JFileChooser fc = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					"Contacts", "csv", "vcf", "ldif", "kdif", "xml");
			fc.setFileFilter(filter);
			int returnVal = fc.showOpenDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION)
			{

				importfile = fc.getSelectedFile();
				selectedfile.setText(importfile.getName());
				selectedfile.repaint();
				String extension = "";
				int i = importfile.getName().lastIndexOf('.');
				if (i > 0)
				{
					extension = importfile.getName().substring(i + 1);
				}
				if (extension.equalsIgnoreCase("csv")) importsource
						.setSelected("GoogleContact");
				else if (extension.equalsIgnoreCase("vcf")) importsource
						.setSelected("Vcard");
				else if (extension.equalsIgnoreCase("ldif")
						|| extension.equalsIgnoreCase("kdif")) importsource
						.setSelected("Ldif");
				else if (extension.equalsIgnoreCase("xml"))
					importsource.setSelected("XML");
				importsource.repaint();
				exceptionbutton.setVisible(false);
				importbutton.setVisible(false);
				importerrors.setVisible(false);

			} else
			{
				System.out.println("Open command cancelled by user.");
			}

		} else if (command == "Print Exceptions")
		{
			JFileChooser fc = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
					"text", "text");
			fc.setFileFilter(filter);
			int returnVal = fc.showSaveDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				exportfile = fc.getSelectedFile();
				PrintWriter writer;
				try
				{
					writer = new PrintWriter(exportfile, "UTF-8");
					for (Entry<String, mcImportexception> except : exceptions
							.entrySet())
					{
						mcImportexception impex = except.getValue();
						writer.println(impex.getToken() + ","
								+ impex.getCount() + ",\"" + impex.getExample()
								+ "\"");
					}
					writer.close();
				} catch (FileNotFoundException | UnsupportedEncodingException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		} else if (command == "Save")
		{
			importfilename = importfile.getPath();
			importtype = importsource.getSelectedValue().toLowerCase();
			
			importmappings = MyContacts.topgui.currentcon.createMappings("import", importtype);
		
			try
			{
				new File(importfilename);
				importbar.setVisible(true);
				testbutton.setVisible(true);
				testbutton.setEnabled(true);
			} catch (Exception e2)
			{

			}
		} else if (command == "Test Import")
		{
			// System.out.println("Test Import");
			importerrors.removeAll();
			jswHorizontalPanel runningmessage = new jswHorizontalPanel();
			importerrors.add(" FILLW ", runningmessage);
			messagelabel = new jswLabel(" ");
			runningmessage.add(" FILLW ", messagelabel);
			importerrors.setVisible(true);
			 if (importtype.equalsIgnoreCase("vcard"))
			{
				System.out.println(" test import vcard ");
				testimports = new mcImportVcard(importfilename);
			} else if (importtype.equalsIgnoreCase("ldif"))
			{
				System.out.println(" test import ldif ");
				testimports = new mcImportLdif(importfilename);
			} else if (importtype.equalsIgnoreCase("xml"))
			{
				System.out.println(" test import xml ");
				testimports = new mcImportXML(importfilename);
			} else
				testimports = new mcImports(importfilename, importtype);
			this.repaint();

			Runnable runnable = new TestData(this);
			Thread thread = new Thread(runnable);
			thread.start();

		} else if (command == "Import File")
		{

			// System.out.println("Import File");
			testbutton.setEnabled(false);
			importerrors.removeAll();
			importerrors.setVisible(true);
			jswHorizontalPanel runningmessage = new jswHorizontalPanel();
			importerrors.add(" FILLW ", runningmessage);
			messagelabel = new jswLabel(" ");
			runningmessage.add(" FILLW ", messagelabel);
			this.repaint();
			if (importtype.equalsIgnoreCase("vcard"))
			{
				System.out.println("  import vcard ");
				MyContacts.topgui.imported = new mcImportVcard(importfilename);
			} else if (importtype.equalsIgnoreCase("ldif"))
			{
				System.out.println(" import ldif ");
				MyContacts.topgui.imported = new mcImportLdif(importfilename);
			} else if (importtype.equalsIgnoreCase("xml"))
			{
				System.out.println(" import xml ");
				MyContacts.topgui.imported = new mcImportXML(importfilename);
			} else
				MyContacts.topgui.imported = new mcImports(importfilename, importtype);
			MyContacts.topgui.imported.setImportFileName(importfilename);
			MyContacts.topgui.imported.setImportType(importtype);
			MyContacts.topgui.imported.setImportrownumber(1);
			this.repaint();
			Runnable runnable = new ImportData(this);
			Thread thread = new Thread(runnable);
			thread.start();
			System.out.println("  import finished");

		} else if (command.startsWith("MODIFY:"))
		{
			String foriegnkey = command.substring(7);
			String[] choices = { "ignore", "name", "address", "phone",
					"mobile", "email", "note","hasmember","related","memberof" , "anniversary", "birthday"};
			String input = (String) JOptionPane.showInputDialog(null,
					foriegnkey, "Add to importkeys as ",
					JOptionPane.QUESTION_MESSAGE, null, // Use
														// default
														// icon
					choices, // Array of choices
					choices[1]); // Initial choice
			if (input != null)
			{
				MyContacts.topgui.currentcon.insertMapping(importmappings, foriegnkey, input);
			}

		} else
			System.out.println(" command in importsetup not recognised "
					+ command);
		this.repaint();
	}

	private void showexceptions(
			LinkedHashMap<String, mcImportexception> exceptions)
	{

		int k = 0;
		importerrors.setVisible(true);
		importerrors.removeAll();
		importerrors.setMaximumSize(new Dimension(0, 400));
		jswVerticalPanel scrollpanel = new jswVerticalPanel("title",false,false);;
		JScrollPane scrollableTextArea = new JScrollPane(scrollpanel);

		scrollableTextArea
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollableTextArea
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		// importerrors.setBorder(setLineBorder(Color.pink, 4));
		importerrors.add(" FILLH ", scrollableTextArea);
		importerrors.repaint();
		int ek = 0;
		for (Entry<String, mcImportexception> except : exceptions.entrySet())
		{
			mcImportexception impex = except.getValue();
			if (impex.getCount() >= 0)
			{
				jswHorizontalPanel anexception = new jswHorizontalPanel();
				jswLabel alabel = new jswLabel(impex.getToken());
				anexception.add(" LEFT ", alabel);
				jswLabel avalue = new jswLabel("(" + impex.getCount() + ")");
				anexception.add(" LEFT ", avalue);
				jswLabel aexample = new jswLabel("(" + impex.getExample() + ")");
				anexception.add(" LEFT ", aexample);
				scrollpanel.add(anexception);
				jswButton modify = new jswButton(this, "modify", "MODIFY:"
						+ impex.getToken());
				anexception.add(" RIGHT ", modify);
				ek++;
			}
		}
		if (ek == 0)
		{
			jswHorizontalPanel anexception = new jswHorizontalPanel();
			jswLabel alabel = new jswLabel(" No non zero exceptions found ");
			anexception.add(" LEFT ", alabel);
			scrollpanel.add(anexception);
		}

	}

	public void refresh()
	{
		// TODO Auto-generated method stub
		
	}

}
