package com.davixdevelop.btemover.utils;

import net.buildtheearth.terraminusminus.projection.GeographicProjection;
import net.buildtheearth.terraminusminus.projection.OutOfProjectionBoundsException;

/**
 * Represents an helper class for transforming a point from the
 * Modified Airocean projection (based on the Dymaxion/Airocean projection)
 * to the WGS84 projection
 *
 * @author DavixDevelop
 */
public class TerraHelper {
    public static double offset = 0.3434;
    public static GeographicProjection projection;

    /**
     * Transform a coordinate from the Modified Airocean projection (based on the Dymaxion/Airocean projection)
     * to the WGS84 projection
     * @param x The longitude
     * @param y The latitude
     * @return An double array of the transformed point [x, y]
     */
    public static double[] toGeo(double x, double y) {
        try{
            return projection.toGeo(x, y);
        }catch (OutOfProjectionBoundsException ex){
            try{
                return projection.toGeo(x + offset, y);
            }catch (OutOfProjectionBoundsException ex2){
                try{
                    return projection.toGeo(x - offset, y);
                }catch (OutOfProjectionBoundsException ex3){
                    try{
                        return projection.toGeo(x, y + offset);
                    }catch (OutOfProjectionBoundsException ex4){
                        try{
                            return projection.toGeo(x, y - offset);
                        }catch (OutOfProjectionBoundsException ex5){
                            try{
                                return projection.toGeo(x + offset, y + offset);
                            }catch (OutOfProjectionBoundsException ex6){
                                try{
                                    return projection.toGeo(x - offset, y - offset);
                                }catch (OutOfProjectionBoundsException ex7){
                                    LogUtils.log(ex7);
                                    return null;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static double[] fromGeo(double x, double y) throws OutOfProjectionBoundsException {
        return projection.fromGeo(x, y);
    }
}
