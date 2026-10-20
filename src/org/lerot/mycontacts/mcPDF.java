package org.lerot.mycontacts;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class mcPDF
{

	private static Font largeBold = new Font(Font.FontFamily.HELVETICA, 16,
			Font.BOLD);
	private static Font smallNormal = new Font(Font.FontFamily.HELVETICA, 10,
			Font.NORMAL);
	private static Font selectedfont;

	Document document;
	PdfWriter writer;
	PdfContentByte canvas; //595 × 842 points
	//int a4h = 842;
	//int a4w = 595;
	float a4w = (PageSize.A4).getWidth();
    float a4h = (PageSize.A4).getHeight();
	private float lm, rm, bm, tm, tw, th, ch,cw;
	private String title;
	private int nrows;
	private int ncols;
	private int ncells;
	private float cellheight,cellwidth;

	private float[] collist;

	private float pb;
	private float pr;
	private float pl;
	private float pt;
	private boolean rotate;
	private boolean border;
	

	public mcPDF(File outfile, String atitle)
	{
		title = atitle;
		if (MyContacts.labeltemplates == null)
		{
			MyContacts.labeltemplates = readTemplates();
		}
		
		document = new Document(PageSize.A4, lm, rm, tm, bm);
		try
		{
			writer = PdfWriter.getInstance(document,
					new FileOutputStream(outfile));
			// canvas = writer.getDirectContent();
		} catch (FileNotFoundException | DocumentException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		document.open();
		
	}
	


	private void addMetaData()
	{
		document.addTitle(title);
		document.addSubject("generated from mcContacts");
		document.addAuthor("author");
	}

	public String makeBlockAddress(mcContact acontact,boolean showuk)
	{
		String ostr = "";
		//String name = acontact.getName("title fn mn sn ");
		//if (!name.trim().isEmpty()) ostr += name + "\n";

		String address = acontact.makeBlockAddress("\n",showuk);
		if (address != null && !address.isEmpty())
		{
				ostr += address;
		} else
		{
            mcAttribute ab = acontact.getAddress();
			System.out.println(" pdf no address for :" + acontact);
			ostr = null;
		}

		return ostr;
	}

	public int makeLabelPage(String address, int startcell)
	{
		addMetaData();
		document.newPage();
		document.setMargins(lm, rm, tm, bm);
		int iborder = 0;
		int k = 0;
		int p = 1;

		try
		{
			document.newPage();
		
			PdfPTable table = new PdfPTable(ncols);
			table.setTotalWidth(tw);
			table.setLockedWidth(true);
			if(!border)
				   table.getDefaultCell().setBorder(0);
				else
					table.getDefaultCell().setBorder(1+2+4+8);

			table.setWidths(collist);
			Paragraph p0 = new Paragraph();
			p0.add(new Phrase(" ", selectedfont));
			PdfPCell c0 = new PdfPCell(p0);
			c0.setFixedHeight(cellheight);
			c0.setPaddingLeft(pl);
			c0.setPaddingRight(pr);
			c0.setPaddingTop(pt);
			c0.setPaddingBottom(pb);
			c0.setBorder(iborder);
			if (startcell > 1)
			{
				for (int i = 1; i < startcell; i++)
				{
					table.addCell(c0);
				}
			}
			k = startcell - 1;

			if (address != null && !address.isEmpty())
			{
				Paragraph p1 = new Paragraph();
				p1.add(new Phrase(address, selectedfont));
				PdfPCell c1 = new PdfPCell(p1);
				c1.setFixedHeight(cellheight);
				
				c1.setPaddingLeft(pl);
				c1.setPaddingRight(pr);
				c1.setPaddingTop(pt);
				c1.setPaddingBottom(pb);			 
				c1.setBorder(iborder);
				if (rotate)
				{
					c1.setRotation(90);
				}
				table.addCell(c1);
			}

			int rowused = k % ncols;
			System.out.println(" remainder is " + rowused);
			if (rowused > 0)
			{
				for (int i = 0; i < ncols - rowused; i++)
				{

					table.addCell(c0);
				}
			}
			table.completeRow();
			document.add(table);
		} catch (DocumentException e)
		{
			e.printStackTrace();
		}
		document.close();
		return p;
	}

	public int makeLabelsPages(mcContacts searchresults, int startcell, boolean showuk)
	{
		addMetaData();
		document.newPage();
		document.setMargins(lm, rm, tm, bm);
		 
		int k = 0;
		int p = 1;
		int iborder =0;
		if(border)
            iborder = 1+2+4+8; //BooleanUtils.toInteger(border);
		try
		{
			document.newPage();
			document.setMargins(lm, rm, tm, bm);
			PdfPTable table = new PdfPTable(ncols);
			table.setWidthPercentage(100);
			//table.setTotalWidth(tw);
			//table.setLockedWidth(true);
			table.getDefaultCell().setBorder(iborder);
			table.setWidths(collist);
			Paragraph p0 = new Paragraph();
			p0.add(new Phrase(" ", selectedfont));
			PdfPCell c0 = new PdfPCell(p0);
			c0.setFixedHeight(cellheight);
			c0.setPaddingLeft(pl);
			c0.setPaddingRight(pr);
			c0.setPaddingTop(pt);
			c0.setPaddingBottom(pb);
			c0.setBorder(iborder);
			if (startcell > 1)
			{
				for (int i = 1; i < startcell; i++)
				{
					table.addCell(c0);
				}
			}
			k = startcell - 1;
			for (mcContact acontact : searchresults.makeOrderedContactsVector())
			{
				acontact.fillContact();
				String address = makeBlockAddress(acontact,showuk);
				if (address != null && !address.isEmpty())
				{
					Paragraph p1 = new Paragraph();
					p1.add(new Phrase(address, selectedfont));
					PdfPCell c1 = new PdfPCell(p1);
					c1.setFixedHeight(cellheight);
					c1.setPaddingLeft(pl);
					c1.setPaddingRight(pr);
					c1.setPaddingTop(pt);
					c1.setPaddingBottom(pb);	
					
					c1.setBorder(iborder);
					
					if (rotate)
					{
						c1.setRotation(90);
					}

					table.addCell(c1);
					k++;
					if (k == ncells)
					{
						document.add(table);
						document.newPage();
						document.setMargins(lm, rm, tm, bm);
						table = new PdfPTable(ncols);
						table.setWidthPercentage(100);
					//	table.setTotalWidth(tw);
					//	table.setLockedWidth(true);
						table.setWidths(collist);
						k = 0;
						p++;
					}
				}
			}
			int rowused = k % ncols;
			System.out.println(" remainder IS " + rowused);
			if (rowused > 0)
			{
				for (int i = 0; i < ncols - rowused; i++)
				{
					table.addCell(c0);
				}
			}
			table.completeRow();
			document.add(table);
		} catch (DocumentException e)
		{
			e.printStackTrace();
		}
		document.close();
		return p;
	}

	private String makeBlockAddress(mcContact acontact)
	{
		return makeBlockAddress(acontact,false);
	}

	public static Map<String, Map<String, String>> readTemplates()
	{
		try
		{
			HashMap<String, Map<String, String>> addlist = new HashMap<String, Map<String, String>>();
			SAXReader reader = SAXReader.createDefault();
			org.dom4j.Document doc = reader
					.read(MyContacts.settingsdir + "/labelTemplates.xml");
			List<Node> nodes = doc.selectNodes("/labels/label");
			for (Node node : nodes)
			{
				Map<String, String> vlines = new HashMap<String, String>();
				String key = node.valueOf("@key");
				List<Node> lines = node.selectNodes("parameter");
				for (Node line : lines)
				{
					String pkey = line.valueOf("@key");
					String pvalue = line.valueOf("@value");
					vlines.put(pkey, pvalue);
				}
				addlist.put(key, vlines);
			}
			return addlist;

		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public void setLayout(String layout)
	{
		Map<String, String> tlayout = MyContacts.labeltemplates.get(layout);
		ncols = mcUtilities.toInteger(tlayout.get("ncols"));
		if(ncols==3)
		   collist = new float[] { 1, 1, 1 };
		else if (ncols==2)
			collist = new float[] { 1, 1 };
		 else if (ncols==1)
			collist = new float[] { 1};

		rotate = mcUtilities.toBoolean(tlayout.get("rotate"));
		nrows = mcUtilities.toInteger(tlayout.get("nrows"));
		rm = mcUtilities.toFloat(tlayout.get("rightmargin"));
		lm = mcUtilities.toFloat(tlayout.get("leftmargin"));
		tm = mcUtilities.toFloat(tlayout.get("topmargin"));
		pt = mcUtilities.toFloat(tlayout.get("toppadding"));
		pl = mcUtilities.toFloat(tlayout.get("leftpadding"));
		pr = mcUtilities.toFloat(tlayout.get("rightpadding"));
		pb = mcUtilities.toFloat(tlayout.get("bottompadding"));
		bm = mcUtilities.toFloat(tlayout.get("bottommargin"));
		ch = mcUtilities.toFloat(tlayout.get("cellheight"));
		cw = mcUtilities.toFloat(tlayout.get("cellwidth"));
		border =  mcUtilities.toBoolean(tlayout.get("border"));
		ncells = nrows * ncols;
		tw = a4w - lm - rm ;
		th = a4h - tm - bm;
		if(ch>0)
			cellheight = ch;
		else
		  cellheight = (th / nrows);
		if(cw>0)
			cellwidth = cw;
		else
		  cellwidth = (tw / ncols);
		System.out.println("tw="+tw+" "+lm +" "+rm);
		System.out.println("cellheight="+cellheight);
		System.out.println("cellwidth="+cellwidth);
		selectedfont = smallNormal;
		if (tlayout.get("font").equalsIgnoreCase("largeBold"))
		{
			selectedfont = largeBold;
		}
		

	}

	

	public void setTitle(String atitle)
	{
		title = atitle;

	}

	public int makeLabelsPages(mcContacts selectedcontactlist, int sp)
	{
	
		return makeLabelsPages(selectedcontactlist,sp,false);
	}

}
