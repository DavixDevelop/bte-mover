package com.davixdevelop.btemover.view;

import com.davixdevelop.btemover.model.Mover_Model;
import com.davixdevelop.btemover.view.components.*;
import com.davixdevelop.btemover.view.style.RegionListRenderer;
import com.davixdevelop.btemover.view.style.RoundedInsetBorder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

/**
 * The view of the mover application (MVC architecture)
 *
 * @author DavixDevelop
 */
public class Mover_View extends JFrame {
    public static final ImageIcon ICON_96 = new ImageIcon(Mover_View.class.getResource("icon96.png"));
    public static final ImageIcon ICON_512 = new ImageIcon(Mover_View.class.getResource("icon.png"));


    private final JPanel previewPanel;
    private final JButton transferButton;
    private final JButton previewButton;
    private final JButton importButton;
    private final JButton sourceFTPButton;
    private final JButton targetFTPButton;
    private final JTextField textField_shapeFile;
    private final JLabel sourceFTP_label;
    private final JPanel ftpOpt;
    private final JLabel targetFTP_label;
    private final PanPanel mapPanel;
    private final JLabel sourceLegendLabel;
    private final ToggleableRoundedLabel onSourceCountLabel;
    private final JLabel targetLegendLabel;
    private final ToggleableRoundedLabel onTargetCountLabel;
    private final JLabel sharedLegendLabel;
    private final ToggleableRoundedLabel onSharedCountLabel;
    private final JLabel transferLegendLabel;
    private final ToggleableRoundedLabel onTransferCountLabel;
    private final JList queryList;
    private final JLabel progressLabel;
    private final LoadingSpinner spinner;

    public void setOnSourceCountLabel(String count){onSourceCountLabel.setText(count);}
    public void setOnTargetCountLabel(String count){onTargetCountLabel.setText(count);}
    public void setOnSharedCountLabel(String count){onSharedCountLabel.setText(count);}
    public void setOnTransferCountLabel(String count){onTransferCountLabel.setText(count);}

    public void setOnSource3DCountLabel(int count){sourceLegendLabel.setToolTipText("3d regions count: " + count);}
    public void setOnTarget3DCountLabel(int count){transferLegendLabel.setToolTipText("3d regions count: " + count);}
    public void setOnShared3DCountLabel(int count){sharedLegendLabel.setToolTipText("3d regions count: " + count);}
    public void setOnTransfer3DCountLabel(int count){transferLegendLabel.setToolTipText("3d regions count: " + count);}

    public Mover_View(Mover_Model model){
        super();
        setTitle("BTE Mover");
        setPreferredSize(new Dimension(1100,880));
        getContentPane().setLayout(new BorderLayout());

        List<Image> icons = new ArrayList<>();
        icons.add(ICON_512.getImage());
        icons.add(ICON_96.getImage());

        setIconImages(icons);

        JPanel panel = new JPanel(){
            @Override
            protected void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int h = getHeight();
                int w = getWidth();
                GradientPaint gradientPaint = new GradientPaint(0,0, UIVars.primaryColor, 0, h, UIVars.secondaryBgColor);
                g2.setPaint(gradientPaint);
                g2.fillRect(0,0,w, h);
            }
        };

        panel.setLayout(new GridBagLayout());

        JPanel optionPanel = new JPanel();
        optionPanel.setBackground(UIVars.transparentColor);
        optionPanel.setOpaque(false);
        optionPanel.setLayout(new GridBagLayout());


        


        GridBagConstraints c = new GridBagConstraints();

        //panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        //panel.setLayout(new GridBagLayout());

        JLabel importLabel = new JLabel("Import Shapefile");
        importLabel.setFont(UIVars.RobotoLight.deriveFont(UIVars.primaryFontSize));
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        optionPanel.add(importLabel, c);



        textField_shapeFile = new RoundedTextField();
        textField_shapeFile.setFont(UIVars.RobotoRegular.deriveFont(UIVars.primaryFontSize));
        textField_shapeFile.setEditable(false);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.weighty = 0.999;
        c.insets = new Insets(5,0,5,0);
        optionPanel.add(textField_shapeFile, c);



        importButton = new RoundedButton("Import");
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.insets = new Insets(5, 5,5,0);
        optionPanel.add(importButton, c);

        ftpOpt = new JPanel();
        ftpOpt.setBackground(UIVars.transparentColor);
        ftpOpt.setOpaque(false);
        ftpOpt.setLayout(new GridBagLayout());

        sourceFTPButton= new RoundedButton("Source FTP");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        ftpOpt.add(sourceFTPButton, c);

        sourceFTP_label = new JLabel("");
        sourceFTP_label.setFont(UIVars.RobotoBold.deriveFont(UIVars.primaryFontSize));
        sourceFTP_label.setHorizontalAlignment(SwingConstants.CENTER);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = new Insets(0,0,5,0);
        ftpOpt.add(sourceFTP_label, c);

        targetFTPButton = new RoundedButton("Target FTP");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        //c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0,0,5,5);
        ftpOpt.add(targetFTPButton, c);

        targetFTP_label = new JLabel("");
        targetFTP_label.setFont(UIVars.RobotoBold.deriveFont(UIVars.primaryFontSize));
        targetFTP_label.setHorizontalAlignment(SwingConstants.CENTER);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0,0,5, 0);
        ftpOpt.add(targetFTP_label, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5,0,0,0);
        optionPanel.add(ftpOpt, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.weighty = 0.0;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.insets = new Insets(20, 20, 0, 20);
        panel.add(optionPanel, c);

        previewPanel = new JPanel(){
            @Override
            protected void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIVars.primaryBg);
                g2.fillRoundRect(0,0,getWidth() - 1, getHeight() - 1, 10, 10);
            }
        };
        previewPanel.setLayout(new GridBagLayout());
        previewPanel.setOpaque(false);

        mapPanel = new PanPanel(model);
        mapPanel.setBackground(Color.GREEN);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.insets = new Insets(10, 10, 10, 10);
        previewPanel.add(mapPanel, c);

        JPanel queryPanel = new JPanel();
        queryPanel.setOpaque(false);
        queryPanel.setBackground(UIVars.transparentColor);
        queryPanel.setLayout(new GridBagLayout());

        JLabel queryTitle = new JLabel("Query");
        queryTitle.setFont(UIVars.RobotoLight.deriveFont(UIVars.primaryFontSize));
        queryTitle.setHorizontalAlignment(SwingConstants.LEFT);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = new Insets(0, 0, 10, 0);
        queryPanel.add(queryTitle, c);

        queryList = new JList(model.getQueryModel());
        //queryList.setOpaque(false);
        queryList.setCellRenderer(new RegionListRenderer());

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        //c.anchor = GridBagConstraints.LAST_LINE_END;
        c.insets = new Insets(0, 0, 10, 0);
        JScrollPane queryScrollPane = new JScrollPane(queryList);
        //queryScrollPane.getViewport().getView().setBackground(UIVars.primaryBg);
        //queryScrollPane.setOpaque(false);
        //queryScrollPane.getViewport().setOpaque(false);
        queryPanel.add(queryScrollPane, c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.gridheight = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        //c.anchor = GridBagConstraints.LAST_LINE_END;
        c.insets = new Insets(10, 10, 10, 10);
        previewPanel.add(queryPanel, c);



        JPanel legendPanel = new JPanel();
        //legendPanel.setLayout(new GridBagLayout());
        legendPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        legendPanel.setOpaque(false);
        legendPanel.setBackground(UIVars.transparentColor);

        onSourceCountLabel = new ToggleableRoundedLabel(UIVars.legendRadius, UIVars.onSourceColor);
        onSourceCountLabel.setPreferredSize(new Dimension(UIVars.legendIconSize, UIVars.legendIconSize));
        onSourceCountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        legendPanel.add(onSourceCountLabel);

        sourceLegendLabel = new JLabel("Source");
        sourceLegendLabel.setFont(UIVars.RobotoBold.deriveFont(14f));
        sourceLegendLabel.setForeground(Color.white);
        legendPanel.add(sourceLegendLabel);

        legendPanel.add(Box.createRigidArea(new Dimension(UIVars.legendPanelSpacing, 0)));

        onTargetCountLabel = new ToggleableRoundedLabel(UIVars.legendRadius, UIVars.onTargetColor);
        onTargetCountLabel.setPreferredSize(new Dimension(UIVars.legendIconSize, UIVars.legendIconSize));
        onTargetCountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        legendPanel.add(onTargetCountLabel);

        targetLegendLabel = new JLabel("Target");
        targetLegendLabel.setFont(UIVars.RobotoBold.deriveFont(14f));
        targetLegendLabel.setForeground(Color.white);
        legendPanel.add(targetLegendLabel);

        legendPanel.add(Box.createRigidArea(new Dimension(UIVars.legendPanelSpacing, 0)));

        onSharedCountLabel = new ToggleableRoundedLabel(UIVars.legendRadius, UIVars.onSharedColor);
        onSharedCountLabel.setPreferredSize(new Dimension(UIVars.legendIconSize, UIVars.legendIconSize));
        onSharedCountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        onSharedCountLabel.setForeground(Color.white);
        legendPanel.add(onSharedCountLabel);

        sharedLegendLabel = new JLabel("Shared");
        sharedLegendLabel.setFont(UIVars.RobotoBold.deriveFont(14f));
        sharedLegendLabel.setForeground(Color.white);
        legendPanel.add(sharedLegendLabel);

        onTransferCountLabel = new ToggleableRoundedLabel(UIVars.legendRadius, UIVars.onTransferColor);
        onTransferCountLabel.setPreferredSize(new Dimension(UIVars.legendIconSize, UIVars.legendIconSize));
        onTransferCountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        onTransferCountLabel.setForeground(Color.white);
        legendPanel.add(onTransferCountLabel);

        transferLegendLabel = new JLabel("To transfer");
        transferLegendLabel.setFont(UIVars.RobotoBold.deriveFont(14f));
        transferLegendLabel.setForeground(Color.white);
        legendPanel.add(transferLegendLabel);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LAST_LINE_START;
        c.weightx = 1.0;
        c.insets = new Insets(10,10,10,10);
        previewPanel.add(legendPanel, c);

        //ETR: 00:01:15 Reg2D: 0/200 Reg3D: 0/500
        progressLabel = new JLabel("");
        progressLabel.setFont(UIVars.RobotoRegular.deriveFont(UIVars.primaryFontSize));
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.LAST_LINE_END;
        c.weightx = 0.0;
        c.insets = new Insets(10,10,10,10);
        previewPanel.add(progressLabel, c);

        JPanel actionPanel = new JPanel();
        actionPanel.setOpaque(false);
        actionPanel.setBackground(UIVars.transparentColor);
        actionPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0,0));

        previewButton = new RoundedButton("Preview");
        previewButton.setEnabled(false);
        /*
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.LAST_LINE_END;
        c.insets = new Insets(5, 0, 10, 0);*/
        actionPanel.add(previewButton);



        transferButton = new RoundedButton("Transfer");
        transferButton.setEnabled(false);
        /*
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 2;
        c.anchor = GridBagConstraints.LAST_LINE_END;
        c.insets = new Insets(5, 5, 10, 20);
        */

        actionPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        actionPanel.add(transferButton);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LAST_LINE_END;
        c.insets = new Insets(10,5,10,10);
        previewPanel.add(actionPanel,c);



        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(20, 20, 10, 20);
        panel.add(previewPanel, c);

        /*
        Update from background thread
        SwingUtilities.invokeLater(() -> {

        });*/
        

        getContentPane().add(panel, BorderLayout.CENTER);

        spinner = new LoadingSpinner();
        setGlassPane(spinner);
        spinner.setOpaque(false);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
    }

    public void initChooseShapeFileListener(ActionListener action){
        importButton.addActionListener(action);
    }
    public void initChooseSourceFTPListener(ActionListener action) {sourceFTPButton.addActionListener(action);}
    public void initCoooseTargetFTPListener(ActionListener action){targetFTPButton.addActionListener(action);}
    public void initPreviewListener(ActionListener action){previewButton.addActionListener(action);}
    public void initTransferListener(ActionListener action){ transferButton.addActionListener(action);}
    public void initExportListener(ActionListener action){mapPanel.getExportButton().addActionListener(action);}
    public void initOSMButtonListener(ActionListener action){mapPanel.getOsmToggleButton().addActionListener(action);}
    public void initExpandButtonListener(ActionListener action){mapPanel.getExpandButton().addActionListener(action);}
    public void toggleOSMButton(boolean toggle){mapPanel.getOsmToggleButton().setToggledOn(toggle);}

    public ToggleableRoundedLabel getOnSourceCountLabel() { return onSourceCountLabel; }
    public ToggleableRoundedLabel getOnTargetCountLabel() { return onTargetCountLabel; }
    public ToggleableRoundedLabel getOnSharedCountLabel() { return onSharedCountLabel; }
    public ToggleableRoundedLabel getOnTransferCountLabel() { return onTransferCountLabel; }

    public CircleButton getToggleShapefileLayerButton() { return mapPanel.getToggleShapefileLayer();}

    public PanPanel getMapPanel(){
        return mapPanel;
    }

    public void setShapeFile_Path(String path){
        textField_shapeFile.setText(path);
    }
    public void setSourceFTP_label(String text){sourceFTP_label.setText(text);}
    public void setTargetFTP_label(String text){targetFTP_label.setText(text);}
    public void enableToolButtons(boolean enable){
            previewButton.setEnabled(enable);
            mapPanel.getOsmToggleButton().setEnabled(enable);
            mapPanel.getExportButton().setEnabled(enable);
            mapPanel.getExpandButton().setEnabled(enable);
            mapPanel.getToggleShapefileLayer().setEnabled(enable);
    }
    public void enableTransferButton(boolean enable){
        transferButton.setEnabled(enable);}

    public void showSpinner(boolean show){
        spinner.setVisible(show);
    }

    public void setProgressText(String progress){ progressLabel.setText(progress);}

}
