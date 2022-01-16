package com.davixdevelop.btemover.model;

import java.util.Hashtable;
import java.util.regex.Pattern;

/**
 * Represents the interface of a region ftp client, that dictates which method one such client should implement
 *
 * @author DavixDevelop
 */
public interface IRegionFTPClient {
    Pattern region2dValidator = Pattern.compile("^(-?\\d+)[.](-?\\d+)[.2dr]", Pattern.CASE_INSENSITIVE);
    Pattern region3dValidator = Pattern.compile("^(-?\\d+)[.](-?\\d+)[.](-?\\d+)[.3dr]", Pattern.CASE_INSENSITIVE);

    boolean open() throws Exception;
    void close() throws Exception;
    Hashtable<String, Region> getRegions();
    //boolean download2DRegion(Region region, String targetFile);
    //boolean download3DRegion(String region3d, String targetFile);
    //boolean upload2DRegion(String region2DPath, Region region);
    //boolean upload3DRegion(String region3DPath, String region3DName);
    boolean testClient();
    boolean sendNoOpCommand() throws Exception;

    byte[] get2DRegion(Region region);
    byte[] get3DRegion(String region3DName);
    boolean put2DRegion(byte[] content, Region region);
    boolean put3DRegion(byte[] content, String region3DName);
}
