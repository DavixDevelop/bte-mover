package com.davixdevelop.btemover.view.components;

import com.davixdevelop.btemover.model.FTPOptions;
import com.davixdevelop.btemover.model.RegionFTPClient;
import com.davixdevelop.btemover.utils.UIUtils;
import com.davixdevelop.btemover.view.UIVars;
import org.apache.commons.validator.routines.UrlValidator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represent and and dialog where the user can enter the details for a ftp connection
 * After the user enters the details, it checks if a connection can be established, closes the dialog,
 * and returns the FTPOptions
 *
 * @author DavixDevelop
 */
public class FTPDialog extends JDialog {
    public static final ImageIcon questionIcon = new ImageIcon(FTPDialog.class.getResource("question.png"));
    public static final ImageIcon successIcon = new ImageIcon(FTPDialog.class.getResource("success.png"));
    public static final ImageIcon failIcon = new ImageIcon(FTPDialog.class.getResource("fail.png"));

    private FTPOptions ftpOptions = new FTPOptions(null,null,0,null, null, null);

    public FTPOptions getFtpOptions() {
        return ftpOptions;
    }

    private final RoundedTextField serverTextField;
    private final RoundedTextField userTextField;
    private final RoundedPasswordField passwordTextField;
    private final JButton okButton;
    private final RoundedButton testButton;

    private boolean serverFieldValid, usernameFieldValid, passwordFieldValid = false;

    private final JOptionPane optionPane;

    public JOptionPane getOptionPane() {
        return optionPane;
    }

    private final UrlValidator urlValidator = new UrlValidator(new String[]{"ftp", "ftps", "sftp", "ftpes"});
    //private Pattern serverAddressValidator = Pattern.compile("ftp://([^-]*):(\\d+)[/]?(.+)?", Pattern.CASE_INSENSITIVE);
    private final Pattern serverAddressValidator = Pattern.compile("^(ftp|sftp|ftps|ftpes)://([^\\s]*):(\\d+)[/]?(.+)?", Pattern.CASE_INSENSITIVE);

    public FTPDialog(Frame _frame, FTPOptions _inOptions){
        super(_frame, true);

        setPreferredSize(new Dimension(700, 390));

        serverTextField = new RoundedTextField();
        serverTextField.setFont(UIVars.RobotoRegular.deriveFont(UIVars.primaryFontSize));
        serverTextField.setPlaceholder("Ex: (ftp|sftp|ftps)://ftp.davixdevelop.com:21/terraworld");
        userTextField = new RoundedTextField();
        userTextField.setFont(UIVars.RobotoRegular.deriveFont(UIVars.primaryFontSize));
        passwordTextField = new RoundedPasswordField();
        passwordTextField.setFont(UIVars.RobotoRegular.deriveFont(UIVars.primaryFontSize));
        passwordTextField.setEchoChar('\u2022');

        okButton = new RoundedButton("OK");
        testButton = new RoundedButton("Test");
        testButton.setAlternative();

        ImageIcon paneIcon;

        if(_inOptions == null) {
            okButton.setEnabled(false);
            testButton.setEnabled(false);
            paneIcon = FTPDialog.questionIcon;
        }
        else {
            ftpOptions = _inOptions;
            serverTextField.setText(ftpOptions.getProtocol() + "://" + ftpOptions.getServer() + ":" + ftpOptions.getPort() +
                    ((ftpOptions.getPath() != null) ? (ftpOptions.getPath().length() != 0) ? "/" + ftpOptions.getPath() : "" : ""));
            serverFieldValid = true;
            userTextField.setText(ftpOptions.getUser());
            usernameFieldValid = true;
            passwordTextField.setText(ftpOptions.getPassword());
            passwordFieldValid = true;

            //Set the pane icon to success or fail, depending if the server is accessible
            if(testConnection()){
                paneIcon = FTPDialog.successIcon;
            }else{
                paneIcon = FTPDialog.failIcon;
            }

        }

        String serverLabel = "Server (With port):";
        String userLabel = "Username:";
        String passwordLabel = "Password:";
        Object[] content = {serverLabel, serverTextField, userLabel, userTextField, passwordLabel, passwordTextField};
         Object[] options = {testButton,okButton};

         optionPane = new JOptionPane(content, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, paneIcon, options, options[0]);
         optionPane.setFont(UIVars.RobotoLight.deriveFont(UIVars.primaryFontSize));
         optionPane.setBackground(UIVars.primaryColor);

         UIUtils.changeComponentsBackground(optionPane, UIVars.primaryColor);

         setContentPane(optionPane);
         setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
         okButton.addActionListener(e -> {
             if(testConnection()){
                 optionPane.setValue(new Integer(JOptionPane.OK_OPTION));
                 setVisible(false);
             }else{
                 optionPane.setIcon(FTPDialog.failIcon);
                 //The background of components changes back to default, so we have to set it back to primaryColor
                 UIUtils.changeComponentsBackground(optionPane, UIVars.primaryColor);
             }

         });
         testButton.addActionListener(e -> {
             if(testConnection()){
                 optionPane.setIcon(FTPDialog.successIcon);
             }else{
                 optionPane.setIcon(FTPDialog.failIcon);
             }
             UIUtils.changeComponentsBackground(optionPane, UIVars.primaryColor);
         });

         addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent e) {
                 optionPane.setValue(new Integer(JOptionPane.CANCEL_OPTION));
                 setVisible(false);
             }
         });

        UIUtils.addTextChangeListener(serverTextField, e -> {
            if(validateUrl(serverTextField.getText())){
                Matcher matcher = serverAddressValidator.matcher(serverTextField.getText());
                if(matcher.find()){
                    MatchResult result = matcher.toMatchResult();
                    ftpOptions.setProtocol(result.group(1));
                    ftpOptions.setServer(result.group(2));
                    ftpOptions.setPort(Integer.parseInt(result.group(3)));
                    if(result.groupCount() == 4){
                        ftpOptions.setPath(result.group(4));
                    }
                    serverFieldValid = true;
                    serverTextField.setBorderColor(Color.white);
                }else{
                    serverFieldValid = false;
                    serverTextField.setBorderColor(UIVars.warningColor);
                }
            }else{
                serverTextField.setBorderColor(UIVars.errorColor);
                serverFieldValid = false;
            }

            refreshPaneButtons();
        });

        UIUtils.addTextChangeListener(userTextField, e -> {
            if(!userTextField.getText().isEmpty()){
                ftpOptions.setUser(userTextField.getText().trim());
                usernameFieldValid = true;
                userTextField.setBorderColor(Color.white);
            }else{
                usernameFieldValid = false;
                userTextField.setBorderColor(UIVars.errorColor);
            }

            refreshPaneButtons();
        });

        UIUtils.addTextChangeListener(passwordTextField, e -> {
            if(passwordTextField.getPassword().length != 0){
                ftpOptions.setPassword(String.valueOf(passwordTextField.getPassword()));
                passwordFieldValid = true;
                passwordTextField.setBorderColor(Color.white);
            }else{
                passwordFieldValid = false;
                passwordTextField.setBorderColor(UIVars.errorColor);
            }

            refreshPaneButtons();
        });

    }

    /**
     * Enables or disables the pane buttons, depending if the entered information is valid
     */
    public void refreshPaneButtons(){
        if(serverFieldValid && usernameFieldValid && passwordFieldValid) {
            okButton.setEnabled(true);
            testButton.setEnabled(true);
        }
        else {
            okButton.setEnabled(false);
            testButton.setEnabled(false);
        }
    }

    /**
     * Validates the user entered ftp/sftp/ftps/ftpes url
     * @param url The ftp/sftp/ftps/ftpes url
     * @return The validness of the url
     */
    private boolean validateUrl(String url){
        return urlValidator.isValid(url);
    }

    /**
     * Test's the connection to the server
     * @return The success of the test
     */
    private boolean testConnection(){
        return RegionFTPClient.testConnection(ftpOptions);
    }
}
