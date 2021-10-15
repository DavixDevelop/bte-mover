package com.davixdevelop.btemover.view.style;

import javax.swing.border.AbstractBorder;
import java.awt.*;

/**
 * Represents an rounder corner border with the supplied radius and background color, that has in inset
 * based on the supplied insets.
 *
 * @author DavixDevelop
 */
public class RoundedInsetBorder extends AbstractBorder {

    private int rad;
    private int[] insets;
    private Color backgroundColor;

    public RoundedInsetBorder(int _rad, int[] _insets, Color _backgroundColor){
        rad = _rad;
        insets = _insets;
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

    @Override
    public Insets getBorderInsets(Component c) {
        return (getBorderInsets(c, new Insets(insets[0], insets[1], insets[2], insets[3])));
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }
}
