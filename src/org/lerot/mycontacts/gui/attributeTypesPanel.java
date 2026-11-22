package org.lerot.mycontacts.gui;

import net.miginfocom.swing.MigLayout;
import org.lerot.mycontacts.mcAttributeType;
import org.lerot.mycontacts.mcAttributeTypes;
import org.lerot.mywidgets.jswPanel;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Tools > Other > Attribute Types: the kinds of detail a contact can have.
 * Each type's label, order and whether it shows on the Edit and Browse pages
 * can be changed in the table (saved at once); a new type can be added, and
 * one no contact uses (and the program does not rely on) can be deleted.
 */
public class attributeTypesPanel extends jswPanel
{
	private static final long serialVersionUID = 1L;
	// the kinds of data a new type can hold
	private static final String[] DATATYPES = { "text", "phone", "cellphone", "email", "http", "date",
			"address", "textlist" };
	// shown in their own parts of the pages, or edited in the heading, so
	// their Edit and Browse ticks stay as they are
	private static final Set<String> FIXED = Set.of("tid", "kind", "memberof", "hasmember", "related");

	private final List<mcAttributeType> types = new ArrayList<>();
	private Map<String, Integer> usage = Map.of();
	private final TypeModel model = new TypeModel();
	private final JTable table = new JTable(model);
	private final JButton delete = new JButton("Delete selected type");
	private final JTextField newLabel = new JTextField(14);
	private final JComboBox<String> newDatatype = new JComboBox<>(DATATYPES);
	private final JComboBox<String> after = new JComboBox<>();

	public attributeTypesPanel()
	{
		super("attributetypes");
		setLayout(new BorderLayout());
		setOpaque(false);

		JLabel intro = new JLabel("<html>The kinds of detail a contact can have. Change a label or order, or tick where "
				+ "a type shows, in the table; changes are saved at once.</html>");
		intro.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
		add(intro, BorderLayout.NORTH);

		table.setRowHeight(table.getRowHeight() + 6);
		table.setFillsViewportHeight(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(e -> updateDelete());
		int[] widths = { 220, 160, 140, 70, 70, 70, 90 };
		for (int i = 0; i < widths.length; i++)
			table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
		// the figures and ticks stay narrow; the names take the spare width
		for (int i = 3; i < widths.length; i++)
			table.getColumnModel().getColumn(i).setMaxWidth(widths[i] + 20);
		add(new JScrollPane(table), BorderLayout.CENTER);

		JPanel bottom = new JPanel(new MigLayout("insets 8 10 10 10", "[][][][][][]push[]"));
		bottom.setOpaque(false);
		bottom.add(new JLabel("New type"));
		bottom.add(newLabel);
		bottom.add(newDatatype);
		bottom.add(new JLabel("after"));
		bottom.add(after);
		JButton add = new JButton("Add");
		bottom.add(add);
		bottom.add(delete);
		add(bottom, BorderLayout.SOUTH);

		add.addActionListener(e -> addType());
		delete.addActionListener(e -> deleteType());
		refresh();
	}

	// reads the types and how many contacts use each again
	public void refresh()
	{
		if (mcAttributeTypes.getAll() == null) return;
		types.clear();
		types.addAll(mcAttributeTypes.getAll().values());
		usage = mcAttributeTypes.usage();
		model.fireTableDataChanged();
		after.removeAllItems();
		for (mcAttributeType t : types) after.addItem(t.getAttributeLabel());
		if (after.getItemCount() > 0) after.setSelectedIndex(after.getItemCount() - 1);
		updateDelete();
	}

	private void updateDelete()
	{
		mcAttributeType t = selected();
		String why = t == null ? "Select a type first"
				: mcAttributeTypes.CORE.contains(t.getKey()) ? "MyContacts relies on this type"
				: uses(t) > 0 ? uses(t) + " contacts use this type"
				: null;
		delete.setEnabled(why == null);
		delete.setToolTipText(why);
	}

	private mcAttributeType selected()
	{
		int row = table.getSelectedRow();
		return row < 0 ? null : types.get(table.convertRowIndexToModel(row));
	}

	private int uses(mcAttributeType t)
	{
		return usage.getOrDefault(t.getKey(), 0);
	}

	private void addType()
	{
		String label = newLabel.getText().trim();
		String key = mcAttributeTypes.keyFor(label);
		if (key.isEmpty())
		{
			JOptionPane.showMessageDialog(this, "Type a label for the new type, such as Skype.");
			return;
		}
		if (mcAttributeTypes.containsKey(key))
		{
			JOptionPane.showMessageDialog(this, "There is already a type " + key + ".");
			return;
		}
		int i = after.getSelectedIndex();
		int order = i < 0 ? 100 : types.get(i).getOrder() + 1;
		mcAttributeTypes.addType(label, (String) newDatatype.getSelectedItem(), order, "EB");
		newLabel.setText("");
		refresh();
		selectKey(key);
	}

	private void deleteType()
	{
		mcAttributeType t = selected();
		if (t == null) return;
		if (JOptionPane.showConfirmDialog(this, "Delete the type " + t.getAttributeLabel() + "?", "Delete type",
				JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
		mcAttributeTypes.deleteType(t.getKey());
		refresh();
	}

	private void selectKey(String key)
	{
		for (int i = 0; i < types.size(); i++)
		{
			if (types.get(i).getKey().equals(key))
			{
				int row = table.convertRowIndexToView(i);
				table.setRowSelectionInterval(row, row);
				table.scrollRectToVisible(table.getCellRect(row, 0, true));
			}
		}
	}

	// the display groups with one letter set or cleared, keeping any others
	private static String withGroup(String groups, char letter, boolean on)
	{
		String without = groups.replace(String.valueOf(letter), "");
		return on ? without + letter : without;
	}

	private class TypeModel extends AbstractTableModel
	{
		private final String[] names = { "Label", "Key", "Kind of data", "Order", "Edit", "Browse", "Contacts" };

		public int getRowCount() { return types.size(); }
		public int getColumnCount() { return names.length; }
		@Override public String getColumnName(int c) { return names[c]; }

		@Override
		public Class<?> getColumnClass(int c)
		{
			return switch (c)
			{
				case 3, 6 -> Integer.class;
				case 4, 5 -> Boolean.class;
				default -> String.class;
			};
		}

		@Override
		public boolean isCellEditable(int r, int c)
		{
			if (c == 0 || c == 3) return true;
			return (c == 4 || c == 5) && !FIXED.contains(types.get(r).getKey());
		}

		public Object getValueAt(int r, int c)
		{
			mcAttributeType t = types.get(r);
			return switch (c)
			{
				case 0 -> t.getAttributeLabel();
				case 1 -> t.getKey();
				case 2 -> t.getDataTypeKey();
				case 3 -> t.getOrder();
				case 4 -> t.getDisplaygroup().contains("E");
				case 5 -> t.getDisplaygroup().contains("B");
				default -> uses(t);
			};
		}

		@Override
		public void setValueAt(Object value, int r, int c)
		{
			mcAttributeType t = types.get(r);
			String label = t.getAttributeLabel();
			int order = t.getOrder();
			String groups = t.getDisplaygroup();
			switch (c)
			{
				case 0 ->
				{
					label = ((String) value).trim();
					if (label.isEmpty()) return;
				}
				case 3 -> order = value == null ? order : (Integer) value;
				case 4 -> groups = withGroup(groups, 'E', (Boolean) value);
				case 5 -> groups = withGroup(groups, 'B', (Boolean) value);
				default -> { return; }
			}
			String key = t.getKey();
			mcAttributeTypes.updateType(key, label, order, groups);
			refresh();
			selectKey(key);
		}
	}
}
