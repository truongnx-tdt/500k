import models.Product;
import repositories.ProductRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class OptionsProductForm extends JDialog {
	private final Product product;
	private final ProductRepository productRepo = new ProductRepository();

	public OptionsProductForm(JFrame parent, Product p) {
		super(parent, "Product Options", true);
		this.product = p;

		setContentPane(buildUI());
		setSize(380, 520);
		setLocationRelativeTo(parent);
	}

	private JPanel buildUI() {
		JPanel root = new JPanel(new BorderLayout());
		root.setBorder(new EmptyBorder(12, 12, 12, 12));
		root.setBackground(new Color(245,245,245));

		// Header with image
		JPanel header = new JPanel(new BorderLayout());
		header.setOpaque(false);
		JLabel imgLabel = new JLabel();
		imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
		if (product.getImages() != null) {
			ImageIcon icon = new ImageIcon(product.getImages());
			Image scaled = icon.getImage().getScaledInstance(300, 180, Image.SCALE_SMOOTH);
			imgLabel.setIcon(new ImageIcon(scaled));
		}
		header.add(imgLabel, BorderLayout.CENTER);
		root.add(header, BorderLayout.NORTH);

		// Info card
		JPanel infoCard = new JPanel(new GridBagLayout());
		infoCard.setOpaque(true);
		infoCard.setBackground(Color.WHITE);
		infoCard.setBorder(BorderFactory.createCompoundBorder(
			new RoundedLineBorder(new Color(210,214,222), 12), new EmptyBorder(12,12,12,12)));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(6, 6, 6, 6);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;

		JLabel nameLabel = new JLabel(product.getNameProduct());
		nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 18f));
		gbc.gridx=0; gbc.gridy=0;
		infoCard.add(nameLabel, gbc);

		JLabel priceLabel = new JLabel(product.getPriceProduct() + " VND");
		priceLabel.setForeground(new Color(255, 111, 0));
		priceLabel.setFont(priceLabel.getFont().deriveFont(Font.BOLD, 16f));
		gbc.gridy=1;
		infoCard.add(priceLabel, gbc);

		JTextArea descArea = new JTextArea(product.getDecriptions() != null ? product.getDecriptions() : "N/A");
		descArea.setLineWrap(true);
		descArea.setWrapStyleWord(true);
		descArea.setEditable(false);
		JScrollPane sp = new JScrollPane(descArea);
		sp.setBorder(BorderFactory.createEmptyBorder());
		JPanel descWrap = new JPanel(new BorderLayout());
		descWrap.setOpaque(false);
		descWrap.add(sp, BorderLayout.CENTER);
		descWrap.setPreferredSize(new Dimension(0, 120));
		gbc.gridy=2; gbc.fill = GridBagConstraints.BOTH; gbc.weighty=1.0;
		infoCard.add(descWrap, gbc);
		root.add(infoCard, BorderLayout.CENTER);

		// Actions
		JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
		actions.setOpaque(false);
		JButton btnAvailable = createPillButton("Available", new Color(0, 160, 64));
		JButton btnSoldOut   = createPillButton("Sold out", new Color(100,149,237));
		JButton btnEdit      = createPillButton("Edit", new Color(117, 130, 255));
		JButton btnDelete    = createPillButton("Delete", new Color(230, 38, 38));

		// Visibility logic based on isActive
		boolean isActive = product.getIsActive();
		btnAvailable.setVisible(!isActive);
		btnSoldOut.setVisible(isActive);

		actions.add(btnAvailable);
		actions.add(btnSoldOut);
		actions.add(btnEdit);
		actions.add(btnDelete);
		root.add(actions, BorderLayout.SOUTH);

		// Events (reuse existing repo methods, adjust as necessary)
		btnSoldOut.addActionListener(e -> {
			int confirm = JOptionPane.showConfirmDialog(this,
					"Mark " + product.getNameProduct() + " as Sold out?",
					"Confirm", JOptionPane.YES_NO_OPTION);
			if (confirm == JOptionPane.YES_OPTION) {
				if (productRepo.markSoldOut(product.getIdProduct())) {
					JOptionPane.showMessageDialog(this, product.getNameProduct() + " marked as Sold out!");
					dispose();
				} else {
					JOptionPane.showMessageDialog(this, "Failed to update product!", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		btnAvailable.addActionListener(e -> {
			int confirm = JOptionPane.showConfirmDialog(this,
					"Mark " + product.getNameProduct() + " as Available?",
					"Confirm", JOptionPane.YES_NO_OPTION);
			if (confirm == JOptionPane.YES_OPTION) {
				if (productRepo.markAvailable(product.getIdProduct())) {
					JOptionPane.showMessageDialog(this, product.getNameProduct() + " is now Available!");
					dispose();
				} else {
					JOptionPane.showMessageDialog(this, "Failed to update product!", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		btnEdit.addActionListener(e -> {
			Window parent = SwingUtilities.getWindowAncestor(this);
			int employeeId = 0; // if you have current employee context, pass it here
			new AddProductForm(parent, employeeId, product).setVisible(true);
			dispose();
		});

		btnDelete.addActionListener(e -> {
			int confirm = JOptionPane.showConfirmDialog(this,
					"Delete " + product.getNameProduct() + "?",
					"Confirm Delete", JOptionPane.YES_NO_OPTION);
			if (confirm == JOptionPane.YES_OPTION) {
				if (productRepo.deleteProduct(product.getIdProduct())) {
					JOptionPane.showMessageDialog(this, "Deleted!");
					dispose();
				} else {
					JOptionPane.showMessageDialog(this, "Failed to delete product!", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		return root;
	}

	// UI helpers
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

	private JButton createPillButton(String text, Color bg) {
		JButton b = new JButton(text) {
			@Override protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(bg);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
				super.paintComponent(g);
				g2.dispose();
			}
			@Override public boolean isContentAreaFilled() { return false; }
		};
		b.setForeground(Color.WHITE);
		b.setFocusPainted(false);
		b.setBorder(new EmptyBorder(8, 16, 8, 16));
		return b;
	}
}
