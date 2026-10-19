package org.lerot.mycontacts.gui;


import org.lerot.mywidgets.jswTabbedPanel;

public class anImportTabPanel extends jswTabbedPanel
{

	private static final long serialVersionUID = 1L;
	private importPanel importpanel;
	private ImportEditPanel importeditpanel;
	private importSetupPanel importsetuppanel;

	public anImportTabPanel()
	{
		super("animporttabpanel");
		//setLayout(new jswVerticalLayout());
		//importpanel = new importPanel();
		//addTab("Import", importpanel);
		importsetuppanel = new importSetupPanel();
		addTab("Import Setup", importsetuppanel);
		importeditpanel = new ImportEditPanel();
		addTab("Import Edit", importeditpanel);
		setSelectedComponent(importpanel);

	}

	

	public void showImportPanel()
	{
		importpanel.refresh();
		importeditpanel.refresh();
		importsetuppanel.refresh();
		
	}
}
