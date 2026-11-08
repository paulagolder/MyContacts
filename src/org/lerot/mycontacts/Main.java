package org.lerot.mycontacts;

import org.lerot.mycontacts.lite.MyContactsLite;
import org.lerot.mywidgets.jswTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/*
 * MyContacts starts here. The small Contacts viewer opens first, and the full
 * MyContacts window opens from it, in the same program.
 *
 *   java -jar MyContacts.jar               the Contacts viewer
 *   java -jar MyContacts.jar --full        the full window
 *   java -jar MyContacts.jar --edit <id>   the full window on that contact
 *
 * Closing a window hides it; when no window is showing the program ends.
 * On a new computer the first start sets up ~/.mccontacts (see mcStarter).
 */
public class Main
{
	private static boolean lookSetUp;

	public static void main(String[] args)
	{
		boolean full = false;
		Integer edit = null;
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("--full")) full = true;
			else if (args[i].equals("--edit") && i + 1 < args.length) edit = Integer.parseInt(args[++i]);
		}
		mcStarter.setUp();
		mcSettings.setUpFolders();
		// one connection, shared by the Contacts viewer and the full window
		mcDataObject.useDataSource(new mcDataSource(mcSettings.databasePath()));
		setupLookAndFeel();
		boolean openFull = full || edit != null;
		Integer contact = edit;
		Runnable start = () ->
		{
			if (openFull) MyContacts.open(contact);
			else MyContactsLite.open();
		};
		if (SwingUtilities.isEventDispatchThread()) start.run();
		else SwingUtilities.invokeLater(start);
	}

	// one look for both windows: the jswTheme palette; the viewer styles its
	// own components on top of it
	public static void setupLookAndFeel()
	{
		if (lookSetUp) return;
		lookSetUp = true;
		MyContacts.settingsdir = mcSettings.folder();
		jswTheme.install();
		UIManager.put("FileChooser.readOnly", Boolean.TRUE);
	}

	// closing the window hides it, and the program ends once none is showing
	public static void closeHides(JFrame frame)
	{
		frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		frame.addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentHidden(ComponentEvent e)
			{
				SwingUtilities.invokeLater(Main::exitIfNoWindows);
			}
		});
	}

	private static void exitIfNoWindows()
	{
		for (Frame frame : Frame.getFrames())
			if (frame.isVisible()) return;
		mcDataSource db = mcDataObject.current();
		if (db != null) db.close();
		System.exit(0);
	}
}
