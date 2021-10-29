package com.davixdevelop.btemover.model;


/**
 * Represents an timer model that stores the current progress and start time of the transfer
 *
 * @author DavixDevelop
 */
public class TimerModel {
    private int total2DRegions;
    public int getTotal2DRegions() {
        return total2DRegions;
    }

    private int total3DRegions;
    public int getTotal3DRegions() {
        return total3DRegions;
    }

    private int totalRegions;
    public int getTotalRegions() {
        return totalRegions;
    }

    private int progress2DRegions = 0;
    private int progress3DRegions = 0;

    private long startTime;

    public long getStartTime() { return startTime;
    }

    public TimerModel(int _total2DRegions, int _total3DRegions){
        total2DRegions = _total2DRegions;
        total3DRegions = _total3DRegions;
        totalRegions = total2DRegions + total3DRegions;
        startTime = System.currentTimeMillis();;
    }



    public void Increase2DRegions(){progress2DRegions++;}
    public int getProgress2DRegions() {
        return progress2DRegions;
    }

    public void Increase3DRegions(){progress3DRegions++;}
    public int getProgress3DRegions() {
        return progress3DRegions;
    }

    public void DecreaseTotal3DRegions() {
        total3DRegions--;
        totalRegions = total2DRegions + total3DRegions;
    }
}
