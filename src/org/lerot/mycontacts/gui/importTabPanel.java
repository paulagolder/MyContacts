package org.lerot.mycontacts.gui;

import org.lerot.mywidgets.jswTabbedPanel;

public class importTabPanel extends jswTabbedPanel
{

	private static final long serialVersionUID = 1L;
	private importPanel importactionpanel;

	public importTabPanel()
	{
		super("importtabpanel");
		importactionpanel = new importPanel();
		importSetupPanel panel2 = new importSetupPanel();
		addTab("Import Setup", panel2);
		ImportEditPanel panel3 = new ImportEditPanel();
		panel3.setVisible(true);
		addTab("Edit Imports", panel3);
		setSelectedComponent(panel2);
	}

	public void showImportPanel()
	{
		importactionpanel.showImportPanel();
	}

    public void refresh()
    {

    }
}
