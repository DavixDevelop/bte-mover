package com.davixdevelop.btemover.model;

/**
 * Represent the model of the JList query model
 *
 * @author DavixDevelop
 */
public class QueriedRegion  {
    private String name;
    private int num3d;
    private int stat;

    public QueriedRegion(String _name, int _num3d, int _stat){
        super();
        name = _name;
        num3d = _num3d;
        stat = _stat;
    }

    /**
     *
     * @return The name of the region
     */
    public String getName() {
        return name;
    }

    /**
     * @return The number of 3d region's
     */
    public int getNum3d() {
        return num3d;
    }

    /**
     * Set's the number of 3d regions
     * @param _num3d
     */
    public void setNum3d(int _num3d) {
        num3d = _num3d;
    }

    /*

         */

    /**
     * @return The status of the query item:
     *              0. None
     *              1. Downloading
     *              2. Uploading
     *              3. Done
     *              4. Failed
     */
    public int getStatus() {
        return stat;
    }

    /**
     * Set's the status of the query item
     * @param _status Values must be one of the following:
     *               0. None
     *               1. Downloading
     *               2. Uploading
     *               3. Done
     *               4. Failed
     */
    public void setStatus(int _status) {
        stat = _status;
    }
}
