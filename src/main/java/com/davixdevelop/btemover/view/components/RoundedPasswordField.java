package com.davixdevelop.btemover.view.components;

import com.davixdevelop.btemover.view.UIVars;
import com.davixdevelop.btemover.view.style.RoundedBorder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Represents an simple rounder corner password filed
 *
 * @author DavixDevelop
 */
public class RoundedPasswordField extends JPasswordField {
    private final int rad;
    private final int[] insets;
    private Color borderColor;

    private Shape shape;

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        updateBorder();
    }

    public RoundedPasswordField(){
        super();
        insets = UIVars.textFieldInsets;
        setOpaque(false);
        rad = UIVars.textFieldRadius;
        setBackground(Color.white);
        setForeground(Color.black);
        setBorderColor(Color.white);
    }


    public void updateBorder(){
        setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(rad, borderColor), new EmptyBorder(insets[0], insets[1], insets[2], insets[3])));
    }

    protected void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRoundRect(0,0,getWidth() - 1, getHeight() - 1, rad, rad);
        super.paintComponent(g);
    }

    @Override
    public boolean contains(int x, int y) {
        if (shape == null || !shape.getBounds().equals(getBounds())){
            shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, rad, rad);
        }
        return shape.contains(x, y);
    }
}
