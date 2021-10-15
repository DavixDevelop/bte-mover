package com.davixdevelop.btemover.utils;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

/**
 * Represents an geo helper class intended for projection a feature geometry to the
 * WGS84 projection
 *
 * @author DavixDevelop
 */
public class GeoFeatureHelper {

    /**
     * Transform an simple feature to WGS84 and set's the simple feature metadata to use WGS84 crs
     * @param scheme The scheme (type) of the simple feature
     * @param feature The source simple feature
     * @param transform The math transform to transform the geometry
     * @return The transformed simple feature
     */
    public static SimpleFeature transform(SimpleFeatureType scheme, SimpleFeature feature, MathTransform transform){
        Object[] attrs = new Object[scheme.getAttributeCount()];

        for(int i = 0; i < attrs.length; i++){
            AttributeDescriptor descriptor = scheme.getDescriptor(i);
            Object attr = feature.getAttribute(descriptor.getName());

            if(attr instanceof Geometry){
                try {

                    Geometry orgGeo = (Geometry) attr;
                    attr = JTS.transform(orgGeo, transform);

                }catch (Exception ex){
                    LogUtils.log(ex);
                }
            }

            attrs[i] = attr;
        }

        try{
            scheme = SimpleFeatureTypeBuilder.retype(scheme, DefaultGeographicCRS.WGS84);
            SimpleFeature  simpleFeature = SimpleFeatureBuilder.build(scheme, attrs, feature.getID());
            simpleFeature.getUserData().putAll(feature.getUserData());

            return simpleFeature;
        }catch (Exception ex){
            LogUtils.log(ex);

            return null;
        }

    }
}
