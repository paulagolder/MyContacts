package org.lerot.mycontacts.gui;

import org.lerot.mywidgets.jswTabbedPanel;

public class OtherTabPanel extends jswTabbedPanel
{

	private static final long serialVersionUID = 1L;
	private dbEditPanel dbpanel;
	private manageTagsPanel manageTagspanel2;
	private mergeContactPanel mergeContactPanel3;
	private deleteContactPanel deleteContactPanel3;
	private editListPanel editListPanel3;

	public OtherTabPanel()
	{
		super("othertabpanel");
		//setLayout(new jswTabLayout());
		//setLayout(new jswVerticalLayout());
		//setPreferredSize(new Dimension (800, 600));
		dbpanel = new dbEditPanel();
		addTab("Edit Databases", dbpanel);
		manageTagspanel2 = new manageTagsPanel();
		//manageTagspanel2.setPreferredSize(new Dimension (800, 600));
		addTab("Manage Tags & Lists", manageTagspanel2);
		mergeContactPanel3 = new mergeContactPanel();
		addTab("Merge Contacts", mergeContactPanel3);
		deleteContactPanel3 = new deleteContactPanel();
		addTab("Delete Contacts", deleteContactPanel3);
		editListPanel3 = new editListPanel();
		addTab("Edit Lists", editListPanel3);
		
		// tabbedPane.addChangeListener(this);

	}

	public void refresh()
	{
		manageTagspanel2.refresh();
		mergeContactPanel3.refresh();
		deleteContactPanel3.initialise();
		editListPanel3.initialise();
	}

}
