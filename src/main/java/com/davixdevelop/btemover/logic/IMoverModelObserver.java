package com.davixdevelop.btemover.logic;

import com.davixdevelop.btemover.model.Region;

public interface IMoverModelObserver {

    /**
     * @param status:
     *              0 - No transfers in query
     *              1 - Transfers in query
     *              2 - Error connecting to Source FTP
     *              3 - Error connecting to Target FTP
     */
    void previewTransfers(Integer status);

    /**
     *
     * @param status 0 - Shapefile layer
     *               1 - Source layer
     *               2 - Target layer
     *               3 - Transfer layer
     *               4 - Shared layer
     */
    void zoomToLayers(Integer status);

    /**
     * Show a message to the user
     * Note: Thread blocking
     * @param message An array of messages per row
     */
    void showMessage(String[] message);
    int questionMessage(String title, String question);
    int numberMessage(String label, String placeholder);

    /**
     * Set's the region query icon in the DefaultListModel
     * @param region The region to update It's query item icon
     * @param status 0. None
     *               1. Downloading
     *               2. Uploading
     *               3. Done
     *               4. Failed
     *               5. Deleting
     *               6. Delete failed
     *               7. Delete done
     *               8. Syncing
     */
    void setQueryItemIcon(Region region, Integer status);
    void setQueryItemCount(Region region, Integer count);
    void updateTransferCounts();

    void updateDeleteCounts();

    /**
     *
     * @param status: >0 - Manual progress
     *                -1 - Increase region2d progress
     *                -2 - Increase region3d progress
     *                -3 - Done
     *                -4 - Decrease total region3d count
     */
    void updateProgress(Integer status);
    void transferDone(boolean[] error);

    void deleteDone(boolean[] error);

    void toggleOSMLayer(boolean toggle);

}
