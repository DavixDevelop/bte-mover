package com.davixdevelop.btemover.view.components;

import com.davixdevelop.btemover.view.style.RoundedInsetBorder;

import javax.swing.*;
import java.awt.*;

public class ToggleableRoundedLabel extends JLabel {
    private final int radius;
    private final Color backgroundColor;
    private boolean _isToggled;
    public boolean isToggled() {
        return _isToggled;
    }
    public void setToggled(boolean isToggled) {
        this._isToggled = isToggled;
        setToolTipText("Layer visibility [" + ((isToggled) ? "VISIBLE" : "INVISIBLE") + "]");
        setBorder(new RoundedInsetBorder(radius, new int[]{0,0,0,0}, (_isToggled) ? backgroundColor : backgroundColor.darker()));
        revalidate();
    }

    public ToggleableRoundedLabel(int radius, Color backgroundColor){
        super();
        this.radius = radius;
        this.backgroundColor = backgroundColor;
        setBorder(new RoundedInsetBorder(radius, new int[]{0,0,0,0}, backgroundColor));

        setToggled(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if(_isToggled)
            g2.setColor(backgroundColor);
        else
            g2.setColor(backgroundColor.darker());

        g2.fillRoundRect(0,0,getWidth(), getHeight(), radius, radius);
        super.paintComponent(g2);
    }
}
