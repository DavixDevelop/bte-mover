package com.davixdevelop.btemover.view.style;

import com.davixdevelop.btemover.model.QueriedRegion;
import com.davixdevelop.btemover.view.components.QueriedRegionLabel;

import javax.swing.*;
import java.awt.*;

/**
 * Represents the query JList cell renderer
 *
 * @author DavixDevelop
 */
public class RegionListRenderer extends DefaultListCellRenderer {
    public static final ImageIcon reg3DIcon = new ImageIcon(RegionListRenderer.class.getResource("reg3dicon.png"));
    public static final ImageIcon downloadIcon = new ImageIcon(RegionListRenderer.class.getResource("download.png"));
    public static final ImageIcon uploadIcon = new ImageIcon(RegionListRenderer.class.getResource("upload.png"));
    public static final ImageIcon doneIcon = new ImageIcon(RegionListRenderer.class.getResource("done.png"));
    public static final ImageIcon failedIcon = new ImageIcon(RegionListRenderer.class.getResource("failed.png"));
    public static final ImageIcon deleteIcon = new ImageIcon(RegionListRenderer.class.getResource("delete.png"));
    public static final ImageIcon deleteFailedIcon = new ImageIcon(RegionListRenderer.class.getResource("delete_failed.png"));
    public static final ImageIcon deleteDoneIcon = new ImageIcon(RegionListRenderer.class.getResource("delete_done.png"));
    public static final ImageIcon downloadUploadIcon = new ImageIcon(RegionListRenderer.class.getResource("download_upload.png"));



    private final QueriedRegionLabel regionLabel;

    public RegionListRenderer(){
        super();
        regionLabel = new QueriedRegionLabel();
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        regionLabel.setRegion((QueriedRegion) value);
        return regionLabel;
    }
}
