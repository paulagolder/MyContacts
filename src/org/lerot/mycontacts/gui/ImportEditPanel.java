package org.lerot.mycontacts.gui;

import org.lerot.mycontacts.gui.widgets.jswEditPanel;
import org.lerot.mycontacts.mcAttribute;
import org.lerot.mycontacts.mcContact;
import org.lerot.mycontacts.MyContacts;
import org.lerot.mywidgets.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map.Entry;
import java.util.Vector;

public class ImportEditPanel extends jswVerticalPanel implements ActionListener
{

	private static final long serialVersionUID = 1L;
	private static final int YES = 0;
	private attributetablemodel tablemodel;
	private mcContact editcontact;
	Vector<String> attributekey;
	jswOptionset optset;

	public ImportEditPanel()
	{
        super("importedit",false,false);
        makeAttributeEditTableStyles();
		makeFieldEditTableStyles();
		// showImportEditPanel();
	}

	@Override
	public void actionPerformed(ActionEvent evt)
	{

		String action = evt.getActionCommand().toUpperCase();
		System.out.println("action " + action);
		Vector<String> list = new Vector<String>();
		if (action.startsWith("DELETE"))
		{
			int nrows = tablemodel.getRowCount();
			int ndel = 0;
			for (int k = 0; k < nrows; k++)
			{
				boolean isselected = (Boolean) tablemodel.getValueAt(k, 3);
				if (isselected)
				{
					System.out.println("deleteing"
							+ tablemodel.getValueAt(k, 1));
					if (((String) tablemodel.getValueAt(k, 1)).isEmpty())
					{
						list.add((String) tablemodel.getValueAt(k, 0));
					} else
					{
						list.add(tablemodel.getValueAt(k, 0) + "/"
								+ tablemodel.getValueAt(k, 1));
					}
					ndel++;
				}
			}
			int n = JOptionPane.showConfirmDialog(this,
					"Do you want to delete " + ndel + " entries ?",
					"DELETE FROM WHITELIST?", JOptionPane.YES_NO_OPTION);
			System.out.println("reply =" + n);
			if (n == YES)
			{
				for (String attkey : list)
				{
					editcontact.deleteAttributebyKey(attkey);
				}
				// selcontact.fillContact();
				System.out.println("deleted!");
				// tablemodel.fireTableDataChanged();
			}

		} else if (action.startsWith("UPDATE"))
		{
			int nrows = tablemodel.getRowCount();
			int ndel = 0;
			for (int k = 0; k < nrows; k++)
			{
				boolean isselected = (Boolean) tablemodel.getValueAt(k, 3);
				if (isselected)
				{
					ndel++;
				}
			}
			int n = JOptionPane.showConfirmDialog(this,
					"Do you want to update " + ndel + " entries ?",
					"UPDATE WHITELIST?", JOptionPane.YES_NO_OPTION);
			System.out.println("reply =" + n);
			if (n == YES)
			{
				for (int k = 0; k < nrows; k++)
				{
					boolean isselected = (Boolean) tablemodel.getValueAt(k, 3);
					if (isselected)
					{
						String edattkey = attributekey.get(k);
						String newroot = (String) tablemodel.getValueAt(k, 0);
						String newqual = (String) tablemodel.getValueAt(k, 1);
						editcontact.deleteAttributebyKey(edattkey);
						String oldvalue = (String) tablemodel.getValueAt(k, 2);
						editcontact.insertAttribute(newroot,newqual);
						editcontact.updateAttribute(newroot,newqual, oldvalue);
					}
				}
				tablemodel.fireTableDataChanged();
			}

		} else

			System.out.println("ep action " + action + " unrecognised ");

		showImportEditPanel();
		MyContacts.topgui.getContentPane().validate();
	}

	public jswStyles makeAttributeEditTableStyles()
	{
		jswStyles tablestyles = new jswStyles("edittable");
		jswStyle cellstyle = tablestyles.makeStyle("cell");
		cellstyle.putAttribute("foregroundColor", "Blue");
		cellstyle.putAttribute("borderWidth", "1");
		cellstyle.putAttribute("borderColor", jswTheme.CELLBORDER);
		cellstyle.setHorizontalAlign("LEFT");
		cellstyle.putAttribute("fontsize", jswTheme.TEXT);

		jswStyle cellcstyle = tablestyles.makeStyle("cellcontent");
		cellcstyle.putAttribute("backgroundColor", "transparent");
		cellcstyle.putAttribute("foregroundColor", "Red");
		cellcstyle.setHorizontalAlign("LEFT");
		cellcstyle.putAttribute("fontsize", jswTheme.TEXT);

		// tablestyles.makeStyle("cell_0_0");
		jswStyle col0style = tablestyles.makeStyle("col_0");
		col0style.putAttribute("fontStyle", Font.BOLD);
		col0style.setHorizontalAlign("RIGHT");
		col0style.putAttribute("minwidth", "true");

		jswStyle col1style = tablestyles.makeStyle("col_1");
		col1style.putAttribute("fontStyle", Font.BOLD);
		col1style.setHorizontalAlign("RIGHT");
		col1style.putAttribute("backgroundColor", "Green");
		col1style.putAttribute("minwidth", "true");

		jswStyle tabletyle = tablestyles.makeStyle("table");
		tabletyle.putAttribute("foregroundColor", "Green");
		tabletyle.putAttribute("borderWidth", "2");
		tabletyle.putAttribute("borderColor", jswTheme.BORDER);

		jswStyle col3style = tablestyles.makeStyle("col_3");
		col3style.putAttribute("horizontalAlignment", "RIGHT");
		col3style.putAttribute("minwidth", "true");
		return tablestyles;
	}

	public jswStyles makeFieldEditTableStyles()
	{
		jswStyles tablestyles = new jswStyles("fieldedit");
		jswStyle cellstyle = tablestyles.makeStyle("cell");
		cellstyle.putAttribute("foregroundColor", "Blue");
		cellstyle.putAttribute("borderWidth", "1");
		cellstyle.putAttribute("borderColor", jswTheme.CELLBORDER);
		cellstyle.setHorizontalAlign("LEFT");
		cellstyle.putAttribute("fontsize", jswTheme.TEXT);

		jswStyle cellcstyle = tablestyles.makeStyle("cellcontent");
		cellcstyle.putAttribute("backgroundColor", "transparent");
		cellcstyle.putAttribute("foregroundColor", "Red");
		cellcstyle.setHorizontalAlign("LEFT");
		cellcstyle.putAttribute("fontsize", jswTheme.TEXT);

		// tablestyles.makeStyle("cell_0_0");
		jswStyle col0style = tablestyles.makeStyle("col_0");
		col0style.putAttribute("fontStyle", Font.BOLD);
		col0style.setHorizontalAlign("RIGHT");
		col0style.putAttribute("minwidth", "true");

		jswStyle col1style = tablestyles.makeStyle("col_1");
		col1style.putAttribute("fontStyle", Font.BOLD);
		col1style.setHorizontalAlign("RIGHT");
		col1style.putAttribute("backgroundColor", "Green");
		// col1style.putAttribute("minwidth", "true");

		jswStyle tabletyle = tablestyles.makeStyle("table");
		tabletyle.putAttribute("foregroundColor", "Green");
		tabletyle.putAttribute("borderWidth", "2");
		tabletyle.putAttribute("borderColor", jswTheme.BORDER);

		jswStyle col3style = tablestyles.makeStyle("col_1");
		col3style.putAttribute("horizontalAlignment", "RIGHT");
		col3style.putAttribute("backgroundColor", "Yellow");
		// col3style.putAttribute("minwidth", "true");
		return tablestyles;
	}

	public ImportEditPanel showImportEditPanel()
	{
		ImportEditPanel thispanel = this;
		editcontact = MyContacts.selbox.getSelcontact();
		thispanel.removeAll();
		thispanel.setBackground(null);
		jswHorizontalPanel idbox = new jswHorizontalPanel("idbox", false);
		thispanel.add(idbox);
		optset = new jswOptionset(this,"source",true,true);
		jswHorizontalPanel actions = new jswHorizontalPanel("actions", false);
		thispanel.add(actions);
		jswButton delete = new jswButton(this, "Delete Selected", "delete");
		actions.add(delete);
		jswButton update = new jswButton(this, "Update Selected", "update");
		actions.add(update);
		jswEditPanel editpanel = new jswEditPanel("testtitle");
		tablemodel = new attributetablemodel();
		editpanel.initiate(tablemodel);
		thispanel.add(editpanel);
		int row = 0;
		attributekey = new Vector<String>();
		for (Entry<String, mcAttribute> anentry : editcontact.getAttributes()
				.entrySet())
		{
			mcAttribute anattribute = anentry.getValue();

			if (anattribute.isImage())
			{

			} else if (anattribute.isArray())
			{

			} else
			{
				Object[] values = new Object[4];
				attributekey.add(anattribute.getKey());
				values[0] = anattribute.getRoot();
				values[1] = anattribute.getQualifier();
				values[2] = anattribute.getFormattedValue();
				values[3] = new Boolean(false);
				tablemodel.insertData(values);
			}

		}
		editpanel.setWidths();
		return thispanel;
	}

	public void refresh()
	{
		// TODO Auto-generated method stub
		
	}
}
