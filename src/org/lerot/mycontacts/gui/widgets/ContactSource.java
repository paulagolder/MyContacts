package org.lerot.mycontacts.gui.widgets;

import org.lerot.mycontacts.MyContacts;
import org.lerot.mycontacts.gui.selectorBox;
import org.lerot.mycontacts.mcContacts;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.Vector;

/*
 * Which contacts a backup, export or label print uses: All contacts, or a
 * Selection chosen like the main window's browse selector (a tag, "tagged"
 * for contacts picked one by one, or the search results).
 * It starts at what the main window is browsing, and follows it when that
 * changes; choosing here leaves the main window alone.
 */
public class ContactSource extends JPanel
{
	private static final long serialVersionUID = 1L;
	// the browse selector's "selection", shown by what it is
	private static final String SEARCHRESULTS = "search results";
	private final JRadioButton all = new JRadioButton();
	private final JRadioButton selection = new JRadioButton("Selection");
	private final JComboBox<String> tags = new JComboBox<>();
	private final JLabel count = new JLabel();
	private String followed; // the browse choice last taken from the main window
	private boolean filling;
	private boolean picked; // a tag chosen here, kept until the main window's choice changes

	public ContactSource()
	{
		super(new FlowLayout(FlowLayout.LEFT, 6, 2));
		setOpaque(false);
		ButtonGroup group = new ButtonGroup();
		group.add(all);
		group.add(selection);
		all.setOpaque(false);
		selection.setOpaque(false);
		tags.setMaximumRowCount(20);
		add(all);
		add(Box.createHorizontalStrut(20));
		add(selection);
		add(tags);
		add(count);
		all.addActionListener(e -> showCount());
		selection.addActionListener(e -> showCount());
		tags.addActionListener(e ->
		{
			if (filling) return;
			picked = true;
			selection.setSelected(true);
			showCount();
		});
		refresh();
	}

	// the tag list and the counts again; a new browse choice is taken up
	public void refresh()
	{
		String browsing = MyContacts.selbox.getBrowseFilter();
		boolean follow = !Objects.equals(browsing, followed);
		followed = browsing;
		if (follow) picked = false;
		Object chosen = picked ? tags.getSelectedItem() : shown(browsing);
		filling = true;
		// the tags, then tagged (even with no one on it yet), friend and the search results
		Vector<String> list = new Vector<>();
		for (String tag : MyContacts.selbox.getTaglist())
			if (!list.contains(tag)) list.add(tag);
		for (String extra : new String[] { selectorBox.TAGGED, "friend", SEARCHRESULTS })
			if (!list.contains(extra)) list.add(extra);
		tags.setModel(new DefaultComboBoxModel<>(list));
		if (chosen != null && list.contains(chosen)) tags.setSelectedItem(chosen);
		filling = false;
		if (follow)
		{
			boolean browsingall = browsing == null || browsing.equals("all");
			all.setSelected(browsingall);
			selection.setSelected(!browsingall);
		}
		all.setText("All contacts (" + MyContacts.selbox.countAll() + ")");
		showCount();
	}

	private void showCount()
	{
		int n = selection.isSelected() ? contacts().size() : 0;
		count.setText(selection.isSelected() ? n + (n == 1 ? " contact" : " contacts") : "");
		count.setEnabled(selection.isSelected());
	}

	private static String shown(String browsechoice)
	{
		return "selection".equals(browsechoice) ? SEARCHRESULTS : browsechoice;
	}

	public boolean isAll()
	{
		return all.isSelected();
	}

	// the contacts chosen
	public mcContacts contacts()
	{
		if (all.isSelected()) return MyContacts.selbox.getAllcontactlist();
		String choice = (String) tags.getSelectedItem();
		mcContacts found = MyContacts.selbox.contactsFor(SEARCHRESULTS.equals(choice) ? "selection" : choice);
		return found == null ? new mcContacts() : found;
	}
}
