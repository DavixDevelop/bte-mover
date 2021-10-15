package com.davixdevelop.btemover.view.components;

import com.davixdevelop.btemover.view.UIVars;
import com.davixdevelop.btemover.view.style.RoundedBorder;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Represents an simple rounded corner text field
 *
 * @author DavixDevelop
 */
public class RoundedTextField extends JTextField {

    private int rad;
    private int[] insets;
    private Color borderColor;
    private String placeholder;

    private Shape shape;

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        updateBorder();
    }

    public void setPlaceholder(String _placeholder) {
        placeholder = _placeholder;
    }

    public RoundedTextField(){
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

        if(placeholder == null || placeholder.length() == 0 || getText().length() > 0)
            return;

        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(UIVars.disabledTextColor2);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.drawString(placeholder, getInsets().left, getHeight() / 2 + (g.getFontMetrics().getAscent() - g.getFontMetrics().getLeading() - g.getFontMetrics().getDescent()) / 2);
    }

    /*
    protected void paintBorder(Graphics g){
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(0));
        g2.setColor(getBackground());
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, rad, rad);
    }*/

    @Override
    public boolean contains(int x, int y) {
        if (shape == null || !shape.getBounds().equals(getBounds())){
            shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, rad, rad);
        }
        return shape.contains(x, y);
    }


}
