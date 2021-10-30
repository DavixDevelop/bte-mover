package com.davixdevelop.btemover.model;

import com.davixdevelop.btemover.utils.LogUtils;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.*;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

/**
 * Represents an region SFTP client (that uses the Jshc library).
 * It can get an list of regions from the server, download/upload 2dr and 3dr region files.
 * It can also test if the sftp server is a valid server that contains both region2d and region3d folder
 * It only works with the sftp protocol
 *
 * @author DavixDevelop
 */
public class JschSFTPRegionClient implements IRegionFTPClient {
    private FTPOptions ftpOptions;
    private Session session;
    private ChannelSftp channelSftp;
    private boolean isValidSession;

    public JschSFTPRegionClient(FTPOptions _ftpOptions){
        super();
        ftpOptions = _ftpOptions;
        isValidSession = false;
        session = null;
        channelSftp = null;
    }

    /**
     * Set the session for the client
     */
    private void setSession(){
        try {
            JSch.setLogger(new JschRegionLogger());
            JSch jSch = new JSch();
            session = jSch.getSession(ftpOptions.getUser(), ftpOptions.getServer(), ftpOptions.getPort());
            session.setPassword(ftpOptions.getPassword());
            //Disable checking for host file
            session.setConfig("StrictHostKeyChecking","no");
            //Connection timeout
            session.setTimeout(5000);
            isValidSession = true;
        }catch (Exception ex){
            isValidSession = false;
            LogUtils.log(ex);
        }
    }

    /**
     * Set's the session if it hasn't been set yet, connect's the session and the sftp channel,
     * therefore opening an connection to the sftp server
     * @return True or False depending if the connection was established
     * @throws Exception If an error occurred while connection to the server
     */
    @Override
    public boolean open() throws Exception {
        if(!isValidSession)
            setSession();

        if(isValidSession){
            try{
                 session.connect();
                 //Data transfer timeout
                 session.setTimeout(9000);
                 //Set buffer size
                 session.setConfig("max_input_buffer_size", String.valueOf(1024 * 1024));
                 channelSftp = (ChannelSftp)session.openChannel("sftp");
                 channelSftp.connect();
                 return true;

            }catch (Exception ex){
                LogUtils.log(ex);
                return false;
            }
        }
        return false;
    }

    /**
     * Disconnects both the session and sftp channel, therefore
     * closeing the connection between the server and client
     * @throws Exception If an error occurred while disconnecting to the server
     */
    @Override
    public void close() throws Exception {
        if(isValidSession) {
            channelSftp.disconnect();
            session.disconnect();
            isValidSession = false;
        }
    }

    /**
     *
     * @return Get's and hashtable of regions from the sftp server
     */
    @Override
    public Hashtable<String, Region> getRegions() {
        try{
            if(!open())
                return null;

            //Get list of region2d and region3d files at once and close connection
            if(ftpOptions.getPath() != null)
                if(ftpOptions.getPath().length() != 0)
                    channelSftp.cd("/" + ftpOptions.getPath() + "/region2d");
                else
                    channelSftp.cd("/region2d");
            else
                channelSftp.cd("/region2d");

            Vector<ChannelSftp.LsEntry> files = channelSftp.ls("*.2dr");

            if(ftpOptions.getPath() != null)
                if(ftpOptions.getPath().length() != 0)
                    channelSftp.cd("/" + ftpOptions.getPath() + "/region3d");
                else
                    channelSftp.cd("/region3d");
            else
                channelSftp.cd("/region3d");

            Vector<ChannelSftp.LsEntry> files2 = channelSftp.ls("*.3dr");
            close();

            //Store regions in a temp hashtable to quickly add 3d regions later
            Hashtable<String, Region> regions = new Hashtable<String, Region>();
            for(ChannelSftp.LsEntry file:files){
                Matcher matcher = region2dValidator.matcher(file.getFilename());
                if(matcher.find()){
                    MatchResult result = matcher.toMatchResult();
                    int x = Integer.parseInt(result.group(1));
                    int z = Integer.parseInt(result.group(2));
                    Region region = new Region(x, z);
                    if(region.isValid())
                        regions.put(region.getName(), region);
                }
            }

            for (ChannelSftp.LsEntry file: files2) {
                Matcher matcher = region3dValidator.matcher(file.getFilename());
                if(matcher.find() && file.getAttrs().getSize() > 16384){
                    int x = Integer.parseInt(matcher.group(1)) >> 1;
                    int z = Integer.parseInt(matcher.group(3)) >> 1;
                    int y = Integer.parseInt(matcher.group(2));
                    if(regions.containsKey(x + "." + z)){
                        Region region = regions.get(x + "." + z);

                        region.addRegion3d(matcher.group(1) + "." + y + "." + matcher.group(3));
                        regions.put(region.getName(), region);
                    }
                }
            }

            return regions;


        }catch (Exception ex){
            LogUtils.log(ex.toString());
            return null;
        }
    }

    /**
     * Downloads the 2d region file from the supplied region
     * @param region The region of which 2d region file to download
     * @param targetFile Where to download the 2d region file
     * @return The success of the download
     */
    @Override
    public boolean download2DRegion(Region region, String targetFile) {
        boolean result;
        try{
            FileOutputStream outputStream = new FileOutputStream(targetFile);
            String source = ((ftpOptions.getPath() != null) ? (ftpOptions.getPath().length() != 0) ?
                    "/" + ftpOptions.getPath() + "/" : "/" : "/") + "region2d/" + (region.getX() + "." + region.getZ() + ".2dr");

            channelSftp.get(source, outputStream);

            outputStream.flush();
            outputStream.close();

            result = true;
        }catch (Exception ex){
            LogUtils.log(ex);
            result = false;
        }

        return result;
    }

    /**
     * Downloads the 3d region files from the supplied region3d name
     * @param region3d The name the 3d region
     * @param targetFile Where to download the 3d region file
     * @return The success of the download
     */
    @Override
    public boolean download3DRegion(String region3d, String targetFile) {
        boolean result;
        try{
            FileOutputStream outputStream = new FileOutputStream(targetFile);
            String source = ((ftpOptions.getPath() != null) ? (ftpOptions.getPath().length() != 0) ?
                    "/" + ftpOptions.getPath() + "/" : "/" : "/") + "region3d/" + region3d + ".3dr";

            channelSftp.get(source, outputStream);

            outputStream.flush();
            outputStream.close();

            result = true;
        }catch (Exception ex){
            LogUtils.log(ex);
            result = false;
        }

        return result;
    }

    /**
     * Uploads the 2d region file from the supplied region2DPath
     * @param region2DPath The path to 2d region file to upload
     * @param region The region to upload
     * @return The success of the upload
     */
    @Override
    public boolean upload2DRegion(String region2DPath, Region region) {
        boolean result;
        try{
            FileInputStream inputStream = new FileInputStream(region2DPath);
            String target = ((ftpOptions.getPath() != null) ? (ftpOptions.getPath().length() != 0) ? "/" + ftpOptions.getPath() + "/" : "/" : "/") + "region2d/" + (region.getX() + "." + region.getZ() + ".2dr");

            channelSftp.put(inputStream, target);

            inputStream.close();

            result = true;
        }catch (Exception ex){
            LogUtils.log(ex);
            result = false;
        }

        return  result;
    }

    /**
     * Uploads the 3d region file from the supplied region3DPath
     * @param region3DPath The path to 3d region file to upload
     * @param region3DName The name of 3d region to upload
     * @return The success of the upload
     */
    @Override
    public boolean upload3DRegion(String region3DPath, String region3DName) {
        boolean result;
        try{
            FileInputStream inputStream = new FileInputStream(region3DPath);
            String target = ((ftpOptions.getPath() != null) ? (ftpOptions.getPath().length() != 0) ? "/" + ftpOptions.getPath() + "/" : "/" : "/") + "region3d/" + region3DName + ".3dr";

            channelSftp.put(inputStream, target);

            inputStream.close();

            result = true;
        }catch (Exception ex){
            LogUtils.log(ex);
            result = false;
        }

        return  result;
    }

    /**
     * Test if the server can be connected to with the ftpOptions and the server contains the
     * region2d and region3d folder
     * @return The success of the test
     */
    @Override
    public boolean testClient() {
        try {
            if(open()){

                if(ftpOptions.getPath() != null)
                    if(ftpOptions.getPath().length() != 0)
                        channelSftp.cd("/" + ftpOptions.getPath() + "/region2d");
                    else
                        channelSftp.cd("/region2d");
                else
                    channelSftp.cd("/region2d");

                if(ftpOptions.getPath() != null)
                    if(ftpOptions.getPath().length() != 0)
                        channelSftp.cd("/" + ftpOptions.getPath() + "/region3d");
                    else
                        channelSftp.cd("/region3d");
                else
                    channelSftp.cd("/region3d");

                close();
                return true;
            }else
                return false;
        }catch (Exception ex){
            LogUtils.log(ftpOptions.getServer() + ":" + ex);
            return false;
        }
    }

    /**
     * Send an NOOP command to the server
     * @return The success of the command
     * @throws Exception If an error occurred during the execution of the command
     */
    @Override
    public boolean sendNoOpCommand() throws Exception {
        session.sendKeepAliveMsg();
        return true;
    }

    /**
     * Get's the content of the 2d region file on the remote server
     * @param region The region to get
     * @return Byte array representing the content of the 2d region
     */
    @Override
    public byte[] get2DRegion(Region region) {
        try{
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(RegionFTPClient.OUTPUT_BUFFER_SIZE);

            String source = ((ftpOptions.getPath() != null) ? (ftpOptions.getPath().length() != 0) ?
                    "/" + ftpOptions.getPath() + "/" : "/" : "/") + "region2d/" + (region.getX() + "." + region.getZ() + ".2dr");

            try(InputStream inputStream = channelSftp.get(source)){
                byte[] buff = new byte[4096];
                int readBytes;
                while((readBytes = inputStream.read(buff)) != -1){
                    byteArrayOutputStream.write(buff, 0, readBytes);
                }

                inputStream.close();

                byteArrayOutputStream.flush();
                byteArrayOutputStream.close();
                return byteArrayOutputStream.toByteArray();
            }

        }catch (Exception ex){
            LogUtils.log(ex);
            return null;
        }
    }

    /**
     * Get's the content of the 2d region file on the remote server
     * @param region3DName The name of the 3d region to get
     * @return Byte array representing the content of the 3d region
     */
    @Override
    public byte[] get3DRegion(String region3DName) {
        try{
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(RegionFTPClient.OUTPUT_BUFFER_SIZE);

            String source = ((ftpOptions.getPath() != null) ? (ftpOptions.getPath().length() != 0) ?
                    "/" + ftpOptions.getPath() + "/" : "/" : "/") + "region3d/" + region3DName + ".3dr";

            try(InputStream inputStream = channelSftp.get(source)){
                byte[] buff = new byte[4096];
                int readBytes;
                while((readBytes = inputStream.read(buff)) != -1){
                    byteArrayOutputStream.write(buff, 0, readBytes);
                }

                inputStream.close();

                byteArrayOutputStream.flush();
                byteArrayOutputStream.close();
                return byteArrayOutputStream.toByteArray();
            }

        }catch (Exception ex){
            LogUtils.log(ex);
            return null;
        }
    }

    /**
     * Uploads a 2d region with it's content to a server
     * @param content The byte array content of the 2d region
     * @param region The target region
     * @return The success of the upload
     */
    @Override
    public boolean put2DRegion(byte[] content, Region region) {
        final boolean[] result = {false};
        try{
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content);

            String target = ((ftpOptions.getPath() != null) ? (ftpOptions.getPath().length() != 0) ? "/" + ftpOptions.getPath() + "/" : "/" : "/") + "region2d/" + (region.getX() + "." + region.getZ() + ".2dr");

            try(OutputStream outputStream = channelSftp.put(target, ChannelSftp.OVERWRITE)){
                byte[] buff = new byte[4096];
                int readBytes;
                while((readBytes = byteArrayInputStream.read(buff)) != -1){
                    outputStream.write(buff, 0, readBytes);
                }

                outputStream.flush();
                outputStream.close();

                result[0] = true;
            }

        }catch (Exception ex){
            LogUtils.log(ex);
            result[0] = false;
        }

        return result[0];
    }

    /**
     * Uploads a 3d region with it's content to a server
     * @param content The byte array content of the 3d region
     * @param region3DName The target 3d region
     * @return The success of the upload
     */
    @Override
    public boolean put3DRegion(byte[] content, String region3DName) {
        final boolean[] result = {false};
        try{
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content);

            String target = ((ftpOptions.getPath() != null) ? (ftpOptions.getPath().length() != 0) ? "/" + ftpOptions.getPath() + "/" : "/" : "/") + "region3d/" + region3DName + ".3dr";

            try(OutputStream outputStream = channelSftp.put(target, ChannelSftp.OVERWRITE)){
                byte[] buff = new byte[4096];
                int readBytes;
                while((readBytes = byteArrayInputStream.read(buff)) != -1){
                    outputStream.write(buff, 0, readBytes);
                }

                outputStream.flush();
                outputStream.close();

                result[0] = true;
            }

        }catch (Exception ex){
            LogUtils.log(ex);
            result[0] = false;
        }

        return result[0];
    }

    public static class JschRegionLogger implements com.jcraft.jsch.Logger {
        static java.util.Hashtable name=new java.util.Hashtable();
        static{
            name.put(new Integer(DEBUG), "DEBUG: ");
            name.put(new Integer(INFO), "INFO: ");
            name.put(new Integer(WARN), "WARN: ");
            name.put(new Integer(ERROR), "ERROR: ");
            name.put(new Integer(FATAL), "FATAL: ");
        }
        public boolean isEnabled(int level){
            return true;
        }
        public void log(int level, String message){
            LogUtils.log(name.get(new Integer(level)) + message);
        }
    }
}
