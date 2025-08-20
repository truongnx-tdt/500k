import repositories.ProductRepository;
import repositories.TypeProductRepository;
import repositories.TypeProductRepository.TypeProductItem;
import models.Product;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;

import java.math.BigDecimal;
import java.util.List;

public class AddProductForm extends JDialog {
	private final int employeeId;
	private final ProductRepository productRepo = new ProductRepository();
	private final TypeProductRepository typeRepo = new TypeProductRepository();
	private final Product editingProduct; // null => add, not null => edit

	private JTextField tbName;
	private JTextField tbPrice;
	private JTextArea  tbDesc;
	private JComboBox<TypeProductItem> cbType;
	private JLabel picPreview;
	private JButton btnPrimary;

	private byte[] imageBytes; // dữ liệu ảnh để lưu DB

	public AddProductForm(Window owner, int employeeId) {
		this(owner, employeeId, null);
	}

	public AddProductForm(Window owner, int employeeId, Product productToEdit) {
		super(owner, productToEdit == null ? "New Product" : "Edit Product", ModalityType.APPLICATION_MODAL);
		this.employeeId = employeeId;
		this.editingProduct = productToEdit;
		buildUI();
		loadTypes();
		prefillIfEditing();
		pack();
		setMinimumSize(new Dimension(560, getHeight()));
		setLocationRelativeTo(owner);
	}

	private void buildUI() {
		JPanel root = new JPanel(new GridBagLayout());
		root.setBorder(new EmptyBorder(16,16,16,16));
		root.setBackground(new Color(245,245,245));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10,10,10,10);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;

		// Name
		tbName = new PlaceholderTextField("Product name");
		styleInput(tbName);
		gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=2; gbc.fill = GridBagConstraints.HORIZONTAL;
		root.add(wrapRoundedWithIcon(tbName, "/icons/food-tray.png"), gbc);

		// Giá + Loại (2 cột)
		tbPrice = new PlaceholderTextField("Price (VNĐ)");
		styleInput(tbPrice);
		gbc.gridx=0; gbc.gridy=1; gbc.gridwidth=1; gbc.weightx = 0.5;
		root.add(wrapRoundedWithIcon(tbPrice, "/icons/pay.png"), gbc);

		cbType = new JComboBox<>();
		styleComboBox(cbType);
		gbc.gridx=1; gbc.gridy=1; gbc.gridwidth=1; gbc.weightx = 1.5;
		root.add(wrapRoundedCombo(cbType), gbc);

		// Mô tả
		tbDesc = new PlaceholderTextArea("Description");
		tbDesc.setLineWrap(true); tbDesc.setWrapStyleWord(true);
		tbDesc.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));
		JScrollPane spDesc = new JScrollPane(tbDesc);
		spDesc.setBorder(BorderFactory.createEmptyBorder());
		JPanel descWrap = wrapRounded(spDesc);
		descWrap.setPreferredSize(new Dimension(0, 120));
		gbc.gridx=0; gbc.gridy=2; gbc.gridwidth=2; gbc.fill = GridBagConstraints.BOTH; gbc.weighty=1;
		root.add(descWrap, gbc);
		gbc.weighty=0; gbc.fill = GridBagConstraints.HORIZONTAL;

		// Ảnh + nút upload
		JPanel imageRow = new JPanel(new GridBagLayout());
		imageRow.setOpaque(false);
		GridBagConstraints gi = new GridBagConstraints();
		gi.insets = new Insets(0,0,0,12);
		gi.fill = GridBagConstraints.NONE;
		gi.anchor = GridBagConstraints.WEST;

		JButton btnUpload = createPillButton("Select image…", new Color(105,142,247));
		btnUpload.addActionListener(e -> onUploadImage());
		imageRow.add(btnUpload, gi);

		picPreview = new JLabel(); // hiển thị thumbnail
		picPreview.setPreferredSize(new Dimension(200, 140));
		picPreview.setHorizontalAlignment(SwingConstants.CENTER);
		picPreview.setVerticalAlignment(SwingConstants.CENTER);
		picPreview.setBorder(new DashedRoundedBorder(new Color(200,200,200), 12));
		picPreview.setOpaque(true);
		picPreview.setBackground(Color.WHITE);
		gi.gridx = 1; gi.weightx = 1.0;
		imageRow.add(picPreview, gi);

		gbc.gridx=0; gbc.gridy=3; gbc.gridwidth=2;
		root.add(imageRow, gbc);

		// Hành động
		JPanel actions = new JPanel(new BorderLayout());
		actions.setOpaque(false);
		JButton btnCancel = createPillButton("Cancel", new Color(140, 146, 160));
		btnPrimary    = createPillButton(editingProduct == null ? "Add" : "Save", new Color(0, 160, 64));
		actions.add(btnCancel, BorderLayout.WEST);
		actions.add(btnPrimary, BorderLayout.EAST);
		gbc.gridx=0; gbc.gridy=4; gbc.gridwidth=2;
		root.add(actions, gbc);

		// Enter để submit ở name/price/desc
		KeyAdapter submitOnEnter = new KeyAdapter() {
			@Override public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) btnPrimary.doClick();
			}
		};
		tbName.addKeyListener(submitOnEnter);
		tbPrice.addKeyListener(submitOnEnter);
		tbDesc.addKeyListener(submitOnEnter);

		btnCancel.addActionListener(e -> dispose());
		btnPrimary.addActionListener(e -> onSubmit());

		setContentPane(root);
		getRootPane().setDefaultButton(btnPrimary);
	}

	private void prefillIfEditing() {
		if (editingProduct == null) return;
		tbName.setText(editingProduct.getNameProduct());
		if (editingProduct.getPriceProduct() != null) tbPrice.setText(editingProduct.getPriceProduct().toPlainString());
		tbDesc.setText(editingProduct.getDecriptions());
		if (editingProduct.getImages() != null) {
			imageBytes = editingProduct.getImages();
			ImageIcon icon = new ImageIcon(imageBytes);
			Image scaled = icon.getImage().getScaledInstance(200, 140, Image.SCALE_SMOOTH);
			picPreview.setIcon(new ImageIcon(scaled));
		}
	}

	private void loadTypes() {
		try {
			List<TypeProductItem> types = typeRepo.findAll();
			DefaultComboBoxModel<TypeProductItem> model = new DefaultComboBoxModel<>();
			for (TypeProductItem it : types) model.addElement(it);
			cbType.setModel(model);
			// select current type if editing
			if (editingProduct != null && editingProduct.getTypeProduct() != null) {
				int currentId = editingProduct.getTypeProduct().getIdTypeProduct();
				for (int i = 0; i < model.getSize(); i++) {
					TypeProductItem it = model.getElementAt(i);
					if (it.id == currentId) { cbType.setSelectedIndex(i); break; }
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Cannot load type: " + ex.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void onUploadImage() {
		// Use native OS dialog (Windows Explorer / macOS Finder)
		FileDialog fd = new FileDialog(this, "Select product image", FileDialog.LOAD);
		fd.setFilenameFilter((dir, name) -> name.toLowerCase().matches(".*\\.(jpg|jpeg|png|bmp)$"));
		fd.setVisible(true);
		String fileName = fd.getFile();
		String dirName  = fd.getDirectory();
		if (fileName == null || dirName == null) return; // user cancelled
		File f = new File(dirName, fileName);
		try {
			BufferedImage img = ImageIO.read(f);
			if (img == null) throw new IOException("Cannot read image.");
			int w = picPreview.getWidth() > 0 ? picPreview.getWidth() : 200;
			int h = picPreview.getHeight() > 0 ? picPreview.getHeight() : 140;
			Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
			picPreview.setIcon(new ImageIcon(scaled));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(img, "jpg", baos);
			imageBytes = baos.toByteArray();
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error reading image: " + ex.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void onSubmit() {
		// Validate
		String name = tbName.getText().trim();
		if (name.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Missing data", JOptionPane.WARNING_MESSAGE);
			tbName.requestFocus(); return;
		}
		String priceText = tbPrice.getText().trim();
		if (priceText.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Price cannot be empty.", "Missing data", JOptionPane.WARNING_MESSAGE);
			tbPrice.requestFocus(); return;
		}
		priceText = priceText.replace(",", "");
		BigDecimal price;
		try {
			price = new BigDecimal(priceText);
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(this, "Price only accepts numbers.", "Invalid", JOptionPane.WARNING_MESSAGE);
			tbPrice.requestFocus(); return;
		}
		TypeProductItem sel = (TypeProductItem) cbType.getSelectedItem();
		if (sel == null) {
			JOptionPane.showMessageDialog(this, "Please select a type.", "Missing data", JOptionPane.WARNING_MESSAGE);
			cbType.requestFocus(); return;
		}
		String desc = tbDesc.getText().trim();
		boolean isActive = true;

		try {
			if (editingProduct == null) {
				int newId = productRepo.insertProduct(name, price, desc, imageBytes, isActive, sel.id, employeeId);
				JOptionPane.showMessageDialog(this, "Add successfully! ID Product = " + newId, "Success", JOptionPane.INFORMATION_MESSAGE);
			} else {
				boolean ok = productRepo.updateProduct(editingProduct.getIdProduct(), name, price, desc, imageBytes, isActive, sel.id, employeeId);
				if (!ok) throw new RuntimeException("Update failed");
				JOptionPane.showMessageDialog(this, "Saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
			}
			dispose();
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error saving product: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	// --- UI helpers ---
	private JPanel wrapRounded(JComponent inner) {
		JPanel p = new JPanel(new BorderLayout());
		p.setOpaque(true);
		p.setBackground(Color.WHITE);
		p.setBorder(BorderFactory.createCompoundBorder(
			new RoundedLineBorder(new Color(210, 214, 222), 12), new EmptyBorder(4,6,4,6)
		));
		p.add(inner, BorderLayout.CENTER);
		return p;
	}

	private JPanel wrapRoundedWithIcon(JComponent inner, String iconPath) {
		JPanel p = new JPanel(new BorderLayout(8, 0));
		p.setOpaque(true);
		p.setBackground(Color.WHITE);
		p.setBorder(BorderFactory.createCompoundBorder(
			new RoundedLineBorder(new Color(210, 214, 222), 12), new EmptyBorder(2,2,2,2)
		));
		JLabel icon = new JLabel(loadIcon(iconPath, 18));
		icon.setBorder(new EmptyBorder(0, 10, 0, 0));
		p.add(icon, BorderLayout.WEST);
		p.add(inner, BorderLayout.CENTER);
		return p;
	}

	private JButton createPillButton(String text, Color bg) {
		JButton b = new JButton(text) {
			@Override protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				Color use = (Color) getClientProperty("bg");
				if (use == null) use = bg;
				g2.setColor(use);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
				super.paintComponent(g);
				g2.dispose();
			}
			@Override public boolean isContentAreaFilled() { return false; }
		};
		b.setForeground(Color.WHITE);
		b.setFocusPainted(false);
		b.setBorder(new EmptyBorder(8, 18, 8, 18));
		Color hover = bg.darker();
		b.addMouseListener(new MouseAdapter() {
			@Override public void mouseEntered(MouseEvent e) { b.putClientProperty("bg", hover); b.repaint(); }
			@Override public void mouseExited(MouseEvent e) { b.putClientProperty("bg", bg); b.repaint(); }
		});
		return b;
	}

	private ImageIcon loadIcon(String resourcePath, int size) {
		java.net.URL url = getClass().getResource(resourcePath);
		if (url == null) return null;
		ImageIcon raw = new ImageIcon(url);
		if (raw.getIconWidth() <= 0 || raw.getIconHeight() <= 0) return null;
		Image img = raw.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
		return new ImageIcon(img);
	}

	// Rounded border helper
	private static class RoundedLineBorder extends javax.swing.border.AbstractBorder {
		private final Color color; private final int arc;
		RoundedLineBorder(Color color, int arc) { this.color = color; this.arc = arc; }
		@Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(color);
			g2.drawRoundRect(x, y, width-1, height-1, arc, arc);
			g2.dispose();
		}
	}

	// Placeholders
	private static class PlaceholderTextField extends JTextField {
		private final String placeholder;
		PlaceholderTextField(String placeholder) { super(20); this.placeholder = placeholder; }
		@Override protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (getText().isEmpty() && !isFocusOwner()) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setColor(new Color(160,160,160));
				Insets ins = getInsets();
				g2.drawString(placeholder, ins.left, getHeight()/2 + g2.getFontMetrics().getAscent()/2 - 3);
				g2.dispose();
			}
		}
	}
	private static class PlaceholderTextArea extends JTextArea {
		private final String placeholder;
		PlaceholderTextArea(String placeholder) { super(4, 30); this.placeholder = placeholder; }
		@Override protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (getText().isEmpty() && !isFocusOwner()) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setColor(new Color(160,160,160));
				Insets ins = getInsets();
				g2.drawString(placeholder, ins.left, ins.top + g2.getFontMetrics().getAscent());
				g2.dispose();
			}
		}
	}

	// --- Extra UI components ---
	private static class GradientHeaderPanel extends JPanel {
		private final String title; private final Icon icon;
		GradientHeaderPanel(String title, Icon icon) {
			this.title = title; this.icon = icon; setOpaque(false); setBorder(new EmptyBorder(6,10,6,10));
		}
		@Override protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			int w = getWidth(), h = getHeight();
			GradientPaint gp = new GradientPaint(0, 0, new Color(255,128,76), w, h, new Color(255,78,109));
			g2.setPaint(gp); g2.fillRoundRect(0, 0, w, h, h, h); g2.dispose();
			super.paintComponent(g);
		}
		@Override public void paint(Graphics g) {
			super.paint(g);
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			int x = 12, y = getHeight()/2 + g2.getFontMetrics().getAscent()/2 - 2;
			if (icon != null) { icon.paintIcon(this, g2, x, (getHeight()-icon.getIconHeight())/2); x += icon.getIconWidth()+8; }
			g2.setColor(Color.WHITE); g2.setFont(getFont().deriveFont(Font.BOLD, 16f)); g2.drawString(title, x, y);
			g2.dispose();
		}
	}

	private static class DashedRoundedBorder extends javax.swing.border.AbstractBorder {
		private final Color color; private final int arc;
		DashedRoundedBorder(Color color, int arc) { this.color = color; this.arc = arc; }
		@Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(color);
			float[] dash = {5f, 5f};
			g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, dash, 0));
			g2.drawRoundRect(x, y, width-1, height-1, arc, arc);
			g2.dispose();
		}
	}

	// Styling helpers for inputs and combo
	private void styleInput(javax.swing.text.JTextComponent c) {
		c.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
		c.setFont(c.getFont().deriveFont(14f));
		c.setOpaque(false);
	}

	private void styleComboBox(JComboBox<?> combo) {
		combo.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
		combo.setOpaque(false);
		combo.setBackground(Color.WHITE);
		combo.setFont(combo.getFont().deriveFont(14f));
		combo.setRenderer(new DefaultListCellRenderer() {
			@Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				setBorder(new EmptyBorder(6,10,6,10));
				return c;
			}
		});
	}

	private JPanel wrapRoundedCombo(JComponent inner) {
		JPanel p = new JPanel(new BorderLayout());
		p.setOpaque(true);
		p.setBackground(Color.WHITE);
		p.setBorder(BorderFactory.createCompoundBorder(new RoundedLineBorder(new Color(120, 165, 255), 12), new EmptyBorder(2,2,2,2)));
		p.setPreferredSize(new Dimension(0, 44));
		p.add(inner, BorderLayout.CENTER);
		return p;
	}
}
