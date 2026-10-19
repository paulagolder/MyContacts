package org.lerot.mycontacts.gui.widgets;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import org.lerot.mycontacts.mcContact;
import org.lerot.mycontacts.mcTIDDataType;
import org.lerot.mycontacts.mcUtilities;
import org.lerot.mycontacts.MyContacts;
import org.lerot.mywidgets.jswHorizontalPanel;
import org.lerot.mywidgets.jswStyle;
import org.lerot.mywidgets.jswStyles;

import javax.imageio.ImageIO;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class jswDropPane extends jswHorizontalPanel
{

    private static final long serialVersionUID = 1L;
    protected jswStyles containerstyles = new jswStyles();
    private DropTarget dropTarget;
    private DropTargetHandler dropTargetHandler;
    private Point dragPoint;
    private boolean dragOver = false;
    private BufferedImage target;
    private JLabel message;
    private String direction;
    private jswStyle style = new jswStyle();


    public jswDropPane(String adirection)
    {
        //super(adirection);
        try
        {
            target = ImageIO
                    .read(new File("/home/paul/.mccontacts/mccontacts.png"));
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        String stylename = this.getClass().getSimpleName();
        containerstyles.copyStyles(MyContacts.panelstyles);
        style.copyAll(containerstyles.getStyle("jswContainer"));
        style.copyAll(containerstyles.getStyle(stylename));
        style.setStyleName(stylename);
        //	doStyling(style);
        direction = adirection;
        setLayout(new GridBagLayout());
        message = new JLabel(direction);
        message.setFont(message.getFont().deriveFont(Font.BOLD, 24));
        add(message);

    }

    @Override
    public Dimension getMinimumSize()
    {
        return new Dimension(600, 100);
    }

    protected DropTarget getMyDropTarget()
    {
        if (dropTarget == null)
        {
            dropTarget = new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE,
                    null);
        }
        return dropTarget;
    }

    protected DropTargetHandler getDropTargetHandler()
    {
        if (dropTargetHandler == null)
        {
            dropTargetHandler = new DropTargetHandler();
        }
        return dropTargetHandler;
    }

    @Override
    public void addNotify()
    {
        super.addNotify();
        try
        {
            getMyDropTarget().addDropTargetListener(getDropTargetHandler());
        } catch (TooManyListenersException ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public void removeNotify()
    {
        super.removeNotify();
        getMyDropTarget().removeDropTargetListener(getDropTargetHandler());
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        if (dragOver)
        {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(new Color(0x5B, 0x7F, 0xA6, 64));
            g2d.fill(new Rectangle(getWidth(), getHeight()));
            if (dragPoint != null && target != null)
            {
                int x = dragPoint.x - 12;
                int y = dragPoint.y - 12;
                g2d.drawImage(target, x, y, this);
            }
            g2d.dispose();
        }
    }

    protected void importFiles(final ArrayList<Path> files)
    {
        Runnable run = new Runnable()
        {
            @Override
            public void run()
            {
                mcContact selcontact = MyContacts.selbox.getSelcontact();
                message.setText("You dropped " + files.size() + " files");
                // List objects =
                // (List)transfer.getTransferData(DataFlavor.javaFileListFlavor);
                for (int f = 0; f < files.size(); f++)
                {
                    //  System.out.println("You dropped " + files.get(f).getClass().getName() );
                    Object bfile = files.get(f);
                    System.out.println("You dropped " + bfile.toString());
                    String fstring = bfile.toString();
                    Path fobject = (new File(fstring)).toPath();
                    //File source = (File) object;
                    String ext = mcUtilities.getFileExtension(fobject);
                    if ("vcf".contentEquals(ext))
                    {

                        try
                        {
                            VCard vcard = Ezvcard.parse(fobject).first();
                            selcontact.importVcard(vcard);
                        } catch (IOException e)
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } else
                    {
                        File directory = new File(MyContacts.docsfolder + File.separator
                                + mcTIDDataType.toTidyStr(selcontact.getTID())+File.separator+direction);
                        if (!directory.exists())
                        {
                            directory.mkdir();
                        }
                        if (fobject instanceof java.nio.file.Path)
                        {
                            SimpleDateFormat sdf = new SimpleDateFormat(
                                    "yyyy-MM-dd");
                            BasicFileAttributes attr = null;
                            try
                            {
                                attr = Files.readAttributes(fobject, BasicFileAttributes.class);
                            } catch (IOException e)
                            {
                                throw new RuntimeException(e);
                            }
                            System.out.println("lastModifiedTime: " + attr.lastModifiedTime());
                            String lmt = String.valueOf(attr.lastModifiedTime());
                            // lmt = lmt.replace("T", " ");
                            lmt = lmt.substring(0,19);
                            //String informat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
                            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
                            Date lmtd = null;
                            try
                            {
                                lmtd = format.parse(lmt);
                            } catch (ParseException e)
                            {
                                throw new RuntimeException(e);
                            }
                            System.out.println("lastModifiedTime..: " + lmtd);

                            File dest = new File(MyContacts.docsfolder + File.separator
                                    +  mcTIDDataType.toTidyStr(selcontact.getTID()) +File.separator+direction+ File.separator
                                    + fobject.getFileName());
                            //     String status = direction;
                            try
                            {
                               File afile = new File(fobject.toString());
                                if (dest.setLastModified(lmtd.getTime()))
                                {
                                    System.out.println("Last modified time is set"+lmtd);
                                }

                             //   LocalDate newLocalDate = LocalDate.of(lmtd);
                                LocalDate ldate = lmtd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                                Instant instant = ldate.atStartOfDay(ZoneId.systemDefault()).toInstant();

                                // convert instant to filetime
                                // update last modified time
                               // Files.setLastModifiedTime(dest, FileTime.from(instant));

                                Files.copy(Paths.get(afile.getAbsolutePath()),
                                        Paths.get(dest.getAbsolutePath()),
                                        StandardCopyOption.REPLACE_EXISTING);
                                File destfile = new File(dest.getAbsolutePath());
                                Files.setLastModifiedTime(destfile.toPath(), FileTime.from(instant));
                                if ("eml".contentEquals(ext))
                                {
                                    Properties props = System.getProperties();
                                    props.put("mail.host", "smtp.dummydomain.com");
                                    props.put("mail.transport.protocol", "smtp");
                                    Session mailSession = Session
                                            .getDefaultInstance(props, null);
                                    InputStream isource = new FileInputStream(afile
                                            .getAbsolutePath());
                                    MimeMessage message = new MimeMessage(
                                            mailSession, isource);
                                    String sdate = sdf.format(message.getSentDate());
                                    Address[] froms = message.getFrom();
                                    String from = ((InternetAddress) froms[0]).getAddress();
                                    Address[] tos = message.getAllRecipients();
                                    System.out.println(from.toString());
                                    System.out.println(((InternetAddress) tos[0]).getAddress());
                                } else if ("doc".contentEquals(ext) || "odt".contentEquals(ext))
                                {
                                    String status = "draft";
                                } else if ("pdf".contentEquals(ext))
                                {
                                    //status= "sent";
                                }
                                //selcontact.addCorrespondance(afile.getName(), date,status,
                                //		dest);
                            } catch (IOException e)
                            {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (MessagingException e)
                            {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                }
                MyContacts.topgui.refreshView();
            }
        };
        SwingUtilities.invokeLater(run);
    }

    public void doStyling()
    {
        // TODO Auto-generated method stub
    }

    public void doStyling(jswStyle style)
    {
        // TODO Auto-generated method stub
    }

    public jswStyle getStyle()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Dimension jswGetMinimumSize()
    {
        // TODO Auto-generated method stub
        return null;
    }

    protected class DropTargetHandler implements DropTargetListener
    {

        protected void processDrag(DropTargetDragEvent dtde)
        {
            if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
            {
                dtde.acceptDrag(DnDConstants.ACTION_COPY);
            } else
            {
                dtde.rejectDrag();
            }
        }

        @Override
        public void dragEnter(DropTargetDragEvent dtde)
        {
            processDrag(dtde);
            SwingUtilities
                    .invokeLater(new DragUpdate(true, dtde.getLocation()));
            repaint();
        }

        @Override
        public void dragOver(DropTargetDragEvent dtde)
        {
            processDrag(dtde);
            SwingUtilities
                    .invokeLater(new DragUpdate(true, dtde.getLocation()));
            repaint();
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent dtde)
        {
        }

        @Override
        public void dragExit(DropTargetEvent dte)
        {
            SwingUtilities.invokeLater(new DragUpdate(false, null));
            repaint();
        }

        @Override
        public void drop(DropTargetDropEvent dtde)
        {
            SwingUtilities.invokeLater(new DragUpdate(false, null));
            Transferable transferable = dtde.getTransferable();
            if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
            {
                dtde.acceptDrop(dtde.getDropAction());
                try
                {
                    ArrayList transferdata1 = (ArrayList) transferable
                            .getTransferData(DataFlavor.javaFileListFlavor);
                    if (transferdata1 != null && transferdata1.size() > 0)
                    {
                        importFiles(transferdata1);
                        dtde.dropComplete(true);
                    }
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            } else
            {
                dtde.rejectDrop();
            }
        }
    }

    public class DragUpdate implements Runnable
    {

        private boolean dragOver;
        private Point dragPoint;
        public DragUpdate(boolean dragOver, Point dragPoint)
        {
            this.dragOver = dragOver;
            this.dragPoint = dragPoint;
        }

        @Override
        public void run()
        {
            jswDropPane.this.dragOver = dragOver;
            jswDropPane.this.dragPoint = dragPoint;
            jswDropPane.this.repaint();
        }
    }
}
