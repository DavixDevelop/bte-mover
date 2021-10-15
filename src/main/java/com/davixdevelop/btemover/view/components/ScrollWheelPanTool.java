package com.davixdevelop.btemover.view.components;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.swing.JMapPane;
import org.geotools.swing.MapPane;
import org.geotools.swing.event.MapMouseEvent;
import org.geotools.swing.tool.AbstractZoomTool;
import org.geotools.swing.tool.PanTool;

import javax.swing.*;
import java.awt.*;

/**
 * Represents the tool that enables zooming and panning with the mouse on the input JMapPane
 *
 * @author DavixDevelop
 */
public class ScrollWheelPanTool extends AbstractZoomTool {

    public ScrollWheelPanTool(JMapPane mapPane) {
        setMapPane(mapPane);
        panning = false;
    }

    private Point panePos;
    boolean panning;



    @Override
    public Cursor getCursor() {
        return null;
    }

    @Override
    public void onMouseWheelMoved(MapMouseEvent ev) {

        Rectangle paneArea = ((JComponent) getMapPane()).getVisibleRect();

        DirectPosition2D mapPos = ev.getWorldPos();

        double scale = getMapPane().getWorldToScreenTransform().getScaleX();
        int clicks = ev.getWheelAmount();

        double actualZoom = 1;
        // positive clicks are down - zoom out

        if (clicks > 0) {
            actualZoom = -1.0 / (clicks * getZoom());
        } else {
            actualZoom = clicks * getZoom();
        }
        double newScale = scale * actualZoom;

        DirectPosition2D corner =
                new DirectPosition2D(
                        mapPos.getX() - 0.5d * paneArea.getWidth() / newScale,
                        mapPos.getY() + 0.5d * paneArea.getHeight() / newScale);

        // I would prefer to offset the new map based on the cursor but this matches
        // the current zoom in/out tools.

        Envelope2D newMapArea = new Envelope2D();
        newMapArea.setFrameFromCenter(mapPos, corner);
        getMapPane().setDisplayArea(newMapArea);
    }

    @Override
    public void onMousePressed(MapMouseEvent ev) {
        panePos = ev.getPoint();
        panning = true;
    }

    @Override
    public void onMouseDragged(MapMouseEvent ev) {
        if(panning){
            Point pos = ev.getPoint();
            if(!pos.equals(panePos)){
                getMapPane().moveImage(pos.x - panePos.x, pos.y - panePos.y);
                panePos = pos;
            }
        }
    }

    @Override
    public void onMouseReleased(MapMouseEvent ev) {
        panning = false;
    }

    @Override
    public boolean drawDragBox() {
        return false;
    }
}
