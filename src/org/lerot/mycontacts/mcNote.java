package org.lerot.mycontacts;


import java.io.File;
import java.io.PrintWriter;


public class mcNote
{


	private String  text;
	private String outputFileName;
    private String outputFileDir;


    public mcNote(mcContact contact)
    {
        text = " Note re: "+contact.getName()+"\n";
        text +=" Date   : "+  mcDateDataType.getNow(" dd MM yyyy")+"\n\n";
        outputFileName = makeFileName(contact);
        outputFileDir = mcSettings.contactFolder(contact) + File.separator + "notes";
    }

    public void addText(String intext)
    {
        text += intext;

    }
	public void printNote(String selfile)
	{
        File directory = new File(selfile).getAbsoluteFile().getParentFile();
        if (!directory.exists())
        {
            directory.mkdirs();
        }
		try
		{
            PrintWriter out = new PrintWriter(selfile);
            out.println(text);
            out.close();

        } catch (Exception e)
		{
			e.printStackTrace();
		}
	}



	public static String makeFileName(mcContact selcontact)
	{
		return mcSettings.fileName(selcontact) + mcDateDataType.getNow("_yyyyMMdd");
	}
	
	public static String makeFileName(String name)
	{
		name = name.replace(" ", "");
		String date = mcDateDataType.getNow("_yyyyMMdd");
		return name + date;
	}

    public String getNoteName()
    {
        return  outputFileName;
    }

    public String getOutputFileName()
    {
        return outputFileName;
    }

    public void setOutputFileName(String outputFileName)
    {
        this.outputFileName = outputFileName;
    }

    public String getOutputFileDir()
    {
        return outputFileDir;
    }

    public void setOutputFileDir(String outputFileDir)
    {
        this.outputFileDir = outputFileDir;
    }

    public String getOutputPath()
    {
        return this.outputFileDir+ File.separator+ this.outputFileName;
    }
}
