package com.davixdevelop.btemover.view.components;

import com.davixdevelop.btemover.utils.LogUtils;
import com.davixdevelop.btemover.utils.UIUtils;
import com.davixdevelop.btemover.view.UIVars;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Represn't a message dialog, that display's one ore more messages
 * with a button to view all the exceptions that occurred up to that point
 *
 * @author DavixDevelop
 */
public class MessageDialog extends JDialog {

    private final JOptionPane optionPane;

    private final JScrollPane logScroll;

    public MessageDialog(Frame _frame, String[] message){
        super(_frame, true);
        setPreferredSize(new Dimension(390, 170));

        RoundedButton okButton = new RoundedButton("OK");
        okButton.setAlternative();

        JPanel logPanel = new JPanel();
        logPanel.setLayout(new GridBagLayout());


        RoundedButton expandButton = new RoundedButton("Show log");
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.weightx = 0.0;
        c.insets = new Insets(10, 0, 5, 0);
        logPanel.add(expandButton, c);

        JTextArea logText = new JTextArea(30,30);
        logText.setEditable(false);
        logText.setText(LogUtils.messageLog);
        logText.setFont(UIVars.RobotoRegular.deriveFont(UIVars.primaryFontSize));
        logText.setLineWrap(true);
        logScroll = new JScrollPane(logText);
        logScroll.setBackground(Color.white);
        logScroll.setVisible(false);

        JPanel pushPanel = new JPanel();
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.FIRST_LINE_END;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        logPanel.add(pushPanel, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.insets = new Insets(10, 0, 5, 0);
        logPanel.add(logScroll, c);


        Object[] content;
        if(message.length == 2){
            JLabel headerLabel = new JLabel(message[0], JLabel.CENTER);
            headerLabel.setFont(UIVars.RobotoBold.deriveFont(UIVars.primaryFontSize));
            content = new Object[] {headerLabel, message[1], logPanel};
        }else{
            content = Stream.concat(Arrays.stream(message), Arrays.stream(new Object[]{logPanel})).toArray();
            //content = Arrays.stream(message).toArray();
        }


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

        expandButton.addActionListener(e -> {
            logScroll.setVisible(!logScroll.isVisible());
            optionPane.revalidate();
            UIUtils.changeComponentsBackground(optionPane, UIVars.primaryColor);
            //getContentPane().repaint();

        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                optionPane.setValue(new Integer(JOptionPane.UNDEFINED_CONDITION));
            }
        });

    }

    public JOptionPane getOptionPane() {
        return optionPane;
    }
}
