package org.lerot.mycontacts;

import org.lerot.mycontacts.lite.MyContactsLite;
import org.lerot.mywidgets.jswTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.RoundRectangle2D;

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

	// soft corners for a window whose title bar FlatLaf draws: Windows 11
	// rounds them itself, and a Mac window keeps its own rounded title bar;
	// elsewhere they are square, so the window is cut to a rounded outline
	// (square again when maximised, to fill the screen)
	public static void roundCorners(JFrame frame)
	{
		String os = System.getProperty("os.name");
		if (os.startsWith("Windows") || os.startsWith("Mac")) return;
		if (!frame.getGraphicsConfiguration().getDevice()
				.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT)) return;
		int arc = org.lerot.mywidgets.jswScale.px(20);
		frame.addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent e)
			{
				frame.setShape(maximised(frame) ? null
						: new RoundRectangle2D.Double(0, 0, frame.getWidth(), frame.getHeight(), arc, arc));
			}
		});
		// FlatLaf's outline stops short of the cut corners, so the corners get
		// theirs on a see-through top layer, over the title bar (it takes no clicks)
		JComponent corners = new JComponent()
		{
			@Override
			protected void paintComponent(Graphics g)
			{
				if (maximised(frame)) return;
				Color line = UIManager.getColor(frame.isActive() ? "RootPane.activeBorderColor"
						: "RootPane.inactiveBorderColor");
				if (line == null) return;
				Point in = SwingUtilities.convertPoint(frame.getRootPane(), 0, 0, this);
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(line);
				g2.setStroke(new BasicStroke(1.5f));
				g2.draw(new RoundRectangle2D.Double(in.x + 0.75, in.y + 0.75,
						frame.getRootPane().getWidth() - 1.5, frame.getRootPane().getHeight() - 1.5, arc - 1, arc - 1));
				g2.dispose();
			}
		};
		JLayeredPane layers = frame.getLayeredPane();
		layers.add(corners, JLayeredPane.DRAG_LAYER);
		layers.addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent e)
			{
				corners.setBounds(0, 0, layers.getWidth(), layers.getHeight());
			}
		});
	}

	private static boolean maximised(JFrame frame)
	{
		return (frame.getExtendedState() & Frame.MAXIMIZED_BOTH) != 0;
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
