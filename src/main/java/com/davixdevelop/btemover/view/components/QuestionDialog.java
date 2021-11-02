package com.davixdevelop.btemover.view.components;

import com.davixdevelop.btemover.utils.UIUtils;
import com.davixdevelop.btemover.view.UIVars;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class QuestionDialog extends JDialog {

    private final JOptionPane optionPane;

    public QuestionDialog(Frame frame, String title, String question){
        super(frame, true);
        setPreferredSize(new Dimension(390, 170));

        RoundedButton yesButton = new RoundedButton("YES");
        yesButton.setAlternative();

        RoundedButton noButton = new RoundedButton("NO");

        Object[] content = {title, question};
        Object[] options = {yesButton, noButton};

        optionPane = new JOptionPane(content, JOptionPane.INFORMATION_MESSAGE, JOptionPane.YES_NO_OPTION, FTPDialog.questionIcon, options, options[0]);
        optionPane.setFont(UIVars.RobotoLight.deriveFont(UIVars.primaryFontSize));
        optionPane.setBackground(UIVars.primaryColor);

        UIUtils.changeComponentsBackground(optionPane, UIVars.primaryColor);

        setContentPane(optionPane);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        yesButton.addActionListener(e -> {
            optionPane.setValue(new Integer(JOptionPane.YES_OPTION));
            setVisible(false);
        });
        noButton.addActionListener(e -> {
            optionPane.setValue(new Integer(JOptionPane.NO_OPTION));
            setVisible(false);
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                optionPane.setValue(new Integer(JOptionPane.NO_OPTION));
                setVisible(false);
            }
        });
    }

    public JOptionPane getOptionPane() {
        return optionPane;
    }
}
