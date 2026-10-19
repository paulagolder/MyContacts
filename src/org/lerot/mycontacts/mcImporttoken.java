package org.lerot.mycontacts;

public class mcImporttoken {
	String charset = "UTD8";
	String encoding = "";
	String status = "emptyline";
	String token;
	String value;
	 private String type ="";

	public mcImporttoken() {
		setToken("");
		setValue("");
	}

	public String shortstring() {
		return (token + ":" + shortValue() + "(" + encoding + ";" + charset
				+ ";" + status + ")");
	}

	public String shortValue() {
		String svalue = value;
		if (value != null) {
			int l = 30;
			if (value.length() < 30)
				l = value.length();
			svalue = value.substring(0, l);
		}
		return svalue;
	}

	public String getToken() {
		return token;
	}
	
	public boolean tokenEquals( String astring)
	{
		if(token==null)return false;
		if(token.isEmpty()) return false;
		if(token.equalsIgnoreCase(astring)) return true;
		return false;
	}

	public String getCharset() {
		return charset;
	}

	public String getStatus() {
		return status;
	}

	public String getValue() {
		return value;
	}

	public void setStatus(String astring) {
		status = astring;

	}

	public void setToken(String astring) {
		token = astring;

	}

	public void setValue(String astring) {
		value = astring;

	}

	public void setEncoding(String astring) {
		encoding = astring;

	}

	public void setCharset(String astring) {
		charset = astring;

	}

	public String getEncoding() {
		return encoding;
	}

	public void setType(String atype)
	{
		type =atype;
		
	}

	String getType()
	{
		return type;
	}
}
