package org.lerot.mycontacts;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;

public class mcUtilities
{

	public static String arraytoString(Vector<String> invector, String sep,
			boolean ignoreblanklines)
	{
		String outline = "";
		for (String aline : invector)
		{
			if (aline == null) aline = "";
			aline = aline.trim();
			if (ignoreblanklines)
			{
				if (!aline.isEmpty()) outline += aline + sep;
			} else
			{
				outline += aline + sep;
			}
		}
		System.out.println(" address " + outline);
		return outline;
	}
	
	public static String settoString(Set<String> tokenlist, String sep,
			boolean ignoreblanklines)
	{
		String outline = "";
		for (String atoken : tokenlist)
		{
			if (atoken == null) atoken = "";
			atoken = atoken.trim();
			if (ignoreblanklines)
			{
				if (!atoken.isEmpty()) outline += atoken + sep;
			} else
			{
				outline += atoken + sep;
			}
		}
		return outline;
	}
	
	public static String settoString(Set<String> tokenlist)
	{
		return  settoString( tokenlist, ",",	true);
	}
	

	public static boolean containedBy(String testvalues, String value)
	{
		if (isArray(testvalues))
		{
			String nvalue = value.toLowerCase();
			Map<String, String> fields = parseArray(testvalues);
			for (Entry<String, String> entry : fields.entrySet())
			{
				String avalue = entry.getValue().toLowerCase();
				if ((!nvalue.contains(avalue))) { return false; }
			}
			return true;

		} else
		{
			String[] values = testvalues.replace(" ", ",").split(",");
			return containedBy(values, value);
		}
	}

	public static boolean containedBy(String[] values, String value)
	{
		String nvalue = value.toLowerCase();
		for (String avalue : values)
		{
			if (!(avalue.trim().isEmpty()))
			{
				avalue = avalue.toLowerCase();
				if (!nvalue.contains(avalue)) { return false; }
			}
		}
		return true;
	}

	public static Vector<String> datetoVector(String svalue)
	{
		Vector<String> tokens = new Vector<String>();
		if (isArray(svalue))
		{
			Map<String, String> fields = parseArray(svalue);
			for (Entry<String, String> entry : fields.entrySet())
			{
				String token = entry.getValue();
				if (token != null && !token.isEmpty()) tokens.add(token);
			}
			return tokens;

		} else
		{
			String[] split = svalue.split("-");
			for (String token : split)
			{
				token = token.trim();
				if (!token.isEmpty() && !token.equals("-"))
				{
					tokens.add(token);
				}
			}
			return tokens;
		}

	}

	public static String formatCumulativeText(String newvalue)
	{
		String normvalue = newvalue.toLowerCase().replace("  ", " ");
		normvalue = normvalue.replace(";", "");
		return normvalue.trim();
	}

	public static String getHost(String value)
	{
		String[] parts = { "", "" };
		parts[0] = value;
		parts[1] = "";
		if (value != null)
		{
			int k = value.indexOf("@");
			if (k > 1)
			{
				parts[0] = value.substring(0, k);
				parts[1] = value.substring(k + 1);
			}
		}
		return parts[1];
	}

	public static boolean isArray(String invalue)
	{
		if (invalue == null) return false;
		invalue = invalue.trim();
		if (invalue != null && invalue != "")
		{
			if (invalue.startsWith("{")
					&& invalue.endsWith("}")) { return true; }
		}
		return false;
	}

	public static boolean isNumeric(String vqual)
	{
		if (vqual == null || vqual.isEmpty()) return false;
		return StringUtils.isNumeric(vqual);

	}

	public static String normaliseaddress(String anaddress)
	{
		String normaddress = "";

		String[] split = anaddress.split(",");
		for (String apart : split)
		{
			if (!apart.equals(",") && !apart.isEmpty())
			{
				if (normaddress.isEmpty())
				{
					normaddress = apart.trim();
				} else
				{
					normaddress = normaddress + ", " + apart.trim();
				}
			}
		}
		return normaddress;
	}

	public static String normalisename(String sname)
	{
		String normname = "";
		sname = sname.toLowerCase().replace(" ", ",");
		sname = sname.replace("?", ",");
		sname = sname.replace(";", ",");
		sname = sname.replace("&", ",");
		sname = sname.replace(" and ", ",");
		sname = sname.replace(" et ", ",");
		String[] split = sname.split(",");
		Arrays.sort(split);
		for (String apart : split)
		{
			if (!apart.equals(",")) normname = normname + apart + " ";
		}
		return normname.trim();
	}

	public static String normalisephonenumber(String sname)
	{
		sname = sname.trim().toLowerCase().replaceAll(" ", "");
		if (sname.startsWith("0")) sname = sname.replaceFirst("0", "+44");
		return sname;
	}

	public static Map<String, String> parse_csv(String invalue, char delimiter)
	{
		if (invalue.contains("="))
			return parse_csv(invalue, ',', '=');
		else
			return parse_csv(invalue, ',', ':');
	}

	public static Map<String, String> parse_csv(String csv_string,
			char delimiter, char sep)
	{ // echo " in parse 2". csv_string;

		Vector<Character> terminator = new Vector<Character>(10);
		for (int i = 0; i < 10; i++)
			terminator.add(' ');
		Map<String, String> outarray = new LinkedHashMap<String, String>();
		int l = csv_string.length();
		int i = 0;
		String token = "";
		int count = 0;
		int depth = 1;
		while (i < l)
		{
			char c = csv_string.charAt(i);
			if (depth == 1)
			{
				if (c == delimiter)
				{
					int k = token.indexOf(sep);
					if (k == -1)
					{
						outarray.put(Integer.toString(count), token);
					} else
					{
						String key = token.substring(0, k).trim();
						String value = token.substring(k + 1).trim();
						if (value != null && value.length() > 0)
						{
							if (value.charAt(0) == '\''
									|| value.charAt(0) == '\"')
								value = value.substring(1);
							int tl = value.length();
							if (value.charAt(tl - 1) == '\''
									|| value.charAt(tl - 1) == '\"')
								value = value.substring(tl - 1);
						}
						outarray.put(key, value);
					}
					token = "";
					count = count + 1;

				} else
				{
					if (c == '"' || c == '\'')
					{
						depth = depth + 1;
						terminator.add(depth, c);
					} else
						token = token + c;
				}
			} else
			{
				if (c == terminator.get(depth))
				{
					depth = depth - 1;
				}
				/*
				 * else if (c == '"' || c == '\'') { depth = depth + 1;
				 * terminator.add(depth, c); }
				 */
				else
					token = token + c;
			}
			i++;
		}
		if (token != "")
		{
			int k = token.indexOf(sep);
			if (k == -1)
			{
				outarray.put(Integer.toString(count), token);
			} else
			{
				String key = token.substring(0, k).trim();
				String value = token.substring(k + 1).trim();
				if (value != null && value.length() != 0)
				{
					if (value.charAt(0) == '\'' || value.charAt(0) == '\"')
						value = value.substring(1);
					int tl = value.length();
					if (value.charAt(tl - 1) == '\''
							|| value.charAt(tl - 1) == '\"')
						value = value.substring(tl - 1);
					outarray.put(key, value);
				}
			}
		}
		return outarray;
	}
	
	public static Map<String, String> parse_csvParameters(String csv_string,
			char delimiter, char sep)
	{ // echo " in parse 2". csv_string;

		Vector<Character> terminator = new Vector<Character>(10);
		for (int i = 0; i < 10; i++)
			terminator.add(' ');
		Map<String, String> outarray = new LinkedHashMap<String, String>();
		int l = csv_string.length();
		int i = 0;
		String token = "";
		int count = 0;
		int depth = 1;
		while (i < l)
		{
			char c = csv_string.charAt(i);
			if (depth == 1)
			{
				if (c == delimiter)
				{
					int k = token.indexOf(sep);
					if (k == -1)
					{
						//outarray.put(Integer.toString(count), token);
						addto(outarray,Integer.toString(count), token,";");
					} else
					{
						String key = token.substring(0, k).trim();
						String value = token.substring(k + 1).trim();
						if (value != null && value.length() > 0)
						{
							if (value.charAt(0) == '\''
									|| value.charAt(0) == '\"')
								value = value.substring(1);
							int tl = value.length();
							if (value.charAt(tl - 1) == '\''
									|| value.charAt(tl - 1) == '\"')
								value = value.substring(tl - 1);
						}
						//outarray.put(key, value);
						addto(outarray,key, value,";");
					}
					token = "";
					count = count + 1;

				} else
				{
					if (c == '"' || c == '\'')
					{
						depth = depth + 1;
						terminator.add(depth, c);
					} else
						token = token + c;
				}
			} else
			{
				if (c == terminator.get(depth))
				{
					depth = depth - 1;
				}
				/*
				 * else if (c == '"' || c == '\'') { depth = depth + 1;
				 * terminator.add(depth, c); }
				 */
				else
					token = token + c;
			}
			i++;
		}
		if (token != "")
		{
			int k = token.indexOf(sep);
			if (k == -1)
			{
				//outarray.put(Integer.toString(count), token);
				addto(outarray,Integer.toString(count), token,";");
			} else
			{
				String key = token.substring(0, k).trim();
				String value = token.substring(k + 1).trim();
				if (value != null && value.length() != 0)
				{
					if (value.charAt(0) == '\'' || value.charAt(0) == '\"')
						value = value.substring(1);
					int tl = value.length();
					if (value.charAt(tl - 1) == '\''
							|| value.charAt(tl - 1) == '\"')
						value = value.substring(tl - 1);
					//outarray.put(key, value);
					addto(outarray,key, value,";");
				}
			}
		}
		return outarray;
	}

	private static void addto(Map<String, String> outarray, String key,
			String token, String sep)
	{
		if(outarray.containsKey(key))
		{
			String nvalue = outarray.get(key)+sep+token;
			outarray.put(key,nvalue);
		}
		else
		{
			outarray.put(key,token);
		}
	}

	public static Vector<String> parse_csv2(String csv_string, char delimiter)
	{ // echo " in parse 2". csv_string;

		Vector<String> outarray = new Vector<String>();
		int l = csv_string.length();
		int i = 0;
		String token = "";
		int count = 0;
		boolean instring = false;
		char endquote = '"';
		while (i < l)
		{
			char c = csv_string.charAt(i);
			if (instring)
			{
				if (c == endquote)
					instring = false;
				else
					token = token + c;
			} else
			{
				if (c == delimiter)
				{
					outarray.add(count, token);
					token = "";
					count = count + 1;

				} else
				{
					if (c == '"')
					{
						instring = true;
						endquote = c;
					} else
						token = token + c;
				}
			}
			i++;
		}
		if (token != "")
		{
			outarray.add(count, token);
			token = "";
			count = count + 1;
		}
		return outarray;
	}

	public static Map<String, String> parseArray(String invalue)
	{
		Map<String, String> outarray = new LinkedHashMap<String, String>();
		invalue = invalue.trim();
		if (invalue.isEmpty()) return null;
		if (invalue.startsWith("{")) invalue = invalue.substring(1);
		if (invalue.endsWith("}"))
			invalue = invalue.substring(0, invalue.length() - 1);
		invalue = invalue.trim();
		if (invalue.length() < 1) return null;
		outarray = parse_csv(invalue, ',');
		return outarray;
	}

	public static String parseKey(String attkey)
	{
		String mainkey = attkey;
		if (attkey != null)
		{
			int k = attkey.indexOf("/");
			if (k > 1) mainkey = attkey.substring(0, k);
		}
		return mainkey;
	}

	public static String[] parseLabel(String label)
	{
		String[] parts = { "", "" };
		parts[0] = label;
		parts[1] = "";
		if (label != null)
		{
			int k = label.indexOf("/");
			if (k > 1)
			{
				parts[0] = label.substring(0, k);
				parts[1] = label.substring(k + 1);
			}
		}
		return parts;
	}

	public static Vector<String> readLinetoArray(Reader bf)
	{
		return readLinetoArray(bf, ',');
	}

	public static Vector<String> readLinetoArray(Reader bf, char sep)
	{
		Vector<String> inarray = new Vector<String>();
		boolean inquotes = false;
		String token = "";
		int k = 0;
		int inch;
		try
		{
			inch = bf.read();
			if (inch == '\n' || inch == '\r') inch = bf.read();
			boolean end = false;
			while (inch != -1 && !end)
			{
				char inchar = (char) inch;
				if (inquotes)
				{
					if (inchar == '\"')
					{
						token = token.replaceAll("(\r\n|\r|\n|\n\r)", ",");
						inquotes = false;
					} else
						token = token + inchar;
				} else
				{
					if (inchar == '\"')
					{
						inquotes = true;
					} else if (inch == '\n' || inch == '\r')
					{

						token = token.replaceAll("[^\\P{Cc}\t\r\n]", "");
						inarray.add(k, token);
						token = "";
						end = true;
					} else if (inch == sep)
					{
						token = token.replaceAll("[^\\P{Cc}\t\r\n]", "");
						inarray.add(k, token);
						token = "";
						k = k + 1;
					} else
					{
						token = token + inchar;
					}
				}
				inch = bf.read();
			}
			if (!end) inarray.add(k, token);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (k < 1) return null;
		return inarray;

	}

	public static String readLinetoString(Reader bf)
	{

		boolean inquotes = false;

		String token = "";
		int inch;
		try
		{
			inch = bf.read();
			if (inch == '\n' || inch == '\r') inch = bf.read();
			boolean end = false;
			while (inch != -1 && !end)
			{
				char inchar = (char) inch;
				if (inquotes)
				{
					token = token + inchar;
				} else
				{
					if (inch == '\n' || inch == '\r')
					{
						end = true;
					} else
					{
						token = token + inch;
					}
				}
				inch = bf.read();
			}

		} catch (IOException e)
		{
			e.printStackTrace();
		}
		if (token.length() < 1) return null;
		return token;

	}

	public static Vector<String> sortByValues(HashMap<String, Integer> map)
	{
		List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(
				map.entrySet());
		Collections.sort(list, new Comparator<Entry<String, Integer>>()
		{
			@Override
			public int compare(Entry<String, Integer> o1,
                               Entry<String, Integer> o2)
			{
				return o2.getValue() - o1.getValue();
			}
		});

		Vector<String> sortedvector = new Vector<String>();
		for (Iterator<Entry<String, Integer>> it = list.iterator(); it
				.hasNext();)
		{
			Entry<String, Integer> entry = it.next();
			sortedvector.add(entry.getKey());
		}
		return sortedvector;
	}

	public static HashMap<String, Integer> sortMapByValues(
			HashMap<String, Integer> map)
	{
		LinkedList<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(
				map.entrySet());
		Collections.sort(list, new Comparator<Entry<String, Integer>>()
		{
			@Override
			public int compare(Entry<String, Integer> o1,
                               Entry<String, Integer> o2)
			{
				int v1 = o1.getValue();
				int v2 = o2.getValue();
				return v2 - v1;
			}
		});
		LinkedHashMap<String, Integer> sortedmap = new LinkedHashMap<String, Integer>();
		for (Entry<String, Integer> entry : list)
		{
			sortedmap.put(entry.getKey(), entry.getValue());
		}
		return sortedmap;
	}

	public static String tidyValue(String value)
	{
		String tidyvalue = "";
		value = value.replaceAll("\\p{M}", "");
		value = value.toLowerCase().replace(" ", ",");
		value = value.replace("?", "");
		value = value.replace("-", "");
		value = value.replace(";", ",");
		value = value.replace(" ", "");
		String[] split = value.split(",");
		for (String apart : split)
		{
			if (!apart.equals(",") && !apart.isEmpty())
			{
				if (tidyvalue.isEmpty())
				{
					tidyvalue = apart.trim();
				} else
				{
					tidyvalue = tidyvalue + " " + apart.trim();
				}
			}
		}
		return tidyvalue.trim();
	}

	public static String keyvaluesmaptoArrayString(Map<String, String> addmap, String sep)
	{
		String out = "{";
		for (Entry<String, String> line : addmap.entrySet())
		{
			String key = line.getKey();
			String value = line.getValue();
			if (value != null && !value.isEmpty())
			{
				out = out + key + sep + "'" + value + "', ";
			}
		}
		out += "}";
		return out;
	}

	public static String tokeniseValue(String sname)
	{
		Vector<String> tokens = tokenVector(sname);
		String tokenisedstring = "";
		for (String token : tokens)
		{
			if (!token.equals(",") && !token.isEmpty())
			{
				if (tokenisedstring.isEmpty())
				{
					tokenisedstring = token.trim();
				} else
				{
					tokenisedstring = tokenisedstring + ", " + token.trim();
				}
			}
		}
		return tokenisedstring.trim();
	}

	public static Vector<String> tokenVector(String svalue)
	{
		Vector<String> tokens = new Vector<String>();
		if (isArray(svalue))
		{
			Map<String, String> fields = parseArray(svalue);
			for (Entry<String, String> entry : fields.entrySet())
			{
				String token = entry.getValue();
				if (token != null && !token.isEmpty()) tokens.add(token);
			}
			return tokens;

		} else
		{
			svalue = svalue.toLowerCase().replace(" ", ",");
			svalue = svalue.replace("?", "");
			svalue = svalue.replace(";", ",");
			String[] split = svalue.split(",");
			for (String token : split)
			{
				token = token.trim();
				if (!token.isEmpty() && !token.equals(","))
				{
					tokens.add(token);
				}
			}
			return tokens;
		}
	}
	
	
	public static int toInteger(String sint, int def)
	{
		if(sint == null || sint.isEmpty())  return def;
		return Integer.parseInt(sint);
	}
	
	public static int toInteger(String sint)
	{
		return toInteger(sint,-1);
	}
	
	public static boolean toBoolean(String sbool, boolean def)
	{
		if(sbool == null || sbool.isEmpty())  return def;
		return (sbool.equalsIgnoreCase("true"));
	}
	
	public static boolean toBoolean(String sbool)
	{
		return toBoolean(sbool, false);	
	}

	public static boolean IsInteger(String atoken)
	{
		if(atoken == null || atoken.isEmpty())  return false;
		 try { 
		        Integer.parseInt(atoken); 
		    } catch(NumberFormatException e) { 
		        return false; 
		    } catch(NullPointerException e) {
		        return false;
		    }
		return true;
	}
	
	public static float toFloat(String astring )
	{
		return toFloat(astring, (float) 0.0);
	}

	public static float toFloat(String astring, float def )
	{
		if(astring == null || astring.isEmpty())  return def;
		return Float.parseFloat(astring);
	}
	
	  public static String getFileExtension(Path file)
	  {
	        String fileName = file.getFileName().toString();
	        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
	        return fileName.substring(fileName.lastIndexOf(".")+1);
	        else return "";
	  }


	public static String makeIDstr(String idstr)
	{
		String ids = ("000000" + idstr);
		int l = ids.length();
		return ids.substring(l - 6);
	}

	public static String makeIDstr(int id)
	{
		String ids = ("000000" + id);
		int l = ids.length();
		return ids.substring(l - 6);
	}
}
