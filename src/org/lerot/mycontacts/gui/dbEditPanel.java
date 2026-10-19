package org.lerot.mycontacts.gui;

import org.lerot.mycontacts.mcDataSource;
import org.lerot.mycontacts.MyContacts;
import org.lerot.mywidgets.*;
import org.lerot.mywidgets.jswTextBox;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

public class dbEditPanel extends jswVerticalPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;
	private ButtonGroup bg;
	Vector<String> found;
	jswOptionset options;
	jswTextBox searchfield;
	private jswTable resulttable;
	jswTextBox newname;
	int mode = 0;

	private String selecteddb;
	private jswVerticalPanel newdbpanel;
	private jswVerticalPanel seldbpanel;
	private jswLabel selname;
	private String seldbname;
	private JComponent loaddbpanel;

	public dbEditPanel()
	{
        super("Edit",false,false);
        jswHorizontalPanel header = new jswHorizontalPanel();
		jswLabel heading = new jswLabel(" Edit Contact Databases ");
		header.add(" FILLW ", heading);
		this.add(header);
		jswHorizontalPanel printbar = new jswHorizontalPanel();
		jswButton refreshbutton = new jswButton(this, "refresh", "refresh");
		printbar.add(refreshbutton);
		this.add(printbar);
		resulttable = new jswTable(this,"dbfound", MyContacts.getTableStyles() );
		this.add(resulttable);

		newdbpanel = new jswVerticalPanel("title",false,false);;
		jswLabel label = new jswLabel(" Create new contact data base");
		newdbpanel.add(label);
		jswHorizontalPanel newbar = new jswHorizontalPanel();

		jswButton createnew = new jswButton(this, "make new", "makenew");
		newbar.add(createnew);
		newname = new jswTextBox(this,"");
		newbar.add(" FILLW ", newname);
		newdbpanel.add(newbar);
		newdbpanel.setVisible(false);
		this.add(newdbpanel);
		seldbpanel = new jswVerticalPanel("title",false,false);;
		jswLabel slabel = new jswLabel(" Select active contact data base");
		seldbpanel.add(slabel);
		jswHorizontalPanel selbar = new jswHorizontalPanel();
		jswButton sel = new jswButton(this, "select", "selectdb");
		selbar.add(sel);
		selname = new jswLabel("");
		selbar.add(" FILLW ", selname);
		jswButton def = new jswButton(this, "make default", "selectdef");
		selbar.add(def);
		seldbpanel.add(selbar);
		seldbpanel.setVisible(false);
		this.add(seldbpanel);
		loaddbpanel = new jswVerticalPanel("title",false,false);;
		jswLabel ldlabel = new jswLabel(" Import to new database");
		loaddbpanel.add(ldlabel);
		jswHorizontalPanel loadbar = new jswHorizontalPanel();
		jswButton load = new jswButton(this, "select import ", "import");
		loadbar.add(load);
		jswLabel loadname = new jswLabel("");
		loadbar.add(" FILLW ", loadname);
		loaddbpanel.add(loadbar);
		loaddbpanel.setVisible(false);
		this.add(loaddbpanel);
		refresh();
		this.repaint();
		MyContacts.topgui.mainpanel.repaint();
		MyContacts.topgui.getContentPane().validate();
	}

	@Override
	public void actionPerformed(ActionEvent evt)
	{
		String action = evt.getActionCommand().toUpperCase();
		System.out.println("action " + action);
		if (action.equalsIgnoreCase("select"))
		{
			newdbpanel.setVisible(false);
			seldbpanel.setVisible(false);
			loaddbpanel.setVisible(false);

			int row = 0;
			options.getSelected();
			for (String adb : found)
			{
				if (options.isSelected(adb))
				{
					System.out.println("selected db  " + adb);
					selecteddb = adb;
					mcDataSource seldb = new mcDataSource(selecteddb);
					Map<String, String> mychecks = seldb.checkmcdb();
					if (mychecks.get("Valid").equalsIgnoreCase("yes"))
					{
						System.out.println("selected db  :"
								+ mychecks.get("No of Contacts") + ":");
						if (adb.toLowerCase().contains("newcontact")
								&& (mychecks.get("No of Contacts")
										.equalsIgnoreCase("1")))
						{
							System.out.println("selected db  new :");
							mode = 1;
							newdbpanel.setVisible(true);
							newdbpanel.repaint();
						} else if (mychecks.get("No of Contacts")
								.equalsIgnoreCase("0"))
						{
							System.out.println("selected db empty :");
							mode = 2;
							seldbname = mychecks.get("dbname");
							selname.setText(selecteddb);
							loaddbpanel.setVisible(true);
							loaddbpanel.repaint();
						} else
						{
							System.out.println("selected db  old :");
							mode = 3;
							seldbname = mychecks.get("dbname");
							selname.setText(selecteddb);
							seldbpanel.setVisible(true);
							seldbpanel.repaint();
						}
						mode = 0;
					}
				}
				row++;
			}
		} else if (action.equalsIgnoreCase("refresh"))
		{
			refresh();
		} else if (action.equalsIgnoreCase("selectdef"))
		{
			saveParamChangesAsXML();
		} else if (action.equalsIgnoreCase("selectdb"))
		{

			MyContacts.topgui.currentcon = new mcDataSource(selecteddb);
			MyContacts.topgui.dbsource= selecteddb;
			//(new mcDataObject()).setConnection(MyContacts.topgui.currentcon);
			MyContacts.topgui.startup();
		}

		else if (action.equalsIgnoreCase("makenew"))
		{
			String newnamestr = newname.getText();
			String[] parts = newnamestr.split("\\.");
			if (!parts[0].isEmpty()
					&& !parts[0].toLowerCase().contains("newcontact"))
			{
				String dnewdbname = parts[0] + ".sqlite";
				File newfile = new File(MyContacts.topgui.dotcontacts + dnewdbname);
				File oldfile = new File(selecteddb);
				try
				{
					copyFileUsingStream(oldfile, newfile);
					System.out.println(" created :" + MyContacts.topgui.dotcontacts
							+ newnamestr);
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				refresh();
			} else
			{
				System.out.println(" name error in " + newnamestr);
			}

		} else
		{
			System.out.println("dbedit action " + action + " unrecognised ");
		}
		this.repaint();
		MyContacts.topgui.mainpanel.repaint();
		MyContacts.topgui.getContentPane().validate();
	}

	public void refresh()
	{
		found = mcDataSource.getdbs(MyContacts.topgui.dotcontacts);
		resulttable.removeAll();
		if (found == null || found.isEmpty())
		{
			resulttable.addCell(new jswLabel(" no contact databases found "),
					0, 0);
		} else
		{
			int dbfound = found.size();
			bg = new ButtonGroup();
			options = new jswOptionset( null,"options",false,false,false) ;
			int row = 0;
			for (String adbpath : found)
			{
				File afile = new File(adbpath);
				jswLabel fname = new jswLabel(afile.getName());
				jswLabel fpath = new jswLabel(afile.getPath());
				mcDataSource testdb = new mcDataSource(adbpath);
				Map<String, String> mychecks = testdb.checkmcdb();
				resulttable.addCell(fname, row, 0);
				resulttable.addCell(fpath, row, 1);
				resulttable.addCell(mychecks.get("Valid"), row, 2);
				resulttable.addCell(mychecks.get("No of Contacts"), row, 3);
				resulttable.addCell(mychecks.get("Latest Update"), row, 4);
				jswHorizontalPanel optionpanel = new jswHorizontalPanel();
				options.addNewOption( "select", adbpath,"select");
				// options[row].setToolTipText(acontact.getIDstr());
				//options[row].addActionListener(this);
				//bg.add(options[row]);
				optionpanel.add(" ", options);
				resulttable.addCell(optionpanel, row, 5);
				row++;
			}
		}

	}

	private static void copyFileUsingStream(File source, File dest)
			throws IOException
	{
		InputStream is = null;
		OutputStream os = null;
		try
		{
			is = new FileInputStream(source);
			os = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0)
			{
				os.write(buffer, 0, length);
			}
		} finally
		{
			is.close();
			os.close();
		}
	}

	public void saveParamChangesAsXML()
	{
		try
		{
			Properties props = MyContacts.topgui.props;
			props.setProperty("database", seldbname);
			File f = new File(MyContacts.topgui.propsfile);
			OutputStream out = new FileOutputStream(f);
			props.storeToXML(out, "mcContact  properties");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
