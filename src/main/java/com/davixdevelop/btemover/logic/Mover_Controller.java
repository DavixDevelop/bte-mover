package com.davixdevelop.btemover.logic;

import com.davixdevelop.btemover.model.Mover_Model;
import com.davixdevelop.btemover.model.QueriedRegion;
import com.davixdevelop.btemover.model.Region;
import com.davixdevelop.btemover.view.UIVars;
import com.davixdevelop.btemover.view.components.FTPDialog;
import com.davixdevelop.btemover.view.Mover_View;
import com.davixdevelop.btemover.view.components.MessageDialog;
import com.davixdevelop.btemover.view.components.NumberDialog;
import com.davixdevelop.btemover.view.components.QuestionDialog;
import org.apache.commons.io.FilenameUtils;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;

public class Mover_Controller implements IMoverModelObserver {
    private final Mover_Model model;
    private final Mover_View view;



    public Mover_Controller(){
        model = new Mover_Model(this);
        //model.setupTempFolder();
        view = new Mover_View(model);

        initListeners();

        view.setVisible(true);

    }

    public void initListeners(){
        view.initChooseShapeFileListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Choose input shapefile");
                fileChooser.setFileFilter(new FileNameExtensionFilter("ESRI Shapefile", "shp"));
                fileChooser.setMultiSelectionEnabled(false);
                int result = fileChooser.showOpenDialog(view);

                if(result == JFileChooser.APPROVE_OPTION){
                    model.setShapefilePath(fileChooser.getSelectedFile().getAbsolutePath());
                    view.setShapeFile_Path(model.getShapefilePath());
                }

                /*
                FileDialog fileDialog = new FileDialog(view, "Choose input shapefile", FileDialog.LOAD);
                fileDialog.setFile("*.shp");
                fileDialog.setFilenameFilter((dir, name) -> name.endsWith(".shp"));
                fileDialog.setVisible(true);
                if(fileDialog.getFile() != null)
                {
                    model.setShapefilePath(Paths.get(fileDialog.getDirectory(), fileDialog.getFile()).toAbsolutePath().toString());
                    view.setShapeFile_Path(model.getShapefilePath());
                }*/

                refreshPreviewButton();
            }
        });

        view.initChooseSourceFTPListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FTPDialog ftpDialog = new FTPDialog(view, model.getSourceFTP());
                ftpDialog.pack();
                ftpDialog.setVisible(true);

                int result = ((Integer)ftpDialog.getOptionPane().getValue()).intValue();
                if(result == JOptionPane.OK_OPTION){
                    model.setSourceFTP(ftpDialog.getFtpOptions());
                    view.setSourceFTP_label(model.getSourceFTP().getUser() + "@" + model.getSourceFTP().getProtocol() + "://" + model.getSourceFTP().getServer() + ":" + model.getSourceFTP().getPort() + ((model.getSourceFTP().getPath() != null) ? (model.getSourceFTP().getPath().length() != 0) ? ("/" + model.getSourceFTP().getPath()) : "" : ""));
                }

                refreshPreviewButton();
            }
        });

        view.initCoooseTargetFTPListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FTPDialog ftpDialog = new FTPDialog(view, model.getTargetFTP());
                ftpDialog.pack();
                ftpDialog.setVisible(true);

                int result = ((Integer)ftpDialog.getOptionPane().getValue()).intValue();
                if(result == JOptionPane.OK_OPTION){
                    model.setTargetFTP(ftpDialog.getFtpOptions());
                    view.setTargetFTP_label(model.getTargetFTP().getUser() + "@" + model.getTargetFTP().getProtocol() + "://" + model.getTargetFTP().getServer() + ":" + model.getTargetFTP().getPort() + ((model.getTargetFTP().getPath() != null) ? (model.getTargetFTP().getPath().length() != 0) ? ("/" + model.getTargetFTP().getPath()) : "" : ""));
                }

                refreshPreviewButton();
            }
        });

        view.initPreviewListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Toogle layers back on visible if the shapefile layer was already added
                if(model.getShapefileLayerStatus() == 2){
                    view.getOnSourceCountLabel().setToggled(true);
                    model.getSourceRegionsLayer().setVisible(true);

                    view.getOnTargetCountLabel().setToggled(true);
                    model.getTargetRegionsLayer().setVisible(true);

                    view.getOnSharedCountLabel().setToggled(true);
                    model.getSharedRegionsLayer().setVisible(true);

                    view.getOnTransferCountLabel().setToggled(true);
                    model.getTransferRegionsLayer().setVisible(true);
                }

                Runnable runnable = () -> {
                    model.previewTransfers();
            /*SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    view.enableTransferButton(previewTransfers);
                }
            });
            */
                };
                view.enableToolButtons(false);
                view.showSpinner(true);
                Thread thread = new Thread(runnable);
                thread.start();
            }
        });

        view.initTransferListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                view.enableToolButtons(false);
                view.enableTransferButton(false);
                model.transferRegions();
            }
        });

        view.initExportListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int imagePixelWidth = 0;
                while (imagePixelWidth == 0){
                    imagePixelWidth = numberMessage("Enter width of image (pixels)", "Ex. 1920");
                }

                //Save map content to image
                GTRenderer renderer = new StreamingRenderer();
                renderer.setMapContent(model.getMapContent());

                Rectangle rectangleImage;
                ReferencedEnvelope mapEnvelope;

                try{
                    mapEnvelope = model.getMapContent().getViewport().getBounds();
                    mapEnvelope.expandBy(0.1);

                    double heightWidthRatio = mapEnvelope.getSpan(1) / mapEnvelope.getSpan(0);
                    //Create the image bounds with a width of imagePixelWidth
                    int imageHeight = (int) Math.round(imagePixelWidth * heightWidthRatio);
                    rectangleImage = new Rectangle(0, 0,imagePixelWidth, imageHeight);

                    Rectangle oldScreenArea = model.getMapContent().getViewport().getScreenArea();
                    ReferencedEnvelope oldEnvelope = model.getMapContent().getViewport().getBounds();
                    model.getMapContent().getViewport().setScreenArea(rectangleImage);
                    model.getMapContent().getViewport().setBounds(mapEnvelope);

                    BufferedImage bufferedImage = new BufferedImage(rectangleImage.width, rectangleImage.height, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = bufferedImage.createGraphics();



                    //Fill the graphics with a transparent color
                    g2.setPaint(UIVars.transparentColor);
                    g2.fill(rectangleImage);

                    //Render the layers onto the graphics
                    renderer.paint(g2, rectangleImage, mapEnvelope);

                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("Save map to...");
                    fileChooser.setFileFilter(new FileNameExtensionFilter("Image", "png"));

                    int result = fileChooser.showSaveDialog(view);

                    if(result == JFileChooser.APPROVE_OPTION){
                        File imageFile = fileChooser.getSelectedFile();
                        if(!FilenameUtils.getExtension(imageFile.getPath()).equalsIgnoreCase("png")){
                            imageFile = new File(imageFile + ".png");
                        }

                        //Write buffered image to file
                        ImageIO.write(bufferedImage, "png", imageFile);

                        //Create aux.xml file
                        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.GERMAN);
                        otherSymbols.setDecimalSeparator('.');
                        DecimalFormat scientificNotion = new DecimalFormat("0.##############E0", otherSymbols);
                        ReferencedEnvelope exportedImageBounds = model.getMapContent().getViewport().getBounds();
                        double[] upper_corner = exportedImageBounds.getUpperCorner().getCoordinate();
                        double[] lower_corner = exportedImageBounds.getLowerCorner().getCoordinate();
                        double d_xres = (upper_corner[0] - lower_corner[0]) / rectangleImage.getWidth();
                        double d_yres = (Math.abs(upper_corner[1] - lower_corner[1]) / rectangleImage.getHeight()) * -1;

                        String ulx = scientificNotion.format(lower_corner[0]);
                        String xres = scientificNotion.format(d_xres);
                        String uly = scientificNotion.format(upper_corner[1]);
                        String yres = scientificNotion.format(d_yres);

                        File auxFile = new File(imageFile + ".aux.xml");
                        PrintWriter printWriter = new PrintWriter(new FileWriter(auxFile));
                        printWriter.println("<PAMDataset>");
                        ////X_COORDINATE_OF_TOP_LEFT_CORNER, (X_SIZE_IN_MAP/X_PIXEL_OF_IMAGE),-0.0000000000000000e+000, Y_COORDINATE_OF_TOP_LEFT_CORNER, 0.0000000000000000e+000,-(Z_SIZE_IN_MAP/Z_PIXEL_OF_IMAGE)
                        printWriter.println("  <GeoTransform> " + ulx + ", " + xres + ", -0.0000000000000000e+000, " + uly + ", 0.0000000000000000e+000, " + yres + "</GeoTransform>");
                        printWriter.println("  <Metadata domain=\"IMAGE_STRUCTURE\">");
                        printWriter.println("    <MDI key=\"INTERLEAVE\">PIXEL</MDI>");
                        printWriter.println("  </Metadata>");
                        printWriter.println("</PAMDataset>");
                        printWriter.close();

                    }

                    model.getMapContent().getViewport().setScreenArea(oldScreenArea);
                    model.getMapContent().getViewport().setBounds(oldEnvelope);


                }catch (Exception ex){
                    MessageDialog messageDialog = new MessageDialog(view, new String[]{"Error while crrating image:", ex.toString()});
                    messageDialog.setVisible(true);

                    int result = ((Integer)messageDialog.getOptionPane().getValue()).intValue();
                }
            }
        });

        view.initOSMButtonListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.toggleOSMLayer();
            }
        });

        view.initExpandButtonListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.zoomToLayer();
            }
        });

        //Add mouse listener to Source Count Label
        view.getOnSourceCountLabel().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //Toggle label if there are regions present and the shapefile layer has been added
                if(model.getSourceRegionsCount() > 0 && model.getShapefileLayerStatus() == 2) {
                    view.getOnSourceCountLabel().setToggled(!view.getOnSourceCountLabel().isToggled());
                    model.getSourceRegionsLayer().setVisible(view.getOnSourceCountLabel().isToggled());
                }
            }
        });

        //Add mouse listener to Target Count Label
        view.getOnTargetCountLabel().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //Toggle label if there are regions present and the shapefile layer has been added
                if(model.getTargetRegionsCount() > 0 && model.getShapefileLayerStatus() == 2) {
                    view.getOnTargetCountLabel().setToggled(!view.getOnTargetCountLabel().isToggled());
                    model.getTargetRegionsLayer().setVisible(view.getOnTargetCountLabel().isToggled());
                }
            }
        });

        //Add mouse listener to Shared Count Label
        view.getOnSharedCountLabel().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //Toggle label if there are regions present and the shapefile layer has been added
                if(model.getSharedRegionsCount() > 0 && model.getShapefileLayerStatus() == 2) {
                    view.getOnSharedCountLabel().setToggled(!view.getOnSharedCountLabel().isToggled());
                    model.getSharedRegionsLayer().setVisible(view.getOnSharedCountLabel().isToggled());
                }
            }
        });

        //Add mouse listener to Transfer Count Label
        view.getOnTransferCountLabel().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //Toggle label if there are regions present and the shapefile layer has been added
                if(model.getTransferRegionsCount() > 0 && model.getShapefileLayerStatus() == 2) {
                    view.getOnTransferCountLabel().setToggled(!view.getOnTransferCountLabel().isToggled());
                    model.getTransferRegionsLayer().setVisible(view.getOnTransferCountLabel().isToggled());
                }
            }
        });

        //Add mouse listener to Shapefile Label
        view.getToggleShapefileLayerButton().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //Toggle label if shapefile layer has been added
                if(model.getShapefileLayerStatus() == 2) {
                    view.getToggleShapefileLayerButton().setToggledOn(!view.getToggleShapefileLayerButton().isToggledOn());
                    model.getShapefileLayer().setVisible(view.getToggleShapefileLayerButton().isToggledOn());
                }
            }
        });
    }

    public void refreshPreviewButton(){
        view.enableToolButtons(!Objects.equals(model.getShapefilePath(),"") && model.getTargetFTP() != null && model.getSourceFTP() != null);
    }

    /**
     * @param status:
     *              0 - No transfers in query
     *              1 - Transfers in query
     *              2 - Error connecting to Source FTP
     *              3 - Error connecting to Target FTP
     */
    @Override
    public void previewTransfers(Integer status) {
        SwingUtilities.invokeLater(model::updateQuery);

        view.showSpinner(false);
        view.enableToolButtons(true);

        if (model.getShapefileLayerStatus() == 1) {
            view.getMapPanel().setMapContent(model.getMapContent());
            model.setShapefileLayerStatus(2);
        }


        model.updateLayers();

        if(status == 1){
            zoomToLayers(3);

            view.enableTransferButton(true);
        }else if(status == 2){
            MessageDialog messageDialog = new MessageDialog(view, new String[]{"Can't connect to Source " + model.getSourceFTP().getProtocol().toUpperCase() + " Server", "Check connection/path/authentication"});
            messageDialog.pack();
            messageDialog.setVisible(true);
            int result = ((Integer)messageDialog.getOptionPane().getValue()).intValue();
            view.enableTransferButton(false);
        }else if(status == 3){
            MessageDialog messageDialog = new MessageDialog(view, new String[]{"Can't connect to Target " + model.getTargetFTP().getProtocol().toUpperCase() + " Server", "Check connection/path/authentication"});
            messageDialog.pack();
            messageDialog.setVisible(true);
            int result = ((Integer)messageDialog.getOptionPane().getValue()).intValue();
            view.enableTransferButton(false);
        }
        else {
            view.enableTransferButton(false);
        }
        view.setOnSourceCountLabel(String.valueOf(model.getSourceRegionsCount()));
        view.setOnTargetCountLabel(String.valueOf(model.getTargetRegionsCount()));
        view.setOnSharedCountLabel(String.valueOf(model.getSharedRegionsCount()));
        view.setOnTransferCountLabel(String.valueOf(model.getTransferRegionsCount()));

        view.setOnSource3DCountLabel(model.getSourceRegions3DCount());
        view.setOnTarget3DCountLabel(model.getTargetRegions3DCount());
        view.setOnShared3DCountLabel(model.getSharedRegions3DCount());
        view.setOnTransfer3DCountLabel(model.getTransferRegions3DCount());

    }

    /**
     *
     * @param layer 0 - Shapefile layer
     *               1 - Source layer
     *               2 - Target layer
     *               3 - Transfer layer
     */
    @Override
    public void zoomToLayers(Integer layer) {
        ReferencedEnvelope envelope = null;
        if(layer == 0){
            envelope = new ReferencedEnvelope(model.getShapefileLayer().getBounds());
        }else if(layer == 1){
            envelope = new ReferencedEnvelope(model.getSourceRegionsLayer().getBounds());
        }else if(layer == 2){
            envelope = new ReferencedEnvelope(model.getTargetRegionsLayer().getBounds());
        }else if(layer == 3){
            envelope = new ReferencedEnvelope(model.getTransferRegionsLayer().getBounds());

        }
        if(envelope != null)
            if(envelope.getMaximum(0) != -1.0 && envelope.getMaximum(1) != -1.0 && envelope.getMinimum(0) != 0.0 && envelope.getMinimum(1) != 0.0) {
                envelope.expandBy(.1);
                view.getMapPanel().setDisplayArea(envelope);

            }
    }

    @Override
    public void showMessage(String[] message) {
        MessageDialog messageDialog = new MessageDialog(view, message);
        messageDialog.pack();
        messageDialog.setVisible(true);

        int result = ((Integer) messageDialog.getOptionPane().getValue()).intValue();
    }

    @Override
    public int questionMessage(String title, String question) {
        QuestionDialog questionDialog = new QuestionDialog(view, title, question);
        questionDialog.pack();
        questionDialog.setVisible(true);

        return ((Integer) questionDialog.getOptionPane().getValue()).intValue();
    }

    @Override
    public int numberMessage(String label, String placeholder) {
        NumberDialog numberDialog = new NumberDialog(view, label, placeholder);
        numberDialog.pack();
        numberDialog.setVisible(true);

        int res = ((Integer) numberDialog.getOptionPane().getValue()).intValue();
        if(res == 0)
            return numberDialog.getNumberValue();
        else
            return 0;
    }

    @Override
    public void setQueryItemCount(Region region, Integer count) {
        SwingUtilities.invokeLater(() -> {
            DefaultListModel queryModel =  model.getQueryModel();
            for(int i = 0; i < queryModel.getSize(); i++){
                QueriedRegion queriedRegion = (QueriedRegion) queryModel.get(i);
                if(Objects.equals(queriedRegion.getName(), region.getName())){

                    queryModel.removeElement(queriedRegion);
                    //Update query item count
                    queriedRegion.setNum3d(count);

                    queryModel.add(0,queriedRegion);

                    /*
                    //If query item isn't on first place, set it as first place, and move the previous first item to the old index
                    if(i != 0){
                        QueriedRegion firstElement = (QueriedRegion)queryModel.get(0);

                        /*queryModel.set(0, queriedRegion);
                        queryModel.set(i, firstElement);

                    }else{
                        //if not, just set the updated query item back to first place
                        queryModel.set(0, queriedRegion);
                    }*/

                    break;
                }
            }
        });


    }

    @Override
    public void updateTransferCounts() {
        view.setOnTargetCountLabel(String.valueOf(model.getTargetRegionsCount()));
        view.setOnSharedCountLabel(String.valueOf(model.getSharedRegionsCount()));
        view.setOnTransferCountLabel(String.valueOf(model.getTransferRegionsCount()));
    }

    /**
     *
     * @param status: >0 - Manual progress
     *                -1 - Increase region2d progress
     *                -2 - Increase region3d progress
     *                -3 - Done
     *                -4 - Decrease total region3d count
     */
    @Override
    public void updateProgress(Integer status) {

        if (status == -1) {
            model.getTimerModel().Increase2DRegions();
        } else if (status == -2) {
            model.getTimerModel().Increase3DRegions();
        }else if(status == -4) {
            model.getTimerModel().DecreaseTotal3DRegions();
        }else if(status == -3){
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - model.getTimerModel().getStartTime();
            long second = (elapsedTime / 1000) % 60;
            long minute = (elapsedTime / (1000 * 60)) % 60;
            long hour = (elapsedTime / (1000 * 60 * 60)) % 24;

            view.setProgressText(String.format("DONE IN: %02d:%02d:%02d Reg2D: %d/%d Reg3D: %d/%d", hour, minute, second,
                    model.getTimerModel().getProgress2DRegions(),
                    model.getTimerModel().getTotal2DRegions(),
                    model.getTimerModel().getProgress3DRegions(),
                    model.getTimerModel().getTotal3DRegions()));

            return;
        }

        //long millis = 0;
        long second = 0;
        long minute = 0;
        long hour = 0;
        long currentTime = System.currentTimeMillis();

        int completeRegions = (status == -1 || status == -2 || status == -4) ? model.getTimerModel().getProgress2DRegions() + model.getTimerModel().getProgress3DRegions() : status;

        long elapsedTime = currentTime - model.getTimerModel().getStartTime();
        long progressTime = elapsedTime * model.getTimerModel().getTotalRegions() / completeRegions;
        long etr = progressTime - elapsedTime;

        //millis = eta % 1000;
        second = (etr / 1000) % 60;
        minute = (etr / (1000 * 60)) % 60;
        hour = (etr / (1000 * 60 * 60)) % 24;

        view.setProgressText(String.format("ETR: %02d:%02d:%02d Reg2D: %d/%d Reg3D: %d/%d", hour, minute, second,
                model.getTimerModel().getProgress2DRegions(),
                model.getTimerModel().getTotal2DRegions(),
                model.getTimerModel().getProgress3DRegions(),
                model.getTimerModel().getTotal3DRegions()));

    }

    @Override
    public void transferDone(boolean[] error) {
        //Enable back the preview button and other tool buttons
        if(model.IncreaseThreadsDone() == model.getThreadCount()){
            view.enableToolButtons(true);

            //Update the 3D regions count
            view.setOnSource3DCountLabel(model.getSourceRegions3DCount());
            view.setOnTarget3DCountLabel(model.getTargetRegions3DCount());
            view.setOnShared3DCountLabel(model.getSharedRegions3DCount());
            view.setOnTransfer3DCountLabel(model.getTransferRegions3DCount());

            if(error[0] == true){
                showMessage(new String[]{"Some error's occurred during transferring", "Check log and press the Preview button to try again"});
            }
        }


    }

    @Override
    public void toggleOSMLayer(boolean toggle) {
        view.toggleOSMButton(toggle);
    }

    @Override
    public void setQueryItemIcon(Region region, Integer status) {
        SwingUtilities.invokeLater(() -> {
            DefaultListModel queryModel =  model.getQueryModel();
            for(int i = 0; i < queryModel.getSize(); i++){
                QueriedRegion queriedRegion = (QueriedRegion) queryModel.get(i);
                if(Objects.equals(queriedRegion.getName(), region.getName())){
                    queryModel.removeElement(queriedRegion);

                    //Update query item status
                    queriedRegion.setStatus(status);

                    queryModel.add(0, queriedRegion);

                    break;
                }
            }
        });
    }
}
