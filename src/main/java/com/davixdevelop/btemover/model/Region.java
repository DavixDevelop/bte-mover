package com.davixdevelop.btemover.model;

import com.davixdevelop.btemover.utils.TerraHelper;
import com.davixdevelop.btemover.utils.UIUtils;
import net.buildtheearth.terraminusminus.projection.OutOfProjectionBoundsException;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the region with it's accosted 3rd regions
 * Also stores the wkt (POLYGON) of the region and the point of the polygon in a list
 */
public class Region {
    private int x;
    private int z;
    private RectanglePoint[] points;
    private String wkt;
    private List<String> region3d;

    private String name;

    public String getName() {
        return name;
    }

    public Region(int _x, int _z){
        x = _x;
        z = _z;
        name = x + "." + z;
        region3d = new ArrayList<String>();
       _isValid = calculateRectangle();
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public RectanglePoint[] getPoints() {
        return points;
    }

    public void addRegion3d(String _region3d) {
        region3d.add(_region3d);
    }
    public void setRegion3d(List<String> _region3d) { region3d = _region3d; }
    public List<String> getRegion3d() {
        return region3d;
    }
    public int getRegion3dCount(){return region3d.size();}

    private boolean _isValid;
    public boolean isValid() {
        return _isValid;
    }

    /**
     * Calculates the POLYGON on the region, by converting the
     * corner points of the region in the Modified Airocean projection,
     * (based on the Dymaxion/Airocean projection) to the WGS84 projection
     * @return If the region inside the projection bounds
     */
    public boolean calculateRectangle() {
        //Get blocks coordinates for region
        double[] p1 = new double[]{x * 512,z * 512};
        double[] p2 = new double[]{ (x + 1) * 512, p1[1]};
        double[] p3 = new double[]{ p2[0], (z + 1) * 512};
        double[] p4 = new double[]{p1[0], p3[1]};

        //Convert block coordinates to geo coordinates in WGS84

        p1 = TerraHelper.toGeo(p1[0], p1[1]);
        if(p1 == null)
            return false;
        p2 = TerraHelper.toGeo(p2[0], p2[1]);
        if(p2 == null)
            return false;
        p3 = TerraHelper.toGeo(p3[0], p3[1]);
        if(p3 == null)
            return false;
        p4 = TerraHelper.toGeo(p4[0], p4[1]);
        if(p4 == null)
            return false;

        wkt = "POLYGON((" + Double.toString(p1[0]) + " " + Double.toString(p1[1]) +
                ", " + Double.toString(p2[0]) + " " + Double.toString(p2[1]) +
                ", " + Double.toString(p3[0]) + " " + Double.toString(p3[1]) +
                ", " + Double.toString(p4[0]) + " " + Double.toString(p4[1]) +
                ", " + Double.toString(p1[0]) + " " + Double.toString(p1[1]) +"))";


        points = new RectanglePoint[]{
                new RectanglePoint(p1[0], p1[1]),
                new RectanglePoint(p2[0], p2[1]),
                new RectanglePoint(p3[0], p3[1]),
                new RectanglePoint(p4[0], p4[1])
        };

        return true;
    }

    public String getWkt(){
        return wkt;
    }
}
