package components;

import javax.swing.*;
import java.awt.*;

public class SoldOutStripe extends JComponent {
    public SoldOutStripe() { setOpaque(false); }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(255, 255, 255, 190)); // trắng bán trong suốt
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(Color.BLACK);
        g2.setFont(getFont().deriveFont(Font.BOLD, 12f));
        String s = "Sold out";
        FontMetrics fm = g2.getFontMetrics();
        int tx = (getWidth() - fm.stringWidth(s)) / 2;
        int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(s, tx, ty);
        g2.dispose();
    }
}
