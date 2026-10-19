package org.lerot.mycontacts;

public class mcImporttokenData {
	public String charset;
	public String encoding;
	public String status;
	public String token;
	public String value;

	public mcImporttokenData(String charset, String status) {
		this.charset = charset;
		this.status = status;
	}
}