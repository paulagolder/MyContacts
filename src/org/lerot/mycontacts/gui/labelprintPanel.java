package org.lerot.mycontacts.gui;

import org.lerot.mycontacts.gui.widgets.ContactSource;
import org.lerot.mycontacts.mcContacts;
import org.lerot.mycontacts.mcLetter;
import org.lerot.mycontacts.mcPDF;
import org.lerot.mycontacts.MyContacts;
import org.lerot.mywidgets.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.lerot.mycontacts.MyContacts.topgui;

public class labelprintPanel extends jswVerticalPanel implements ActionListener, ComponentListener
{
    private static final long serialVersionUID = 1L;
    private final ContactSource source;
    private final jswButton selbutton;
    private final jswTextBox selectedfile;
    private final jswLabel prog;
    private final jswDropDownBox layoutpanel;
    private final jswThumbwheel startpos;
    //private final jswLabel selectedcontacts;
    private final jswCheckbox showcountry;
    jswTextBox searchfield;
    private ActionListener parentlistener;
    private File exportfile;
    private mcPDF labelpages;
    private mcContacts sellist;

    public labelprintPanel()
    {

        super("labelprint", false, false);
        this.addComponentListener(this);
        int ncontacts = MyContacts.selbox.countAll();
        int nbrowsecontacts = MyContacts.selbox.getBrowsecontactlist().size();
        //String searchterm = MyContacts.selbox.getSearchterm();
        this.removeAll();
        jswHorizontalPanel header = new jswHorizontalPanel();
        jswLabel heading = new jswLabel(" Print address Labels ");
        header.add(" FILLW ", heading);
        this.add(header);
        source = new ContactSource();
        add(" FILLW ", source);
        jswHorizontalPanel filebar = new jswHorizontalPanel();
        selbutton = new jswButton(this, "Select");
        filebar.add(" LEFT ", selbutton);
        selectedfile = new jswTextBox(this, " ");
        selectedfile.setText(MyContacts.letterfolder + "/" + mcLetter.makeFileName("Labels") + ".pdf");
        selectedfile.setEnabled(true);
        this.add(" ", filebar);
        filebar.add(" LEFT  ", selectedfile);
        selectedfile.setStyleAttribute("mywidth", 300);
        selectedfile.applyStyle();
        jswHorizontalPanel optionbar = new jswHorizontalPanel();
        layoutpanel = new jswDropDownBox(this, "Select layout");
        for (Entry<String, Map<String, String>> entry : MyContacts.labeltemplates.entrySet())
        {
            layoutpanel.addItem(entry.getKey()); // paul fixing
        }
        optionbar.add(layoutpanel);
        startpos = new jswThumbwheel(this, "Start Position", 1, 10);
        //	startpos.setValue(1);
        optionbar.add(startpos);
        this.add(optionbar);
        jswHorizontalPanel countrybar = new jswHorizontalPanel();
        showcountry = new jswCheckbox(this, "Show UK?");
        showcountry.setEnabled(true);
        showcountry.setSelected(false);
        countrybar.add(showcountry);
        this.add(countrybar);
        jswHorizontalPanel printbar = new jswHorizontalPanel();
        this.add(printbar);
        jswButton testbutton = new jswButton(this, "PRINT");
        printbar.add(" MIDDLE ", testbutton);
        jswHorizontalPanel progressbar = new jswHorizontalPanel();
        this.add(progressbar);
        prog = new jswLabel(" Selecting source and output file ");
        progressbar.add(" FILLW ", prog);

        topgui.mainpanel.repaint();
        topgui.getContentPane().validate();
    }

    @Override
    public void actionPerformed(ActionEvent evt)
    {
        String cmd = evt.getActionCommand().toUpperCase();
        System.out.println(" here we are lp : " + cmd);
       // HashMap<String, String> cmdmap = jswUtils.parsecsvstring(cmd);
        String action=cmd;
       // if( cmdmap.get("COMMAND") == null) action = cmd;
        //String action = cmdmap.get("COMMAND").toUpperCase();
     //   System.out.println("action : " + action);
        if (action.equals("SELECT"))
        {
            JFileChooser fc = new JFileChooser(MyContacts.letterfolder);
            fc.setDialogTitle("Specify a file to save label");
            String labelname = mcLetter.makeFileName("Labels");
            fc.setSelectedFile(
                    new File(MyContacts.letterfolder + "/" + labelname + ".pdf"));
            FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF",
                    "pdf");
            fc.setDialogTitle("Specify a file to save");
            fc.setFileFilter(filter);
            int returnVal = fc.showSaveDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                File fileToSave = fc.getSelectedFile();
                selectedfile.setText(fileToSave.getPath());
                exportfile = fileToSave;
                String filename = selectedfile.getText();
                File afile = new File(filename);
                //labelpages = new mcPDF(afile, "Lerot Contacts Labels");

            } else
            {
                System.out.println("Open command cancelled by user.");
            }
        } else if (action.startsWith("PRINT"))
        {
            prog.setText(" Printing ");
            MyContacts.labeltemplates = mcPDF.readTemplates();
            String filename = selectedfile.getText();
            File afile = new File(filename);
            afile.getAbsoluteFile().getParentFile().mkdirs();
            int sp = startpos.getValue();
            boolean showcountryselected = showcountry.isSelected();
            String pagelayout = layoutpanel.getSelectedValue();
            labelpages = new mcPDF(afile, "Lerot Contacts Labels");
            labelpages.setLayout(pagelayout);
            sellist = source.contacts();
            System.out.println("label print : " + sellist.size());
            int ncount = labelpages.makeLabelsPages(sellist, sp, showcountryselected);
            prog.setText(" Printing complete " + ncount + " pages");
        } else
            System.out.println("label print action " + action + " unrecognised ");
        topgui.refreshView();
        topgui.getContentPane().validate();
    }

    public void refresh()
    {
        source.refresh();
        topgui.getContentPane().validate();
    }

    @Override
    public void componentResized(ComponentEvent componentEvent)
    {

    }

    @Override
    public void componentMoved(ComponentEvent componentEvent)
    {

    }

    @Override
    public void componentShown(ComponentEvent componentEvent)
    {
        refresh();
    }

    @Override
    public void componentHidden(ComponentEvent componentEvent)
    {

    }
}
