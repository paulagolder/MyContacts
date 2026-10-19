package org.lerot.mycontacts.gui.widgets;

import org.lerot.mywidgets.jswHorizontalPanel;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

public class jswEditPanel extends jswHorizontalPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;
	JTable avtable;
	Dimension paneldimension, innerdimension, tabledimension;
	JScrollPane scrollpane;

	public jswEditPanel(String title)
	{
		super(title, true);
		paneldimension = new Dimension(420, 320);
		innerdimension = new Dimension(410, 310);
		tabledimension = new Dimension(400, 300);
		setPreferredSize(paneldimension);

	}

	public String getSelectedKEY()
	{
		int r = avtable.getSelectedRow();
		String v = avtable.getValueAt(r, 0).toString();
		return v;
	}

	public String getSelectedValue()
	{
		int r = avtable.getSelectedRow();
		int c = avtable.getSelectedColumn();
		String v = avtable.getValueAt(r, c).toString();
		return v;
	}

	public void initiate(AbstractTableModel atm)
	{
		int nrows = atm.getRowCount();
		int ncols = atm.getColumnCount();
		final JTable vtable = new JTable(nrows, ncols);
		//jswTable vtable = new jswTable("attributes", tablestyles);
		avtable = vtable;
		vtable.setCellSelectionEnabled(true);
		vtable.setCellSelectionEnabled(false);
		vtable.setModel(atm);
		// vtable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		JScrollPane scroll = new JScrollPane(vtable);
		if (getComponentCount() > 0) remove(0);
		this.add("FILLW", scroll);
		this.revalidate();

	}

	public JTable getTable()
	{
		return avtable;
	}

	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		System.out.println(" happy days ");

	}

	public void setWidths()
	{
		TableModel atm = avtable.getModel();
		Vector<Integer> charcount = new Vector<Integer>();
		int nrows = atm.getRowCount();

		for (int j = 0; j < 2; j++)
		{
			int sum = 0;
			for (int k = 0; k < nrows; k++)
			{
				String str = (String) atm.getValueAt(k, j);
				if (str != null && sum < str.length()) sum = str.length();
			}
			charcount.add(j, sum);
		}
		avtable.getColumnModel().getColumn(0).setMaxWidth(charcount.get(0) * 8);
		avtable.getColumnModel().getColumn(0).setMinWidth(charcount.get(0) * 8);
		avtable.getColumnModel().getColumn(1).setMaxWidth(charcount.get(1) * 8);
		avtable.getColumnModel().getColumn(1).setMinWidth(charcount.get(1) * 8);
		avtable.getColumnModel().getColumn(2).setPreferredWidth(900);
		avtable.getColumnModel().getColumn(3).setMaxWidth(10);
		avtable.getColumnModel().getColumn(3).setMinWidth(10);

	}

}
