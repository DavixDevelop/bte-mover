package com.davixdevelop.btemover.model;

import com.davixdevelop.btemover.utils.LogUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

/**
 * Represents an region file client
 * It has the same functionality as the other client's that implement IRegionFTPClient,
 * but this one specifically only works with the local file system.
 *
 * @author DavixDevelop
 */
public class FileClient implements IRegionFTPClient {
    private final FTPOptions folderOptions;

    public FileClient(FTPOptions _ftpOptions){
        folderOptions = _ftpOptions;
    }

    @Override
    public boolean open() throws Exception {
        return true;
    }

    @Override
    public void close() throws Exception {

    }

    /**
     * Get's and hashtable of regions from the local storage
     * @return The hashtable of regions returned from the local storage
     */
    @Override
    public Hashtable<String, Region> getRegions() {
        if(folderOptions.getPath() == null)
            return null;

        Hashtable<String, Region> regions = new Hashtable<>();

        try{
            File[] files_2d = new File(get2DFolder()).listFiles();
            File[] files_3d = new File(get3DFolder()).listFiles();

            for(File region2d_file : files_2d){
                if(region2d_file.isFile()){
                    Matcher matcher = region2dValidator.matcher(region2d_file.getName());
                    if(matcher.find()){
                        MatchResult result = matcher.toMatchResult();
                        int x = Integer.parseInt(result.group(1));
                        int z = Integer.parseInt(result.group(2));
                        Region region = new Region(x, z);
                        if(region.isValid())
                            regions.put(region.getName(), region);
                    }
                }
            }

            for(File region3d_file : files_3d){
                if(region3d_file.isFile()) {
                    Matcher matcher = region3dValidator.matcher(region3d_file.getName());
                    Path path = Paths.get(region3d_file.getPath());


                    if (matcher.find() && Files.size(path) > 16384) {
                        int x = Integer.parseInt(matcher.group(1)) >> 1;
                        int z = Integer.parseInt(matcher.group(3)) >> 1;
                        int y = Integer.parseInt(matcher.group(2));
                        if (regions.containsKey(x + "." + z)) {
                            Region region = regions.get(x + "." + z);

                            region.addRegion3d(matcher.group(1) + "." + y + "." + matcher.group(3));
                            regions.put(region.getName(), region);
                        }
                    }
                }
            }

            return regions;

        }catch (Exception ex){
            return null;
        }
    }

    /**
     * Test if the input world path is correct
     * @return The success of the test
     */
    @Override
    public boolean testClient() {
        if(folderOptions.getPath() != null){

            File world_folder = new File(folderOptions.getPath());
            if(world_folder.isDirectory()){
                File folder2d = new File(get2DFolder());
                if(folder2d.isDirectory()){
                    File folder3d = new File(get3DFolder());
                    if(folder3d.isDirectory())
                        return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean sendNoOpCommand() throws Exception {
        return true;
    }

    /**
     * Get's the content of the 2d region file on the local storage
     * @param region The region to get
     * @return Byte array representing the content of the 2d region
     */
    @Override
    public byte[] get2DRegion(Region region) {

        try {
            String region_2d = Paths.get(get2DFolder(), region.getName() + ".2dr").toString();
            File file = new File(region_2d);
            
            return FileUtils.readFileToByteArray(file);
        }catch (IOException ex){
            LogUtils.log(ex);
            return null;
        }
    }

    /**
     * Get's the content of the 3d region file on the local storage
     * @param region3DName The name of the 3d region to get
     * @return Byte array representing the content of the 3d region
     */
    @Override
    public byte[] get3DRegion(String region3DName) {

        try {
            String region_3d = Paths.get(get3DFolder(), region3DName + ".3dr").toString();
            File file = new File(region_3d);

            return FileUtils.readFileToByteArray(file);
        }catch (IOException ex){
            LogUtils.log(ex);
            return null;
        }
    }

    /**
     * Puts a 2d region with it's content to the local storage
     * @param content The byte array content of the 2d region
     * @param region The target region
     * @return The success of the upload
     */
    @Override
    public boolean put2DRegion(byte[] content, Region region) {
        try {
            String region_2d = Paths.get(get2DFolder(), region.getName() + ".2dr").toString();
            File file = new File(region_2d);

            FileUtils.writeByteArrayToFile(file, content);

            return true;
        }catch (IOException ex){
            LogUtils.log(ex);
            return false;
        }
    }

    /**
     * Uploads a 3d region with it's content to a local storage
     * @param content The byte array content of the 3d region
     * @param region3DName The target 3d region
     * @return The success of the upload
     */
    @Override
    public boolean put3DRegion(byte[] content, String region3DName) {
        try {
            String region_3d = Paths.get(get3DFolder(), region3DName + ".3dr").toString();
            File file = new File(region_3d);

            FileUtils.writeByteArrayToFile(file, content);

            return true;
        }catch (Exception ex){
            LogUtils.log(ex);
            return false;
        }
    }

    @Override
    public boolean delete2DRegion(Region region) {
        try {
            String region_2d = Paths.get(get2DFolder(), region.getName() + ".2dr").toString();
            File file = new File(region_2d);

            FileUtils.forceDelete(file);

        }catch (IOException ex){
            LogUtils.log(ex);
            return false;
        }
        return true;
    }

    @Override
    public boolean delete3DRegion(String region3DName) {
        try {
            String region_3d = Paths.get(get3DFolder(), region3DName + ".3dr").toString();
            File file = new File(region_3d);

            FileUtils.forceDelete(file);
        }catch (IOException ex){
            LogUtils.log(ex);
            return false;
        }

        return true;
    }

    private String get2DFolder(){
        return Paths.get(folderOptions.getPath(), "region2d").toString();
    }

    private String get3DFolder(){
        return Paths.get(folderOptions.getPath(), "region3d").toString();
    }
}
