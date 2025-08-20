import dbContext.DB;
import models.Employee;
import models.RoleEmployee;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginForm extends JFrame {
	private JTextField txtUsername;
	private JPasswordField txtPassword;
	private JButton btnLogin;
    private JButton btnClose;
	private JCheckBox chkShow;
	private char defaultEchoChar;

	public LoginForm() {
		setTitle("Login");
		setUndecorated(true);
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		initializeComponents();
		pack();
		setLocationRelativeTo(null);
	}

	private void initializeComponents() {
		GradientPanel content = new GradientPanel(new Color(255, 128, 76), new Color(255, 78, 109));
		content.setLayout(new GridBagLayout());
		content.setBorder(new EmptyBorder(8, 8, 8, 8));
		setContentPane(content);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(8, 10, 8, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.gridwidth = 1;

		// Top bar with close button
		JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		topBar.setOpaque(false);
		btnClose = new JButton("✕");
		btnClose.setFont(btnClose.getFont().deriveFont(Font.BOLD, 16f));
		btnClose.setMargin(new Insets(0, 4, 0, 4));
		btnClose.setForeground(new Color(255, 255, 255, 230));
		btnClose.setContentAreaFilled(false);
		btnClose.setBorder(BorderFactory.createEmptyBorder());
		btnClose.setFocusPainted(false);
		btnClose.setOpaque(false);
		btnClose.addActionListener(e -> onCancel());
		topBar.add(btnClose);
		content.add(topBar, gbc);

		// Avatar
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.CENTER;
		JComponent avatar = createAvatarComponent(96);
		content.add(avatar, gbc);
		
		int fieldWidth = 260;
		
		// Email field with icon inside rounded container
		gbc.gridy = 3;
		JPanel emailContainer = createInputField("/icons/email.png", "📧", true);
		txtUsername = (JTextField) emailContainer.getClientProperty("textField");
		if (txtUsername instanceof PlaceholderSupport) {
			((PlaceholderSupport) txtUsername).setPlaceholder("Email address");
		}
		emailContainer.setPreferredSize(new Dimension(fieldWidth, 36));
		content.add(emailContainer, gbc);
		
		// Password field with icon
		gbc.gridy = 4;
		JPanel passContainer = createInputField("/icons/password.png", "🔒", false);
		txtPassword = (JPasswordField) passContainer.getClientProperty("passwordField");
		defaultEchoChar = txtPassword.getEchoChar();
		if (txtPassword instanceof PlaceholderSupport) {
			((PlaceholderSupport) txtPassword).setPlaceholder("Password");
		}
		passContainer.setPreferredSize(new Dimension(fieldWidth, 36));
		content.add(passContainer, gbc);

		// Show checkbox
		gbc.gridy = 5;
		gbc.fill = GridBagConstraints.NONE;
		JPanel showPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
		showPanel.setOpaque(false);
		chkShow = new JCheckBox("Show");
		chkShow.setOpaque(false);
		chkShow.setForeground(Color.WHITE);
		chkShow.addActionListener(e -> {
			if (chkShow.isSelected()) {
				txtPassword.setEchoChar((char) 0);
			} else {
				txtPassword.setEchoChar(defaultEchoChar);
			}
		});
		showPanel.add(chkShow);
		content.add(showPanel, gbc);

		// Login button centered
		gbc.gridy = 6;
		gbc.fill = GridBagConstraints.NONE;
		btnLogin = new JButton("Login");
		btnLogin.setForeground(Color.WHITE);
		btnLogin.setBackground(new Color(63, 114, 255));
		btnLogin.setFocusPainted(false);
		btnLogin.setBorder(new RoundedLineBorder(new Color(63, 114, 255), 16));
		btnLogin.setOpaque(true);
		btnLogin.setPreferredSize(new Dimension(160, 38));
		content.add(btnLogin, gbc);

		getRootPane().setDefaultButton(btnLogin);
		getRootPane().registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

		btnLogin.addActionListener(e -> onLogin());

		// Enable drag on the content area for convenience
		MouseAdapter dragger = new DragWindowAdapter(this);
		content.addMouseListener(dragger);
		content.addMouseMotionListener(dragger);
	}

	private void onLogin() {
		String username = txtUsername.getText().trim();
		char[] password = txtPassword.getPassword();

		if (username.isEmpty() || password.length == 0) {
			JOptionPane.showMessageDialog(this, "Please enter your username and password.", "Missing information", JOptionPane.WARNING_MESSAGE);
			return;
		}

		try {
            Employee employee = authenticate(username, new String(password));
			if (employee != null) {
				SwingUtilities.invokeLater(() -> {
					new DashboardForm(employee).setVisible(true);
				});
				dispose();
			} else {
				JOptionPane.showMessageDialog(this, "Username or password is incorrect.", "Login failed", JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Error when authenticating: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void onCancel() {
		dispose();
		System.exit(0);
	}

	// return Employee
    private Employee authenticate(String email, String password) throws Exception {
        String sql = """
        SELECT IdEmployee, NameEmployee, BirthDay, Gender, Address, Email, Password, IdRole
        FROM Employee
        WHERE Email = ?  AND Password = ? 
        """;

        try (Connection cn = DB.getConnection();
             PreparedStatement st = cn.prepareStatement(sql)) {

            st.setString(1, email);
            st.setString(2, password);

            try (ResultSet rs = st.executeQuery()) {
                if (!rs.next()) return null;

                Employee e = new Employee();
                e.setIdEmployee(rs.getInt("IdEmployee"));
                e.setNameEmployee(rs.getString("NameEmployee"));
                if (rs.getDate("BirthDay") != null) {
                    e.setBirthDay(rs.getDate("BirthDay").toLocalDate());
                }
                e.setGender((Boolean) rs.getObject("Gender"));
                e.setAddress(rs.getString("Address"));
                e.setEmail(rs.getString("Email"));

                Integer idRole = (Integer) rs.getObject("IdRole");
                if (idRole != null) {
                    RoleEmployee r = new RoleEmployee();
                    r.setIdRole(idRole);
                    e.setRoleEmployee(r);
                }
                return e;
            }
        }
    }


	// UI helpers
	private JPanel createInputField(String iconResourcePath, String fallbackEmoji, boolean isTextField) {
		RoundedPanel container = new RoundedPanel(18, Color.WHITE);
		container.setLayout(new BorderLayout(8, 0));
		container.setBorder(new EmptyBorder(4, 10, 4, 10));

		ImageIcon iconImg = tryLoadIcon(iconResourcePath, 18);
		JLabel iconLabel = iconImg != null ? new JLabel(iconImg) : new JLabel(fallbackEmoji);
		iconLabel.setOpaque(false);
		if (iconImg == null) {
			iconLabel.setFont(iconLabel.getFont().deriveFont(Font.PLAIN, 16f));
			iconLabel.setForeground(new Color(255, 255, 255));
		}
		// Put icon on a small translucent badge
		RoundedPanel badge = new RoundedPanel(12, new Color(255, 255, 255, 60));
		badge.setLayout(new GridBagLayout());
		badge.add(iconLabel);
		container.add(badge, BorderLayout.WEST);

		if (isTextField) {
			PlaceholderTextField tf = new PlaceholderTextField();
			tf.setBorder(BorderFactory.createEmptyBorder());
			tf.setOpaque(false);
			container.putClientProperty("textField", tf);
			container.add(tf, BorderLayout.CENTER);
		} else {
			PlaceholderPasswordField pf = new PlaceholderPasswordField();
			pf.setBorder(BorderFactory.createEmptyBorder());
			pf.setOpaque(false);
			container.putClientProperty("passwordField", pf);
			container.add(pf, BorderLayout.CENTER);
		}

		return container;
	}

	private JComponent createAvatarComponent(int size) {
		ImageIcon avatarIcon = tryLoadIcon("/icons/avatar.png", size);
		if (avatarIcon != null) {
			return new AvatarImagePanel(avatarIcon, size);
		}
		AvatarPanel fallback = new AvatarPanel();
		fallback.setPreferredSize(new Dimension(size, size));
		return fallback;
	}

	private ImageIcon tryLoadIcon(String resourcePath, int size) {
		java.net.URL url = getClass().getResource(resourcePath);
		if (url == null) return null;
		ImageIcon raw = new ImageIcon(url);
		if (raw.getIconWidth() <= 0 || raw.getIconHeight() <= 0) return null;
		Image img = raw.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
		return new ImageIcon(img);
	}

	// Gradient background panel
	private static class GradientPanel extends JPanel {
		private final Color start;
		private final Color end;

		GradientPanel(Color start, Color end) {
			this.start = start;
			this.end = end;
			setOpaque(false);
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			int w = getWidth();
			int h = getHeight();
			GradientPaint gp = new GradientPaint(0, 0, start, w, h, end);
			g2.setPaint(gp);
			g2.fillRect(0, 0, w, h);
			g2.dispose();
			super.paintComponent(g);
		}
	}

	// Rounded container panel
	private static class RoundedPanel extends JPanel {
		private final int radius;
		private final Color backgroundColor;

		RoundedPanel(int radius, Color backgroundColor) {
			this.radius = radius;
			this.backgroundColor = backgroundColor;
			setOpaque(false);
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(backgroundColor);
			g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius * 2, radius * 2);
			g2.dispose();
			super.paintComponent(g);
		}
	}

	// Rounded outline border for button
	private static class RoundedLineBorder extends javax.swing.border.AbstractBorder {
		private final Color color;
		private final int arc;

		RoundedLineBorder(Color color, int arc) {
			this.color = color;
			this.arc = arc;
		}

		@Override
		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(color.darker());
			g2.drawRoundRect(x, y, width - 1, height - 1, arc, arc);
			g2.dispose();
		}
	}

	// Simple avatar circle
	private static class AvatarPanel extends JPanel {
		AvatarPanel() { setOpaque(false); }

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			int w = getWidth();
			int h = getHeight();
			int d = Math.min(w, h);
			g2.setColor(new Color(255, 204, 102));
			g2.fillOval((w - d) / 2, (h - d) / 2, d, d);
			// simple head & shoulders
			g2.setColor(new Color(117, 130, 255));
			int cx = w / 2;
			int cy = h / 2;
			g2.fillOval(cx - d / 8, cy - d / 6, d / 4, d / 4); // head
			g2.fillRoundRect(cx - d / 4, cy, d / 2, d / 3, d / 6, d / 6); // body
			g2.dispose();
		}
	}

	// Avatar with resource image clipped in a circle
	private static class AvatarImagePanel extends JPanel {
		private final Image image;
		private final int size;
		AvatarImagePanel(ImageIcon icon, int size) {
			this.image = icon.getImage();
			this.size = size;
			setOpaque(false);
			setPreferredSize(new Dimension(size, size));
		}
		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			int d = Math.min(getWidth(), getHeight());
			int x = (getWidth() - d) / 2;
			int y = (getHeight() - d) / 2;
			g2.setClip(new java.awt.geom.Ellipse2D.Float(x, y, d, d));
			g2.drawImage(image, x, y, d, d, null);
			g2.dispose();
		}
	}

	// Drag to move window
	private static class DragWindowAdapter extends MouseAdapter {
		private final JFrame frame;
		private Point pressPoint;

		DragWindowAdapter(JFrame frame) { this.frame = frame; }

		@Override
		public void mousePressed(MouseEvent e) { pressPoint = e.getPoint(); }

		@Override
		public void mouseDragged(MouseEvent e) {
			if (pressPoint != null) {
				Point loc = frame.getLocation();
				frame.setLocation(loc.x + e.getX() - pressPoint.x, loc.y + e.getY() - pressPoint.y);
			}
		}
	}

	// Placeholder support
	private interface PlaceholderSupport {
		void setPlaceholder(String text);
	}

	private static class PlaceholderTextField extends JTextField implements PlaceholderSupport {
		private String placeholder = "";
		@Override public void setPlaceholder(String text) { this.placeholder = text; repaint(); }
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (getText().isEmpty() && !isFocusOwner() && placeholder != null && !placeholder.isEmpty()) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g2.setColor(new Color(150, 150, 150));
				g2.setFont(getFont().deriveFont(Font.PLAIN));
				Insets ins = getInsets();
				g2.drawString(placeholder, ins.left, getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 3);
				g2.dispose();
			}
		}
	}

	private static class PlaceholderPasswordField extends JPasswordField implements PlaceholderSupport {
		private String placeholder = "";
		@Override public void setPlaceholder(String text) { this.placeholder = text; repaint(); }
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (getPassword().length == 0 && !isFocusOwner() && placeholder != null && !placeholder.isEmpty()) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g2.setColor(new Color(150, 150, 150));
				g2.setFont(getFont().deriveFont(Font.PLAIN));
				Insets ins = getInsets();
				g2.drawString(placeholder, ins.left, getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 3);
				g2.dispose();
			}
		}
	}
}
