package org.lerot.mycontacts;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.TreeSet;

/*
 * Compares two folders of screenshots. For each screen it prints the share
 * of pixels that changed and writes a diff image: the baseline faded, with
 * changed pixels in red, and a before-and-after image side by side.
 *
 *   args: baseline-folder current-folder diff-folder
 */
public class ScreenshotCompare
{
	public static void main(String[] args) throws Exception
	{
		File baseline = new File(args[0]), current = new File(args[1]), diffs = new File(args[2]);
		diffs.mkdirs();
		TreeSet<String> names = new TreeSet<>();
		for (File dir : new File[] { baseline, current })
		{
			String[] pngs = dir.list((d, n) -> n.endsWith(".png"));
			if (pngs != null) names.addAll(Arrays.asList(pngs));
		}
		int changed = 0;
		for (String name : names)
		{
			File b = new File(baseline, name), c = new File(current, name);
			if (!b.exists())
			{
				System.out.printf("  NEW      %s%n", name);
				changed++;
				continue;
			}
			if (!c.exists())
			{
				System.out.printf("  MISSING  %s%n", name);
				changed++;
				continue;
			}
			BufferedImage bi = ImageIO.read(b), ci = ImageIO.read(c);
			int w = Math.max(bi.getWidth(), ci.getWidth()), h = Math.max(bi.getHeight(), ci.getHeight());
			BufferedImage diff = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			long different = 0;
			for (int y = 0; y < h; y++)
				for (int x = 0; x < w; x++)
				{
					int bp = pixel(bi, x, y), cp = pixel(ci, x, y);
					if (bp != cp)
					{
						different++;
						diff.setRGB(x, y, 0xFF0000);
					} else
						diff.setRGB(x, y, fade(bp));
				}
			double percent = 100.0 * different / ((long) w * h);
			if (different > 0)
			{
				changed++;
				ImageIO.write(diff, "png", new File(diffs, name));
				ImageIO.write(sideBySide(bi, ci), "png",
						new File(diffs, name.replace(".png", "-before-after.png")));
			}
			System.out.printf("  %6.2f%%  %s%s%n", percent, name,
					bi.getWidth() != ci.getWidth() || bi.getHeight() != ci.getHeight() ? "  (size changed)" : "");
		}
		System.out.println(changed == 0 ? "all screens identical"
				: changed + " screen(s) differ; diff images in " + diffs);
	}

	// before on the left, after on the right
	static BufferedImage sideBySide(BufferedImage before, BufferedImage after)
	{
		int gap = 12;
		BufferedImage out = new BufferedImage(before.getWidth() + gap + after.getWidth(),
				Math.max(before.getHeight(), after.getHeight()), BufferedImage.TYPE_INT_RGB);
		java.awt.Graphics2D g = out.createGraphics();
		g.setColor(java.awt.Color.DARK_GRAY);
		g.fillRect(0, 0, out.getWidth(), out.getHeight());
		g.drawImage(before, 0, 0, null);
		g.drawImage(after, before.getWidth() + gap, 0, null);
		g.dispose();
		return out;
	}

	static int pixel(BufferedImage img, int x, int y)
	{
		return x < img.getWidth() && y < img.getHeight() ? img.getRGB(x, y) & 0xFFFFFF : 0xFF00FF;
	}

	static int fade(int rgb)
	{
		int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
		int grey = (r + g + b) / 3;
		int v = 255 - (255 - grey) / 3;
		return (v << 16) | (v << 8) | v;
	}
}
