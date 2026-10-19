package org.lerot.mycontacts.gui;

import org.lerot.mywidgets.jswTabbedPanel;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class ExportTabPanel extends jswTabbedPanel implements ComponentListener
{

	private static final long serialVersionUID = 1L;
	private exportPanel exportpanel1;
	private labelprintPanel labelpanel2;

	public ExportTabPanel() 
	{
        super("exporttabpanel");
		//setLayout(new jswVerticalLayout());
		exportpanel1 = new exportPanel();
		addTab("Export Contacts", exportpanel1);
		labelpanel2 = new labelprintPanel();
		addTab("Print Labels", labelpanel2);
		setSelectedComponent(exportpanel1);
	}

	public void refresh()
	{
		exportpanel1.refresh();
		labelpanel2.refresh();
	}

	@Override
	public void componentHidden(ComponentEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentMoved(ComponentEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentResized(ComponentEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentShown(ComponentEvent arg0)
	{
		exportpanel1.refresh();
		labelpanel2.refresh();
	}


}
