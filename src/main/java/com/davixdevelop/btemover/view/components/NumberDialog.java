package com.davixdevelop.btemover.view.components;

import com.davixdevelop.btemover.utils.UIUtils;
import com.davixdevelop.btemover.view.UIVars;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberDialog extends JDialog {
    private int numberValue;

    public int getNumberValue() {
        return numberValue;
    }

    private RoundedButton okButton;
    private RoundedTextField numberField;
    private JOptionPane optionPane;

    private Pattern numberValidator = Pattern.compile("^(\\d+)", Pattern.CASE_INSENSITIVE);

    public NumberDialog(Frame frame, String label, String placeholder){
        super(frame, true);
        setPreferredSize(new Dimension(390, 170));

        numberField = new RoundedTextField();
        numberField.setFont(UIVars.RobotoRegular.deriveFont(UIVars.primaryFontSize));
        numberField.setPlaceholder(placeholder);

        okButton = new RoundedButton("OK");
        okButton.setEnabled(false);

        Object[] content = {label, numberField};
        Object[] options = {okButton};

        optionPane = new JOptionPane(content, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_OPTION, FTPDialog.questionIcon, options, options[0]);
        optionPane.setFont(UIVars.RobotoLight.deriveFont(UIVars.primaryFontSize));
        optionPane.setBackground(UIVars.primaryColor);

        UIUtils.changeComponentsBackground(optionPane, UIVars.primaryColor);

        setContentPane(optionPane);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        okButton.addActionListener(e -> {
            optionPane.setValue(new Integer(JOptionPane.OK_OPTION));
            setVisible(false);
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                optionPane.setValue(new Integer(JOptionPane.UNDEFINED_CONDITION));
            }
        });

        UIUtils.addTextChangeListener(numberField, e -> {
            Matcher matcher = numberValidator.matcher(numberField.getText());
            if(matcher.find()){
                MatchResult result = matcher.toMatchResult();
                numberValue = Integer.valueOf(result.group(1));
                numberField.setBorderColor(Color.white);
                okButton.setEnabled(true);
            }else{
                numberField.setBorderColor(UIVars.errorColor);
                okButton.setEnabled(false);
            }
        });
    }

    public JOptionPane getOptionPane() {
        return optionPane;
    }
}
