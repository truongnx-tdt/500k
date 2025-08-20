package UI;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

public class ModernScrollBarUI extends BasicScrollBarUI {
    private final Color trackColor = new Color(245,245,245);
    private final Color thumbColor = new Color(180, 200, 255);
    private final Color thumbHover = new Color(150, 180, 255);

    @Override
    protected void configureScrollBarColors() {
        thumbDarkShadowColor = thumbColor;
        thumbHighlightColor  = thumbColor;
        thumbLightShadowColor= thumbColor;
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return zeroButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return zeroButton();
    }

    private JButton zeroButton() {
        JButton b = new JButton();
        b.setPreferredSize(new Dimension(0, 0));
        b.setMinimumSize(new Dimension(0, 0));
        b.setMaximumSize(new Dimension(0, 0));
        b.setBorder(null);
        b.setFocusable(false);
        b.setOpaque(false);
        return b;
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(trackColor);
        g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        g2.dispose();
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
        if (!c.isEnabled() || r.width <= 0 || r.height <= 0) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // hover/drag
        boolean hovering = isThumbRollover() || scrollbar.getModel().getValueIsAdjusting();
        g2.setColor(hovering ? thumbHover : thumbColor);

        int arc = 10;
        g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, arc, arc);
        g2.dispose();
    }

    @Override
    protected Dimension getMinimumThumbSize() {
        // để dễ kéo khi list dài
        return new Dimension(8, 40);
    }
}
