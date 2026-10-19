package org.lerot.mycontacts;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;

public class vcardContactReader
{

	vcardTokenReader tr;
	int contactnumber = 1;

	LinkedHashMap<String, mcImportexception> exceptions;
	private mcMappings mappings;


	public vcardContactReader(String importfilename, mcMappings amapping)
	{
		mappings = amapping;
		try
		{
			exceptions = new LinkedHashMap<String, mcImportexception>();
			tr = new vcardTokenReader(importfilename);
		} catch (FileNotFoundException e)
		{
			System.out.println(" FileNotFoundException");
		} catch (UnsupportedEncodingException e)
		{
			System.out.println(" UnsupportedEncodingException ");
		} catch (IOException e)
		{
			System.out.println(" IOException ");
			e.printStackTrace();
		}
	}

	public mcImportContact getContact() throws mcGetContactException
	{
		mcImportContact newcontact = new mcImportContact(contactnumber);
		contactnumber++;
		 String tid="";
		String fn="";
		boolean foundbegin = false;
		boolean foundend = false;
		mcImporttoken intoken;
		if(!tr.HasNext()) return null;
		intoken = tr.getNextToken();
		if (intoken.tokenEquals("begin"))
			foundbegin = true;
		while (tr.HasNext() && !foundbegin)
		{	
			intoken = tr.getNextToken();
			if (intoken.tokenEquals("begin"))
				foundbegin = true;
		}
		intoken = tr.getNextToken();
		while (intoken!=null && !foundend)
		{		
			if (intoken.tokenEquals("begin"))
			{
				System.out.println(" Unexpected begin around  line number :"+tr.getLinenumber());
			}			
			if (intoken.tokenEquals("end"))
				foundend = true;
			else
			{
				newcontact.processvcardinputtoken(intoken, mappings, exceptions);
				if(intoken.token.equalsIgnoreCase("TID")) tid= intoken.value;
				if(intoken.token.equalsIgnoreCase("FN")) fn= intoken.value;
				intoken = tr.getNextToken();
			}	
		}
		if (foundbegin && foundend)
		{
			if(tid.isEmpty()  && !fn.isEmpty())
			{
				tid=fn;		
			}
			if(!tid.isEmpty()  && fn.isEmpty())
			{			
				mcImportAttribute tidatt = new mcImportAttribute("name","",fn);			
				newcontact.addImportAttribute(tidatt);			
			}
			newcontact.setTID(tid.toLowerCase());
			return newcontact;
		}
		else if (foundbegin && !foundend)
		{
			System.out.println(" Unexpected end of input without end around  line number :"+tr.getLinenumber());
			throw new mcGetContactException();
		}
		else
		{
			System.out.println(" End of input around  line number :"+tr.getLinenumber());
			return null;
		}
	}

	public LinkedHashMap<String, mcImportexception> getExceptions()
	{
		return exceptions;
	}

}
