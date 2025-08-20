package components;

import javax.swing.*;
import java.awt.*;

public class PriceBadge extends JComponent {
    private final String text;
    public PriceBadge(String text) {
        this.text = text;
        setOpaque(false);
        // Gắn font mặc định để không bao giờ null
        Font base = UIManager.getFont("Label.font");
        if (base == null) base = new Font("SansSerif", Font.PLAIN, 12);
        setFont(base.deriveFont(Font.BOLD, 11f));
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight(), arc = 12;

        GradientPaint gp = new GradientPaint(0, 0, new Color(255,128,150),
                w, 0, new Color(255,128,76));
        g2.setPaint(gp);
        g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);

        g2.setColor(new Color(255,120,120));
        g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

        // Vẽ chữ bằng font đã set (không null)
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics(getFont());
        int tx = (w - fm.stringWidth(text)) / 2;
        int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
        g2.setColor(Color.WHITE);
        g2.drawString(text, tx, ty);

        g2.dispose();
    }
}
