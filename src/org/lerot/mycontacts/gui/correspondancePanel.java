package org.lerot.mycontacts.gui;

import org.lerot.mycontacts.gui.widgets.jswDropPane;
import org.lerot.mycontacts.*;
import org.lerot.mywidgets.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Vector;

public class correspondancePanel extends jswVerticalPanel implements ActionListener
{

    private final mcContact selcontact;
    private final String tidyTID;
    private final String foldername;
    private final String notesfolder;
    private final String sentfolder;
    private final String receivedfolder;
    private Vector<mcCorrespondance> letters;
    private int editid = 0;
    private jswDropDownBox statuseditbox;
    private jswTextBox dateeditbox;
    private jswTextBox subjecteditbox;
    private Vector<mcCorrespondance> letterlist;

    public correspondancePanel(mcContact selcontact,
                               String title)
    {
        super("correspondance panel", false, false);
        jswStyles cstyles = StylesMakeCorrespondence();
        this.selcontact = selcontact;
        tidyTID = mcTIDDataType.toTidyStr(selcontact.getTID());
        foldername = MyContacts.docsfolder + File.separator + tidyTID;
        notesfolder = foldername + File.separator + "notes";
        sentfolder = foldername + File.separator + "sent";
        receivedfolder = foldername + File.separator + "received";

        jswTable letterpanel = new jswTable(this, "correspondance table", cstyles);
        jswScrollPane correspondancescroll = new jswScrollPane(letterpanel, 0, 0);
        correspondancescroll.setName("resultscroll");
        correspondancescroll
                .setBorder(jswStyle.makeLineBorder());
        if (selcontact != null)
        {
            letters = getCorrespondance();
            System.out.println("found " + letters.size() + " letters");
            if (!(letters == null))
            {
                int row = 0;
                for (mcCorrespondance anentry : letters)
                {
                    mcCorrespondance aletter = anentry;
                    int letterid = aletter.getCorrespondanceid();

                    String date = aletter.getLastmodifieddate();
                    String status = aletter.getRole();
                    String subject = aletter.getFilename();


                    jswButton deleteref = new jswButton(this, "DELETE",
                            "DELETEREF:" + row);
                    jswButton viewletter = new jswButton(this, "VIEW",
                            "VIEWLETTER:" + row);
                    letterpanel.addCell(date, row, 0);
                    letterpanel.addCell(status, row, 1);
                    letterpanel.addCell(subject, row, 2);
                    letterpanel.addCell("  ", row, 3);
                    letterpanel.addCell(deleteref, row, 5);
                    letterpanel.addCell(viewletter, row, 6);
                    row++;

                }
            }
        }
        this.setVisible(true);
        this.add(" FILLW FILLH  ", correspondancescroll);
        jswHorizontalPanel bottom = new jswHorizontalPanel("fred", false);
        jswDropPane correspondancesent = new jswDropPane("sent");
        bottom.add("  FILLW ", correspondancesent);
        jswButton makenote = new jswButton(this, "note", "MAKENOTE");
        bottom.add(" WIDTH=100 ", makenote);
        jswDropPane correspondancereceived = new jswDropPane("received");
        bottom.add("  FILLW ", correspondancereceived);
        correspondancereceived.setBorder(jswStyle.makeLineBorder());
        makenote.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
        correspondancesent.setBorder(jswStyle.makeLineBorder());
        this.add("FILLW  ", bottom);


    }

    private static void copyFileUsingStream(File source, File dest)
            throws IOException
    {
        InputStream is = null;
        OutputStream os = null;
        try
        {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0)
            {
                os.write(buffer, 0, length);
            }
        } finally
        {
            is.close();
            os.close();
        }
    }

    public Vector<mcCorrespondance> getCorrespondance()
    {
        letterlist = new Vector<mcCorrespondance>();
        File folder = new File(MyContacts.docsfolder + "/" + tidyTID);
        if (!folder.exists())
        {
            System.out.println("folder:" + tidyTID + ": does not exist");
            return letterlist;
        }
        Vector<File> listoffiles = new Vector();
        collectCorrespondancefrom(listoffiles, null);
        collectCorrespondancefrom(listoffiles, "notes");
        collectCorrespondancefrom(listoffiles, "sent");
        collectCorrespondancefrom(listoffiles, "received");
        // File[] listOfFiles = folder.listFiles();
        if (letterlist.size() < 1)
        {
            System.out.println("folder:" + tidyTID + ": is empty");
            return letterlist;
        }

        Comparator myComparator = new mcCorrespondance.SortByDate();
        Collections.sort(letterlist, myComparator);

        return letterlist;
    }

    private void collectCorrespondancefrom(Vector<File> files, String subdir)
    {
        File folder;
        if (subdir == null)
            folder = new File(foldername);
        else
            folder = new File(foldername + File.separator + subdir);
        File[] alistOfFiles = folder.listFiles();
        if (alistOfFiles == null) return;
        int lid = 1;
        for (File afile : alistOfFiles)
        {
            if (afile.isFile())
            {
                //files.add(afile);
                SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String sdate = dt.format(new Date(afile.lastModified()));
                //   System.out.println("found file: " + file.getName() + " " + sdate);
                mcCorrespondance aletter = new mcCorrespondance(lid, selcontact.getCID(), afile.getName(), sdate, subdir);
                letterlist.add(aletter);

            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent evt)
    {
        String action = evt.getActionCommand().toUpperCase();
        System.out.println("action " + action);
        if (action.startsWith("VIEWLETTER:"))
        {
            int lettkey = Integer.parseInt(action.substring(11));
            mcCorrespondance aletter = letters.get(lettkey);
            //aletter.getLetter(lettkey);

            //System.out.println(aletter.toString());
            if (!Desktop.isDesktopSupported())
            {
                System.out.println("no desktop");
            } else
            {
                String path;
                if (aletter.getRole() == null)
                    path = MyContacts.docsfolder + "/"
                            + tidyTID + File.separator + aletter.getFilename();
                else
                    path = MyContacts.docsfolder + "/"
                            + tidyTID + File.separator + aletter.getRole() + File.separator + aletter.getFilename();
                System.out.println(" reading " + path);
                File letter = new File(path);
                boolean exists = letter.exists();
                if (!exists)
                {
                    letter = new File(aletter.getPath());
                    exists = letter.exists();
                }
                if (exists)
                {
                    try
                    {
                        Desktop.getDesktop().open(letter);
                    } catch (IOException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else
                    System.out.println(
                            "file does not exist:" + letter);

            }

        } else if (action.startsWith("DELETEREF:"))
        {
            int lettkey = Integer.parseInt(action.substring(10));
            mcCorrespondance aletter = letterlist.get(lettkey);
            Path fileToDeletePath = Paths.get(makeLetterPath(aletter));
            try
            {
                Files.delete(fileToDeletePath);
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
            MyContacts.topgui.refreshView();
        } else if (action.startsWith("xxEDITREF:"))
        {
            int lettkey = Integer.parseInt(action.substring(8));
            mcCorrespondance aletter = new mcCorrespondance(lettkey);
            MyContacts.topgui.refreshView();

        } else if (action.startsWith("EDITLETTER:"))
        {
            int lettkey = Integer.parseInt(action.substring(11));
            editid = lettkey;
            MyContacts.topgui.refreshView();

        } else if (action.startsWith("CANCELEDIT:"))
        {
            int lettkey = Integer.parseInt(action.substring(11));
            mcCorrespondance aletter = new mcCorrespondance(lettkey);
            editid = 0;
            MyContacts.topgui.refreshView();

        } else if (action.startsWith("SAVEEDIT:"))
        {
            int lettkey = Integer.parseInt(action.substring(9));
            mcCorrespondance aletter = new mcCorrespondance(lettkey);
     /*       aletter.getLetter(lettkey);
            aletter.setRole(statuseditbox.getSelectedValue());
            aletter.setFilename(subjecteditbox.getText());
            aletter.setLastmodifieddate(dateeditbox.getText());
            aletter.saveLetter();*/
            editid = 0;
            MyContacts.topgui.refreshView();
        } else if (action.startsWith("MAKENOTE"))

        {
            makeNote();
        } else
        {
            System.out.println("correspondance panel action " + action + " unrecognised ");
        }
        this.repaint();
        MyContacts.topgui.mainpanel.repaint();
        MyContacts.topgui.getContentPane().validate();
    }

    public jswHorizontalPanel makeBottomPanel()
    {
        jswHorizontalPanel bottom = new jswHorizontalPanel("fred", false);
        bottom.setName("bottom");
        // bottom.setTag("trace");
        jswDropPane correspondancesent = new jswDropPane("sent");
        bottom.add("  FILLW ", correspondancesent);
        jswButton makenote = new jswButton(this, "note", "MAKENOTE");
        bottom.add(" WIDTH=100 ", makenote);
        jswDropPane correspondancereceived = new jswDropPane("received");
        bottom.add("  FILLW ", correspondancereceived);
        correspondancereceived.setBorder(jswStyle.makeLineBorder());
        makenote.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
        correspondancesent.setBorder(jswStyle.makeLineBorder());
        return bottom;
    }


    private void makeNote()
    {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Specify a file to save Note");
        mcNote newnote = new mcNote(selcontact);

        fc.setSelectedFile(new File(newnote.getOutputPath()));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("txt",
                "txt");
        fc.setFileFilter(filter);
        File directory = new File(newnote.getOutputFileDir());
        if (!directory.exists())
        {
            directory.mkdir();
        }
        fc.setCurrentDirectory(directory);
        int returnVal = fc.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            File fileToSave = fc.getSelectedFile();
            String filepath = fileToSave.getPath();
            try
            {
                newnote.printNote(filepath);

            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void makeLetter()
    {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Specify a file to save Note");
        String lettername = mcLetter.makeFileName(selcontact);
        String nfilepath = MyContacts.docsfolder + "/" + mcTIDDataType.toTidyStr(selcontact.getTID()) + "/notes/"
                + lettername + ".txt";
        File newfile = new File(nfilepath);


        System.out.println(nfilepath + "=" + newfile.getPath());
        fc.setSelectedFile(newfile);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("ODT",
                "odt");
        fc.setFileFilter(filter);
        fc.setCurrentDirectory(newfile);
        int returnVal = fc.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            File fileToSave = fc.getSelectedFile();
            String filepath = fileToSave.getPath();
            try
            {
                // String address = selcontact.makeBlockAddress("\n");
                String salutation = selcontact.getName();
                String printdate = mcDateDataType.getNow("dd MMM yyyy");
                String filedate = mcDateDataType.getNow("yyyy-MM-dd");
                mcLetter letter = new mcLetter();
                letter.setOutputFileName(filepath);
                letter.setTemplateFileName(
                        MyContacts.topgui.dotcontacts + "/note_template.odt");

                letter.setVariable("salutation", salutation);
                letter.setVariable("printdate", printdate);
                letter.printLetter();

            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }


    private jswStyles StylesMakeCorrespondence()
    {
        jswStyles tablestyles = jswStyles.clone("CorrespondanceStyles", MyContacts.getTableStyles());

        jswStyle tablestyle = tablestyles.makeStyle("table");

        //tablestyle.putAttribute("backgroundColor", new Color(200,220,200));
        tablestyle.putAttribute("foregroundColor", "Green");
        tablestyle.putAttribute("borderWidth", "1");
        tablestyle.putAttribute("borderColor", jswTheme.BORDER);

        jswStyle cellstyle = tablestyles.makeStyle("xcell");
        cellstyle.putAttribute("foregroundColor", "Blue");
        cellstyle.putAttribute("borderWidth", "1");
        cellstyle.putAttribute("borderColor", jswTheme.CELLBORDER);
        cellstyle.setHorizontalAlign("LEFT");
        cellstyle.putAttribute("fontsize", jswTheme.TEXT);

        jswStyle cellcstyle = tablestyles.makeStyle("xcellcontent");
        cellcstyle.putAttribute("backgroundColor", "transparent");
        cellcstyle.putAttribute("foregroundColor", "Red");
        cellcstyle.setHorizontalAlign("LEFT");
        cellcstyle.putAttribute("fontsize", jswTheme.TEXT);

        jswStyle rowstyle = tablestyles.makeStyle("row");
        //col0style.putAttribute("fontStyle", Font.BOLD);
        //col0style.setHorizontalAlign("RIGHT");
        rowstyle.putAttribute("height", "50");

        jswStyle col0style = tablestyles.makeStyle("col_0");
        col0style.putAttribute("fontStyle", Font.BOLD);
        col0style.setHorizontalAlign("RIGHT");
        col0style.putAttribute("minwidth", "true");

        jswStyle col1style = tablestyles.makeStyle("col_1");
        col1style.putAttribute("fontStyle", Font.BOLD);
        col1style.setHorizontalAlign("LEFT");
        col1style.putAttribute("minwidth", "true");
        col1style.putAttribute("width", "10");
        col1style.putAttribute("horizontalAlignment", "LEFT");

        jswStyle col2style = tablestyles.makeStyle("col_2");
        // col2style.putAttribute("backgroundColor", "green");
        col2style.putAttribute("horizontalAlignment", "LEFT");
        col2style.putAttribute("minwidth", "true");
        col2style.putAttribute("width", "10");

        jswStyle col4style = tablestyles.makeStyle("col_4");
        // col4style.putAttribute("backgroundColor", "green");
        col4style.putAttribute("horizontalAlignment", "LEFT");
        col4style.putAttribute("minwidth", "true");

        jswStyle col5style = tablestyles.makeStyle("col_5");
        // col5style.putAttribute("backgroundColor", "green");
        col5style.putAttribute("horizontalAlignment", "LEFT");
        col5style.putAttribute("minwidth", "true");

        jswStyle col6style = tablestyles.makeStyle("col_6");
        // col6style.putAttribute("backgroundColor", "green");
        col6style.putAttribute("horizontalAlignment", "LEFT");
        col6style.putAttribute("minwidth", "true");

        return tablestyles;
    }

    public String makeLetterPath(mcCorrespondance aletter)
    {
        if (aletter.getRole() == null)
            return foldername + File.separator + aletter.getFilename();
        else
            return foldername + File.separator + aletter.getRole() + File.separator + aletter.getFilename();
    }

}
