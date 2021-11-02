package com.davixdevelop.btemover.view.style;

import javax.swing.border.AbstractBorder;
import java.awt.*;

/**
 * Represents an rounder corner border with the supplied radius and background color
 *
 * @author DavixDevelop
 */
public class RoundedBorder extends AbstractBorder {
    private final int rad;
    private final Color backgroundColor;

    public RoundedBorder(int _rad, Color _backgroundColor){
        rad = _rad;
        backgroundColor = _backgroundColor;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        super.paintBorder(c, g, x, y, width, height);
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(2));
        g2.setColor(backgroundColor);
        //g2.setBackground(backgroundColor);
        g2.drawRoundRect(x, y, width - 1, height - 1, rad, rad);
    }
}
