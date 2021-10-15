package com.davixdevelop.btemover.view.components;

import com.davixdevelop.btemover.view.UIVars;
import com.davixdevelop.btemover.view.style.RegionListRenderer;

import javax.swing.*;
import java.awt.*;

/**
 * Represents a circle button with a icon based on the JButton,
 * that can be optionally toggled
 *
 * @author DavixDevelop
 */
public class CircleButton extends JButton {
    public static ImageIcon SAVE_ICON = new ImageIcon(CircleButton.class.getResource("save.png"));
    public static ImageIcon OSM_ICON = new ImageIcon(CircleButton.class.getResource("osm.png"));
    public static ImageIcon EXPAND_ICON = new ImageIcon(CircleButton.class.getResource("expand.png"));


    private int rad;
    private Color backgroundColor;
    private ImageIcon icon;
    private boolean isToggleButton;
    private boolean toggledOn;


    public boolean isToggledOn() {
        return toggledOn;
    }

    /**
     * Set's the toggle mode
     * @param toggle
     */
    public void setToggledOn(boolean toggle) {
        this.toggledOn = toggle;
        repaint();
    }

    /**
     * Creates a circle button with a solid background
     */
    public CircleButton(ImageIcon _icon, boolean _isToggleButton){
        super("");
        icon = _icon;
        setPreferredSize(new Dimension(icon.getIconWidth() + UIVars.circleButtonInsets[1] + UIVars.circleButtonInsets[3], icon.getIconHeight() + UIVars.circleButtonInsets[0] + UIVars.circleButtonInsets[2]));
        isToggleButton = _isToggleButton;
        setFont(UIVars.RobotoLight.deriveFont(UIVars.primaryFontSize));
        setContentAreaFilled(false);
        rad = 50;
        backgroundColor = UIVars.circleButtonBgColor;
        //setBackground(UIVars.secondaryBgDarkenedColor);

        setMargin( new Insets(UIVars.circleButtonInsets[0], UIVars.circleButtonInsets[1], UIVars.circleButtonInsets[2], UIVars.circleButtonInsets[3]));
    }

    @Override
    protected void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if(!isToggleButton) {
            if(getModel().isArmed()){
                g2.setColor(backgroundColor.darker());
                setForeground(Color.white);
            } else if(!getModel().isEnabled()){
                g2.setColor(UIVars.circleButtonDisabledColor);
                setForeground(UIVars.disabledTextColor);
            }else {
                g2.setColor(backgroundColor);
                setForeground(Color.black);
            }

        }else{
            if(getModel().isArmed()){
                g2.setColor(backgroundColor.darker());
            }else{
                if(isToggledOn()) {
                    g2.setColor(backgroundColor.darker().darker());
                    setForeground(Color.black);
                }else{
                    g2.setColor(backgroundColor);
                    setForeground(Color.black);
                }
            }
        }


        g2.fillRoundRect(0, 0, getWidth(), getHeight(), rad, rad);

        g2.drawImage(icon.getImage(), UIVars.circleButtonInsets[1], getHeight() / 2 - icon.getIconHeight() / 2, null);

        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(2));
        if(!isToggleButton) {
            if (getModel().isArmed()) {
                g2.setColor(backgroundColor.darker());
            } else if(!getModel().isEnabled()){
                g2.setColor(UIVars.circleButtonDisabledColor);
            }else {
                g2.setColor(backgroundColor);
            }
        }else{
            if (getModel().isArmed()) {
                g2.setColor(backgroundColor.darker());
            }else if(!getModel().isEnabled()){
                g2.setColor(UIVars.circleButtonDisabledColor);
            } else {
                if(isToggledOn())
                    g2.setColor(backgroundColor.darker().darker());
                else
                    g2.setColor(backgroundColor);
            }
        }
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, rad, rad);
    }
}
