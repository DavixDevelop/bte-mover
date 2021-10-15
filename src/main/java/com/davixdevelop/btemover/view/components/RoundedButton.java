package com.davixdevelop.btemover.view.components;

import com.davixdevelop.btemover.view.UIVars;

import javax.swing.*;
import java.awt.*;

/**
 * Represents an rounder corner button based on the JButton. It also offers and alternative color mode
 *
 * @author DavixDevelop
 */
public class RoundedButton extends JButton {

    private int rad;
    private Color backgroundColor;

    private boolean isAlternative = false;

    /**
     * Creates a rounded button with a solid background
     * @param text The text in the button
     */
    public RoundedButton(String text){
        super(text);
        setFont(UIVars.RobotoLight.deriveFont(UIVars.primaryFontSize));
        setContentAreaFilled(false);
        rad = UIVars.buttonRadius;
        backgroundColor = UIVars.secondaryBgDarkenedColor;
        setMargin( new Insets(UIVars.buttonInsets[0], UIVars.buttonInsets[1], UIVars.buttonInsets[2], UIVars.buttonInsets[3]));
    }

    public void setAlternative(boolean _val){
        isAlternative = true;
        backgroundColor = UIVars.alternativeBgColor;
    }

    @Override
    protected void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if(getModel().isArmed()){
            g2.setColor(backgroundColor.darker());
            setForeground(Color.white);
        } else if(!getModel().isEnabled()){
            g2.setColor(UIVars.disabledBgColor);
            setForeground(UIVars.disabledTextColor);
        }
        else {
            g2.setColor(backgroundColor);
            setForeground(Color.black);
        }


        g2.fillRoundRect(0, 0, getWidth(), getHeight(), rad, rad);

        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(0));
        if (getModel().isArmed()) {
            g2.setColor(backgroundColor.darker());
        } else {
            g2.setColor(backgroundColor);
        }
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, rad, rad);
    }

}
