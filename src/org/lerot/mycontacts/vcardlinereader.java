package org.lerot.mycontacts;

import java.io.*;
import java.util.LinkedList;
import java.util.Queue;

public class vcardlinereader
{
	private BufferedReader bufferedreader;
	Queue<String> linelist;
	private boolean inputended = false;
	private int linenumber = 0;
	private FileInputStream inputStream;
	private String message;
	private String nextline;

	public vcardlinereader(String animportfilename)
	{
		try
		{
			inputStream = new FileInputStream(animportfilename);
			bufferedreader = new BufferedReader(new InputStreamReader(
					inputStream, "UTF8"));
			linelist = new LinkedList<String>();
			readLine();
		} catch (FileNotFoundException e)
		{
			inputended = true;
			message = "File not found";
		} catch (UnsupportedEncodingException e)
		{
			message = "File Encoding error";
		}
           //System.out.println("vcardlinereader" + message);
	}

	public String getLine()
	{
		if (inputended) return null;
		readLine();
		return linelist.poll();
	}

	private void readLine()
	{
		boolean endofinput;
		String newline;

		try
		{
			if (nextline == null)
			{
				nextline = bufferedreader.readLine();
				linenumber++;
			}
			newline = nextline;
			// System.out.println(" Reading " + newline);
			nextline = null;
			if (newline == null)
			{
				newline = "EOF";
				inputended = true;
				linelist.add(newline);
				message = "No more input";
				System.out.println(" Reading0 " + message);
				return;
			}
			endofinput = false;
			nextline = bufferedreader.readLine();
			linenumber++;
			// System.out.println(" Reading2 " + nextline);
			while (!endofinput)
			{

				if (nextline == null)
				{
					linelist.add(newline);
					nextline = "EOF";
					linelist.add(nextline);
					inputended = false;
					endofinput = true;
					message = "No more input";
					//System.out.println(" Reading1 " + newline);
				} else if (nextline.isEmpty() || nextline.trim().isEmpty())
				{
					linelist.add(newline);
					endofinput = true;
					//System.out.println(" Reading2 " + newline);
				} else if (nextline.startsWith(" "))
				{
					newline += nextline.trim();
					nextline = bufferedreader.readLine();
					//System.out.println(" Reading3 " + newline);
				} else
				{
					linelist.add(newline);
					endofinput = true;
					//System.out.println(" Reading4 " + newline);
				}
			}

		} catch (IOException e)
		{
			newline = "ERROR";
			message = "IO Exception";
			linelist.add(newline);
		}

	}

	public boolean hasInput()
	{
		if (inputended)
			return false;
		else if (linelist.peek().equals("EOF"))
			return false;
		else return true;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public int getLinenumber()
	{
		return linenumber;
	}

	public void setLinenumber(int linenumber)
	{
		this.linenumber = linenumber;
	}
}
