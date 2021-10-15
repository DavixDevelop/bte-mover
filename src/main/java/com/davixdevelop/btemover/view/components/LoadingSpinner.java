package com.davixdevelop.btemover.view.components;

import com.davixdevelop.btemover.view.UIVars;

import javax.swing.*;
import java.awt.*;

/**
 * Represent the glass pane content of the main view.
 * It displays a spinner if visible
 *
 * @author DavixDevelop
 */
public class LoadingSpinner extends JComponent {
    final static ImageIcon spinnerIcon =  new ImageIcon(LoadingSpinner.class.getResource("spinner.gif"));
    public LoadingSpinner(){
        super();
        setLayout(new BorderLayout());
        JLabel spinnerLabel = new JLabel("", spinnerIcon, JLabel.CENTER);
        spinnerLabel.setOpaque(false);
        add(spinnerLabel);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(UIVars.semiTransparentColor);
        g.fillRect(0,0,getWidth(), getHeight());
        super.paintComponent(g);
    }
}
