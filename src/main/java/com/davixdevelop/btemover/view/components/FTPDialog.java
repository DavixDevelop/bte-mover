package com.davixdevelop.btemover.view.components;

import com.davixdevelop.btemover.model.FTPOptions;
import com.davixdevelop.btemover.model.JschSFTPRegionClient;
import com.davixdevelop.btemover.model.RegionFTPClient;
import com.davixdevelop.btemover.utils.LogUtils;
import com.davixdevelop.btemover.utils.UIUtils;
import com.davixdevelop.btemover.view.UIVars;
import com.davixdevelop.btemover.view.style.RegionListRenderer;
import org.apache.commons.validator.routines.UrlValidator;
import org.checkerframework.checker.guieffect.qual.UI;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Objects;
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

    private final JLabel serverTextLabel;
    private final RoundedTextField serverTextField;
    private final JLabel userTextLabel;
    private final RoundedTextField userTextField;
    private final JLabel passwordTextLabel;
    private final RoundedPasswordField passwordTextField;
    private final JButton okButton;
    private final RoundedButton testButton;

    private final JPanel keyFilePanel;
    private final JLabel keyFileName;
    private final RoundedButton keyFileButton;
    private CircleButton removeKeyFileButton;

    //If file dialog is enabled
    private boolean isFileDialog = false;
    //If add key-file button is enabled
    private boolean isSFTPDialog = false;
    //If key file is added
    private boolean isKeyFileAdded = false;

    private boolean serverFieldValid, usernameFieldValid, passwordFieldValid = false;

    private final JOptionPane optionPane;

    public JOptionPane getOptionPane() {
        return optionPane;
    }

    private Frame parentFrame;

    private final UrlValidator urlValidator = new UrlValidator(new String[]{"ftp", "ftps", "sftp", "ftpes"});
    //private Pattern serverAddressValidator = Pattern.compile("ftp://([^-]*):(\\d+)[/]?(.+)?", Pattern.CASE_INSENSITIVE);
    private final Pattern serverAddressValidator = Pattern.compile("^(ftp|sftp|ftps|ftpes)://(.+):(\\d+)[/]?(.+)?(?<!/)$", Pattern.CASE_INSENSITIVE);
    private final Pattern localAddressValidator = Pattern.compile("^(file)://(.+)?(?<!/)$", Pattern.CASE_INSENSITIVE);

    public FTPDialog(Frame _frame, FTPOptions _inOptions){
        super(_frame, true);

        parentFrame = _frame;

        setPreferredSize(new Dimension(700, 390));

        serverTextLabel = new JLabel("Server (With port):");
        serverTextLabel.setFont(UIVars.RobotoRegular.deriveFont(UIVars.smallFontSize));
        serverTextField = new RoundedTextField();
        serverTextField.setFont(UIVars.RobotoRegular.deriveFont(UIVars.primaryFontSize));
        serverTextField.setPlaceholder("Ex: (ftp|sftp|ftps)://ftp.davixdevelop.com:21/terraworld");
        userTextLabel = new JLabel("Username:");
        userTextLabel.setFont(UIVars.RobotoRegular.deriveFont(UIVars.smallFontSize));
        userTextField = new RoundedTextField();
        userTextField.setFont(UIVars.RobotoRegular.deriveFont(UIVars.primaryFontSize));
        passwordTextLabel = new JLabel("Password:");
        passwordTextLabel.setFont(UIVars.RobotoRegular.deriveFont(UIVars.smallFontSize));
        passwordTextField = new RoundedPasswordField();
        passwordTextField.setFont(UIVars.RobotoRegular.deriveFont(UIVars.primaryFontSize));
        passwordTextField.setEchoChar('\u2022');

        keyFileButton = new RoundedButton("Add key-file");
        keyFileButton.setAlternative();
        keyFileName = new JLabel();
        keyFileName.setFont(UIVars.RobotoLight.deriveFont(UIVars.primaryFontSize));

        removeKeyFileButton = new CircleButton(RegionListRenderer.failedIcon, false);
        removeKeyFileButton.setToolTipText("Remove key-file");
        removeKeyFileButton.setBackgroundColor(UIVars.primaryColor);
        removeKeyFileButton.setMargin(15);
        removeKeyFileButton.setVisible(false);

        keyFilePanel = new JPanel();
        keyFilePanel.setLayout(new BoxLayout(keyFilePanel, BoxLayout.X_AXIS));
        keyFilePanel.add(keyFileButton);
        keyFilePanel.add(Box.createRigidArea(new Dimension(15, 0)));
        keyFilePanel.add(keyFileName);
        keyFilePanel.add(Box.createRigidArea(new Dimension(15, 0)));
        keyFilePanel.add(removeKeyFileButton);
        keyFilePanel.setVisible(false);

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
            if(Objects.equals(ftpOptions.getProtocol(), "file")){
                serverTextField.setText(ftpOptions.getProtocol() + "://" + ftpOptions.getPath());
                userTextLabel.setVisible(false);
                userTextField.setVisible(false);
                passwordTextLabel.setVisible(false);
                passwordTextField.setVisible(false);
            }
            else {
                serverTextField.setText(ftpOptions.getProtocol() + "://" + ftpOptions.getServer() + ":" + ftpOptions.getPort() +
                        ((ftpOptions.getPath() != null) ? (ftpOptions.getPath().length() != 0) ? "/" + ftpOptions.getPath() : "" : ""));

                //Check if ftp options use key-file based auth
                if(JschSFTPRegionClient.isSFTPKeyFile(ftpOptions)){
                    File keyFile = new File(JschSFTPRegionClient.getKeyFile(ftpOptions));
                    keyFileName.setText(keyFile.getName());

                    keyFilePanel.setVisible(true);

                    passwordTextLabel.setVisible(false);
                    passwordTextField.setVisible(false);

                    passwordFieldValid = true;
                    isKeyFileAdded = true;

                    removeKeyFileButton.setVisible(true);
                }else{
                    passwordTextField.setText(ftpOptions.getPassword());
                    passwordFieldValid = true;
                }
            }
            serverFieldValid = true;
            userTextField.setText(ftpOptions.getUser());
            usernameFieldValid = true;
            /*
            //Set the pane icon to success or fail, depending if the server is accessible
            if(testConnection()){
                paneIcon = FTPDialog.successIcon;
            }else{
                paneIcon = FTPDialog.failIcon;
            }*/
            paneIcon = FTPDialog.successIcon;
        }

        Object[] content = {serverTextLabel, serverTextField, userTextLabel, userTextField, passwordTextLabel, passwordTextField, keyFilePanel};
         Object[] options = {testButton,okButton};

         optionPane = new JOptionPane(content, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, paneIcon, options, options[0]);
         optionPane.setFont(UIVars.RobotoLight.deriveFont(UIVars.primaryFontSize));
         optionPane.setBackground(UIVars.primaryColor);

         UIUtils.changeComponentsBackground(optionPane, UIVars.primaryColor);

         setContentPane(optionPane);
         setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

         keyFileButton.addActionListener(e -> {
             JFileChooser fileChooser = new JFileChooser();
             fileChooser.setDialogTitle("Choose key-file");
             fileChooser.setMultiSelectionEnabled(false);
             int result = fileChooser.showOpenDialog(this);

             if(result == JFileChooser.APPROVE_OPTION){
                 String keyFilePath = fileChooser.getSelectedFile().getAbsolutePath();
                 ftpOptions = JschSFTPRegionClient.setKeyFile(ftpOptions, keyFilePath);

                 File keyFile = new File(keyFilePath);
                 keyFileName.setText(keyFile.getName());
                 removeKeyFileButton.setVisible(true);
                 passwordTextLabel.setVisible(false);
                 passwordTextField.setVisible(false);
                 passwordFieldValid = true;
                 isKeyFileAdded = true;
                 refreshPaneButtons();
             }
         });

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

         removeKeyFileButton.addActionListener(e -> {
             //Remove key file and re-enable password field
             ftpOptions.setPassword("");
             isKeyFileAdded = false;
             passwordFieldValid = false;
             keyFileName.setText("");
             removeKeyFileButton.setVisible(false);
             refreshPaneButtons();
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
                    String proto = result.group(1);
                    ftpOptions.setProtocol(result.group(1));
                    //Enable sftp dialog (hide input keyfile button) if protocol is sftp
                    isSFTPDialog = (Objects.equals(result.group(1),"sftp")) ? true : false;
                    //Disable file dialog (hidden username and password fields) if enabled
                    if(isFileDialog)
                        isFileDialog = false;
                    ftpOptions.setServer(result.group(2));
                    ftpOptions.setPort(Integer.parseInt(result.group(3)));
                    if (result.groupCount() == 4) {
                        ftpOptions.setPath(result.group(4));
                    }

                    serverFieldValid = true;
                    serverTextField.setBorderColor(Color.white);
                }else{
                    matcher = localAddressValidator.matcher(serverTextField.getText());
                    if(matcher.find()){
                        MatchResult result = matcher.toMatchResult();

                        //Enable file dialog (hidden username and password fields)
                        isFileDialog = true;
                        //Disable sftp dialog if enabled (hide input keyfile button)
                        if(isSFTPDialog)
                            isSFTPDialog = false;

                        ftpOptions.setProtocol(result.group(1));
                        ftpOptions.setPath(result.group(2));
                        if(result.group(2).contains("\\"))
                            ftpOptions.setPath(result.group(2).replace("\\","/"));

                        serverFieldValid = true;
                        serverTextField.setBorderColor(Color.white);
                    }else{
                        serverFieldValid = false;
                        serverTextField.setBorderColor(UIVars.warningColor);

                        if(isKeyFileAdded){
                            isKeyFileAdded = false;
                            keyFileName.setText("");
                            removeKeyFileButton.setVisible(false);
                            ftpOptions.setPassword("");
                            passwordFieldValid = false;
                        }
                    }
                }
            }else{
                serverTextField.setBorderColor(UIVars.errorColor);
                serverFieldValid = false;

                if(isSFTPDialog)
                    isSFTPDialog = false;

                if(isKeyFileAdded){
                    isKeyFileAdded = false;
                    keyFileName.setText("");
                    removeKeyFileButton.setVisible(false);
                    ftpOptions.setPassword("");
                    passwordFieldValid = false;
                }

                if(isFileDialog)
                    isFileDialog = false;
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

        pack();
        setLocationRelativeTo(null);

    }

    /**
     * Enables or disables the pane buttons, depending if the entered information is valid
     */
    public void refreshPaneButtons(){
        if(!isFileDialog) {

            userTextLabel.setVisible(true);
            userTextField.setVisible(true);
            //Don't re-enable password field if key-file is added
            if(!isKeyFileAdded) {
                passwordTextLabel.setVisible(true);
                passwordTextField.setVisible(true);
            }

            keyFilePanel.setVisible(isSFTPDialog);

            if (serverFieldValid && usernameFieldValid && passwordFieldValid) {
                okButton.setEnabled(true);
                testButton.setEnabled(true);
            } else {
                okButton.setEnabled(false);
                testButton.setEnabled(false);
            }


        }else if(isFileDialog){
            if(serverFieldValid) {
                okButton.setEnabled(true);
                testButton.setEnabled(true);
            }else{
                okButton.setEnabled(false);
                testButton.setEnabled(false);
            }

            userTextLabel.setVisible(false);
            userTextField.setVisible(false);
            passwordTextLabel.setVisible(false);
            passwordTextField.setVisible(false);

            keyFilePanel.setVisible(isSFTPDialog);
        }
    }

    /**
     * Validates the user entered ftp/sftp/ftps/ftpes/file url
     * @param url The ftp/sftp/ftps/ftpes/file url
     * @return The validness of the url
     */
    private boolean validateUrl(String url){
        if(url.startsWith("file"))
            return true;
        else
            return urlValidator.isValid(url);
    }

    /**
     * Test's the connection to the server
     * @return The success of the test
     */
    private boolean testConnection(){
        boolean result = RegionFTPClient.testConnection(ftpOptions);
        if(!result && isSFTPDialog){
            if(JschSFTPRegionClient.isSFTPKeyFile(ftpOptions)){
                //Show message dialog if user entered a wrong private file (ex. a Putty private key file)
                if(LogUtils.returnLatest().endsWith("com.jcraft.jsch.JSchException: invalid privatekey")){

                    MessageDialog messageDialog = new MessageDialog(this, new String[]{"Invalid private key", "Please use a OpenSSH format private key"});
                    messageDialog.setVisible(true);
                    ((Integer)messageDialog.getOptionPane().getValue()).intValue();

                    //Remove added key-file and re-enable password field
                    ftpOptions.setPassword("");
                    isKeyFileAdded = false;
                    passwordFieldValid = false;
                    keyFileName.setText("");
                    removeKeyFileButton.setVisible(false);
                    refreshPaneButtons();
                }
            }
        }

        return result;
    }
}
