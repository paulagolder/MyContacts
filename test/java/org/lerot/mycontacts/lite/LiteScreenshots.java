package org.lerot.mycontacts.lite;

import org.lerot.mycontacts.Screenshots;
import org.lerot.mycontacts.mcSettings;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;

/*
 * Screenshots of myContactsLite on the made-up test database, added to the
 * main app's set as lite-*.png (run by ./gradlew screenshots).
 *   args: output-folder home-folder
 */
public class LiteScreenshots
{
	static MyContactsLite window;

	public static void main(String[] args) throws Exception
	{
		File out = new File(args[0]);
		out.mkdirs();
		System.setProperty("user.home", Screenshots.makeHome(Path.of(args[1])).toString());
		MyContactsLite.setupLookAndFeel();
		SwingUtilities.invokeAndWait(() ->
		{
			window = new MyContactsLite(new ContactStore(mcSettings.databasePath()));
			window.setAlwaysOnTop(false);
			window.setFocusableWindowState(false);
			window.setVisible(true);
		});
		settle();
		capture(out, "lite-list");
		SwingUtilities.invokeAndWait(() -> window.search.setText("smith"));
		settle();
		capture(out, "lite-search");
		for (int id : new int[] { 1, 2 })
		{
			SwingUtilities.invokeAndWait(() -> window.open(window.store.find(id).orElseThrow()));
			settle();
			capture(out, "lite-card-" + id);
		}
		System.exit(0);
	}

	static void capture(File out, String name) throws Exception
	{
		BufferedImage[] image = new BufferedImage[1];
		SwingUtilities.invokeAndWait(() ->
		{
			JRootPane root = window.getRootPane();
			image[0] = new BufferedImage(root.getWidth(), root.getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics2D g = image[0].createGraphics();
			root.paint(g);
			g.dispose();
		});
		ImageIO.write(image[0], "png", new File(out, name + ".png"));
		System.out.println("captured " + name);
	}

	static void settle() throws Exception
	{
		Thread.sleep(500);
		SwingUtilities.invokeAndWait(() ->
		{
			KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
			window.getRootPane().validate();
		});
	}
}
