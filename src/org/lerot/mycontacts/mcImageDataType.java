package org.lerot.mycontacts;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public class mcImageDataType extends mcDataType
{

	public mcImageDataType()
	{
		super("image", "image");
	}

	// photos are stored no bigger than this on their longer side
	public static final int PHOTOSIZE = 133;

	/*
	 * A picture file (JPEG, PNG, GIF or BMP) as a stored photo: shrunk to
	 * fit PHOTOSIZE, as a JPEG in base64. Null if it cannot be read as a
	 * picture.
	 */
	public static String photoFromFile(File file) throws IOException
	{
		BufferedImage picture = ImageIO.read(file);
		if (picture == null) return null;
		double scale = Math.min(1.0, (double) PHOTOSIZE / Math.max(picture.getWidth(), picture.getHeight()));
		int width = Math.max(1, (int) Math.round(picture.getWidth() * scale));
		int height = Math.max(1, (int) Math.round(picture.getHeight() * scale));
		// drawn onto white, as JPEG has no transparency
		BufferedImage photo = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = photo.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.drawImage(picture, 0, 0, width, height, Color.WHITE, null);
		g.dispose();
		ByteArrayOutputStream jpeg = new ByteArrayOutputStream();
		ImageIO.write(photo, "jpg", jpeg);
		return Base64.getEncoder().encodeToString(jpeg.toByteArray());
	}

	public static String[] getVCardEncodedLines(String foriegnlabel, String rawvalue)
	{

		int linelen = 75;
		String alltext = foriegnlabel + ":" + rawvalue;
		int k = 0;
		int len = alltext.length();
		int linecount = (len - 1) / linelen;
		String[] outlist = new String[linecount + 2];
		int lineno = 0;
		String padding = "";
		while (k + linelen < len)
		{
			String subtext = alltext.substring(k, k + linelen);
			outlist[lineno] = padding + subtext;
			lineno++;
			k = k + linelen;
			linelen = 74;
			padding = " ";
		}
		String lastline = alltext.substring(k);
		int groupcount = (lastline.length() + 2) / 3;
		int lastlinelength = groupcount * 3;
		String vlastline = (lastline + "==").substring(0, lastlinelength - 1);
		outlist[lineno] = padding + vlastline;
		return outlist;
	}
	
	public static String[] getEncodedLines( String rawvalue)
	{

		int linelen = 75;
		String alltext = rawvalue;
		int k = 0;
		int len = alltext.length();
		int linecount = (len - 1) / linelen;
		String[] outlist = new String[linecount + 1];
		int lineno = 0;
		while (k + linelen < len)
		{
			String subtext = alltext.substring(k, k + linelen);
			outlist[lineno] =  subtext;
			lineno++;
			k = k + linelen;
		}
		String lastline = alltext.substring(k);
		outlist[lineno] = lastline;
		return outlist;
	}


	@Override
	public Vector<String> getFieldKeyList()
	{
		return null;
	}

	@Override
	public LinkedHashMap<String, mcfield> getFieldList()
	{
		return null;
	}

	@Override
	public Map<mcfield, String> getFieldValueMap(String value)
	{
		return null;
	}

	@Override
	public String getFormattedValue(String value, String fmt)
	{
		return null;
	}

	@Override
	public Map<String, String> getKeyValueMap(String value)
	{
		return null;
	}

	@Override
	public String toVcardValue(String value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toXML(String value)
	{
		String output="";
		String[] lines = getEncodedLines(value);
		for(String aline : lines)
		{
			output  += "  <value>"+aline+"</value>"+ "\n";;
		}
		return output;
		
	}

	@Override
	public boolean valueContained(String attvalue, String testvalue)
	{

		if (testvalue.equals(attvalue)) return true;
		else
			return false;
	}

	

	@Override
	public int compareTo(String aarray, String barray)
	{
		return 0;
	}
	
	@Override
	public boolean matchesVcardValue(String avalue, String bvalue)
	{
		if(avalue!=null && !avalue.isEmpty())
		{
			if(bvalue!=null && ! bvalue.isEmpty())
			{
				int al = avalue.length();
				int bl = bvalue.length();
				if (Math.abs(al-bl) > 1 ) return false;
				else return true;
			}
			return false ;
		}
		else		
		{
			if(bvalue!=null && ! bvalue.isEmpty())
			{
				 return false;
			}
			else return true;
		}
	}
}
