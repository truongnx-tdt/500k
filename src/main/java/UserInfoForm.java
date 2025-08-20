import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import models.Employee;
import models.RoleEmployee;

public class UserInfoForm extends JDialog {
    private final Employee employee;
    private final ImageIcon avatar;

    public UserInfoForm(Window owner, Employee employee, ImageIcon avatar64) {
        super(owner, "User Info", ModalityType.APPLICATION_MODAL);
        this.employee = employee;
        this.avatar = avatar64;
        buildUI();
        pack();
        setMinimumSize(new Dimension(360, getHeight()));
    }

    private void buildUI() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBorder(new EmptyBorder(16,16,16,16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6,6,6,6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Avatar + Tên
        JLabel pic = new JLabel(avatar != null ? avatar : UIManager.getIcon("OptionPane.informationIcon"));
        pic.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel name = new JLabel(notNull(employee.getNameEmployee()));
        name.setFont(name.getFont().deriveFont(Font.BOLD, 18f));

        JPanel header = new JPanel(new BorderLayout(12, 12));
        header.add(pic, BorderLayout.WEST);
        header.add(name, BorderLayout.CENTER);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        root.add(header, gbc);

        // Hàng helper
        gbc.gridwidth = 1;

        addRow(root, gbc, 1, "ID",         str(employee.getIdEmployee()));
        addRow(root, gbc, 2, "Email",      notNull(employee.getEmail()));
        addRow(root, gbc, 3, "Address",    notNull(employee.getAddress()));
        addRow(root, gbc, 4, "Gender",  genderText(employee.getGender()));
        addRow(root, gbc, 5, "Birthday",  dobText(employee));
        addRow(root, gbc, 6, "Role",    roleText(employee.getRoleEmployee()));

        // nút đóng
        JButton close = new JButton("Close");
        close.addActionListener(e -> dispose());
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE;
        root.add(close, gbc);

        setContentPane(root);
    }

    private static void addRow(JPanel root, GridBagConstraints gbc, int row, String label, String value) {
        JLabel l = new JLabel(label + ":");
        l.setForeground(new Color(90,90,90));
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.WEST;
        root.add(l, gbc);

        JTextField v = new JTextField(value);
        v.setEditable(false);
        v.setBorder(BorderFactory.createEmptyBorder(2,4,2,4));
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        root.add(v, gbc);
    }

    private static String roleText(RoleEmployee r) {
        if (r == null || r.getIdRole() == null) return "—";
        return switch (r.getIdRole()) {
            case 1 -> "Admin";
            case 2 -> "Employee";
            default -> "Other (" + r.getIdRole() + ")";
        };
    }

    private static String genderText(Boolean g) {
        if (g == null) return "—";
        return g ? "Male" : "Female";
    }

    private static String dobText(Employee e) {
        if (e.getBirthDay() == null) return "—";
        return e.getBirthDay().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private static String str(Object o) { return o == null ? "—" : String.valueOf(o); }
    private static String notNull(String s) { return s == null ? "—" : s; }
}
