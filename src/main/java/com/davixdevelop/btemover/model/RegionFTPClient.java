package com.davixdevelop.btemover.model;

import java.util.Hashtable;
import java.util.Objects;

/**
 * Represents a wrapper for all the ftp clients, by choosing which one to use depending on the
 * supplied ftpOption protocol
 * @author DavixDevelop
 */
public class RegionFTPClient implements IRegionFTPClient {
    public static int OUTPUT_BUFFER_SIZE = 1024 * 1024;

    private IRegionFTPClient ftpClient;

    /**
     * Creates an RegionFTPClient and decides which client to use depending on the protocol
     * described in the supplied _ftpOptions
     * @param _ftpOptions The ftp options to create the client from
     */
    public RegionFTPClient(FTPOptions _ftpOptions){
        if(Objects.equals(_ftpOptions.getProtocol(), "ftp") || Objects.equals(_ftpOptions.getProtocol(), "ftps") || Objects.equals(_ftpOptions.getProtocol(), "ftpes"))
            ftpClient = new Ftp4jRegionFTPClient(_ftpOptions);
        else if(Objects.equals(_ftpOptions.getProtocol(),"sftp"))
            ftpClient = new JschSFTPRegionClient(_ftpOptions);
        else if(Objects.equals(_ftpOptions.getProtocol(), "file"))
            ftpClient = new FileClient(_ftpOptions);
    }

    /**
     * Open's an connection to the server
     * @return True or False depending if the connection was established
     * @throws Exception If an error occurred while connection to the server
     */
    public boolean open() throws Exception {
        return ftpClient.open();
    }

    /**
     * Closes the connection to the server
     * @throws Exception If an error occurred while disconnecting to the server
     */
    public void close() throws Exception {
        ftpClient.close();
    }

    /**
     * Get's and hashtable of regions from the server
     * @return The hashtable of regions returned from the server
     */
    public Hashtable<String,Region> getRegions(){
        return ftpClient.getRegions();
    }

    /**
     * Downloads the 2d region file from the supplied region
     * @param region The region of which 2d region file to download
     * @param targetFile Where to download the 2d region file
     * @return The success of the download
     */
    /*public boolean download2DRegion(Region region, String targetFile) {
        return ftpClient.download2DRegion(region, targetFile);
    }*/

    /**
     * Downloads the 3d region files from the supplied region3d name
     * @param region3d The name the 3d region
     * @param targetFile Where to download the 3d region file
     * @return The success of the download
     */
    /*public boolean download3DRegion(String region3d, String targetFile) {
        return ftpClient.download3DRegion(region3d, targetFile);
    }*/

    /**
     * Uploads the 2d region file from the supplied region2DPath
     * @param region2DPath The path to 2d region file to upload
     * @param region The region to upload
     * @return The success of the upload
     */
    /*public boolean upload2DRegion(String region2DPath, Region region) {
        return ftpClient.upload2DRegion(region2DPath, region);
    }*/

    /**
     * Uploads the 2d region file from the supplied region2DPath
     * @param region3DPath The path to 3d region file to upload
     * @param region3DName The name of 3d region to upload
     * @return The success of the upload
     */
    /*public boolean upload3DRegion(String region3DPath, String region3DName) {
        return ftpClient.upload3DRegion(region3DPath, region3DName);
    }*/

    /**
     * The internal call to the chosen client testClient method
     * @return The success of the test
     */
    @Override
    public boolean testClient() {
        return ftpClient.testClient();
    }

    /**
     * Send an NOOP command to the server
     * @return The success of the command
     * @throws Exception If an error occurred during the execution of the command
     */
    public boolean sendNoOpCommand() throws Exception{
        return ftpClient.sendNoOpCommand();
    }

    /**
     * Get's the content of the 2d region file on the remote server
     * @param region The region to get
     * @return Byte array representing the content of the 2d region
     */
    @Override
    public byte[] get2DRegion(Region region) {
        return ftpClient.get2DRegion(region);
    }

    /**
     * Get's the content of the 3d region file on the remote server
     * @param region3DName The name of the 3d region to get
     * @return Byte array representing the content of the 3d region
     */
    @Override
    public byte[] get3DRegion(String region3DName) {
        return ftpClient.get3DRegion(region3DName);
    }

    /**
     * Uploads a 2d region with it's content to a server
     * @param content The byte array content of the 2d region
     * @param region The target region
     * @return The success of the upload
     */
    @Override
    public boolean put2DRegion(byte[] content, Region region) {
        return ftpClient.put2DRegion(content, region);
    }

    /**
     * Uploads a 3d region with it's content to a server
     * @param content The byte array content of the 3d region
     * @param region3DName The target 3d region
     * @return The success of the upload
     */
    @Override
    public boolean put3DRegion(byte[] content, String region3DName) {
        return ftpClient.put3DRegion(content, region3DName);
    }

    @Override
    public boolean delete2DRegion(Region region) {
        return ftpClient.delete2DRegion(region);
    }

    @Override
    public boolean delete3DRegion(String region3DName) {
        return ftpClient.delete3DRegion(region3DName);
    }

    /**
     * Test if the server can be connected to with the ftpOptions and the server contains the
     * region2d and region3d folder
     * @return The success of the test
     */
    public static boolean testConnection(FTPOptions _ftpOptions){
        RegionFTPClient regionFTPClient = new RegionFTPClient(_ftpOptions);
        return regionFTPClient.testClient();
    }
}
