package org.lerot.mycontacts.gui;

import org.lerot.mycontacts.*;
import org.lerot.mywidgets.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

/*
 * "Use this address": write a letter, print a label or copy the address.
 * Opened from the full window's Browse page and from the Contacts viewer,
 * so it needs nothing from the full window.
 */
public class addressDialog implements ActionListener
{
	private final Component parent;
	private final mcContact contact;

	private addressDialog(Component parent, mcContact contact)
	{
		this.parent = parent;
		this.contact = contact;
	}

	// address is the block address, with the name, one line per row
	public static void show(Component parent, mcContact contact, String address)
	{
		new addressDialog(parent, contact).choose(address);
	}

	// the drop-downs report each choice; they are read when a button is pressed
	@Override
	public void actionPerformed(ActionEvent e)
	{
	}

	private void choose(String address)
	{
		String[] options = new String[] { "Letter", "Label", "Copy", "Cancel" };
		TextTransfer textTransfer = new TextTransfer();
		textTransfer.setClipboardContents(address);
		// the letter template is chosen here too, so Letter goes straight to the file name
		jswDropDownBox templatelist = new jswDropDownBox(this, "template");
		for (String text : mcLetter.getTemplateList())
		{
			templatelist.addItem(text);
		}
		templatelist.setStyleAttribute("mywidth", 300);
		templatelist.applyStyle();
		jswHorizontalPanel templatebar = new jswHorizontalPanel();
		templatebar.add(new jswLabel("Letter template"));
		templatebar.add(templatelist);
		Object[] message = new Object[] { "Use this address\n" + address, templatebar };
		int n = JOptionPane.showOptionDialog(parent,
				message, "Address",
				JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
				options, options[0]);
		if (n == 0)
			printletter(address, templatelist.getSelectedValue());
		else if (n == 1)
			printlabel(address);
		else if (n == 2)
			textTransfer.setClipboardContents(address);
	}

	private void printlabel(String address)
	{
		String labelfolder = mcSettings.labelsFolder();
		JFileChooser fc = new JFileChooser(labelfolder);
		fc.setDialogTitle("Specify a file to save label");
		String labelname = mcLetter.makeFileName(contact);
		fc.setSelectedFile(new File(labelfolder + "/" + labelname + ".pdf"));
		FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF", "pdf");
		fc.setFileFilter(filter);
		if (fc.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

		File afile = fc.getSelectedFile();
		afile.getAbsoluteFile().getParentFile().mkdirs();
		jswVerticalPanel panel = new jswVerticalPanel("options", false, false);
		jswTextArea addressarea = new jswTextArea("address", true);
		addressarea.setText(address);
		jswDropDownBox pagelayout = new jswDropDownBox(this, "Layout");
		if (MyContacts.labeltemplates == null) MyContacts.labeltemplates = mcPDF.readTemplates();
		for (Entry<String, Map<String, String>> entry : MyContacts.labeltemplates.entrySet())
		{
			pagelayout.addItem(entry.getKey());
		}
		jswThumbwheel startpos = new jswThumbwheel(this, "Starting Position", 1, 10);
		startpos.setValue(1);
		pagelayout.setPreferredSize(new Dimension(100, 50));
		panel.add(new jswLabel("Address"));
		panel.add(addressarea);
		panel.add(startpos);
		panel.add(pagelayout);
		JOptionPane optionPane = new JOptionPane();
		optionPane.setMessage("");
		optionPane.setMessageType(JOptionPane.PLAIN_MESSAGE);
		optionPane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
		optionPane.add(panel);
		// owned by the parent, so it shows above the always-on-top Contacts viewer
		JDialog dialog = optionPane.createDialog(parent, "Print Label");
		dialog.setVisible(true);
		Object pane = optionPane.getValue();
		if (pane instanceof Integer i && i == JOptionPane.OK_OPTION)
		{
			mcPDF labelpages = new mcPDF(afile, "Lerot Contacts Labels");
			labelpages.setLayout(pagelayout.getSelectedValue());
			labelpages.makeLabelPage(addressarea.getText(), startpos.getValue());
		}
	}

	private void printletter(String address, String template)
	{
		if (template == null || template.isBlank())
		{
			JOptionPane.showMessageDialog(parent, "There are no letter templates to choose from.",
					"Letter", JOptionPane.ERROR_MESSAGE);
			return;
		}
		// the contact's folder is made for the dialog, and removed again if nothing is saved
		File contactfolder = new File(mcSettings.contactFolder(contact));
		boolean madefolder = !contactfolder.exists() && contactfolder.mkdirs();
		JFileChooser fc = new JFileChooser(contactfolder);
		fc.setDialogTitle("Specify a file to save Letter");
		String lettername = mcLetter.makeFileName(contact);
		fc.setSelectedFile(new File(contactfolder, lettername + ".odt"));
		FileNameExtensionFilter filter = new FileNameExtensionFilter("ODT", "odt");
		fc.setFileFilter(filter);
		boolean written = false;

		if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION)
		{
			File fileToSave = fc.getSelectedFile();
			String filepath = fileToSave.getPath();
			mcLetter letter = new mcLetter();
			letter.setOutputFileName(filepath);
			letter.setTemplateFileName(mcSettings.folder() + template);
			letter.setVariable("address", address);
			letter.setVariable("salutation", mcLetter.getSalutation(contact));
			letter.setVariable("printdate", mcDateDataType.getNow("dd MMM yyyy"));
			written = letter.printLetter();
			if (written) offerToOpen(fileToSave);
			else JOptionPane.showMessageDialog(parent,
					"The letter could not be written to\n" + filepath,
					"Letter", JOptionPane.ERROR_MESSAGE);
		}
		if (!written && madefolder) removeEmptyFolders(contactfolder);
	}

	// says where the letter went, and opens it in the default word processor
	private void offerToOpen(File letter)
	{
		int n = JOptionPane.showConfirmDialog(parent,
				"Letter saved as\n" + letter.getPath() + "\n\nOpen it now?",
				"Letter", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
		if (n != JOptionPane.YES_OPTION) return;
		try
		{
			Desktop.getDesktop().open(letter);
		} catch (Exception e)
		{
			JOptionPane.showMessageDialog(parent,
					"Could not open the letter: " + e.getMessage(),
					"Letter", JOptionPane.ERROR_MESSAGE);
		}
	}

	// removes a folder made for a save dialog, and its parents up to the correspondence
	// folder, while they are empty
	static void removeEmptyFolders(File folder)
	{
		File top = new File(mcSettings.correspondenceFolder()).getAbsoluteFile();
		File f = folder.getAbsoluteFile();
		while (f != null && !f.equals(top) && f.delete())
			f = f.getParentFile();
	}
}
