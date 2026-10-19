package org.lerot.mycontacts;


public class mcImportexception
{
	
		public int count;
		public String example;
		public String token;

		public mcImportexception(int count, String example, String token) {
			this.count = count;
			this.example = example;
			this.token = token;
		}
	

	//private importexceptionData data = 	new importexceptionData(0, "", "");

	public mcImportexception() {
			
		}


	public int getCount()
	{
		return count;
	}

	public String getExample()
	{
		return example;
	}

	public String getToken()
	{
		return token;
	}

	public void setCount(int count)
	{
		this.count = count;
	}

	public void setExample(String example)
	{
		this.example = example;
	}

	public void setToken(String token)
	{
		this.token = token;
	}
	}



