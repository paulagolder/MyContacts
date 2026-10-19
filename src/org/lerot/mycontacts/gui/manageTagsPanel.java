package org.lerot.mycontacts.gui;

import org.lerot.mycontacts.mcContacts;
import org.lerot.mycontacts.MyContacts;
import org.lerot.mycontacts.mctagList;
import org.lerot.mywidgets.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map.Entry;

public class manageTagsPanel extends jswVerticalPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;
	private static final int YES = 0;
	jswTextBox searchfield;
	int nsearchcontacts;
	mcContacts found;
	JRadioButton[] options;
	private jswCheckbox[] checkboxes;
	private mctagList tagList;
	private jswTable atttable;
	private jswStyles tagstablestyles;

	@Override
	public void actionPerformed(ActionEvent evt)
	{
		String action = evt.getActionCommand().toUpperCase();
		System.out.println("tags panel action " + action);
		int row = 0;
		if (action.equals("ACTION"))
		{
          /*   if(cmdmap.get("HANDLERNAME").equalsIgnoreCase("JCHECKBOX"))
			{
				String nrow = cmdmap.get("ROW");
				String ncol= cmdmap.get("COLUMN");
                checkboxes[row].setSelected(true);
			}*/
            System.out.println("tags panel action OH DEAR ");
		}
		if (action.equals("MERGE"))
		{
			String mergeto = null;
			for (jswCheckbox checkbox : checkboxes)
			{
				if (checkbox != null && checkbox.isSelected())
				{
					if (mergeto == null)
					{
						mergeto = checkbox.getTag();
						System.out.println("merge to  " + mergeto);
					} else
					{
						System.out.println("merge  " + checkbox.getTag());
						row++;
					}
				}
			}
			if (row < 1)
				System.out.println("nothing to merge");
			else
			{

				mergeto.replace(";", "");
				mergeto = mergeto + ";";
				for (jswCheckbox checkbox : checkboxes)
				{
					if (checkbox != null && checkbox.isSelected())
					{
						String mergefrom = checkbox.getTag();
						mergefrom.replace(";", "");
						mergefrom = mergefrom + ";";
						if (!mergeto.equals(mergefrom))
						{
							tagList.replaceall("tags", mergefrom, mergeto);
						}
					}
				}
			}
			System.out.println("merging");
		} else if (action.equals("DELETE"))
		{

			for (jswCheckbox checkbox : checkboxes)
			{
				if (checkbox != null && checkbox.isSelected())
				{
					System.out.println("delete  " + checkbox.getTag());
					row++;
				}
			}
			if (row < 1)
				System.out.println("nothing to delete");
			else
			{
				int n = JOptionPane.showConfirmDialog(this,
						"Do you want to delete " + row + " tags?",
						"DELETE TAGS?", JOptionPane.YES_NO_OPTION);
				if (n == YES)
				{
					for (jswCheckbox checkbox : checkboxes)
					{
						if (checkbox != null && checkbox.isSelected())
						{
							String todelete = checkbox.getTag();
                            tagList.delete("tags",todelete);
							//(new mctagList()).delete("tags", todelete);

						}
					}
				}
			}
			System.out.println("deleting");

		} else if (action.equals("NORM"))
		{
			tagList.renorm();
			System.out.println("norminging");
		} else if (action.equals("RENAME"))
		{
			for (jswCheckbox checkbox : checkboxes)
			{
				if (checkbox != null && checkbox.isSelected())
				{
					System.out.println("renaming  " + checkbox.getTag());
					row++;
				}
			}
			if (row < 1) System.out.println("nothing to rename");
			String newtag = JOptionPane.showInputDialog("Enter New tag name");
			if (newtag != null)
			{
				newtag.replace("#", "");
				newtag.replace(";", "");
				newtag = newtag + ";";
				if (newtag.length() > 4)
				{

					for (jswCheckbox checkbox : checkboxes)
					{
						if (checkbox != null && checkbox.isSelected())
						{
							String changefrom = checkbox.getTag();
							tagList.replaceall("tags", changefrom, newtag);
							row++;
						}
					}
				}
			}

		} else if (action.equals("DUPLICATE"))
		{
			row = 0;
			for (jswCheckbox checkbox : checkboxes)
			{
				//System.out.println("new name  " + checkbox.getTag());
				if (checkbox != null && checkbox.isSelected())
				{
					System.out.println("new name  " + checkbox.getTag());
					row++;
				}
			}
			if (row < 1) System.out.println("nothing to copy");
			String newtag = JOptionPane.showInputDialog("Enter New tag name");
			if (newtag != null)
			{
				newtag.replace("#", "");
				newtag.replace(";", "");
				newtag =  newtag + ";";
				if (newtag.length() > 4)
				{

					for (jswCheckbox checkbox : checkboxes)
					{
						if (checkbox != null && checkbox.isSelected())
						{
							String changefrom = checkbox.getTag();
							tagList.duplicateall("tags", changefrom, newtag);
							row++;
						}
					}
				}
			}

		} else
			System.out
					.println("label print action " + action + " unrecognised ");
		tagList.reloadTags();

		buildTagPanel();
		atttable.repaint();
		MyContacts.topgui.getContentPane().validate();
	}

	public manageTagsPanel()
	{
		super("managetagspanel",false,false);
		tagstablestyles = makeTagsTableStyles();
		tagList = new mctagList();
		jswHorizontalPanel header = new jswHorizontalPanel();
		jswLabel heading = new jswLabel("Manage Tags");
		header.add(" FILLW ", heading);
		this.add(header);
		jswHorizontalPanel scorebar = new jswHorizontalPanel();
		this.add(scorebar);
		jswLabel score;
		tagList.reloadTags();
		// tagList.getAllTags();
		if (tagList == null || tagList.isEmpty())
		{
			score = new jswLabel("No tags found ");
		} else
			score = new jswLabel(
					" " + tagList.size() + " different tags found ");
		scorebar.add(score);
		jswHorizontalPanel printbar = new jswHorizontalPanel();
		this.add(printbar);
		found = MyContacts.selbox.getSearchResultList();
		jswButton testbutton = new jswButton(this, "MERGE");
		printbar.add(" MIDDLE ", testbutton);
		jswButton deletebutton = new jswButton(this, "DELETE");
		printbar.add(" MIDDLE ", deletebutton);
		jswButton renamebutton = new jswButton(this, "RENAME");
		printbar.add(" MIDDLE ", renamebutton);
		jswButton duplicatebutton = new jswButton(this, "DUPLICATE");
		printbar.add(" MIDDLE ", duplicatebutton);
		jswButton normbutton = new jswButton(this, "NORM");
		printbar.add(" MIDDLE ", normbutton);
		atttable = new jswTable(null,"tags", tagstablestyles);
		//jswScrollPane scrollableTextArea = new jswScrollPane(atttable, -4, -4);
		jswScrollPane scrollableTextArea = new jswScrollPane(atttable, 10, 10);
		scrollableTextArea.setName("resultscroll");
		scrollableTextArea.setVisible(true);
		scrollableTextArea.setHorizontalScrollBarPolicy(
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollableTextArea.setVerticalScrollBarPolicy(
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.add(" FILLH FILLW ", scrollableTextArea);

		atttable.setVisible(true);
		buildTagPanel();
        atttable.repaint();
        scrollableTextArea.repaint();
		this.repaint();
		MyContacts.topgui.mainpanel.repaint();
		MyContacts.topgui.getContentPane().validate();

	}

	public void buildTagPanel()
	{
		atttable.removeAll();
		int row = 0;
		int col = 0;
		int cb=0;
		checkboxes = new jswCheckbox[40];
		for (Entry<String, Integer> anentry : tagList.entrySet())
		{
			String attkey = anentry.getKey();
			int attcount = anentry.getValue();
			jswLabel alabel = new jswLabel(attkey);
			jswLabel acount = new jswLabel(attcount);
			atttable.addCell(alabel, row, col);
			atttable.addCell(acount, row, col + 1);
			checkboxes[cb] = new jswCheckbox(null,"");
			checkboxes[cb].setTag(attkey);
			atttable.addCell(checkboxes[cb], row, col + 2);
			//System.out.println("adding tag:"+attkey);
			row++;
			cb++;
			if (row > 10)
			{
				row = 0;
				col = col + 4;
			}
		}
		atttable.setVisible(true);
        atttable.repaint();
	
	}

	public void refresh()
	{
		tagList.reloadTags();
		buildTagPanel();
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
		col0style.putAttribute("minwidth", "true");
		col0style.putAttribute("width", 10);

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
		col4style.putAttribute("width", 10);

		jswStyle col5style = tablestyles.makeStyle("col_5");
		col5style.putAttribute("horizontalAlignment", "RIGHT");
		col5style.putAttribute("minwidth", "true");

		jswStyle col6style = tablestyles.makeStyle("col_6");
		col6style.putAttribute("horizontalAlignment", "RIGHT");
		col6style.putAttribute("minwidth", "true");

		return tablestyles;
	}
}
