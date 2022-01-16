package com.davixdevelop.btemover.model;

import com.davixdevelop.btemover.utils.TerraHelper;
import net.buildtheearth.terraminusminus.generator.EarthGeneratorSettings;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;

import java.io.File;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Optional;

public class RegionFTPClientTestJunit {
    private FakeFtpServer ftpServer;
    private FTPOptions ftpOptions;

    private byte[] actualData;

    @Before
    public void setup() throws Exception {
        ftpServer = new FakeFtpServer();
        ftpServer.addUserAccount(new UserAccount("david","test","/"));

        UnixFakeFileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new DirectoryEntry("/data/region2d"));
        fileSystem.add(new DirectoryEntry("/data/region3d"));
        FileEntry testRegion = new FileEntry("/data/region2d/0.-8732.2dr");
        //Read region from resource to input stream
        InputStream inputStream = RegionFTPClientTestJunit.class.getResourceAsStream("0.-8732.2dr");
        //Create byte array of size of input stream
        byte[] actualData = new byte[inputStream.available()];
        //Read input stream to byte array and close it
        inputStream.read(actualData);
        inputStream.close();
        //Set the fake region content to that of the read byte array
        testRegion.setContents(actualData);
        //Add the fake region file to the fake filesystem
        fileSystem.add(testRegion);

        ftpServer.setFileSystem(fileSystem);
        ftpServer.setServerControlPort(0);

        ftpServer.start();

        ftpOptions = new FTPOptions("ftp","localhost", ftpServer.getServerControlPort(), "data", "david","test");

        //Setup the helper class
        EarthGeneratorSettings bteSettings = EarthGeneratorSettings.parse(EarthGeneratorSettings.BTE_DEFAULT_SETTINGS);
        TerraHelper.projection = bteSettings.projection();
    }

    @Test
    public void checkIfRemoteRegionFileIsPresent() throws Exception {
        RegionFTPClient ftpClient = new RegionFTPClient(ftpOptions);
        Hashtable<String, Region> testRegions = ftpClient.getRegions();
        Optional<Region> optionalRegion = testRegions.values().stream().findFirst();
        Region testRegion = optionalRegion.get();
        Assert.assertNotEquals(null, testRegion);
        Assert.assertEquals("0.-8732",testRegion.getName());
    }

    @Test
    public void checkIfRemoteRegionFileGetsDownloaded() throws Exception{
        RegionFTPClient ftpClient = new RegionFTPClient(ftpOptions);
        if(ftpClient.open()){
            Region testRegion = new Region(0, -8732);
            byte[] data = ftpClient.get2DRegion(testRegion);
            Assert.assertArrayEquals(actualData, data);

            ftpClient.close();
        }

    }

    @After
    public void end() throws Exception {
        ftpServer.stop();
    }
}