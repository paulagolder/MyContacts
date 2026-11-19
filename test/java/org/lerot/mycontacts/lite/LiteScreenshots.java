package org.lerot.mycontacts.lite;

import org.lerot.mycontacts.Screenshots;
import org.lerot.mycontacts.mcSettings;
import org.lerot.mycontacts.mcDataObject;
import org.lerot.mycontacts.mcDataSource;
import org.lerot.mycontacts.mcImageDataType;

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
			mcDataObject.useDataSource(new mcDataSource(mcSettings.databasePath()));
			window = new MyContactsLite(new ContactStore());
			window.setAlwaysOnTop(false);
			window.setFocusableWindowState(false);
			window.setVisible(true);
		});
		settle();
		capture(out, "lite-list");
		SwingUtilities.invokeAndWait(() -> window.search.setText("smith"));
		settle();
		capture(out, "lite-search");
		givePhoto(2, args[1]);
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

	// a made-up photo (a soft gradient) for a contact
	static void givePhoto(int id, String folder) throws Exception
	{
		BufferedImage picture = new BufferedImage(120, 140, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = picture.createGraphics();
		g.setPaint(new GradientPaint(0, 0, new Color(0x9DB7D9), 120, 140, new Color(0xE8C9A0)));
		g.fillRect(0, 0, 120, 140);
		g.dispose();
		File file = new File(folder, "photo.png");
		ImageIO.write(picture, "png", file);
		new mcDataObject().doExecute("insert into attributeValues(cid, root, qualifier, value) values ("
				+ id + ", 'photo', '', '" + mcImageDataType.photoFromFile(file) + "')");
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
