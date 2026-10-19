package org.lerot.mycontacts.gui;

import org.lerot.mycontacts.mcContact;
import org.lerot.mycontacts.mcContacts;
import org.lerot.mycontacts.MyContacts;
import org.lerot.mycontacts.mctagList;
import org.lerot.mywidgets.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map.Entry;

public class editListPanel extends jswVerticalPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;
	private static final int YES = 0;
	private jswDropDownBox taglistbox;
	private String tag;
	jswTextBox searchfield;
	private jswTable atttable;
	private jswButton deletebutton;
	private mcContacts selectedcontacts;
	private jswScrollPane scrollableTextArea;

	public editListPanel()
	{
        super("editlist",false,false);
        jswHorizontalPanel header = new jswHorizontalPanel();
		jswLabel heading = new jswLabel(" Edit Lists (Tags) ");
		header.add(" FILLW ", heading);
		this.add(header);

		jswHorizontalPanel toolbar = new jswHorizontalPanel("toolbar", false);
		deletebutton = new jswButton(this, "delete all", "deleteall");
		toolbar.add(deletebutton);
		this.add(" FILLW ", toolbar);
		jswHorizontalPanel progressbar = new jswHorizontalPanel("progressbar",
				false);
		this.add(" FILLW ", progressbar);
		taglistbox = new jswDropDownBox((ActionListener)this,"tags", "selectlist");
	
		mctagList tags = new mctagList();
		tags.reloadTags();
		progressbar.add(" FILLW ", taglistbox);
		atttable = new jswTable(this,"members", makeTagsTableStyles());
		atttable.setBackground(Color.lightGray);
		atttable.setBorder(BorderFactory.createLineBorder(Color.blue));
		taglistbox.setList(tags.getTaglist());
		tag = taglistbox.getSelectedValue();
		displaylist(tag);
		jswScrollPane scrollpane = new jswScrollPane(atttable,0, 0);
		scrollpane.setName("resultscroll");
		scrollpane
				.setBorder(BorderFactory.createLineBorder(Color.green));
		this.add(" FILLH FILLW ", scrollpane);
		//scrollableTextArea.setMaximumSize(new Dimension(600, 200));
		atttable.setVisible(true);
		scrollpane.setVisible(true);
		this.repaint();
		MyContacts.topgui.mainpanel.repaint();
		MyContacts.topgui.getContentPane().validate();

	}

    private jswStyles makeTagsTableStyles()
    {
        jswStyles tablestyles = MyContacts.getTableStyles();

        jswStyle tablestyle = tablestyles.makeStyle("table");
        tablestyle.putAttribute("backgroundColor", "White");
        tablestyle.putAttribute("foregroundColor", "Green");
        tablestyle.putAttribute("borderWidth", "2");
        tablestyle.putAttribute("borderColor", "blue");

        jswStyle cellstyle = tablestyles.makeStyle("cell");
        cellstyle.putAttribute("backgroundColor", "#C0C0C0");
        cellstyle.putAttribute("foregroundColor", "Blue");
        cellstyle.putAttribute("borderWidth", "1");
        cellstyle.putAttribute("borderColor", "white");
        cellstyle.setHorizontalAlign("LEFT");
        cellstyle.putAttribute("fontsize", "14");

        jswStyle cellcstyle = tablestyles.makeStyle("cellcontent");
        cellcstyle.putAttribute("backgroundColor", "transparent");
        cellcstyle.putAttribute("foregroundColor", "Red");
        cellcstyle.setHorizontalAlign("LEFT");
        cellcstyle.putAttribute("fontsize", "11");

        jswStyle col0style = tablestyles.makeStyle("col_0");
        col0style.putAttribute("fontStyle", Font.BOLD);
        col0style.setHorizontalAlign("RIGHT");
       // col0style.putAttribute("minwidth", "true");
        col0style.putAttribute("width", 100);

        jswStyle col1style = tablestyles.makeStyle("col_1");
        col0style.putAttribute("minwidth", "true");
        col1style.putAttribute("horizontalAlignment", "LEFT");

        jswStyle col2style = tablestyles.makeStyle("col_2");
        col2style.putAttribute("horizontalAlignment", "RIGHT");
        col2style.putAttribute("minwidth", "true");

        jswStyle col3style = tablestyles.makeStyle("col_3");
        col3style.putAttribute("FILLW", "true");

        jswStyle col4style = tablestyles.makeStyle("col_4");
        col4style.putAttribute("fontStyle", Font.BOLD);
        col4style.putAttribute("minwidth", "true");
        col4style.putAttribute("width", 100);

        jswStyle col5style = tablestyles.makeStyle("col_5");
        col5style.putAttribute("horizontalAlignment", "RIGHT");
        col5style.putAttribute("minwidth", "true");

        jswStyle col6style = tablestyles.makeStyle("col_6");
        col6style.putAttribute("horizontalAlignment", "RIGHT");
        col6style.putAttribute("minwidth", "true");

        return tablestyles;
    }
	@Override
	public void actionPerformed(ActionEvent evt)
	{
		String action = evt.getActionCommand().toUpperCase();

		if (action.equals("SELECTLIST"))
		{
			tag = taglistbox.getSelectedValue();
			displaylist(tag);

		} else if (action.equals("DELETEALL"))
		{
			String cnstr = action.substring(7);
			System.out.println("delete all listed contact ");
			int dcount = taglistbox.getItemCount();
			int n = JOptionPane.showConfirmDialog(this,
					"Do you want to delete " + dcount + " contacts",
					"DELETE CONTACTS?", JOptionPane.YES_NO_OPTION);
			System.out.println("reply =" + n);
			if (n == YES)
			{
				selectedcontacts.deleteAllContacts();
			}
			displaylist(tag);
		} else if (action.startsWith("DELETE"))
		{
			String cnstr = action.substring(7);
			System.out.println("delete contact " + cnstr);
			mcContact selcon = mcContacts.retrieveContact(cnstr);
			int n = JOptionPane.showConfirmDialog(this,
					"Do you want to delete contact:" + selcon.getName(),
					"DELETE CONTACT?", JOptionPane.YES_NO_OPTION);

			if (n == YES)
			{
				selcon.deleteContact();
				System.out.println("reply =" + n);
			}
			displaylist(tag);
		} else if (action.startsWith("REMOVE"))
		{
			String cnstr = action.substring(7);
			System.out.println("remove contact " + cnstr);
			mcContact selcon = mcContacts.retrieveContact(cnstr);
			int n = JOptionPane.showConfirmDialog(this,
					"Do you want to remove tag " + tag + " from :"
							+ selcon.getName(),
					"REMOVE CONTACT?", JOptionPane.YES_NO_OPTION);
			if (n == YES)
			{
				selcon.deleteTag(tag);
				// System.out.println("reply =" + n);
			}
			displaylist(tag);
		} else
			System.out.println("contact  action " + action + " unrecognised ");

		MyContacts.topgui.getContentPane().validate();
	}

	public void displaylist(String tag)
	{

		selectedcontacts = MyContacts.selbox.searchTag(tag);
		atttable.removeAll();
		int i = 0;
		if(selectedcontacts != null)
		{
		for (Entry<String, mcContact> contactentry : selectedcontacts
				.entrySet())
		{

			mcContact ct = contactentry.getValue();
			String cname = contactentry.getValue().getName();
			atttable.addCell(cname, i, 0);
			jswButton removecontact = new jswButton(this, "REMOVE",
					"REMOVE:" + ct.getIDstr());
			atttable.addCell(removecontact, i, 2);
			jswButton deletecontact = new jswButton(this, "DELETE",
					"DELETE:" + ct.getIDstr());
			atttable.addCell(deletecontact, i, 3);
			i++;
			// System.out.println(" adding "+ ct);
		}
		atttable.repaint();
		}

	}

	public void initialise()
	{

		mctagList tags = new mctagList();
		tags.reloadTags();
		taglistbox.setList(tags.getTaglist());
		if(tags.size()>0)
		{
			displaylist(tags.get(0));
		}
		this.repaint();
		MyContacts.topgui.mainpanel.repaint();
		MyContacts.topgui.getContentPane().validate();
		// taglist.addActionListener(this, "selectlist");

	}

	

}
