package com.davixdevelop.btemover.model;

import org.geotools.feature.FeatureCollection;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapLayerEvent;
import org.geotools.styling.Style;

/**
 * Represent an FeatureLayer, that can be updated later
 *
 * @author DavixDevelop
 */
public class DynamicLayer extends FeatureLayer {

    public DynamicLayer(FeatureCollection collection, Style style, String title) {
        super(collection, style, title);
    }

    /**
     * Notifies the feature layer, that it's feature collection has changed
     */
    public void update(){
        fireMapLayerListenerLayerChanged(MapLayerEvent.DATA_CHANGED);
    }
}
