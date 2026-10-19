package org.lerot.mycontacts;

/************************************************************************
 *
 * Copyright 2009 J David Eisenberg All rights reserved.
 *
 * Uses ODF Toolkit which is Copyright 2008 Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Use is subject to license terms.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0. You can also
 * obtain a copy of the License at http://odftoolkit.org/docs/license.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ************************************************************************/

import org.odftoolkit.odfdom.dom.element.office.OfficeTextElement;
import org.odftoolkit.simple.TextDocument;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * based on code by
 * 
 * @author J David Eisenberg
 */
public class mcLetter
{

	private Map<String, String> map = new HashMap<String, String>();
	private TextDocument odf;
	private static Vector<String> templates;
	private String outputFileName;
	String templateFileName;

	public void printLetter()
	{
		File file1 = new File(templateFileName);
		File file2 = new File(outputFileName);
		try
		{
			readOdt(file1);
			saveOdt(file2);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void iteratorOverEveryVariableSet(NodeList childNodes)
	{
		for (int i = 0; i < childNodes.getLength(); i++)
		{
			Node item = childNodes.item(i);

			if (item instanceof NodeList)
				iteratorOverEveryVariableSet(item.getChildNodes());

			if (item.getNodeName().equals("text:variable-set"))
			{
				String nodeValue = item.getAttributes()
						.getNamedItem("text:name").getNodeValue();
				{
					// System.out.println("nodeValue :" + nodeValue);
				}
				Node target = item.getChildNodes().item(0);
				if (target != null)
				{
					target.setNodeValue(map.get(nodeValue));
				}
			}
		}
	}

	public void readOdt(File file) throws Exception
	{
		odf = TextDocument.loadDocument(file);
	}

	public void saveOdt(File file) throws Exception
	{
		OfficeTextElement contentRoot = odf.getContentRoot();
		contentRoot.getAttributeNS("dc.title", " a long title ");
		iteratorOverEveryVariableSet(contentRoot.getChildNodes());
		odf.save(file);
	}

	public void setVariable(String key, String value)
	{
		map.put(key, value);
	}

	String getOutputFileName()
	{
		return outputFileName;
	}

	public void setOutputFileName(String fileName)
	{
		outputFileName = fileName;
	}

	String getTemplateFileName()
	{
		return templateFileName;
	}

	public void setTemplateFileName(String fileName)
	{
		templateFileName = fileName;
	}

	public static String getSalutation(mcContact selcontact)
	{
		String format = "Dear fn sn";
		if (selcontact.hasTag("family") || selcontact.hasTag("friends"))
			format = "Dear fn ";
		return selcontact.getName(format);
	}

	public static void getTemplates(String dotcontacts)
	{
		templates = new Vector<String>();
		File dotdir = new File(dotcontacts);
		File[] files = dotdir.listFiles();
		for (File file : files)
		{
			String name = file.getName();
			if (name.contains("letter") && name.endsWith("template.odt"))
			{
				templates.add(file.getName());
				//System.out.println(file.getName());
			}
		}
	}

	public static Vector<String> getTemplateList()
	{
		return templates;
	}

	public static String makeFileName(mcContact selcontact)
	{
		String name = selcontact.getName("sn fn");
		name = name.replace(" ", "");
		String date = mcDateDataType.getNow("_yyyyMMdd");
		return name + date;
	}
	
	public static String makeFileName(String name)
	{
		name = name.replace(" ", "");
		String date = mcDateDataType.getNow("_yyyyMMdd");
		return name + date;
	}
}
