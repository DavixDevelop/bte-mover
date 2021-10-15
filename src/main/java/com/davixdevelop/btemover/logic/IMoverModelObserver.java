package com.davixdevelop.btemover.logic;

import com.davixdevelop.btemover.model.Region;
import org.opengis.geometry.Envelope;

import java.time.LocalDateTime;

public interface IMoverModelObserver {

    /**
     * @param status:
     *              0 - No transfers in query
     *              1 - Transfers in query
     *              2 - Error connecting to Source FTP
     *              3 - Error connecting to Target FTP
     */
    public void previewTransfers(Integer status);

    /**
     *
     * @param status 0 - Shapefile layer
     *               1 - Source layer
     *               2 - Target layer
     *               3 - Transfer layer
     */
    public void zoomToLayers(Integer status);
    public void showMessage(String[] message);
    public void setQueryItemIcon(Region region, Integer status);
    public void setQueryItemCount(Region region, Integer count);
    public void updateTransferCounts();

    /**
     *
     * @param status: >0 - Manual progress
     *                -1 - Increase region2d progress
     *                -2 - Increase region3d progress
     */
    public void updateProgress(Integer status);
    public void transferDone();

    public void toggleOSMLayer(boolean toggle);

}
