package com.davixdevelop.btemover.model;

import com.davixdevelop.btemover.utils.LogUtils;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPListParseException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Hashtable;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

/**
 * Represents an region FTP client (that extends the ftp4j FTPClient).
 * It can get an list of regions from the server, download/upload 2dr and 3dr region files.
 * It can also test if the ftp server is a valid server that contains both region2d and region3d folder
 * It works with the ftp, ftps and ftpes protocol.
 *
 * @author DavixDevelop
 */
public class Ftp4jRegionFTPClient extends FTPClient implements IRegionFTPClient{
    private final FTPOptions ftpOptions;

    //trust manager to trust every certificate given by a remote server
    private static TrustManager[] TRUST_MANAGERS = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) { }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) { }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }};

    public Ftp4jRegionFTPClient(FTPOptions _ftpOptions){
        super();
        ftpOptions = _ftpOptions;
        //System.setProperty("ftp4j.activeDataTransfer.acceptTimeout", "5000");



        if(Objects.equals(ftpOptions.getProtocol(), "ftps") || Objects.equals(ftpOptions.getProtocol(), "ftpes")){
            SSLContext sslContext;
            try{
                sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, TRUST_MANAGERS, new SecureRandom());

                SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                setSSLSocketFactory(sslSocketFactory);

                if(Objects.equals(ftpOptions.getProtocol(), "ftps"))
                    setSecurity(FTPClient.SECURITY_FTPS);
                else if(Objects.equals(ftpOptions.getProtocol(), "ftpes"))
                    setSecurity(FTPClient.SECURITY_FTPES);

            }catch (Exception ex){
                LogUtils.log(ex);
            }
        }
    }

    /**
     * Open's an connection to the ftp server
     * @return True or False depending if the connection was established
     * @throws Exception If an error occurred while connection to the server
     */
    @Override
    public boolean open() throws Exception {
        try{
            String[] message = connect(ftpOptions.getServer(), ftpOptions.getPort());
            login(ftpOptions.getUser(), ftpOptions.getPassword());

            setType(FTPClient.TYPE_BINARY);
            return true;

        }catch(Exception ex){
            LogUtils.log(ex);
            return false;
        }
    }

    /**
     * Closes the connection to the ftp server
     * @throws Exception If an error occurred while disconnecting to the server
     */
    @Override
    public void close() throws Exception {
        disconnect(true);
    }

    /**
     *
     * @return Get's and hashtable of regions from the ftp server
     */
    @Override
    public Hashtable<String, Region> getRegions() {
        try {
            if(!open())
                return null;

            if(ftpOptions.getPath() != null){
                if(ftpOptions.getPath().length() != 0)
                    changeDirectory("/" + ftpOptions.getPath());
            }

            //Store regions in a temp hashtable to quickly add 3d regions later
            Hashtable<String, Region> regions = new Hashtable<>();

            try {
                //Get list of region2d and region3d file at once and close connection
                changeDirectory("region2d");
                FTPFile[] files = list();
                changeDirectoryUp();
                changeDirectory("region3d");
                FTPFile[] files2 = list();
                close();

                for(FTPFile file : files){
                    Matcher matcher = region2dValidator.matcher(file.getName());
                    if(matcher.find()){
                        MatchResult result = matcher.toMatchResult();
                        int x = Integer.parseInt(result.group(1));
                        int z = Integer.parseInt(result.group(2));
                        Region region = new Region(x, z);
                        if(region.isValid())
                            regions.put(region.getName(), region);
                    }
                }

                for(FTPFile file : files2){
                    Matcher matcher = region3dValidator.matcher(file.getName());
                    if(matcher.find() && file.getSize() > 16384){
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

            }catch (FTPListParseException ex) {
                LogUtils.log("Server doesn't support LIST, switching to legacy mode (only list file names)");

                regions = new Hashtable<>();

                if(!isConnected())
                    if(!open())
                        return null;

                if(ftpOptions.getPath() != null){
                    if(ftpOptions.getPath().length() != 0)
                        changeDirectory("/" + ftpOptions.getPath());
                }

                //Get list of region2d and region3d file names at once and close connection
                changeDirectory("region2d");
                String[] fileNames = listNames();
                changeDirectoryUp();
                changeDirectory("region3d");
                String[] fileNames2 = listNames();
                close();


                for (String file:
                        fileNames) {
                    Matcher matcher = region2dValidator.matcher(file);
                    if(matcher.find()){
                        MatchResult result = matcher.toMatchResult();
                        int x = Integer.parseInt(result.group(1));
                        int z = Integer.parseInt(result.group(2));
                        Region region = new Region(x, z);
                        region.setLegacy(true);
                        if(region.isValid())
                            regions.put(region.getName(), region);
                    }
                }

                for (String file: fileNames2) {
                    Matcher matcher = region3dValidator.matcher(file);
                    if(matcher.find()){
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
            }

            return regions;


        }catch (Exception ex){
            LogUtils.log(ex);
            return null;
        }
    }

    /**
     * Downloads the 2d region file from the supplied region
     * @param region The region of which 2d region file to download
     * @param targetFile Where to download the 2d region file
     * @return The success of the download
     */
    /*@Override
    public boolean download2DRegion(Region region, String targetFile) {
        final boolean[] result = {false};
        try{
            FileOutputStream outputStream = new FileOutputStream(targetFile);
            String source = ((ftpOptions.getPath() != null) ? (ftpOptions.getPath().length() != 0) ?
                    "/" + ftpOptions.getPath() + "/" : "/" : "/") + "region2d/" + (region.getX() + "." + region.getZ() + ".2dr");

            download(source, outputStream, 0, new FTPDataTransferListener() {
                @Override
                public void started() {

                }

                @Override
                public void transferred(int length) {

                }

                @Override
                public void completed() {
                    result[0] = true;
                }

                @Override
                public void aborted() {
                    result[0] = false;
                }

                @Override
                public void failed() {
                    result[0] = false;
                }
            });

            outputStream.flush();
            outputStream.close();
        }catch (Exception ex){
            result[0] = false;
            LogUtils.log(ex);
        }

        return result[0];
    }
     */
    /**
     * Downloads the 3d region files from the supplied region3d name
     * @param region3d The name the 3d region
     * @param targetFile Where to download the 3d region file
     * @return The success of the download
     */
    /*@Override
    public boolean download3DRegion(String region3d, String targetFile) {
        final boolean[] result = {false};
        try{
            FileOutputStream outputStream = new FileOutputStream(targetFile);
            String source = ((ftpOptions.getPath() != null) ? (ftpOptions.getPath().length() != 0) ?
                    "/" + ftpOptions.getPath() + "/" : "/" : "/") + "region3d/" + region3d + ".3dr";

            download(source, outputStream, 0, new FTPDataTransferListener() {
                @Override
                public void started() {

                }

                @Override
                public void transferred(int length) {

                }

                @Override
                public void completed() {
                    result[0] = true;
                }

                @Override
                public void aborted() {
                    result[0] = false;
                }

                @Override
                public void failed() {
                    result[0] = false;
                }
            });

            outputStream.flush();
            outputStream.close();
        }catch (Exception ex){
            result[0] = false;
            LogUtils.log(ex);
        }

        return result[0];
    }
    */
    /**
     * Uploads the 2d region file from the supplied region2DPath
     * @param region2DPath The path to 2d region file to upload
     * @param region The region to upload
     * @return The success of the upload
     */
    /*@Override
    public boolean upload2DRegion(String region2DPath, Region region) {
        boolean[] result = {false};
        try{
            FileInputStream inputStream = new FileInputStream(region2DPath);
            String target = ((ftpOptions.getPath() != null) ? (ftpOptions.getPath().length() != 0) ? "/" + ftpOptions.getPath() + "/" : "/" : "/") + "region2d/" + (region.getX() + "." + region.getZ() + ".2dr");

            upload(target, inputStream, 0, 0, new FTPDataTransferListener() {
                @Override
                public void started() {

                }

                @Override
                public void transferred(int length) {

                }

                @Override
                public void completed() {
                    result[0] = true;
                }

                @Override
                public void aborted() {
                    result[0] = false;
                }

                @Override
                public void failed() {
                    result[0] = false;
                }
            });

            inputStream.close();
        }catch (Exception ex){
            LogUtils.log(ex);
            result[0] = false;
        }
        return result[0];
    }
    */
    /**
     * Uploads the 3d region file from the supplied region3DPath
     * @param region3DPath The path to 3d region file to upload
     * @param region3DName The name of 3d region to upload
     * @return The success of the upload
     */
    /*@Override
    public boolean upload3DRegion(String region3DPath, String region3DName) {
        boolean[] result = {false};
        try{
            FileInputStream inputStream = new FileInputStream(region3DPath);
            String target = ((ftpOptions.getPath() != null) ? (ftpOptions.getPath().length() != 0) ? "/" + ftpOptions.getPath() + "/" : "/" : "/") + "region3d/" + region3DName + ".3dr";

            upload(target, inputStream, 0, 0, new FTPDataTransferListener() {
                @Override
                public void started() {

                }

                @Override
                public void transferred(int length) {

                }

                @Override
                public void completed() {
                    result[0] = true;
                }

                @Override
                public void aborted() {
                    result[0] = false;
                }

                @Override
                public void failed() {
                    result[0] = false;
                }
            });

            inputStream.close();
        }catch (Exception ex){
            LogUtils.log(ex);
            result[0] = false;
        }
        return result[0];
    }
    */
    /**
     * Test if the server can be connected to with the ftpOptions and the server contains the
     * region2d and region3d folder
     * @return The success of the test
     */
    @Override
    public boolean testClient() {
        try{
            if(open()){

                if (ftpOptions.getPath() != null) {
                    if(ftpOptions.getPath().length() != 0)
                        changeDirectory("/" + ftpOptions.getPath());
                }

                changeDirectory("region2d");
                changeDirectoryUp();
                changeDirectory("region3d");

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
        noop();
        return true;
    }

    /**
     * Get's the content of the 2d region file on the remote server
     * @param region The region to get
     * @return Byte array representing the content of the 2d region
     */
    @Override
    public byte[] get2DRegion(Region region) {
        byte[] content = null;

        final boolean[] result = {false};
        try{
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(RegionFTPClient.OUTPUT_BUFFER_SIZE);
            String source = ((ftpOptions.getPath() != null) ? (ftpOptions.getPath().length() != 0) ?
                    "/" + ftpOptions.getPath() + "/" : "/" : "/") + "region2d/" + (region.getX() + "." + region.getZ() + ".2dr");

            download(source, byteArrayOutputStream, 0, new FTPDataTransferListener() {
                @Override
                public void started() {

                }

                @Override
                public void transferred(int length) {

                }

                @Override
                public void completed() {
                    result[0] = true;
                }

                @Override
                public void aborted() {
                    result[0] = false;
                }

                @Override
                public void failed() {
                    result[0] = false;
                }
            });

            //Read byte output stream to array
            if(result[0]){
                byteArrayOutputStream.flush();
                byteArrayOutputStream.close();
                content = byteArrayOutputStream.toByteArray();
            }

        }catch (Exception ex){
            content = null;
            LogUtils.log(ex);
        }

        return content;
    }

    /**
     * Get's the content of the 2d region file on the remote server
     * @param region3DName The name of the 3d region to get
     * @return Byte array representing the content of the 3d region
     */
    @Override
    public byte[] get3DRegion(String region3DName) {
        byte[] content = null;

        final boolean[] result = {false};
        try{
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(RegionFTPClient.OUTPUT_BUFFER_SIZE);

            String source = ((ftpOptions.getPath() != null) ? (ftpOptions.getPath().length() != 0) ?
                    "/" + ftpOptions.getPath() + "/" : "/" : "/") + "region3d/" + region3DName + ".3dr";

            download(source, byteArrayOutputStream, 0, new FTPDataTransferListener() {
                @Override
                public void started() {

                }

                @Override
                public void transferred(int length) {

                }

                @Override
                public void completed() {
                    result[0] = true;
                }

                @Override
                public void aborted() {
                    result[0] = false;
                }

                @Override
                public void failed() {
                    result[0] = false;
                }
            });

            //Read byte output stream to array
            if(result[0]){
                byteArrayOutputStream.flush();
                byteArrayOutputStream.close();
                content = byteArrayOutputStream.toByteArray();
            }




        }catch (Exception ex){
            content = null;
            LogUtils.log(ex);
        }

        return content;
    }


    /**
     * Uploads a 2d region with it's content to a server
     * @param content The byte array content of the 2d region
     * @param region The target region
     * @return The success of the upload
     */
    @Override
    public boolean put2DRegion(byte[] content, Region region) {
        boolean[] result = {false};
        try{
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content);
            String target = ((ftpOptions.getPath() != null) ? (ftpOptions.getPath().length() != 0) ? "/" + ftpOptions.getPath() + "/" : "/" : "/") + "region2d/" + (region.getX() + "." + region.getZ() + ".2dr");

            upload(target, byteArrayInputStream, 0, 0, new FTPDataTransferListener() {
                @Override
                public void started() {

                }

                @Override
                public void transferred(int length) {

                }

                @Override
                public void completed() {
                    result[0] = true;
                }

                @Override
                public void aborted() {
                    result[0] = false;
                }

                @Override
                public void failed() {
                    result[0] = false;
                }
            });

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
        boolean[] result = {false};
        try{
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content);
            String target = ((ftpOptions.getPath() != null) ? (ftpOptions.getPath().length() != 0) ? "/" + ftpOptions.getPath() + "/" : "/" : "/") + "region3d/" + region3DName + ".3dr";

            upload(target, byteArrayInputStream, 0, 0, new FTPDataTransferListener() {
                @Override
                public void started() {

                }

                @Override
                public void transferred(int length) {

                }

                @Override
                public void completed() {
                    result[0] = true;
                }

                @Override
                public void aborted() {
                    result[0] = false;
                }

                @Override
                public void failed() {
                    result[0] = false;
                }
            });

        }catch (Exception ex){
            LogUtils.log(ex);
            result[0] = false;
        }
        return result[0];
    }

    @Override
    public boolean delete2DRegion(Region region) {
        try{
            String source = ((ftpOptions.getPath() != null) ? (ftpOptions.getPath().length() != 0) ?
                    "/" + ftpOptions.getPath() + "/" : "/" : "/") + "region2d/" + (region.getX() + "." + region.getZ() + ".2dr");


            deleteFile(source);

        }catch (Exception ex){
            LogUtils.log(ex);
            return false;
        }

        return true;
    }

    @Override
    public boolean delete3DRegion(String region3DName) {
        try{
            String source = ((ftpOptions.getPath() != null) ? (ftpOptions.getPath().length() != 0) ?
                    "/" + ftpOptions.getPath() + "/" : "/" : "/") + "region3d/" + region3DName + ".3dr";
            deleteFile(source);

        }catch (Exception ex){
            LogUtils.log(ex);
            return false;
        }

        return true;
    }
}
