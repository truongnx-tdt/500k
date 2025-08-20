package components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

public class BreadcrumbBar extends JPanel {
    private final Consumer<Integer> onClick; // click vào segment i
    public BreadcrumbBar(Consumer<Integer> onClick) {
        super(new FlowLayout(FlowLayout.LEFT, 6, 6));
        this.onClick = onClick;
        setOpaque(true);
        setBackground(Color.WHITE);
    }

    public void setPath(List<String> segments) {
        removeAll();
        if (segments == null || segments.isEmpty()) {
            add(makeLabel("Home", true)); // fallback
        } else {
            for (int i = 0; i < segments.size(); i++) {
                boolean isLast = (i == segments.size() - 1);
                String seg = segments.get(i);

                JLabel lbl = makeLabel(seg, isLast);
                if (!isLast && onClick != null) {
                    final int idx = i;
                    lbl.setForeground(new Color(33, 111, 219));
                    lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    lbl.addMouseListener(new MouseAdapter() {
                        @Override public void mouseClicked(MouseEvent e) { onClick.accept(idx); }
                        @Override public void mouseEntered(MouseEvent e) { lbl.setText("<html><u>"+seg+"</u></html>"); }
                        @Override public void mouseExited (MouseEvent e) { lbl.setText(seg); }
                    });
                }
                add(lbl);

                if (!isLast) add(new JLabel(">"));
            }
        }
        revalidate();
        repaint();
    }

    private JLabel makeLabel(String text, boolean isLast) {
        JLabel l = new JLabel(text);
        Font base = l.getFont();
        l.setFont(isLast ? base.deriveFont(Font.BOLD) : base);
        l.setForeground(isLast ? new Color(60,60,60) : new Color(90,90,90));
        return l;
    }
}
