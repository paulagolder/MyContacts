package org.lerot.mycontacts;

import org.apache.commons.codec.DecoderException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class vcardTokenReader
{
	static String[] Nfields = { "sn", "fn", "mn", "title", "sufix" };
	static String[] BDAYfields = { "year", "month", "day" };
	static String[] ADRfields = { "pobox", "building", "street", "city",
			"county", "postcode", "country" };
	// private BufferedReader bufferedreader;
	Queue<mcImporttoken> tokenlist;
	private String lastline;
	private int linenumber = -1;
	private vcardlinereader lr;

	public vcardTokenReader(String filename) throws IOException
	{
		tokenlist = new LinkedList<mcImporttoken>();
		lr = new vcardlinereader(filename);
		getToken();
	}

	public mcImporttoken getNextToken()
	{

		if (tokenlist.peek() == null)
		{
			getToken();
		}
		mcImporttoken nexttoken = tokenlist.poll();
		getToken();
		return nexttoken;
	}

	private void getToken()
	{

		mcImporttoken atoken = new mcImporttoken();
		String token = "";
		if (!lr.hasInput()) return;
		try
		{
			String newline = lr.getLine();         
			linenumber = lr.getLinenumber();
			if (newline == null) { return; }
			lastline = newline;
			String invalue = "";
			int j = newline.indexOf(":");
			if (j > 0)
			{
				token = newline.substring(0, j).toUpperCase();
				invalue = newline.substring(j + 1).trim();
			} else
			{
				atoken.setValue(newline);
				atoken.setStatus("notoken");
				tokenlist.add(atoken);
				return;
			}
			if (token.contains(";"))
			{
				int k = token.indexOf(";");
				String parameters = token.substring(k);
				token = token.substring(0, k);
                if(parameters.contains("lison"))
                {
                	System.out.println(" alison parames");
                }
				Map<String, String> paramlist = mcUtilities.parse_csvParameters(
						parameters, ';', '=');

				if (paramlist.containsKey("CHARSET"))
				{
					atoken.setCharset(paramlist.get("CHARSET"));
				}
				if (paramlist.containsValue("JPEG"))
				{

				}
				if (paramlist.containsKey("TYPE"))
				{
					atoken.setType(paramlist.get("TYPE"));
				}
				if (paramlist.containsKey("ENCODING"))
				{
					String encoding = paramlist.get("ENCODING");
					// vCards write it QUOTED-PRINTABLE; also accept the old spelling
					if (encoding.equalsIgnoreCase("QUOTED-PRINTABLE")
							|| encoding.equalsIgnoreCase("QUOTEDPRINTABLE"))
					{
						atoken.setEncoding("qp");
						while (invalue.endsWith("="))
						{
							newline = lr.getLine().trim();
							linenumber = lr.getLinenumber();
							invalue = invalue
									.substring(0, invalue.length() - 1)
									+ newline;
						}
						String dcvalue = invalue;
						atoken.setStatus("ok");
						try
						{
							dcvalue = new String(
									org.apache.commons.codec.net.QuotedPrintableCodec
											.decodeQuotedPrintable(invalue
													.getBytes()), "UTF-8");
						} catch (DecoderException
								| UnsupportedEncodingException e)
						{
							dcvalue = invalue;
							atoken.setStatus("QP decoding error");
							System.out.println("QP decoding is not working "
									+ dcvalue);
						}
						invalue = dcvalue;

					} else if (encoding.equalsIgnoreCase("BASE64"))
					{
						atoken.setEncoding("b64");
					} else if (encoding.equalsIgnoreCase("b"))
					{
						atoken.setEncoding("b64");
					}
				}
			}
			if (invalue.contains(","))
			{
				invalue = invalue.replaceAll("\\,", ",");
			}
			if (token.startsWith("ADR"))
			{
				invalue = makeArrayString(ADRfields, invalue);
				//System.out.println("address <"+invalue+">" );	

			} else if (token.startsWith("BDAY") || token.startsWith("ANNIVERSARY"))
			{
				invalue = mcDateDataType.makeArrayStringfromVcard(invalue);
			} else if (token.equalsIgnoreCase("N"))
			{
				invalue = makeArrayString(Nfields, invalue);
			}
			 else if (token.equalsIgnoreCase("NOTE"))
			{
				invalue = invalue.replaceAll("\\r\\n|\\r|\\n", "; ");
			}
			 else if (token.equalsIgnoreCase("CATEGORIES"))
			{
				invalue = invalue.replaceAll("\\r\\n|\\r|\\n|,", "; ");
				//System.out.println("tag <"+invalue+">" );			
			}
			
			atoken.setToken(token);
			atoken.setValue(invalue);
			atoken.setStatus("ok");
			tokenlist.add(atoken);
		} catch (Exception e)
		{
			System.out.println(" this is here ");
			atoken.setToken("ERROR");
			atoken.setValue(lastline);
			atoken.setStatus("error");
			tokenlist.add(atoken);
			System.out.println(" error" + atoken.shortstring() + " line:"
					+ lr.getLinenumber());
		}

		return;
	}

	private String makeArrayString(String[] fieldlist, String value, String sep)
	{
		String[] split = value.split(sep);
		String out = "{ ";
		int k = 0;
		int j = 0;
		String onlytoken = "";
		for (String atoken : split)
		{
			if (atoken != null && !atoken.trim().isEmpty())
			{
				if (!onlytoken.isEmpty()) out = out + ", ";
				out = out + fieldlist[k] + ": " + mcUtilities.quoteValue(atoken.trim());
				onlytoken = atoken;
				j++;
			}
			k++;
			if (k >= fieldlist.length) break;
		}
		out = out + "}";
	//	if (j == 1) return onlytoken;
		return out;
	}
	
	private String makeArrayString(String[] fieldlist, String value)
	{
    	return makeArrayString(fieldlist,value,";");
	}

	

	public boolean HasNext()
	{
		if (tokenlist.peek() == null)
			return false;
		else
			return true;

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
