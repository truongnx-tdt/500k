import repositories.EmployeeRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ChangePasswordForm extends JDialog {
	private final int employeeId;
	private final EmployeeRepository repo = new EmployeeRepository();

	private JPasswordField txtCurrent;
	private JPasswordField txtNew;
	private JPasswordField txtConfirm;
	private JCheckBox chkShow;
	private JButton btnExit;
	private JButton btnConfirm;
	private char defaultEchoChar;

	public ChangePasswordForm(Window owner, int employeeId) {
		super(owner, "Change password", ModalityType.APPLICATION_MODAL);
		this.employeeId = employeeId;
		buildUI();
		pack();
		setMinimumSize(new Dimension(420, getHeight()));
		setLocationRelativeTo(owner);
	}

	private void buildUI() {
		JPanel root = new JPanel(new GridBagLayout());
		root.setBorder(new EmptyBorder(16,16,16,16));
		root.setBackground(new Color(245,245,245));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 6);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;

		// Current/Old password
		txtCurrent = createPasswordField("Old password");
		gbc.gridy = 0; gbc.gridwidth = 2;
		root.add(wrapRoundedWithIcon(txtCurrent, "/icons/password.png"), gbc);

		// New password
		txtNew = createPasswordField("New password");
		gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
		root.add(wrapRoundedWithIcon(txtNew, "/icons/password.png"), gbc);

		// Confirm new password (kept for logic consistency)
		txtConfirm = createPasswordField("Confirm new password");
		gbc.gridy = 2;
		root.add(wrapRoundedWithIcon(txtConfirm, "/icons/password.png"), gbc);

		defaultEchoChar = txtNew.getEchoChar();

		// Show checkbox
		chkShow = new JCheckBox("Show");
		chkShow.setOpaque(false);
		chkShow.addActionListener(e -> toggleShowPasswords(chkShow.isSelected()));
		gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.WEST;
		root.add(chkShow, gbc);

		// Actions row: Exit (left) and Confirm (right)
		JPanel actions = new JPanel(new BorderLayout());
		actions.setOpaque(false);
		btnExit = createPillButton("Exit", new Color(105, 142, 247));
		btnExit.addActionListener(e -> dispose());
		actions.add(btnExit, BorderLayout.WEST);
		btnConfirm = createPillButton("Confirm", new Color(230, 38, 38));
		btnConfirm.addActionListener(e -> onSave());
		actions.add(btnConfirm, BorderLayout.EAST);

		gbc.gridy = 4; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.CENTER;
		root.add(actions, gbc);

		setContentPane(root);
		getRootPane().setDefaultButton(btnConfirm);
	}

	private void toggleShowPasswords(boolean show) {
		char echo = show ? (char)0 : defaultEchoChar;
		txtNew.setEchoChar(echo);
		txtCurrent.setEchoChar(echo);
		txtConfirm.setEchoChar(echo);
	}

	private JPasswordField createPasswordField(String placeholder) {
		PlaceholderPasswordField pf = new PlaceholderPasswordField();
		pf.setPlaceholder(placeholder);
		pf.setColumns(20);
		pf.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
		pf.setOpaque(false);

		// Focus border highlight on its container
		pf.addFocusListener(new FocusAdapter() {
			@Override public void focusGained(FocusEvent e) {
				JComponent container = (JComponent) pf.getClientProperty("container");
				if (container != null) container.setBorder(BorderFactory.createCompoundBorder(new RoundedLineBorder(new Color(120, 165, 255), 12), new EmptyBorder(2,2,2,2)));
			}
			@Override public void focusLost(FocusEvent e) {
				JComponent container = (JComponent) pf.getClientProperty("container");
				if (container != null) container.setBorder(BorderFactory.createCompoundBorder(new RoundedLineBorder(new Color(200, 212, 255), 12), new EmptyBorder(2,2,2,2)));
			}
		});
		return pf;
	}

	private JPanel wrapRoundedWithIcon(JComponent inner, String iconPath) {
		JPanel panel = new JPanel(new BorderLayout(8, 0));
		panel.setOpaque(true);
		panel.setBackground(Color.WHITE);
		panel.setBorder(BorderFactory.createCompoundBorder(
			new RoundedLineBorder(new Color(200, 212, 255), 12),
			new EmptyBorder(2,2,2,2)
		));
		panel.setPreferredSize(new Dimension(0, 40));

		JLabel icon = new JLabel(loadIcon(iconPath, 18));
		icon.setBorder(new EmptyBorder(0, 10, 0, 0));
		panel.add(icon, BorderLayout.WEST);
		panel.add(inner, BorderLayout.CENTER);
		inner.putClientProperty("container", panel);
		return panel;
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
		b.setBorder(new EmptyBorder(8, 18, 8, 18));
		Color hover = bg.darker();
		b.addMouseListener(new MouseAdapter() {
			@Override public void mouseEntered(MouseEvent e) { b.setForeground(Color.WHITE); b.putClientProperty("bg", hover); b.repaint(); }
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

	private void onSave() {
		String current = new String(txtCurrent.getPassword());
		String newPass  = new String(txtNew.getPassword());
		String confirm  = new String(txtConfirm.getPassword());

		if (current.isBlank() || newPass.isBlank() || confirm.isBlank()) {
			JOptionPane.showMessageDialog(this, "Please enter all information.", "Missing data", JOptionPane.WARNING_MESSAGE);
			return;
		}
		if (!newPass.equals(confirm)) {
			JOptionPane.showMessageDialog(this, "The re-entered password does not match.", "Confirmation error", JOptionPane.WARNING_MESSAGE);
			txtConfirm.requestFocus();
			return;
		}

		try {
			boolean ok = repo.changePasswordIfMatch(employeeId, current, newPass);
			if (!ok) {
				JOptionPane.showMessageDialog(this, "The current password is incorrect or the user is not found.", "Failed", JOptionPane.ERROR_MESSAGE);
				return;
			}
			JOptionPane.showMessageDialog(this, "Change password successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
			dispose();
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Change password failed", JOptionPane.ERROR_MESSAGE);
		}
	}

	// Helper components
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

	private static class PlaceholderPasswordField extends JPasswordField {
		private String placeholder = "";
		public void setPlaceholder(String text) { this.placeholder = text; }
		@Override protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (getPassword().length == 0 && !isFocusOwner() && placeholder != null && !placeholder.isEmpty()) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g2.setColor(new Color(160, 160, 160));
				Insets ins = getInsets();
				g2.drawString(placeholder, ins.left, getHeight()/2 + g2.getFontMetrics().getAscent()/2 - 3);
				g2.dispose();
			}
		}
	}
}
