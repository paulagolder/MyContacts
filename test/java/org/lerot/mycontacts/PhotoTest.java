package org.lerot.mycontacts;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

// pictures chosen as a contact's photo
class PhotoTest
{
	@TempDir
	Path folder;

	private static BufferedImage stored(String base64) throws Exception
	{
		return ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(base64)));
	}

	private File picture(String name, int width, int height, int type) throws Exception
	{
		File file = folder.resolve(name).toFile();
		ImageIO.write(new BufferedImage(width, height, type), name.substring(name.indexOf('.') + 1), file);
		return file;
	}

	@Test
	void aBigPictureIsShrunkKeepingItsShape() throws Exception
	{
		BufferedImage photo = stored(mcImageDataType.photoFromFile(
				picture("big.jpg", 4000, 3000, BufferedImage.TYPE_INT_RGB)));
		assertEquals(133, photo.getWidth());
		assertEquals(100, photo.getHeight());
	}

	@Test
	void aSmallPictureKeepsItsSize() throws Exception
	{
		BufferedImage photo = stored(mcImageDataType.photoFromFile(
				picture("small.png", 96, 80, BufferedImage.TYPE_INT_ARGB)));
		assertEquals(96, photo.getWidth());
		assertEquals(80, photo.getHeight());
	}

	@Test
	void aFileThatIsNotAPictureGivesNull() throws Exception
	{
		File text = folder.resolve("notes.jpg").toFile();
		Files.writeString(text.toPath(), "not a picture");
		assertNull(mcImageDataType.photoFromFile(text));
	}
}
