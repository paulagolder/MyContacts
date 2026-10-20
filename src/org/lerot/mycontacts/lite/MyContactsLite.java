package org.lerot.mycontacts.lite;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import net.miginfocom.swing.MigLayout;
import org.lerot.mycontacts.MyContacts;
import org.lerot.mycontacts.mcSettings;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.net.URI;
import java.util.List;

/*
 * myContactsLite: a small, phone-shaped window titled "Contacts" for looking
 * up a contact while doing something else. Search, pick from the list, and
 * the contact's name, address, phone, mobile, email and birthday appear as
 * panels of selectable text. The email opens the mail program; "Edit in
 * MyContacts" opens the main app on that contact. Reads the database
 * read-only, and stays on top unless unpinned.
 *
 *   ./gradlew runLite
 *   java -cp MyContacts.jar org.lerot.mycontacts.lite.MyContactsLite
 */
public class MyContactsLite extends JFrame
{
	// a soft warm off-white page, with a slightly darker shade for the
	// search box, the list panel and the contact's tiles
	static final Color PAGE = Color.decode("#F4F1EA");
	static final Color SHADE = Color.decode("#E9E4DA");
	static final String SHADEHEX = "#E9E4DA";
	static final Color MUTED = Color.decode("#8A8A8E");
	static final String ACCENT = "#2F6FDB";
	static final Color HIGHLIGHT = Color.decode("#DCE6F8");

	final ContactStore store;
	private final CardLayout pages = new CardLayout();
	private final JPanel content = new JPanel(pages);
	final JTextField search = new JTextField();
	private final DefaultListModel<ContactCard> model = new DefaultListModel<>();
	private final JList<ContactCard> list = new JList<>(model);
	private final JLabel count = new JLabel();
	private final JPanel tiles = new TilePanel();
	private ContactCard showing;

	public static void main(String[] args)
	{
		setupLookAndFeel();
		ContactStore store = new ContactStore(mcSettings.databasePath());
		SwingUtilities.invokeLater(() -> new MyContactsLite(store).setVisible(true));
	}

	static void setupLookAndFeel()
	{
		MyContacts.settingsdir = mcSettings.folder();
		FlatLaf.setGlobalExtraDefaults(java.util.Map.of("@accentColor", ACCENT));
		FlatMacLightLaf.setup();
		UIManager.put("List.selectionArc", 10);
		// the title bar in the page colour, so the window is all one colour scheme
		UIManager.put("TitlePane.background", PAGE);
		UIManager.put("TitlePane.inactiveBackground", PAGE);
		UIManager.put("ScrollBar.showButtons", false);
		UIManager.put("ScrollBar.width", 8);
		UIManager.put("ScrollBar.thumbArc", 999);
		UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
		// FlatLaf draws the title bar, so the pin can sit in it
		JFrame.setDefaultLookAndFeelDecorated(true);
	}

	public MyContactsLite(ContactStore store)
	{
		super("Contacts");
		this.store = store;
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setIcon();
		setJMenuBar(titleBar());
		content.add(listPage(), "list");
		content.add(cardPage(), "card");
		setContentPane(content);
		setAlwaysOnTop(true);
		setSize(380, 720);
		placeTopRight();

		load();
		// pick up changes made in the main app when coming back to this window
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowActivated(WindowEvent e)
			{
				if (store.refresh()) reshow();
			}
		});
		bindKey(content, "ESCAPE", this::escape);
	}

	// ---- the list page: heading, search, contacts ----

	private JPanel listPage()
	{
		// the bottom inset keeps the list panel clear of the window's edge
		JPanel page = new JPanel(new MigLayout("insets 4 14 12 14, wrap, fill", "[grow, fill]", "[][]6[]4[grow, fill]"));
		page.setBackground(PAGE);

		JLabel heading = new JLabel("Contacts");
		heading.putClientProperty("FlatLaf.styleClass", "h1");
		page.add(heading);

		search.putClientProperty("JTextField.placeholderText", "Search");
		search.putClientProperty("JTextField.leadingIcon", icon("search.svg", 14));
		search.putClientProperty("JTextField.showClearButton", true);
		search.putClientProperty("JComponent.roundRect", true);
		search.putClientProperty("FlatLaf.style", "background: " + SHADEHEX);
		search.getDocument().addDocumentListener(new DocumentListener()
		{
			public void insertUpdate(DocumentEvent e) { filter(); }
			public void removeUpdate(DocumentEvent e) { filter(); }
			public void changedUpdate(DocumentEvent e) { filter(); }
		});
		bindKey(search, "DOWN", () -> move(1));
		bindKey(search, "UP", () -> move(-1));
		bindKey(search, "ENTER", this::openSelected);
		page.add(search, "h 30!");

		count.putClientProperty("FlatLaf.styleClass", "small");
		count.setForeground(MUTED);
		page.add(count, "gapleft 4");

		list.setCellRenderer(new ContactRenderer());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setOpaque(false);
		// the renderer draws the selection itself
		list.putClientProperty("FlatLaf.style",
				"selectionBackground: $List.background; selectionInactiveBackground: $List.background");
		list.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				int i = list.locationToIndex(e.getPoint());
				if (i >= 0 && list.getCellBounds(i, i).contains(e.getPoint())) open(model.get(i));
			}
		});
		bindKey(list, "ENTER", this::openSelected);
		JScrollPane scroll = new JScrollPane(list);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setOpaque(false);
		scroll.getViewport().setOpaque(false);
		// the list in a rounded panel, outlined like the search box
		JPanel panel = new RoundedPanel(SHADE, UIManager.getColor("Component.borderColor"), 14);
		panel.setLayout(new MigLayout("insets 6 4 6 4, fill"));
		panel.add(scroll, "grow");
		page.add(panel);
		return page;
	}

	private void load()
	{
		try
		{
			store.refresh();
			filter();
		} catch (RuntimeException e)
		{
			count.setText("Cannot read the contacts: " + e.getMessage());
		}
	}

	private void filter()
	{
		List<ContactCard> found = store.search(search.getText());
		model.clear();
		model.addAll(found);
		if (!found.isEmpty()) list.setSelectedIndex(0);
		int all = store.all().size();
		count.setText(search.getText().isBlank() ? all + " contacts"
				: found.isEmpty() ? "No matches" : found.size() + " of " + all);
	}

	private void move(int step)
	{
		if (model.isEmpty()) return;
		int i = Math.max(0, Math.min(model.size() - 1, list.getSelectedIndex() + step));
		list.setSelectedIndex(i);
		list.ensureIndexIsVisible(i);
	}

	private void openSelected()
	{
		ContactCard selected = list.getSelectedValue();
		if (selected != null) open(selected);
	}

	// ---- the card page: one contact ----

	private JPanel cardPage()
	{
		JPanel page = new JPanel(new BorderLayout());
		page.setBackground(PAGE);

		JButton back = new JButton("Contacts", icon("back.svg", 14));
		back.putClientProperty("JButton.buttonType", "toolBarButton");
		back.setForeground(Color.decode(ACCENT));
		back.addActionListener(e -> showList());
		JPanel top = new JPanel(new MigLayout("insets 4 6 0 6", "[]push"));
		top.setOpaque(false);
		top.add(back);
		page.add(top, BorderLayout.NORTH);

		JScrollPane scroll = new JScrollPane(tiles);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.getViewport().setBackground(PAGE);
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		page.add(scroll, BorderLayout.CENTER);
		return page;
	}

	void open(ContactCard card)
	{
		showing = card;
		tiles.removeAll();
		tiles.add(textTile(card.name(), "h3"));
		addTile(card.address());
		addTile(card.phone());
		addTile(card.mobile());
		if (card.email() != null) tiles.add(emailTile(card.email()));
		addTile(card.birthday());

		JButton edit = new JButton("Edit in MyContacts");
		edit.putClientProperty("JButton.buttonType", "borderless");
		edit.putClientProperty("FlatLaf.styleClass", "small");
		edit.setForeground(Color.decode(ACCENT));
		edit.addActionListener(e -> editInMainApp(card.id()));
		tiles.add(edit, "right, gaptop 6");

		tiles.revalidate();
		tiles.repaint();
		pages.show(content, "card");
		SwingUtilities.invokeLater(() -> tiles.scrollRectToVisible(new Rectangle(0, 0, 1, 1)));
	}

	private void addTile(String text)
	{
		if (text != null) tiles.add(textTile(text, null));
	}

	private void showList()
	{
		showing = null;
		pages.show(content, "list");
		search.requestFocusInWindow();
	}

	// after the database changed: refresh the list and the open card
	private void reshow()
	{
		filter();
		if (showing != null)
			store.find(showing.id()).ifPresentOrElse(this::open, this::showList);
	}

	private void escape()
	{
		if (showing != null) showList();
		else search.setText("");
	}

	// ---- tiles: rounded panels of selectable text ----

	private static Tile textTile(String text, String styleClass)
	{
		JTextArea area = new JTextArea(text);
		area.setEditable(false);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setOpaque(false);
		area.setBorder(BorderFactory.createEmptyBorder());
		if (styleClass != null) area.putClientProperty("FlatLaf.styleClass", styleClass);
		return new Tile(area);
	}

	// the address in link colour: a click opens the mail program, a drag
	// selects text as usual
	private static Tile emailTile(String email)
	{
		Tile tile = textTile(email, null);
		JTextArea area = (JTextArea) tile.getComponent(0);
		area.setForeground(Color.decode(ACCENT));
		area.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		area.setToolTipText("Write an email");
		area.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (area.getSelectedText() == null) openMail(email);
			}
		});
		return tile;
	}

	static class Tile extends RoundedPanel
	{
		Tile(JComponent text)
		{
			super(SHADE, null, 16);
			setLayout(new MigLayout("insets 10 14 10 14, fill", "[grow, fill]"));
			add(text, "wmin 0");
		}
	}

	// a panel with a rounded background, and optionally a 1px outline
	static class RoundedPanel extends JPanel
	{
		private final Color fill, outline;
		private final int arc;

		RoundedPanel(Color fill, Color outline, int arc)
		{
			this.fill = fill;
			this.outline = outline;
			this.arc = arc;
			setOpaque(false);
		}

		@Override
		protected void paintComponent(Graphics g)
		{
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(fill);
			g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));
			if (outline != null)
			{
				g2.setColor(outline);
				g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1, getHeight() - 1, arc, arc));
			}
			g2.dispose();
		}
	}

	// the column of tiles: as wide as the window, scrolling vertically
	static class TilePanel extends JPanel implements Scrollable
	{
		TilePanel()
		{
			super(new MigLayout("wrap, insets 6 14 14 14, gapy 8", "[grow, fill]"));
			setBackground(PAGE);
		}

		public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
		public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
		public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return r.height; }
		public boolean getScrollableTracksViewportWidth() { return true; }
		public boolean getScrollableTracksViewportHeight() { return false; }
	}

	// ---- a list row: the contact's name on one line ----

	static class ContactRenderer extends JPanel implements ListCellRenderer<ContactCard>
	{
		private final JLabel name = new JLabel();
		private boolean selected;

		ContactRenderer()
		{
			super(new MigLayout("insets 3 10 3 10", "[grow]"));
			setOpaque(false);
			add(name);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends ContactCard> list, ContactCard card,
				int index, boolean selected, boolean focused)
		{
			name.setText(card.listName());
			name.setForeground(list.getForeground());
			this.selected = selected;
			return this;
		}

		// the selected row (the one Enter opens) gets a soft rounded highlight,
		// visible even while typing in the search box
		@Override
		protected void paintComponent(Graphics g)
		{
			if (!selected) return;
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(HIGHLIGHT);
			g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
			g2.dispose();
		}
	}

	// ---- title bar, window and actions ----

	private JMenuBar titleBar()
	{
		JMenuBar bar = new JMenuBar();
		bar.add(Box.createHorizontalGlue());
		JToggleButton pin = new JToggleButton(icon("pin.svg", 14), true);
		pin.setToolTipText("Keep on top of other windows");
		pin.putClientProperty("JButton.buttonType", "toolBarButton");
		pin.setFocusable(false);
		pin.addActionListener(e -> setAlwaysOnTop(pin.isSelected()));
		bar.add(pin);
		return bar;
	}

	private void placeTopRight()
	{
		Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		setLocation(screen.x + screen.width - getWidth() - 24, screen.y + 48);
	}

	private void setIcon()
	{
		File icon = new File(mcSettings.folder(), "mccontacts.png");
		if (icon.isFile()) setIconImage(new ImageIcon(icon.getPath()).getImage());
	}

	static Icon icon(String name, int size)
	{
		return new FlatSVGIcon("org/lerot/mycontacts/lite/icons/" + name, size, size);
	}

	private static void bindKey(JComponent c, String key, Runnable action)
	{
		c.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(key), key);
		c.getActionMap().put(key, new AbstractAction()
		{
			public void actionPerformed(ActionEvent e) { action.run(); }
		});
	}

	static void openMail(String email)
	{
		new Thread(() ->
		{
			try
			{
				URI uri = new URI("mailto", email, null);
				if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.MAIL))
					Desktop.getDesktop().mail(uri);
				else
					new ProcessBuilder("xdg-open", uri.toString()).start();
			} catch (Exception e)
			{
				System.err.println("Cannot open mail for " + email + ": " + e.getMessage());
			}
		}).start();
	}

	// start the main app on this contact's Edit page
	static void editInMainApp(int id)
	{
		try
		{
			String java = ProcessHandle.current().info().command()
					.orElse(System.getProperty("java.home") + "/bin/java");
			new ProcessBuilder(java, "-cp", System.getProperty("java.class.path"),
					"org.lerot.mycontacts.MyContacts", "--edit", String.valueOf(id))
					.redirectErrorStream(true)
					.redirectOutput(ProcessBuilder.Redirect.DISCARD)
					.start();
		} catch (Exception e)
		{
			System.err.println("Cannot start MyContacts: " + e.getMessage());
		}
	}
}
