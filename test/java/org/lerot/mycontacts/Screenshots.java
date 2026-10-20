package org.lerot.mycontacts;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/*
 * Starts MyContacts on the made-up test database (in a temporary home
 * folder, so no personal data) and saves a PNG of every main view and every
 * Tools tab, for comparing layouts before and after a change.
 *
 *   ./gradlew screenshots          writes build/screenshots/current
 *   ./gradlew compareScreenshots   compares them with screenshots/baseline
 *
 * Needs a display (the window appears briefly).
 */
public class Screenshots
{
	public static void main(String[] args) throws Exception
	{
		File out = new File(args[0]);
		out.mkdirs();
		for (File old : out.listFiles((d, n) -> n.endsWith(".png"))) old.delete();
		// a fixed home folder, so paths shown on screen are the same every run
		System.setProperty("user.home", makeHome(Path.of(args[1])).toString());

		SwingUtilities.invokeAndWait(() ->
		{
			MyContacts.main(new String[0]);
			// keep mouse position and keyboard focus out of the pictures: a glass
			// pane absorbs mouse events (no hover highlights) and the window
			// never takes focus (no focus outlines)
			JComponent glass = (JComponent) MyContacts.mframe.getGlassPane();
			glass.addMouseListener(new java.awt.event.MouseAdapter() {});
			glass.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {});
			glass.setVisible(true);
			MyContacts.mframe.setFocusableWindowState(false);
		});
		settle();
		for (String mode : new String[] { "BROWSE", "SEARCH", "EDIT", "TOOLS" })
		{
			SwingUtilities.invokeAndWait(() ->
			{
				MyContacts.topgui.mode = mode;
				MyContacts.topgui.refreshView();
			});
			settle();
			if (mode.equals("TOOLS"))
				captureTabs(out, MyContacts.topgui.mainpanel, "tools");
			else
				capture(out, mode.toLowerCase());
		}
		System.out.println("screenshots written to " + out);
		System.exit(0);
	}

	// a home folder with the test database and settings
	public static Path makeHome(Path home) throws Exception
	{
		if (Files.exists(home))
			try (var files = Files.walk(home))
			{
				files.sorted(java.util.Comparator.reverseOrder()).forEach(f -> f.toFile().delete());
			}
		Path settings = home.resolve(".mccontacts");
		Files.createDirectories(settings);
		Files.createDirectories(home.resolve("Documents/correspondance"));
		Files.createDirectories(home.resolve("Desktop"));
		TestDatabase.createFile(settings.resolve("test.sqlite"));
		for (String f : new String[] { "labelTemplates.xml", "addressTemplates.xml" })
			Files.copy(Path.of(TestDatabase.settingsFolder(), f), settings.resolve(f),
					StandardCopyOption.REPLACE_EXISTING);
		Files.writeString(settings.resolve("properties.xml"),
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
						+ "<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\n"
						+ "<properties>\n<entry key=\"database\">test.sqlite</entry>\n"
						+ "<entry key=\"backupdir\">backups</entry>\n</properties>\n");
		return home;
	}

	// visit every tab, and every tab inside it, capturing each
	static void captureTabs(File out, Container root, String prefix) throws Exception
	{
		JTabbedPane tabs = findTabs(root);
		if (tabs == null)
		{
			capture(out, prefix);
			return;
		}
		for (int i = 0; i < tabs.getTabCount(); i++)
		{
			int index = i;
			SwingUtilities.invokeAndWait(() -> tabs.setSelectedIndex(index));
			settle();
			String name = prefix + "-" + tabs.getTitleAt(i).toLowerCase().replaceAll("[^a-z0-9]+", "-");
			captureTabs(out, (Container) tabs.getComponentAt(i), name);
		}
	}

	static JTabbedPane findTabs(Container c)
	{
		for (Component child : c.getComponents())
		{
			if (child instanceof JTabbedPane t) return t;
			if (child instanceof Container cc)
			{
				JTabbedPane t = findTabs(cc);
				if (t != null) return t;
			}
		}
		return null;
	}

	static void capture(File out, String name) throws Exception
	{
		BufferedImage[] image = new BufferedImage[1];
		SwingUtilities.invokeAndWait(() ->
		{
			JComponent content = (JComponent) MyContacts.mframe.getContentPane();
			content.validate();
			image[0] = new BufferedImage(content.getWidth(), content.getHeight(),
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g = image[0].createGraphics();
			content.paint(g);
			g.dispose();
		});
		ImageIO.write(image[0], "png", new File(out, name + ".png"));
		// and where every component is, for finding the source of a difference
		StringBuilder tree = new StringBuilder();
		SwingUtilities.invokeAndWait(() -> dump(MyContacts.mframe.getContentPane(), "", tree));
		java.nio.file.Files.writeString(new File(out, name + ".txt").toPath(), tree);
		System.out.println("captured " + name);
	}

	static void dump(Component c, String indent, StringBuilder out)
	{
		if (!c.isVisible()) return;
		String name = c instanceof org.lerot.mywidgets.jswPanel p ? " '" + p.getPanelname() + "'" : "";
		Rectangle r = c.getBounds();
		out.append(indent).append(c.getClass().getSimpleName()).append(name)
				.append(" ").append(r.x).append(",").append(r.y).append(" ").append(r.width).append("x")
				.append(r.height).append("\n");
		if (c instanceof Container cc && !(c instanceof JComboBox) && !(c instanceof JScrollBar))
			for (Component child : cc.getComponents())
				dump(child, indent + "  ", out);
	}

	// let layout and painting finish
	static void settle() throws Exception
	{
		Thread.sleep(400);
		SwingUtilities.invokeAndWait(() ->
		{
			KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
			MyContacts.mframe.getContentPane().validate();
		});
	}
}
