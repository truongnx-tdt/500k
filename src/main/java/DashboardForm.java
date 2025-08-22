import UI.ModernScrollBarUI;
import components.BreadcrumbBar;
import components.PriceBadge;
import components.SoldOutStripe;
import models.Employee;
import models.Product;
import repositories.ProductRepository;
import utils.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class DashboardForm extends JFrame {
    private Employee _employee;

    private JPanel sidebar;
    private JPanel header;
    private JPanel content;
    private JPanel orderPanel;
    private JPanel orderItemsList;
    private JLabel lblSubtotal;
    private JLabel lblVat;
    private JLabel lblTotal;

    private final Map<Integer, OrderLine> orderLines = new HashMap<>();
    private BigDecimal subtotal = BigDecimal.ZERO;

    private BreadcrumbBar breadcrumb;
    private String currentMenu = "foods";

    private JTextField searchField;
    private JPopupMenu searchPopup;
    private javax.swing.Timer searchTimer;

    public DashboardForm(Employee employee) {
        _employee = employee;
        setTitle("HS FOOD");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 680));
        initializeUI();
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // init data
        List<Product> products = null;
        try {
            products = ProductRepository.findByTypeId(11);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        renderProductsGrid(products);
    }

    private void initializeUI() {
        getContentPane().setLayout(new BorderLayout());

        // Sidebar - completely on the left
        sidebar = buildSidebar();
        getContentPane().add(sidebar, BorderLayout.WEST);

        // Right side container (header + content)
        JPanel rightContainer = new JPanel(new BorderLayout());

        // Header - spans full width of remaining area
        header = buildHeader();
        rightContainer.add(header, BorderLayout.NORTH);

        // Content area with breadcrumb and two columns
        JPanel contentArea = new JPanel(new GridBagLayout());
        contentArea.setBackground(Color.WHITE);
        contentArea.setBorder(new EmptyBorder(8, 12, 8, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        // Breadcrumb row spanning two columns
        breadcrumb = new BreadcrumbBar(idx -> {
        });
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentArea.add(breadcrumb, gbc);

        // set initial path
        breadcrumb.setPath(java.util.List.of("Category", "Foods"));

        // Left content area
        content = new JPanel();
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createDashedBorder(new Color(200, 200, 200), 2, 4));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.65;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentArea.add(content, gbc);

        // Right column with New Order header + items + total
        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);
        GridBagConstraints gbcR = new GridBagConstraints();
        gbcR.insets = new Insets(8, 0, 8, 0);
        gbcR.fill = GridBagConstraints.BOTH;
        gbcR.weightx = 1.0;

        JPanel newOrderHeader = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(255, 140, 76), w, h, new Color(255, 78, 145));
                g2.setPaint(gp);
                int arc = h; // full pill
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        newOrderHeader.setOpaque(false);
        newOrderHeader.setLayout(new BorderLayout());
        JLabel lblNewOrder = new JLabel("New Order", SwingConstants.CENTER);
        lblNewOrder.setForeground(Color.WHITE);
        lblNewOrder.setFont(lblNewOrder.getFont().deriveFont(Font.BOLD, 15f));
        newOrderHeader.add(lblNewOrder, BorderLayout.CENTER);
        newOrderHeader.setPreferredSize(new Dimension(0, 40));
        gbcR.gridx = 0;
        gbcR.gridy = 0;
        gbcR.weighty = 0.0;
        gbcR.ipady = 16;
        right.add(newOrderHeader, gbcR);
        gbcR.ipady = 0;

        orderItemsList = new JPanel();
        orderItemsList.setBackground(Color.WHITE);
        orderItemsList.setBorder(BorderFactory.createDashedBorder(new Color(200, 200, 200), 2, 4));
        orderItemsList.setLayout(new BoxLayout(orderItemsList, BoxLayout.Y_AXIS));
        gbcR.gridy = 1;
        gbcR.weighty = 1.0;
        JScrollPane sp = new JScrollPane(orderItemsList);
        JScrollBar vsb = sp.getVerticalScrollBar();
        vsb.setUI(new ModernScrollBarUI());
        vsb.setPreferredSize(new Dimension(10, 0));
        vsb.setUnitIncrement(24);

        right.add(sp, gbcR);


        orderPanel = buildTotalPanel();
        gbcR.gridy = 2;
        gbcR.weighty = 0.0;
        right.add(orderPanel, gbcR);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.35;
        gbc.weighty = 1.0;
        contentArea.add(right, gbc);

        rightContainer.add(contentArea, BorderLayout.CENTER);
        getContentPane().add(rightContainer, BorderLayout.CENTER);
    }

    private JPanel buildSidebar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(140, 0));
        panel.setBackground(Color.WHITE);
        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        logo.setOpaque(false);
        logo.setBorder(new EmptyBorder(12, 25, 6, 8));
        JLabel logoLabel = new JLabel();
        ImageIcon appIcon = loadIcon("/icons/cutlery.png", 40);
        if (appIcon != null) logoLabel.setIcon(appIcon);
        logo.add(logoLabel);
        panel.add(logo, BorderLayout.NORTH);

        // CENTER: menu + stripe
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);

        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setOpaque(false);
        menu.setBorder(new EmptyBorder(8, 8, 8, 8));

        menu.add(iconMenuItem("Foods", "/icons/food-tray.png", () -> showCard("foods")));
        menu.add(iconMenuItem("Drink", "/icons/soda.png", () -> showCard("drink")));
        menu.add(iconMenuItem("Snack", "/icons/snack.png", () -> showCard("snack")));
        menu.add(iconMenuItem("Dessert", "/icons/snack.png", () -> showCard("dessert")));
        menu.add(iconMenuItem("Combo", "/icons/fast-food.png", () -> showCard("combo")));
        menu.add(Box.createVerticalGlue());
        if (_employee.getRoleEmployee().getIdRole() == 1) {
            menu.add(iconMenuItem("Add item", "/icons/plus.png", () -> openAddItemDialog()));
        }
        menu.add(iconMenuItem("Settings", "/icons/gear.png", () -> openSettings()));
        center.add(menu, BorderLayout.CENTER);

        // Welcome stripe - light gray with dark text
        JPanel stripe = new JPanel(new GridBagLayout());
        stripe.setPreferredSize(new Dimension(50, 0));
        stripe.setOpaque(true);
        stripe.setBackground(new Color(240, 240, 240)); // Light gray background
        stripe.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1)); // Light border

        // Create vertical text panel
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.setBorder(new EmptyBorder(10, 5, 10, 5)); // Add padding

        String[] letters = "WELCOME TO YOU".split("");
        for (String letter : letters) {
            if (!letter.trim().isEmpty()) {
                JLabel letterLabel = new JLabel(letter);
                letterLabel.setForeground(new Color(50, 50, 50)); // Dark gray text like image
                letterLabel.setFont(letterLabel.getFont().deriveFont(Font.BOLD, 12f));
                letterLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                textPanel.add(letterLabel);
                textPanel.add(Box.createVerticalStrut(2)); // Small spacing between letters
            }
        }

        stripe.add(textPanel);

        center.add(stripe, BorderLayout.EAST);
        panel.add(center, BorderLayout.CENTER);

        return panel;
    }

    // ---------------------------------- handle button click -------------------------------
    private void openSettings() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        ChangePasswordForm dlg = new ChangePasswordForm(owner, _employee.getIdEmployee());
        dlg.setVisible(true);
    }


    private void openAddItemDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        AddProductForm dlg = new AddProductForm(owner, _employee.getIdEmployee());
        dlg.setVisible(true);
        showCard(currentMenu);
    }


    private void showCard(String name) {
        currentMenu = name;
        breadcrumb.setPath(java.util.List.of("Category", name));
        int typeId = switch (name.toLowerCase()) {
            case "foods" -> 11;
            case "drink" -> 12;
            case "snack" -> 13;
            case "dessert" -> 14;
            case "combo" -> 15;
            default -> -1;
        };
        if (typeId < 0) {
            JOptionPane.showMessageDialog(this, "Màn hình \"" + name + "\" chưa được cấu hình.",
                    "Không tìm thấy", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 1) lấy dữ liệu từ DB
        List<Product> products = null;
        try {
            products = ProductRepository.findByTypeId(typeId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        renderProductsGrid(products);
    }


    private void renderProductsGrid(List<Product> products) {
        content.removeAll();
        content.setLayout(new BorderLayout());

        JPanel grid = new JPanel(new GridLayout(0, 3, 12, 12));
        grid.setBorder(new EmptyBorder(8, 8, 8, 8));
        grid.setBackground(Color.WHITE);

        for (Product p : products) {
            grid.add(buildItemPanel(p));
        }

        JScrollPane sp = new JScrollPane(grid);
        sp.setBorder(null);

//        // vertical
        JScrollBar vsb = sp.getVerticalScrollBar();
        vsb.setUI(new ModernScrollBarUI());
        vsb.setPreferredSize(new Dimension(10, 0));
        vsb.setUnitIncrement(24);

        JScrollBar hsb = sp.getHorizontalScrollBar();
        hsb.setUI(new ModernScrollBarUI());
        hsb.setPreferredSize(new Dimension(0, 10));
        hsb.setUnitIncrement(24);

        content.add(sp, BorderLayout.CENTER);

        content.revalidate();
        content.repaint();
    }

    private JPanel buildItemPanel(Product p) {
        JPanel card = new JPanel(null);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(190, 190, 190)));
        card.setPreferredSize(new Dimension(220, 200));

        // 1) Ảnh gốc để scale lại mỗi lần resize
        final Image srcImage;
        if (p.getImages() != null) {
            try {
                srcImage = ImageIO.read(new ByteArrayInputStream(p.getImages()));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else srcImage = null;

        JLabel img = new JLabel();
        img.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(img);

        // 2) Badge giá
        String priceText = Utils.formatPriceOnly(p.getPriceProduct());
        PriceBadge badge = new PriceBadge(priceText);
        card.add(badge);

        // 3) Sold out stripe
        final boolean soldOut = Boolean.FALSE.equals(p.getIsActive());
        SoldOutStripe stripe = soldOut ? new SoldOutStripe() : null;
        if (stripe != null) card.add(stripe);

        // 4) Tên món
        JLabel name = new JLabel(p.getNameProduct(), SwingConstants.CENTER);
        name.setFont(name.getFont().deriveFont(Font.BOLD, 12f));
        card.add(name);

        // 5) Tính lại layout khi card thay đổi kích thước
        card.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int pad = 8;
                int w = card.getWidth() - pad * 2;
                int h = card.getHeight() - pad * 2;

                // Tỷ lệ cho khu vực ảnh (chiếm ~65% chiều cao)
                int imgH = Math.max(60, (int) (h * 0.65));
                int imgY = pad;
                img.setBounds(pad, imgY, w, imgH);

                // Scale ảnh theo kích thước label
                if (srcImage != null) {
                    Image scaled = srcImage.getScaledInstance(w, imgH, Image.SCALE_SMOOTH);
                    img.setIcon(new ImageIcon(scaled));
                }

                // Badge (góc trái trên của vùng ảnh)
                FontMetrics fm = badge.getFontMetrics(badge.getFont());
                int badgeH = 20;
                int badgeW = Math.max(54, fm.stringWidth(priceText) + 16);
                badge.setBounds(pad, imgY + 4, badgeW, badgeH);

                // Sold out stripe (nằm ngang giữa vùng ảnh)
                if (stripe != null) {
                    int stripeH = 24;
                    int stripeY = imgY + (imgH - stripeH) / 2;
                    stripe.setBounds(pad, stripeY, w, stripeH);
                    // đảm bảo stripe ở trên cùng
                    card.setComponentZOrder(stripe, 0);
                }

                // Tên món: chiếm phần dưới
                int nameY = imgY + imgH + 10;
                int nameH = Math.max(20, h - imgH - 12);
                name.setBounds(pad, nameY, w, nameH);

                // Đưa badge lên trên ảnh
                card.setComponentZOrder(badge, 0);
            }
        });

        // 6) Click/Right-click như cũ
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) onItemClick(p);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e, p, card);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e, p, card);
            }
        });

        return card;
    }

    private void maybeShowPopup(MouseEvent e, Product p, JComponent owner) {
        if (e.isPopupTrigger()) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem mi = new JMenuItem("Options: " + p.getNameProduct());
            mi.addActionListener(ae -> onItemRightClick(p));
            menu.add(mi);
            menu.show(owner, e.getX(), e.getY());
        }
    }

    private void onItemClick(Product p) {
        if (p.getIsActive()) {
            addOrIncrementItem(p);
        } else {
            JOptionPane.showMessageDialog(this, "This product is out of stock.",
                    "Out of stock", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onItemRightClick(Product p) {
            OptionsProductForm form = new OptionsProductForm(null, p, _employee);
            form.setVisible(true);
            showCard(currentMenu);
    }

    // ---------------------------------- handle button click -------------------------------


    private JPanel iconMenuItem(String text, String iconPath, Runnable onClick) {
        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBorder(new EmptyBorder(8, 4, 8, 4));
        wrap.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Color normalBg = Color.WHITE;
        Color hoverBg = new Color(240, 240, 240);

        wrap.setOpaque(true);
        wrap.setBackground(normalBg);

        JLabel icon = new JLabel();
        ImageIcon ii = loadIcon(iconPath, 24);
        if (ii != null) icon.setIcon(ii);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        wrap.add(icon);

        JLabel lbl = new JLabel(text);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 11f));
        wrap.add(Box.createVerticalStrut(4));
        wrap.add(lbl);

        Color finalNormalBg = normalBg;
        MouseAdapter ad = new MouseAdapter() {
            private void setHover(boolean on) {
                wrap.setBackground(on ? hoverBg : finalNormalBg);
                wrap.repaint();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setHover(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), wrap);
                if (wrap.contains(p)) return;
                setHover(false);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (onClick != null) onClick.run();
            }
        };

        wrap.addMouseListener(ad);
        icon.addMouseListener(ad);
        lbl.addMouseListener(ad);

        return wrap;
    }


    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));
        panel.setBackground(Color.WHITE);

        // Left: title + subtitle
        JPanel titleArea = new JPanel();
        titleArea.setOpaque(false);
        titleArea.setLayout(new BoxLayout(titleArea, BoxLayout.Y_AXIS));
        JLabel lblTitle = new JLabel("HS FOOD");
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 22f));
        lblTitle.setForeground(new Color(60, 60, 60));
        JLabel lblSub = new JLabel("Delicious and Cheap");
        lblSub.setFont(lblSub.getFont().deriveFont(Font.PLAIN, 12f));
        lblSub.setForeground(new Color(120, 120, 120));
        titleArea.add(lblTitle);
        titleArea.add(Box.createVerticalStrut(2));
        titleArea.add(lblSub);
        panel.add(titleArea, BorderLayout.WEST);

        // Center: search field with icon + reload
        JPanel center = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        center.setOpaque(false);
        ImageIcon searchIcon = loadIcon("/icons/search.png", 16);
        JLabel searchLbl = new JLabel(searchIcon);
        searchField = new JTextField(30);
        searchField.putClientProperty("JTextField.placeholderText", "Search for menu");
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        JButton btnReload = new JButton();
        ImageIcon refreshIcon = loadIcon("/icons/refresh.png", 18);
        if (refreshIcon != null) btnReload.setIcon(refreshIcon); else btnReload.setText("Reload");
        btnReload.setContentAreaFilled(false);
        btnReload.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        btnReload.setFocusPainted(false);
        btnReload.addActionListener(e -> {
            searchField.setText("");
            searchPopup.setVisible(false);
            showCard(currentMenu);
        });
        center.add(searchLbl);
        center.add(searchField);
        center.add(btnReload);
        panel.add(center, BorderLayout.CENTER);

        searchPopup = new JPopupMenu();
        searchPopup.setFocusable(false); // do not steal focus
        searchTimer = new javax.swing.Timer(220, e -> handleSearchChanged());
        searchTimer.setRepeats(false);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void schedule() { searchTimer.restart(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { schedule(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { schedule(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { schedule(); }
        });
        searchField.addKeyListener(new java.awt.event.KeyAdapter(){
            @Override public void keyPressed(java.awt.event.KeyEvent e){ if (e.getKeyCode()==java.awt.event.KeyEvent.VK_ESCAPE) searchPopup.setVisible(false); }
        });

        // Right: Login pill + refresh circle
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);
        GradientPillButton loginBtn = new GradientPillButton(_employee.getNameEmployee(), new Color(255, 128, 76), new Color(255, 78, 109));
        ImageIcon avatar = loadIcon("/icons/avatar.png", 18);
        if (avatar != null) loginBtn.setIcon(avatar);
        actions.add(loginBtn);
        // add event click
        loginBtn.addActionListener(ev -> {
            Window parent = SwingUtilities.getWindowAncestor(loginBtn);
            UserInfoForm dlg = new UserInfoForm(parent, _employee, loadIcon("/icons/avatar.png", 64));
            dlg.setLocationRelativeTo(parent);
            dlg.setVisible(true);
        });

        JButton logoutBtn = new JButton("Logout");
        ImageIcon logoutIcon = loadIcon("/icons/logout.png", 20);
        if (logoutIcon != null) logoutBtn.setIcon(logoutIcon);

        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setBorder(BorderFactory.createEmptyBorder());
        logoutBtn.setFocusPainted(false);

        actions.add(logoutBtn);

        logoutBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    null,
                    "Are you sure you want to logout?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                for (Window w : Window.getWindows()) {
                    w.dispose();
                }
                _employee = new Employee();
                SwingUtilities.invokeLater(() -> {
                    LoginForm login = new LoginForm();
                    login.setLocationRelativeTo(null);
                    login.setVisible(true);
                });
            }
        });

        panel.add(actions, BorderLayout.EAST);

        return panel;
    }

    private void handleSearchChanged() {
        String term = searchField.getText().trim();
        if (term.isEmpty()) { searchPopup.setVisible(false); return; }
        try {
            List<Product> results = repositories.ProductRepository.searchByName(term, 8);
            searchPopup.removeAll();
            for (Product p : results) {
                JMenuItem mi = new JMenuItem(p.getNameProduct());
                mi.setFocusable(false);
                mi.addActionListener(e -> {
                    searchField.setText(p.getNameProduct());
                    searchPopup.setVisible(false);
                    // Render only the selected product
                    renderProductsGrid(java.util.List.of(p));
                    breadcrumb.setPath(java.util.List.of("Search", p.getNameProduct()));
                    SwingUtilities.invokeLater(() -> searchField.requestFocusInWindow());
                });
                searchPopup.add(mi);
            }
            if (searchPopup.getComponentCount() > 0) {
                SwingUtilities.invokeLater(() -> {
                    searchPopup.show(searchField, 0, searchField.getHeight());
                    searchField.requestFocusInWindow();
                });
            } else {
                searchPopup.setVisible(false);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private JPanel buildTotalPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createDashedBorder(new Color(200, 200, 200), 2, 4));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("SubTotal:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        lblSubtotal = new JLabel(formatVND(subtotal));
        panel.add(lblSubtotal, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("VAT(5%):"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        lblVat = new JLabel(formatVND(BigDecimal.ZERO));
        lblVat.setForeground(new Color(230, 0, 64));
        panel.add(lblVat, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel total = new JLabel("Total:");
        total.setFont(total.getFont().deriveFont(Font.BOLD, 16f));
        panel.add(total, gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        lblTotal = new JLabel(formatVND(BigDecimal.ZERO));
        lblTotal.setForeground(new Color(230, 0, 64));
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 18f));
        panel.add(lblTotal, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(16, 8, 8, 8);
        JButton pay = new JButton("Pay now");
        ImageIcon payIcon = loadIcon("/icons/pay.png", 20);
        if (payIcon != null) pay.setIcon(payIcon);
        pay.setBackground(new Color(0, 160, 64));
        pay.setForeground(Color.WHITE);
        pay.setFocusPainted(false);
        pay.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        panel.add(pay, gbc);

        pay.addActionListener(e -> onPay());

        return panel;
    }

    private String formatVND(BigDecimal amount) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return nf.format(amount == null ? BigDecimal.ZERO : amount);
    }

    private void addOrIncrementItem(Product p) {
        OrderLine line = orderLines.get(p.getIdProduct());
        if (line == null) {
            line = new OrderLine(p);
            orderLines.put(p.getIdProduct(), line);
            orderItemsList.add(line.container);
            orderItemsList.add(line.separator);
        } else {
            line.increment();
        }
        orderItemsList.revalidate();
        orderItemsList.repaint();
        recalculateTotals();
    }

    private void recalculateTotals() {
        subtotal = BigDecimal.ZERO;
        for (OrderLine l : orderLines.values()) {
            subtotal = subtotal.add(l.getLineTotal());
        }
        BigDecimal vat = subtotal.multiply(new BigDecimal("0.05"));
        BigDecimal total = subtotal.add(vat);
        lblSubtotal.setText(formatVND(subtotal));
        lblVat.setText(formatVND(vat));
        lblTotal.setText(formatVND(total));
    }

    private class OrderLine {
        private final Product product;
        private int quantity = 1;
        private final JPanel container;
        private final JSeparator separator;
        private final JFormattedTextField lblQty;
        private final JLabel lblName;
        private final JLabel lblLineTotal;
        private final JLabel lblUnitPrice;
        private final JSpinner spinner;

        OrderLine(Product p) {
            this.product = p;
            container = new JPanel(new GridBagLayout());
            container.setOpaque(false);
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(6, 8, 6, 8);
            g.anchor = GridBagConstraints.WEST;
            g.fill = GridBagConstraints.NONE;

            // Thumbnail
            JLabel thumb = new JLabel();
            thumb.setPreferredSize(new Dimension(32, 32));
            thumb.setOpaque(true);
            thumb.setBackground(Color.BLACK);
            if (p.getImages() != null) {
                try {
                    Image img = ImageIO.read(new ByteArrayInputStream(p.getImages()));
                    if (img != null) thumb.setIcon(new ImageIcon(img.getScaledInstance(32, 32, Image.SCALE_SMOOTH)));
                } catch (IOException ignored) {
                }
            }
            g.gridx = 0;
            g.gridy = 0;
            g.weightx = 0;
            container.add(thumb, g);

            // Name + time
            JPanel info = new JPanel();
            info.setOpaque(false);
            info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
            lblName = new JLabel(p.getNameProduct());
            lblName.setFont(lblName.getFont().deriveFont(Font.BOLD));
            JLabel time = new JLabel(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a")));
            time.setForeground(new Color(120, 120, 120));
            time.setFont(time.getFont().deriveFont(11f));
            info.add(lblName);
            info.add(time);
            g.gridx = 1;
            g.weightx = 1.0;
            container.add(info, g);

            // Unit price (VND)
            lblUnitPrice = new JLabel(formatVND(p.getPriceProduct()));
            g.gridx = 2;
            g.weightx = 0;
            container.add(lblUnitPrice, g);

            // Qty spinner
            SpinnerNumberModel model = new SpinnerNumberModel(1, 1, 999, 1);
            spinner = new JSpinner(model);
            styleQtySpinner(spinner);
            lblQty = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
            lblQty.setText("1");
            g.gridx = 3;
            container.add(spinner, g);

            // Remove button
            JButton btnRemove = new JButton();
            ImageIcon trash = loadIcon("/icons/trash.png", 16);
            if (trash != null) btnRemove.setIcon(trash);
            else btnRemove.setText("x");
            btnRemove.setContentAreaFilled(false);
            btnRemove.setBorder(BorderFactory.createEmptyBorder());
            btnRemove.setFocusPainted(false);
            g.gridx = 4;
            container.add(btnRemove, g);

            // Line total (VND) right
            lblLineTotal = new JLabel(formatVND(p.getPriceProduct()));
            g.gridx = 5;
            g.anchor = GridBagConstraints.EAST;
            g.weightx = 0;
            container.add(lblLineTotal, g);

            separator = new JSeparator();
            separator.setForeground(new Color(200, 200, 200));

            // events
            spinner.addChangeListener(e -> {
                quantity = (int) model.getNumber();
                update();
            });
            btnRemove.addActionListener(e -> removeSelf());
        }

        void increment() {
            quantity++;
            update();
        }

        void update() {
            ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setText(String.valueOf(quantity));
            BigDecimal lineTotal = getLineTotal();
            lblLineTotal.setText(formatVND(lineTotal));
            recalculateTotals();
        }

        void removeSelf() {
            orderItemsList.remove(container);
            orderItemsList.remove(separator);
            orderLines.remove(product.getIdProduct());
            orderItemsList.revalidate();
            orderItemsList.repaint();
            recalculateTotals();
        }

        BigDecimal getLineTotal() {
            BigDecimal price = product.getPriceProduct() == null ? BigDecimal.ZERO : product.getPriceProduct();
            return price.multiply(new BigDecimal(quantity));
        }
    }

    private void styleQtySpinner(JSpinner spinner) {
        spinner.setPreferredSize(new Dimension(52, 24));
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor ed) {
            JTextField tf = ed.getTextField();
            tf.setHorizontalAlignment(SwingConstants.CENTER);
            tf.setBackground(new Color(246, 102, 102));
            tf.setForeground(Color.WHITE);
            tf.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        }
    }

    // Helpers
    private static class GradientPanel extends JPanel {
        private final Color start;
        private final Color end;

        GradientPanel(Color start, Color end) {
            this.start = start;
            this.end = end;
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, start, w, h, end));
            g2.fillRect(0, 0, w, h);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private ImageIcon loadIcon(String resourcePath, int size) {
        java.net.URL url = getClass().getResource(resourcePath);
        if (url == null) return null;
        ImageIcon raw = new ImageIcon(url);
        if (raw.getIconWidth() <= 0 || raw.getIconHeight() <= 0) return null;
        Image img = raw.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    // Simple gradient pill button for Login
    private static class GradientPillButton extends JButton {
        private final Color start;
        private final Color end;

        GradientPillButton(String text, Color start, Color end) {
            super(text);
            this.start = start;
            this.end = end;
            setForeground(Color.WHITE);
            setOpaque(false);
            setBorder(new EmptyBorder(8, 14, 8, 14));
            setFocusPainted(false);
            setIconTextGap(8);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int arc = h;
            g2.setPaint(new GradientPaint(0, 0, start, w, h, end));
            g2.fillRoundRect(0, 0, w, h, arc, arc);
            super.paintComponent(g);
            g2.dispose();
        }

        @Override
        public boolean isContentAreaFilled() {
            return false;
        }
    }

    // Vertical text label UI
    private static class VerticalLabelUI extends javax.swing.plaf.basic.BasicLabelUI {
        final boolean clockwise;

        VerticalLabelUI(boolean clockwise) {
            this.clockwise = clockwise;
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            Dimension d = super.getPreferredSize(c);
            return new Dimension(d.height, d.width);
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g.create();
            if (clockwise) {
                g2.rotate(Math.PI / 2);
                g2.translate(0, -c.getWidth());
            } else {
                g2.rotate(-Math.PI / 2);
                g2.translate(-c.getHeight(), 0);
            }
            super.paint(g2, c);
            g2.dispose();
        }
    }

    private void onPay() {
        if (orderLines.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Order is empty.");
            return;
        }
        int totalQty = orderLines.values().stream().mapToInt(l -> l.quantity).sum();
        BigDecimal vat = subtotal.multiply(new BigDecimal("0.05"));
        BigDecimal total = subtotal.add(vat);

        // Persist order
        try {
            repositories.OrderRepository repo = new repositories.OrderRepository();
            Map<Integer, Integer> m = new java.util.HashMap<>();
            for (Map.Entry<Integer, OrderLine> e : orderLines.entrySet()) m.put(e.getKey(), e.getValue().quantity);
            int orderId = repo.createOrder(_employee.getIdEmployee(), totalQty, total, m);
            showInvoicePreview(orderId, vat, total);
            // reset
            orderItemsList.removeAll();
            orderLines.clear();
            recalculateTotals();
            orderItemsList.revalidate();
            orderItemsList.repaint();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Payment failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showInvoicePreview(int orderId, BigDecimal vat, BigDecimal total) {
        java.util.List<LineItem> items = new java.util.ArrayList<>();
        for (OrderLine l : orderLines.values()) {
            items.add(new LineItem(l.product.getNameProduct(), l.quantity, l.product.getPriceProduct()));
        }
        InvoicePanel panel = new InvoicePanel(_employee.getNameEmployee(), items, subtotal, vat, total);
        panel.setPreferredSize(new Dimension(800, 1100));
        JDialog dlg = new JDialog(this, "Print preview", true);
        dlg.setLayout(new BorderLayout());
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);
        JButton btnPrint = new JButton("Print");
        JButton btnClose = new JButton("Close");
        tb.add(btnPrint);
        tb.add(Box.createHorizontalStrut(8));
        tb.add(btnClose);
        dlg.add(tb, BorderLayout.NORTH);
        dlg.add(new JScrollPane(panel), BorderLayout.CENTER);
        btnClose.addActionListener(e -> dlg.dispose());
        btnPrint.addActionListener(e -> {
            java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
            job.setJobName("Invoice #" + orderId);
            job.setPrintable((graphics, pageFormat, pageIndex) -> {
                if (pageIndex > 0) return java.awt.print.Printable.NO_SUCH_PAGE;
                Graphics2D g2 = (Graphics2D) graphics;
                g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                double sx = pageFormat.getImageableWidth() / panel.getWidth();
                double sy = pageFormat.getImageableHeight() / panel.getHeight();
                double scale = Math.min(sx, sy);
                g2.scale(scale, scale);
                panel.printAll(g2);
                return java.awt.print.Printable.PAGE_EXISTS;
            });
            try {
                if (job.printDialog()) job.print();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private static class LineItem {
        final String name;
        final int qty;
        final BigDecimal price;

        LineItem(String n, int q, BigDecimal p) {
            this.name = n;
            this.qty = q;
            this.price = p == null ? BigDecimal.ZERO : p;
        }
    }

    private class InvoicePanel extends JPanel {
        private final String employeeName;
        private final java.util.List<LineItem> items;
        private final BigDecimal subtotal, vat, total;

        InvoicePanel(String employeeName, java.util.List<LineItem> items, BigDecimal subtotal, BigDecimal vat, BigDecimal total) {
            this.employeeName = employeeName;
            this.items = items;
            this.subtotal = subtotal;
            this.vat = vat;
            this.total = total;
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int margin = 40;
            int x = margin;
            int y = margin;
            int width = getWidth() - margin * 2;
            // Title
            g2.setFont(new Font("Arial", Font.BOLD, 28));
            String title = "HS FOOD";
            int tw = g2.getFontMetrics().stringWidth(title);
            g2.drawString(title, x + (width - tw) / 2, y + 30);
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            g2.drawString("Invoice", x + (width - g2.getFontMetrics().stringWidth("Invoice")) / 2, y + 60);
            g2.setFont(new Font("Arial", Font.PLAIN, 13));
            g2.drawString("Phone: +84347877704", x + 10, y + 90);
            g2.drawString("Email: hs-food@gmail.com", x + 10, y + 115);
            g2.drawString("Address: 475 Dien Bien Phu, Binh Thanh District, HCM City", x + 10, y + 140);
            g2.drawLine(x, y + 160, x + width, y + 160);
            g2.drawString("Date: " + java.time.LocalDateTime.now(), x + 10, y + 185);
            g2.drawString("Employee: " + (employeeName == null ? "" : employeeName), x + 10, y + 210);
            // table header
            int tableTop = y + 240;
            g2.drawString("Item", x + 10, tableTop);
            g2.drawString("Qty", x + 220, tableTop);
            g2.drawString("Price", x + 340, tableTop);
            g2.drawString("Amount", x + 520, tableTop);
            g2.drawLine(x + 10, tableTop + 10, x + width - 10, tableTop + 10);
            int rowY = tableTop + 30;
            for (LineItem it : items) {
                g2.drawString(it.name, x + 10, rowY);
                g2.drawString(String.valueOf(it.qty), x + 220, rowY);
                g2.drawString(formatVND(it.price), x + 340, rowY);
                g2.drawString(formatVND(it.price.multiply(new BigDecimal(it.qty))), x + 520, rowY);
                rowY += 24;
            }
            g2.drawLine(x + 10, rowY + 10, x + width - 10, rowY + 10);
            // totals
            rowY += 40;
            g2.drawString("Bill total", x + 10, rowY);
            g2.drawString(formatVND(subtotal), x + 520, rowY);
            rowY += 24;
            g2.drawString("VAT(5%)", x + 10, rowY);
            g2.drawString("+ " + formatVND(vat), x + 520, rowY);
            rowY += 24;
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.drawString("Last bill", x + 10, rowY);
            g2.setFont(new Font("Arial", Font.PLAIN, 13));
            g2.drawString(formatVND(total), x + 520, rowY);
            g2.dispose();
        }
    }
}
