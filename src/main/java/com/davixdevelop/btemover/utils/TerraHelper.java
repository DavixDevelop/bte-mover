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
        //x += 0.5;
        //y += 0.5;
        try{
            double[] try1 = projection.toGeo(x, y);
            return try1;
        }catch (OutOfProjectionBoundsException ex){
            try{
                double[] try1 = projection.toGeo(x + offset, y);
                return try1;
            }catch (OutOfProjectionBoundsException ex2){
                try{
                    double[] try1 = projection.toGeo(x - offset, y);
                    return try1;
                }catch (OutOfProjectionBoundsException ex3){
                    try{
                        double[] try1 = projection.toGeo(x, y + offset);
                        return try1;
                    }catch (OutOfProjectionBoundsException ex4){
                        try{
                            double[] try1 = projection.toGeo(x, y - offset);
                            return try1;
                        }catch (OutOfProjectionBoundsException ex5){
                            try{
                                double[] try1 = projection.toGeo(x + offset, y + offset);
                                return try1;
                            }catch (OutOfProjectionBoundsException ex6){
                                try{
                                    double[] try1 = projection.toGeo(x - offset, y - offset);
                                    return try1;
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
