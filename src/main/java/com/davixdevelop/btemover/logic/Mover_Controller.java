package com.davixdevelop.btemover.logic;

import com.davixdevelop.btemover.model.Mover_Model;
import com.davixdevelop.btemover.model.QueriedRegion;
import com.davixdevelop.btemover.model.Region;
import com.davixdevelop.btemover.model.TimerModel;
import com.davixdevelop.btemover.utils.LogUtils;
import com.davixdevelop.btemover.view.UIVars;
import com.davixdevelop.btemover.view.components.FTPDialog;
import com.davixdevelop.btemover.view.Mover_View;
import com.davixdevelop.btemover.view.components.MessageDialog;
import org.apache.commons.io.FilenameUtils;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;

public class Mover_Controller implements IMoverModelObserver {
    private Mover_Model model;
    private Mover_View view;



    public Mover_Controller(){
        model = new Mover_Model(this);
        //Setup temp folder for downloaded source regions
        model.setupTempFolder();
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
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        model.previewTransfers();
                /*SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        view.enableTransferButton(previewTransfers);
                    }
                });
                */
                    }
                };
                view.enablePreviewButton(false);
                view.showSpinner(true);
                Thread thread = new Thread(runnable);
                thread.start();
            }
        });

        view.initTransferListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                view.enablePreviewButton(false);
                view.enableTransferButton(false);
                model.transferRegions();
            }
        });

        view.initExportListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Save map content to image
                GTRenderer renderer = new StreamingRenderer();
                renderer.setMapContent(model.getMapContent());

                Rectangle rectangleImage = null;
                ReferencedEnvelope mapEnvelope = null;

                try{
                    mapEnvelope = model.getMapContent().getViewport().getBounds();
                    mapEnvelope.expandBy(0.1);

                    double heightWidthRatio = mapEnvelope.getSpan(1) / mapEnvelope.getSpan(0);
                    //Create the image bounds with a fixed width of 1920 pixels
                    int imageHeight = (int) Math.round(1920 * heightWidthRatio);
                    rectangleImage = new Rectangle(0, 0,1920, imageHeight);

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
                            imageFile = new File(imageFile.toString() + ".png");
                        }

                        //Write buffered image to file
                        ImageIO.write(bufferedImage, "png", imageFile);
                    }


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
    }

    public void refreshPreviewButton(){
        if(model.getShapefilePath() != "" && model.getTargetFTP() != null && model.getSourceFTP() != null)
            view.enablePreviewButton(true);
        else
            view.enablePreviewButton(false);

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
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                model.updateQuery();
            }
        });

        view.showSpinner(false);
        view.enablePreviewButton(true);

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
            envelope = model.getShapefileLayer().getBounds();
        }else if(layer == 1){
            envelope = model.getSourceRegionsLayer().getBounds();
        }else if(layer == 2){
            envelope = model.getTargetRegionsLayer().getBounds();
        }else if(layer == 3){
            envelope = model.getTransferRegionsLayer().getBounds();

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
    public void setQueryItemCount(Region region, Integer count) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                DefaultListModel queryModel =  model.getQueryModel();
                for(int i = 0; i < queryModel.getSize(); i++){
                    QueriedRegion queriedRegion = (QueriedRegion) queryModel.get(i);
                    if(queriedRegion.getName() == region.getName()){

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
     */
    @Override
    public void updateProgress(Integer status) {

        if (status == -1) {
            model.getTimerModel().Increase2DRegions();
        } else if (status == -2) {
            model.getTimerModel().Increase3DRegions();
        }
        else if(status == -3){
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

    int completeRegions = (status == -1 || status == -2) ? model.getTimerModel().getProgress2DRegions() + model.getTimerModel().getProgress3DRegions() : status;

    if(completeRegions == model.getTimerModel().getTotalRegions()){
        String wat = "0";
    }

    long elapsedTime = currentTime - model.getTimerModel().getStartTime();
    long progressTime = elapsedTime * model.getTimerModel().getTotalRegions() / completeRegions;
    long eta = progressTime - elapsedTime;

    //millis = eta % 1000;
    second = (eta / 1000) % 60;
    minute = (eta / (1000 * 60)) % 60;
    hour = (eta / (1000 * 60 * 60)) % 24;

    view.setProgressText(String.format("ETR: %02d:%02d:%02d Reg2D: %d/%d Reg3D: %d/%d", hour, minute, second,
            model.getTimerModel().getProgress2DRegions(),
            model.getTimerModel().getTotal2DRegions(),
            model.getTimerModel().getProgress3DRegions(),
            model.getTimerModel().getTotal3DRegions()));
    }

    @Override
    public void transferDone() {
        view.enablePreviewButton(true);
    }

    @Override
    public void toggleOSMLayer(boolean toggle) {
        view.toggleOSMButton(toggle);
    }

    @Override
    public void setQueryItemIcon(Region region, Integer status) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                DefaultListModel queryModel =  model.getQueryModel();
                for(int i = 0; i < queryModel.getSize(); i++){
                    QueriedRegion queriedRegion = (QueriedRegion) queryModel.get(i);
                    if(queriedRegion.getName() == region.getName()){
                        queryModel.removeElement(queriedRegion);

                        //Update query item status
                        queriedRegion.setStatus(status);

                        queryModel.add(0, queriedRegion);

                        /*
                        //If query item isn't on first place, set it as first place, and move the previous first item to the old index
                        if(i != 0){
                            QueriedRegion firstElement = (QueriedRegion)queryModel.get(0);
                            queryModel.set(0, queriedRegion);
                            queryModel.set(i, firstElement);
                        }else{
                            //if not, just set the updated query item back to first place
                            queryModel.set(0, queriedRegion);
                        }*/

                        break;
                    }
                }
            }
        });
    }
}
