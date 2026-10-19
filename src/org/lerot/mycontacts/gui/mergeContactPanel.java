package org.lerot.mycontacts.gui;

import org.lerot.mycontacts.*;
import org.lerot.mywidgets.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;

public class mergeContactPanel extends jswVerticalPanel implements
		ActionListener
{
	private static final long serialVersionUID = 1L;
	private ButtonGroup bg;
	private jswCheckbox[] checkboxlist;
	mcContacts found;
	private Vector<mcContact> mergefromlist;
	int nsearchcontacts;
	jswOptionset options;
	private jswLabel prog;
	jswTextBox searchfield;
	private jswTable resulttable;

	public mergeContactPanel()
	{
        super("mergecontacts",false,false);

        jswHorizontalPanel header = new jswHorizontalPanel();
		jswLabel heading = new jswLabel(" Merge Contacts ");
		header.add(" FILLW ", heading);
		this.add(header);
		jswHorizontalPanel printbar = new jswHorizontalPanel();
		this.add(printbar);
		jswButton testbutton = new jswButton(this, "MERGE");
		printbar.add(" MIDDLE ", testbutton);
		resulttable = new jswTable(this,"contactsfound", MyContacts.getTableStyles() );

		this.add(resulttable);
		jswHorizontalPanel progressbar = new jswHorizontalPanel();
		this.add(progressbar);
		prog = new jswLabel(" Selecting contacts to merge");
		progressbar.add(" FILLW ", prog);
		this.repaint();
		MyContacts.topgui.mainpanel.repaint();
		MyContacts.topgui.getContentPane().validate();

	}

	@Override
	public void actionPerformed(ActionEvent evt)
	{
		String cmd = evt.getActionCommand().toUpperCase();
		// TreeSet<mcContact> found = new TreeSet<mcContact>();
		//System.out.println("action " + action);


       // String cmd = evt.getActionCommand();
        System.out.println(" here we are mcp " + cmd);
       // HashMap<String, String> cmdmap = jswUtils.parsecsvstring(cmd);
       // String action= cmdmap.get("COMMAND");
      String  action = cmd;

		if (action.equals("MERGE"))
		{
			mergefromlist = new Vector<mcContact>();
			mcContact mergeto = null;
			int row = 0;
			for (mcContact acontact : found.makeOrderedContactsVector())
			{
				if (options.isSelected(row))
				{
					System.out.println("merge to " + acontact);
					mergeto = acontact;
				}
				if (checkboxlist[row].isSelected())
				{
					mergefromlist.add(acontact);
				}
				row++;
			}
			if (mergeto != null && mergefromlist.size() > 0)
			{
				for (mcContact acontact : mergefromlist)
				{
					mergeto.mergeContact(acontact);
					System.out.println("deleteing" + acontact);
					MyContacts.selbox.remove(acontact);
                    acontact.deleteContact();
				}
                System.out.println( mergeto.toString());
                mergeto.getAttributes().printList("++");
                mergeto.updateContact();
			}
			System.out.println("merging");
		} else
			System.out.println("contact merge action " + action
					+ " unrecognised ");

		MyContacts.topgui.getContentPane().validate();
	}

	private jswTable makeAttributePanel(mcContact contact, String selector)
	{
		jswTable attributepanel = new jswTable(this, "attributes",
				MyContacts.getTableStyles() );
		mcAttributes attributes = contact.getAttributes();
		int row = 0;

		for (Entry<String, mcAttribute> anentry : attributes.entrySet())
		{
			mcAttribute anattribute = anentry.getValue();
			if (anattribute != null)
			{

				String attributekey = anattribute.getKey();
				jswLabel alabel = new jswLabel(attributekey);
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
				row++;

			}

		}
		return attributepanel;
	}

	public void refresh()
	{
		nsearchcontacts = MyContacts.selbox.getSearchResultList().size();
		found = MyContacts.selbox.getSearchResultList();

		resulttable.removeAll();
		if (nsearchcontacts == 0)
		{
			resulttable.addCell(new jswLabel(" no contact selected "), 0, 0);
		} else if (nsearchcontacts < 4)
		{
			checkboxlist = new jswCheckbox[nsearchcontacts];
			bg = new ButtonGroup();
			options = new jswOptionset(this ,"mergeoptions", true ,true);
			// this.setPreferredSize(new Dimension(0, 800));
			// resulttable.setPreferredSize(new Dimension(0, 800));

			int row = 0;
			for (mcContact acontact : found.makeOrderedContactsVector())
			{
				jswLabel atid = new jswLabel(acontact.getIDstr());
				jswLabel atTID = new jswLabel(acontact.getTID());
				resulttable.addCell(atid, row, 0);
				resulttable.addCell(atTID, row, 1);
				jswTable atttable = makeAttributePanel(acontact, "");
				resulttable.addCell(atttable, row, 2);
				jswHorizontalPanel optionpanel = new jswHorizontalPanel();
				checkboxlist[row] = new jswCheckbox(this,"from");
				jswOption anoption = options.addNewOption("to");
				anoption.setToolTipText(acontact.getIDstr());
				//anoption.addActionListener(this);
				//bg.add(options[row]);
				optionpanel.add(checkboxlist[row]);
				optionpanel.add(anoption);
				resulttable.addCell(optionpanel, row, 3);
				row++;
			}

		} else
		{
			resulttable.addCell(new jswLabel(" Too many contacts selected (>3) "),
					0, 0);
		}

		this.repaint();
		MyContacts.topgui.mainpanel.repaint();
		MyContacts.topgui.getContentPane().validate();

	}
}
