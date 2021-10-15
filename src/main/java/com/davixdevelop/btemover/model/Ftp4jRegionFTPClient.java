package com.davixdevelop.btemover.model;

import com.davixdevelop.btemover.utils.LogUtils;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
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
    private FTPOptions ftpOptions;

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
                sslContext = null;
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

            //Get list of region2d and region3d files at once and close connection
            changeDirectory("region2d");
            String[] files = listNames();
            changeDirectoryUp();
            changeDirectory("region3d");
            String[] files2 = listNames();
            close();

            //Store regions in a temp hashtable to quickly add 3d regions later
            Hashtable<String, Region> regions = new Hashtable<>();
            for (String file:
                    files) {
                Matcher matcher = region2dValidator.matcher(file);
                if(matcher.find()){
                    MatchResult result = matcher.toMatchResult();
                    int x = Integer.parseInt(result.group(1));
                    int z = Integer.parseInt(result.group(2));
                    Region region = new Region(x, z);
                    if(region.isValid())
                        regions.put(region.getName(), region);
                }
            }

            for (String file: files2) {
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

            return regions;


        }catch (Exception ex){
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
        final boolean[] result = {false};
        try{
            FileOutputStream outputStream = new FileOutputStream(targetFile);
            String source = ((ftpOptions.getPath() != null) ? (ftpOptions.getPath().length() != 0) ?
                    "/" + ftpOptions.getPath() + "/" : "/" : "/") + "region2d/" + (region.getX() + "." + region.getZ() + ".2dr");

            setType(FTPClient.TYPE_BINARY);

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

    /**
     * Downloads the 3d region files from the supplied region3d name
     * @param region3d The name the 3d region
     * @param targetFile Where to download the 3d region file
     * @return The success of the download
     */
    @Override
    public boolean download3DRegion(String region3d, String targetFile) {
        final boolean[] result = {false};
        try{
            FileOutputStream outputStream = new FileOutputStream(targetFile);
            String source = ((ftpOptions.getPath() != null) ? (ftpOptions.getPath().length() != 0) ?
                    "/" + ftpOptions.getPath() + "/" : "/" : "/") + "region3d/" + region3d + ".3dr";

            setType(FTPClient.TYPE_BINARY);

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

    /**
     * Uploads the 2d region file from the supplied region2DPath
     * @param region2DPath The path to 2d region file to upload
     * @param region The region to upload
     * @return The success of the upload
     */
    @Override
    public boolean upload2DRegion(String region2DPath, Region region) {
        boolean[] result = {false};
        try{
            FileInputStream inputStream = new FileInputStream(region2DPath);
            String target = ((ftpOptions.getPath() != null) ? (ftpOptions.getPath().length() != 0) ? "/" + ftpOptions.getPath() + "/" : "/" : "/") + "region2d/" + (region.getX() + "." + region.getZ() + ".2dr");

            setType(FTPClient.TYPE_BINARY);

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

    /**
     * Uploads the 3d region file from the supplied region3DPath
     * @param region3DPath The path to 3d region file to upload
     * @param region3DName The name of 3d region to upload
     * @return The success of the upload
     */
    @Override
    public boolean upload3DRegion(String region3DPath, String region3DName) {
        boolean[] result = {false};
        try{
            FileInputStream inputStream = new FileInputStream(region3DPath);
            String target = ((ftpOptions.getPath() != null) ? (ftpOptions.getPath().length() != 0) ? "/" + ftpOptions.getPath() + "/" : "/" : "/") + "region3d/" + region3DName + ".3dr";

            setType(FTPClient.TYPE_BINARY);

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
}
