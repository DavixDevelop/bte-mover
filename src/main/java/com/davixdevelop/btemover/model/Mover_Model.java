package com.davixdevelop.btemover.model;

import com.davixdevelop.btemover.logic.IMoverModelObserver;
import com.davixdevelop.btemover.utils.GeoFeatureHelper;
import com.davixdevelop.btemover.utils.LogUtils;
import com.davixdevelop.btemover.view.UIVars;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.geotools.data.DataUtilities;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.WKTReader2;
import org.geotools.map.MapContent;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.tile.TileService;
import org.geotools.tile.impl.osm.OSMService;
import org.geotools.tile.util.TileLayer;
import org.locationtech.jts.geom.*;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import javax.swing.*;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Represents the main model (MVC architecture) for the Mover. It stores all the layers for the MapContent
 * alongside with the supporting list's (Hashtable) of regions for each layer, the ftp options for the source and
 * target server, and other supporting variables. It also includes all the methods for getting the regions
 * from the source server, downloading/uploading the regions in a background thread, alongside with supporting
 * methods, such as the function to get a list of features from the the list of regions, to be displayed
 * on the layer
 *
 * @author DavixDevelop
 */
public class Mover_Model implements IMoverModel {

    private IMoverModelObserver observer;

    private String shapefilePath = "";
    private String shapefileName = null;
    public String getShapefilePath() {return shapefilePath;}

    public void setShapefilePath(String _shapefilePath) {
        if(shapefileLayerStatus == 2 && Objects.equals(_shapefilePath, shapefilePath))
            shapefileLayerStatus = 3;

        shapefilePath = _shapefilePath;
    }

    private FTPOptions SourceFTP;
    public FTPOptions getSourceFTP() {
        return SourceFTP;
    }
    public void setSourceFTP(FTPOptions _sourceFTP) {
        SourceFTP = _sourceFTP;
    }

    private FTPOptions TargetFTP;
    public FTPOptions getTargetFTP() {
        return TargetFTP;
    }
    public void setTargetFTP(FTPOptions _targetFTP) {
        TargetFTP = _targetFTP;
    }

    public DynamicLayer shapefileLayer;
    private DefaultFeatureCollection shapefileFeatures;
    private Integer shapefileLayerStatus = 0;

    /**
     *
     * @return  0 - Shapefile layer not yet added to map content
     *          1 - Shapefile layer to be added tp map content
     *          2 - Shapefile layer added to map content
     *          3 - Force refresh layer (re-read the same shapefile)
     */
    public Integer getShapefileLayerStatus() {
        return shapefileLayerStatus;
    }
    public void setShapefileLayerStatus(Integer _shapefileLayerStatus) {
        shapefileLayerStatus = _shapefileLayerStatus;
    }

    public DynamicLayer getShapefileLayer(){return shapefileLayer;}

    private TileLayer osmTileLayer;
    private boolean osmLayerToggled;
    private TileService osmTileService;

    private DynamicLayer sourceRegionsLayer;
    public DynamicLayer getSourceRegionsLayer() { return sourceRegionsLayer; }
    private DefaultFeatureCollection sourceFeatures;
    private Hashtable<String,Region> sourceRegions;
    public Hashtable<String,Region> getSourceRegions() {
        return sourceRegions;
    }
    public Integer getSourceRegionsCount(){return sourceRegions.size();}

    private DynamicLayer targetRegionsLayer;
    public DynamicLayer getTargetRegionsLayer() { return targetRegionsLayer; }
    private DefaultFeatureCollection targetFeatures;
    private Hashtable<String,Region> targetRegions;
    public Hashtable<String,Region> getTargetRegions() { return targetRegions; }
    public Integer getTargetRegionsCount(){return targetRegions.size();}

    private DynamicLayer sharedRegionsLayer;
    public DynamicLayer getSharedRegionsLayer() { return sharedRegionsLayer; }
    private DefaultFeatureCollection sharedFeatures;
    private Hashtable<String,Region> sharedRegions;
    public Hashtable<String,Region> getSharedRegions() { return sharedRegions; }
    public Integer getSharedRegionsCount(){return sharedRegions.size();}

    private DynamicLayer transferRegionsLayer;
    public DynamicLayer getTransferRegionsLayer() {
        return transferRegionsLayer;
    }
    private DefaultFeatureCollection transferFeatures;
    private Hashtable<String,Region> transferRegions;

    public Hashtable<String,Region> getTransferRegions() { return transferRegions; }
    public Integer getTransferRegionsCount(){return transferRegions.size();}

    public Geometry shapefileGeometry;

    private MapContent mapContent;
    public MapContent getMapContent() {
        return mapContent;
    }

    private DefaultListModel<QueriedRegion> queryModel;

    public DefaultListModel<QueriedRegion> getQueryModel() {
        return queryModel;
    }

    //Queue to save regions to be downloaded
    private ConcurrentLinkedQueue<Region> downloadQueue = new ConcurrentLinkedQueue<>();
    //Queue to save regions to be uploaded
    private ConcurrentLinkedQueue<Region> uploadQueue = new ConcurrentLinkedQueue<>();

    /*private String tempFolder;
    private String temp2dFolder;
    private String temp3dFolder;*/

    private TimerModel timerModel;
    public TimerModel getTimerModel() {
        return timerModel;
    }

    private SimpleFeatureType TYPE;

    public Mover_Model(IMoverModelObserver _observer){
        observer = _observer;

        queryModel = new DefaultListModel();

        sourceRegions = new Hashtable<>();
        targetRegions = new Hashtable<>();
        sharedRegions = new Hashtable<>();
        transferRegions = new Hashtable<>();

        try {

            osmTileService =  new OSMService("OSM", "http://tile.openstreetmap.org/");
            osmTileLayer = new TileLayer(osmTileService);
            osmTileLayer.setVisible(false);

            TYPE = DataUtilities.createType("location", "geom:Polygon");
            SimpleFeatureBuilder regionFeatureBuilder = new SimpleFeatureBuilder(TYPE);
            SimpleFeature emptyFeature = regionFeatureBuilder.buildFeature(null);

            //Create empty layer for source regions
            sourceFeatures = new DefaultFeatureCollection("internal", TYPE);
            StyleBuilder styleBuilder = new StyleBuilder();
            PolygonSymbolizer polygonSourceSymbolize = styleBuilder.createPolygonSymbolizer(UIVars.onSourceColor, UIVars.onSourceColor, 0);
            polygonSourceSymbolize.getFill().setOpacity(styleBuilder.literalExpression(UIVars.layerTransparency));
            Style sourceStyle = styleBuilder.createStyle(polygonSourceSymbolize);
            sourceFeatures.add(emptyFeature);
            sourceRegionsLayer = new DynamicLayer(sourceFeatures, sourceStyle, "Source");

            //Create empty layer for target regions
            targetFeatures = new DefaultFeatureCollection("internal", TYPE);
            PolygonSymbolizer polygonTargetSymbolize = styleBuilder.createPolygonSymbolizer(UIVars.onTargetColor, UIVars.onTargetColor, 0);
            polygonTargetSymbolize.getFill().setOpacity(styleBuilder.literalExpression(UIVars.layerTransparency));
            Style targetStyle = styleBuilder.createStyle(polygonTargetSymbolize);
            targetFeatures.add(emptyFeature);
            targetRegionsLayer = new DynamicLayer(targetFeatures, targetStyle, "Target");

            //Create empty layer for shared regions
            sharedFeatures = new DefaultFeatureCollection("internal", TYPE);
            PolygonSymbolizer polygonSharedSymbolize = styleBuilder.createPolygonSymbolizer(UIVars.onSharedColor, UIVars.onSharedColor, 0);
            polygonSharedSymbolize.getFill().setOpacity(styleBuilder.literalExpression(UIVars.layerTransparency));
            Style sharedStyle = styleBuilder.createStyle(polygonSharedSymbolize);
            sharedFeatures.add(emptyFeature);
            sharedRegionsLayer = new DynamicLayer(sharedFeatures, sharedStyle, "Shared");

            //Create empty layer for target regions
            transferFeatures = new DefaultFeatureCollection("internal", TYPE);
            PolygonSymbolizer polygonTransferSymbolize = styleBuilder.createPolygonSymbolizer(UIVars.onTransferColor, UIVars.onTransferColor, 0);
            polygonTransferSymbolize.getFill().setOpacity(styleBuilder.literalExpression(UIVars.layerTransparency));
            Style transferStyle = styleBuilder.createStyle(polygonTransferSymbolize);
            transferFeatures.add(emptyFeature);
            transferRegionsLayer = new DynamicLayer(transferFeatures, transferStyle, "Transfer");

            mapContent = new MapContent();

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    /**
     * Read's the shapefile if it hasn't been added yet to the map or if it changed,
     * and loads the features into the shapefile layer
     */
    private void setShapefileLayer(){
        try{
            URL url = new File(shapefilePath).toURI().toURL();
            String shapeName = FilenameUtils.getName(url.getPath());


            //Only set new shapefile layer if it hasn't been set yet, or the shapefile changed
            if(!Objects.equals(shapefileName, shapeName) || shapefileLayerStatus == 3){
                StyleBuilder styleBuilder = new StyleBuilder();
                PolygonSymbolizer polygonShapefileSymbolize = styleBuilder.createPolygonSymbolizer(UIVars.onShapefileBg, UIVars.onShapefileBdColor, 0);
                polygonShapefileSymbolize.getFill().setOpacity(styleBuilder.literalExpression(0.7));
                Style shapefileStyle = styleBuilder.createStyle(polygonShapefileSymbolize);

                shapefileName = shapeName;
                ShapefileDataStore shapefile = new ShapefileDataStore(url);



                //Get iterator for features
                SimpleFeatureIterator iterator = shapefile.getFeatureSource().getFeatures().features();
                shapefileGeometry = null;

                shapefileFeatures = new DefaultFeatureCollection();

                SimpleFeatureType schema = shapefile.getSchema();
                //Get shapefile crs
                CoordinateReferenceSystem sourceCRS =  schema.getCoordinateReferenceSystem();

                if(sourceCRS == null){
                    observer.showMessage(new String[]{"Shapefile has no CRS defined"});
                    shapefileLayerStatus = 0;
                }

                //Set transform from shapefile crs to wgs84 if the shapefile crs is different from WGS84
                MathTransform transform = null;
                if(!Objects.equals(sourceCRS.getName().getCode(), DefaultGeographicCRS.WGS84.getName().getCode()))
                    transform = CRS.findMathTransform(sourceCRS, DefaultGeographicCRS.WGS84, true);

                shapefileFeatures.clear();

                try{
                    while (iterator.hasNext()){
                        SimpleFeature simpleFeature = iterator.next();

                        //Transform feature geometry to WGS84, if crs is different from WGS84
                        if(transform != null)
                            simpleFeature = GeoFeatureHelper.transform(schema, simpleFeature, transform);

                        if(simpleFeature != null){
                            Geometry trGoe = (Geometry) simpleFeature.getDefaultGeometry();

                            //Join all feature geometries into a single geometry
                            if(shapefileGeometry == null)
                                shapefileGeometry = trGoe;
                            else
                                shapefileGeometry = shapefileGeometry.union(trGoe);

                            //Add feature to shapefile features
                            shapefileFeatures.add(simpleFeature);
                        }


                    }
                }finally {
                    iterator.close();
                }

                shapefileLayer = new DynamicLayer(shapefileFeatures, shapefileStyle, "Shapefile");
                shapefileLayerStatus = 1;
            }
        }catch (Exception ex){
            ex.printStackTrace();
            shapefileLayerStatus = 0;
        }
    }

    /**
     * Get's a hashtable of regions from the server, which info is supplied by the ftpOptions param
     * @param ftpOptions The ftp options that indicate which server to read from
     * @return A Hashtable of regions if the server has regions or not, else it return null if it
     * encountered an error.
     */
    private Hashtable<String,Region> getRegionsList(FTPOptions ftpOptions){
        RegionFTPClient client = new RegionFTPClient(ftpOptions);
        return  client.getRegions();
    }

    /**
     * Set's up a new SimpleFeature collection from the supplied regions
     * @param regions The Hashtable of regions to create features for
     * @return An collection of SimpleFeatures, else if and exception occurred null
     */
    private Collection<SimpleFeature> getRegionsCollection(Hashtable<String,Region> regions){
        try {
            final SimpleFeatureType TYPE = DataUtilities.createType("location", "geom:Polygon");
            Collection<SimpleFeature> regionsCollection = new ArrayList<>();
            WKTReader2 wktReader = new WKTReader2();

            for (Map.Entry<String, Region> entry : regions.entrySet()) {
                Region reg = entry.getValue();
                regionsCollection.add(SimpleFeatureBuilder.build(TYPE, new Object[]{wktReader.read(reg.getWkt())}, reg.getName()));
            }

            return regionsCollection;

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Check's if the supplied polygon intersects with the loaded shapefile geometry
     * @param polygon The polygon to check intersection with
     * @return True or False depending if the polygon intersects
     */
    public boolean polygonIntersectsWithShapefile(Polygon polygon){
        //Check if the region polygon intersects with the shapefile geometry
        return shapefileGeometry.intersects(polygon);
    }

    /**
     * Clear the query model, and add new element's to it from the transfer regions
     */
    public void updateQuery(){
        queryModel.clear();
        List<String> transferRegionKeys = new ArrayList<>(transferRegions.keySet());
        for (String transferRegionKey : transferRegionKeys) {
            Region region = transferRegions.get(transferRegionKey);
            queryModel.addElement(new QueriedRegion(region.getName(), region.getRegion3d().size(), 0));
        }
    }

    /**
     * Updates the stored layers, to indicate the layers feature collection has changed
     */
    public void updateLayers(){
        sourceRegionsLayer.update();
        targetRegionsLayer.update();
        sharedRegionsLayer.update();
        transferRegionsLayer.update();
    }

    /**
     * Move's the newly transferred region to the shared regions, by removing it from the
     * transfer region hashtable, and adding it to the shared and target region hashtable.
     * After that it removed the supplied region feature from the transfer layer
     * and adds it to the shared layer
     * @param region The transfer region to move
     */
    public void moveTransferRegionToSharedRegionLayer(Region region){
        transferRegions.remove(region.getName());
        sharedRegions.put(region.getName(), region);
        targetRegions.put(region.getName(), region);

        //Get transfer feature with the id of the region name
        Optional<SimpleFeature> transferOptionalFeature = transferFeatures.stream().filter(p -> p.getID().equals(region.getName())).findFirst();
        if(transferOptionalFeature.isPresent()) {
            SimpleFeature transferFeature = transferOptionalFeature.get();

            //Remove feature with the same id as the region name
            transferFeatures.removeIf(f -> Objects.equals(f.getID(), region.getName()));

            //Add transfer feature with the id of the region name to the shared layer
            sharedFeatures.add(transferFeature);

            transferRegionsLayer.update();
            sharedRegionsLayer.update();
        }
    }

    /**
     * Toggle's the OSM layer's visibility and notifies the model observer of the change
     */
    public void toggleOSMLayer(){
        if(shapefileLayerStatus == 2){

            if(!osmLayerToggled){
                osmTileLayer.setVisible(true);
                osmLayerToggled = true;
            }else{
                osmTileLayer.setVisible(false);
                osmLayerToggled = false;
            }
            observer.toggleOSMLayer(osmLayerToggled);
        }else
            observer.toggleOSMLayer(false);
    }

    /**
     * Notifies to observer to which layer to zoom to, depending on the count of each layers features.
     * It does this by checking the count of each layer and choosing whihc layer to zoom
     * to in the following order:
     * transfer layer -> source layer -> shapefile layer
     */
    public void zoomToLayer(){
        if(shapefileLayerStatus == 2){
            if(transferFeatures.size() == 0){
                if(sourceRegions.size() == 0){
                    observer.zoomToLayers(0); //Zoom to shapefile layer
                }else{
                    observer.zoomToLayers(1); //Zoom to source layer
                }
            }
            else
                observer.zoomToLayers(3); //Zoom to transfers layer
        }
    }

    /**
     * Reset's the supplied featureCollection, by removing and item, and clearing the collection,
     * therefore setting the bounds of the collection back to null,
     * so that it's bounds can be recalculated again later with the call to getBounds
     * @param featureCollection The feature collection to reset
     */
    public void resetDefaultCollection(DefaultFeatureCollection featureCollection){
        if(featureCollection.size() > 0){
            Iterator iterator = featureCollection.iterator();
            Object firstElement = iterator.next();
            iterator.remove();

            if(featureCollection.size() > 0)
                featureCollection.clear();
        }else{
            SimpleFeatureBuilder builder = new SimpleFeatureBuilder(TYPE);
            featureCollection.add(builder.buildFeature(null));
            Iterator iterator = featureCollection.iterator();
            Object firstElement = iterator.next();
            iterator.remove();
        }
    }

    /**
     * This method the following order of tasks:
     * - Set's the shapefile layer if it hasn't been set yet or changed
     * - Get's an list of source regions
     * - Get's and list target regions
     * - Set's and list of region's that are shared on both server
     * - Calculates the region to be transferred, if the source region intersects with the input shapefile
     * - Set's the download concurrent query and the JList model query
     * - Set's the layer for each type of region
     * - Notifies the layer of eahc
     * After it has finished with the task's it notifies the model observer of the success of each task
     */
    @Override
    public void previewTransfers() {
        boolean enableTransfer = false;
        int prevShapefileLayerStatus = shapefileLayerStatus;

        setShapefileLayer();

        if(shapefileLayerStatus == 0)
            return;

        if(shapefileLayerStatus == 1){
            mapContent = new MapContent();
            osmTileLayer.setVisible(osmLayerToggled);
            mapContent.addLayer(osmTileLayer);
            mapContent.addLayer(shapefileLayer);
            mapContent.addLayer(targetRegionsLayer);
            mapContent.addLayer(sourceRegionsLayer);
            mapContent.addLayer(sharedRegionsLayer);
            mapContent.addLayer(transferRegionsLayer);
        }

        //Reset the layer's feature collections
        resetDefaultCollection(sourceFeatures);
        resetDefaultCollection(targetFeatures);
        resetDefaultCollection(sharedFeatures);
        resetDefaultCollection(transferFeatures);

        boolean getRegionsFromRemote = false;

        //Shapefile was not yet added before and is to be added -> fetch the regions from the servers
        if(prevShapefileLayerStatus == 0 && shapefileLayerStatus == 1)
            getRegionsFromRemote = true;
        else if(prevShapefileLayerStatus == 2 && shapefileLayerStatus == 1){
            //Shapefile layer was already added before, and new shapefile was added.
            //Ask the user if they want to get the list of regions from the servers again
            int ans = observer.questionMessage("New shapefile added", "Fetch regions from server again?");
            getRegionsFromRemote = ans == 0;

        }else if(prevShapefileLayerStatus == 3 && shapefileLayerStatus == 1){
            //Shapefile layer was already added before, and same shapefile was added.
            //Ask the user if they want to get the list of regions from the servers again
            int ans = observer.questionMessage("Shapefile changed", "Fetch regions from server again?");
            getRegionsFromRemote = ans == 0;
        }else if(shapefileLayerStatus == 2 && prevShapefileLayerStatus == 2){
            //Nothing changed, ask the user if they want to get the list of regions from the servers again
            int ans = observer.questionMessage("No changes detected in shapefile", "Fetch regions from server again?");
            getRegionsFromRemote = ans == 0;
        }

        if(getRegionsFromRemote) {

            Hashtable<String, Region> _sourceRegions = getRegionsList(SourceFTP);
            if (_sourceRegions == null) {

                //2 - Error connecting to Source FTP
                observer.previewTransfers(2);
                return;
            }


            if (_sourceRegions.size() > 0) {
                sourceRegions = _sourceRegions;
                sourceFeatures.addAll(getRegionsCollection(sourceRegions));
            }

            Hashtable<String, Region> _targetRegions = getRegionsList(TargetFTP);
            if (_targetRegions == null) {
                //3 - Error connecting to Target FTP
                observer.previewTransfers(3);
                return;
            }


            if (_targetRegions.size() > 0) {
                targetRegions = _targetRegions;
                targetFeatures.addAll(getRegionsCollection(targetRegions));
            }
        }else{
            sourceFeatures.addAll(getRegionsCollection(sourceRegions));
            targetFeatures.addAll(getRegionsCollection(targetRegions));
        }

        sharedRegions = new Hashtable<>();
        transferRegions = new Hashtable<>();

        if(sourceRegions.size() > 0) {
            GeometryFactory geometryFactory = new GeometryFactory();
            for (Map.Entry<String, Region> entry : sourceRegions.entrySet()) {
                //Check if source region is already present on the target
                if(targetRegions.containsKey(entry.getKey())){
                    //if it is, add it to the shared layer
                    sharedRegions.put(entry.getKey(), entry.getValue());



                    //Check if target region contains the same 3d regions as the source region
                    if(!targetRegions.get(entry.getKey()).getRegion3d().containsAll(entry.getValue().getRegion3d())){
                        Region sourceRegion = entry.getValue();
                        RectanglePoint[] rectanglePoints = sourceRegion.getPoints();

                        Polygon regionPoly = geometryFactory.createPolygon(new Coordinate[]{
                                new Coordinate(rectanglePoints[0].x,rectanglePoints[0].y),
                                new Coordinate(rectanglePoints[1].x,rectanglePoints[1].y),
                                new Coordinate(rectanglePoints[2].x,rectanglePoints[2].y),
                                new Coordinate(rectanglePoints[3].x,rectanglePoints[3].y),
                                new Coordinate(rectanglePoints[0].x,rectanglePoints[0].y)
                        });

                        //if it doesn't, check first if the region intersects with the shapefile
                        if(polygonIntersectsWithShapefile(regionPoly)) {
                            //If it does, create new region from the source region with the missing region 3d's and add it to the queue
                            Region targetRegion = targetRegions.get(entry.getKey());



                            List<String> missing3d = new ArrayList<>(sourceRegion.getRegion3d());
                            missing3d.removeAll(targetRegion.getRegion3d());
                            targetRegion.setRegion3d(missing3d);
                            transferRegions.put(entry.getKey(), targetRegion);
                        }
                    }

                    //Remove region from source and target layer to prevent overlay
                    sourceFeatures.removeIf(p -> Objects.equals(p.getID(), entry.getKey()));
                    targetFeatures.removeIf(p -> Objects.equals(p.getID(), entry.getKey()));
                }else{
                    Region sourceRegion = entry.getValue();
                    RectanglePoint[] rectanglePoints = sourceRegion.getPoints();

                    Polygon regionPoly = geometryFactory.createPolygon(new Coordinate[]{
                            new Coordinate(rectanglePoints[0].x,rectanglePoints[0].y),
                            new Coordinate(rectanglePoints[1].x,rectanglePoints[1].y),
                            new Coordinate(rectanglePoints[2].x,rectanglePoints[2].y),
                            new Coordinate(rectanglePoints[3].x,rectanglePoints[3].y),
                            new Coordinate(rectanglePoints[0].x,rectanglePoints[0].y)
                    });

                    //Check if the region polygon intersects with the shapefile geometry
                    if(polygonIntersectsWithShapefile(regionPoly)) {
                        //If it does, add region to transfer queue
                        transferRegions.put(entry.getKey(), sourceRegion);

                        //Remove region from source and target layer to prevent overlay
                        sourceFeatures.removeIf(p -> Objects.equals(p.getID(), entry.getKey()));
                        targetFeatures.removeIf(p -> Objects.equals(p.getID(), entry.getKey()));
                    }
                }
            }
        }

        if(sharedRegions.size() > 0){
            sharedFeatures.addAll(getRegionsCollection(sharedRegions));
        }

        if(transferRegions.size() >0) {
            transferFeatures.addAll(getRegionsCollection(transferRegions));
            enableTransfer = true;

            //Add transfer regions to download queue
            downloadQueue.addAll(transferRegions.values());
        }

        observer.previewTransfers((enableTransfer) ? 1 : 0);
    }

    /*
     * Creates a temporary folder and creates the region2d and region3d folder inside of it.
     * It also adds shutdown hook to the runtime, to delete the temporary folder after the program
     * shuts down

    public void setupTempFolder(){
        try{
            tempFolder = Files.createTempDirectory("bte_mover").toString().replace("\\","/");
            temp2dFolder = tempFolder + "/region2d";
            temp3dFolder = tempFolder + "/region3d";
            Files.createDirectory(Paths.get(temp2dFolder));
            Files.createDirectory(Paths.get(temp3dFolder));

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try{
                    FileUtils.deleteDirectory(new File(tempFolder));
                }catch (Exception ex){
                    LogUtils.log(ex);
                }
            }));

        }catch (Exception ex){
            LogUtils.log(ex);
            tempFolder = null;
        }

    }
     */

    /*
    private boolean uploadRunnableStarted;
    private boolean uploadQueueHasItems;
    private boolean downloadQueueHasItems;
    private int regionsDownloaded;

     *
     * The old main method that downloads the region from it download query and uploads it to the target server,
     * effectively transferring it, but note that it doesn't delete it from the source server.
     * Both download and upload are done in two separate threads. While it's transferring the region, it also
     * simultaneously updates the model query of the JList and notifies the model observer of each change/progress
     * transfer progress
     *
    public void old_transferRegions() {
        timerModel = new TimerModel(getTransferRegionsCount(), getTransferRegions().values().stream().mapToInt(Region::getRegion3dCount).sum());
        uploadRunnableStarted = false;
        uploadQueueHasItems = false;
        downloadQueueHasItems = false;
        regionsDownloaded = 0;

        Runnable downloadRunnable = () -> {

            RegionFTPClient ftpClient = new RegionFTPClient(getSourceFTP());

            try{
                if(ftpClient.open()){

                    String tempRegion3DFolder = temp3dFolder;
                    String tempRegion2DFolder = temp2dFolder;

                    while(!downloadQueue.isEmpty()){
                        downloadQueueHasItems = true;
                        Region region = downloadQueue.poll();

                        String region2DFile = tempRegion2DFolder + "/" + (region.getX() + "." + region.getZ() + ".2dr");

                        try{
                            //Set icon in query item to downloading
                            observer.setQueryItemIcon(region, 1);
                            //Thread.sleep(100);


                            //Download region file to temp region2d folder
                            if(ftpClient.download2DRegion(region, region2DFile)){
                                if(!uploadRunnableStarted)
                                    observer.updateProgress(++regionsDownloaded);

                                Thread.sleep(200);
                                ftpClient.sendNoOpCommand();

                                //Download the region3d files to the temp region3d folder
                                List<String> region3DList = region.getRegion3d();
                                for(int i = 0; i < region3DList.size(); i++){
                                    String region3DFile = tempRegion3DFolder + "/" + (region3DList.get(i) + ".3dr");
                                    if(ftpClient.download3DRegion(region3DList.get(i), region3DFile)){
                                        //Decrease the 3d region count in the query item
                                        observer.setQueryItemCount(region, region3DList.size() - i - 1);
                                        ftpClient.sendNoOpCommand();

                                        if(!uploadRunnableStarted)
                                            observer.updateProgress(++regionsDownloaded);

                                        Thread.sleep(200);
                                    }
                                }
                                //Add downloaded region to upload queue
                                uploadQueue.add(region);
                                uploadQueueHasItems = true;


                                ftpClient.sendNoOpCommand();
                            }else{
                                //Set icon in query item to X (failed)
                                observer.setQueryItemIcon(region, 4);
                            }
                        }catch (Exception ex){
                            //Set icon in query item to X (failed)
                            LogUtils.log(ex);
                            observer.setQueryItemIcon(region, 4);
                        }

                        //Start upload thread if it hasn't started yet and if the uploadQueue isn't empty
                        if(!uploadRunnableStarted && uploadQueueHasItems){
                            uploadRunnableStarted = true;
                            Runnable uploadRunnable = () -> {
                                RegionFTPClient targetFTP = new RegionFTPClient(getTargetFTP());

                                try{
                                    if(targetFTP.open()){
                                        //While downloadQueue has items run while loop, and if it doesn't run loop as long as uploadQueue isn't empty
                                        while (downloadQueueHasItems || !uploadQueue.isEmpty()){
                                            targetFTP.sendNoOpCommand();
                                            if(!uploadQueue.isEmpty()){
                                                Region uploadRegion = uploadQueue.poll();
                                                String uploadRegion2DFile = tempRegion2DFolder + "/" + (uploadRegion.getName() + ".2dr");

                                                //Set icon in query item to upload
                                                observer.setQueryItemIcon(uploadRegion, 2);
                                                try{
                                                    if(targetFTP.upload2DRegion(uploadRegion2DFile, uploadRegion)){
                                                        //Update ETR label
                                                        observer.updateProgress(-1);

                                                        Thread.sleep(200);
                                                        targetFTP.sendNoOpCommand();

                                                        List<String> upload3DList = uploadRegion.getRegion3d();
                                                        for(int i = 0; i < upload3DList.size(); i++){
                                                            String uploadRegion3DFile = tempRegion3DFolder + "/" + (upload3DList.get(i) + ".3dr");
                                                            if(targetFTP.upload3DRegion(uploadRegion3DFile, upload3DList.get(i))){
                                                                //Update ETR label
                                                                observer.updateProgress(-2);

                                                                Thread.sleep(200);
                                                                targetFTP.sendNoOpCommand();

                                                                //Increase the 3d region count in the query item
                                                                observer.setQueryItemCount(uploadRegion, i + 1);

                                                                //Delete the temp region3d file
                                                                Files.delete(Paths.get(uploadRegion3DFile));
                                                            }
                                                        }

                                                        //Set icon in query item to done
                                                        observer.setQueryItemIcon(uploadRegion, 3);

                                                        //Delete the temp region2d file
                                                        Files.delete(Paths.get(uploadRegion2DFile));

                                                        //Update transfer & shared region layer
                                                        moveTransferRegionToSharedRegionLayer(uploadRegion);

                                                        //Update legend
                                                        observer.updateTransferCounts();

                                                        targetFTP.sendNoOpCommand();

                                                    }else {
                                                        //Set icon in query item to X (failed)
                                                        observer.setQueryItemIcon(uploadRegion, 4);
                                                    }
                                                }catch (Exception ex){
                                                    //Set icon in query item to X (failed)
                                                    LogUtils.log(ex);
                                                    observer.setQueryItemIcon(uploadRegion, 4);
                                                }

                                            }
                                            else
                                                Thread.sleep(200);
                                        }
                                        targetFTP.close();

                                        //Notify observer of progress done
                                        observer.updateProgress(-3);
                                        observer.transferDone();
                                    }else{
                                        observer.showMessage(new String[]{"Could not login to Target FTP", "Check connection or login info"});
                                    }
                                }catch (Exception ex){
                                    LogUtils.log(ex);
                                    observer.showMessage(new String[]{"Error while uploading", ex.toString()});
                                }
                            };
                            Thread uploadThread = new Thread(uploadRunnable);
                            uploadThread.start();
                        }
                    }
                    downloadQueueHasItems = false;
                    ftpClient.close();
                }else{
                    observer.showMessage(new String[]{"Could not login to Source FTP", "Check connection or login info"});
                }
            }catch (Exception ex){
                LogUtils.log(ex);
                observer.showMessage(new String[]{"Error while downloading", ex.toString()});
            }
        };

        Thread downloadThread = new Thread(downloadRunnable);
        downloadThread.start();
    }
     */

    private int threadCount = 2;
    public int getThreadCount() {
        return threadCount;
    }

    private int threadsDone = 0;
    public int IncreaseThreadsDone(){
        threadsDone++;
        return threadsDone;
    }

    /**
     * The new main method that get's the region from it download query and uploads it's to the target server,
     * effectively transferring it, but note that it doesn't delete it from the source server.
     * Both download and upload are done in one the same thread, but multiple threads can be run at one. While it's transferring the region, it also
     * simultaneously updates the model query of the JList and notifies the model observer of each change/progress
     * transfer progress
     *
     */
    @Override
    public void transferRegions() {
        timerModel = new TimerModel(getTransferRegionsCount(), getTransferRegions().values().stream().mapToInt(Region::getRegion3dCount).sum());
        threadsDone = 0;
        for( int i = 0; i < threadCount; i++){
            Runnable getPutRunnable = () -> {
                RegionFTPClient sourceFTPClient = new RegionFTPClient(getSourceFTP());
                RegionFTPClient targetFTPClient = new RegionFTPClient(getTargetFTP());

                try {
                    if (sourceFTPClient.open()){
                        if(targetFTPClient.open()){
                            while(!downloadQueue.isEmpty()){
                                Region region = downloadQueue.poll();

                                try{
                                    //Set icon in query item to downloading
                                    observer.setQueryItemIcon(region, 1);

                                    //Get the region 2d content
                                    byte[] regionContent = sourceFTPClient.get2DRegion(region);
                                    if(regionContent != null){
                                        //Set icon in query item to uploading
                                        observer.setQueryItemIcon(region,2);

                                        //Put the region 2d content int the the target remote 2d region
                                        if(targetFTPClient.put2DRegion(regionContent, region)){
                                            //Increase region2d count and update ETR
                                            observer.updateProgress(-1);

                                            List<String> region3DList = region.getRegion3d();
                                            for(int d = 0; d < region3DList.size(); d++){
                                                //Set icon in query item to downloading
                                                observer.setQueryItemIcon(region, 1);

                                                //Get the region3d content
                                                regionContent = sourceFTPClient.get3DRegion(region3DList.get(d));
                                                if(regionContent != null){
                                                    //Set icon in query item to uploading
                                                    observer.setQueryItemIcon(region,2);

                                                    //Put the region3d content in the target remote 3d region
                                                    if(targetFTPClient.put3DRegion(regionContent, region3DList.get(d))){
                                                        //Increase region3d count and update ETR
                                                        observer.updateProgress(-2);

                                                        //Decrease the 3d region count in the query item
                                                        observer.setQueryItemCount(region, region3DList.size() - d - 1 );

                                                    }

                                                    Thread.sleep(200);
                                                }
                                            }

                                            //Set icon in query item to done
                                            observer.setQueryItemIcon(region, 3);

                                            //Update transfer & shared region layer
                                            moveTransferRegionToSharedRegionLayer(region);

                                            //Update legend
                                            observer.updateTransferCounts();

                                            //Make sure to keep the connection to both servers alive
                                            sourceFTPClient.sendNoOpCommand();
                                            targetFTPClient.sendNoOpCommand();

                                        }else{
                                            //Set icon in query item to X (failed)
                                            observer.setQueryItemIcon(region, 4);
                                        }

                                    }else {
                                        //Set icon in query item to X (failed)
                                        observer.setQueryItemIcon(region, 4);
                                    }
                                }catch (Exception ex){
                                    LogUtils.log(ex);
                                    //Set icon in query item to X (failed)
                                    observer.setQueryItemIcon(region, 4);
                                }
                            }

                            //Close both connections to server once the download queue is empty
                            sourceFTPClient.close();
                            targetFTPClient.close();

                            //Notify observer of progress done
                            observer.updateProgress(-3);
                        }else
                            observer.showMessage(new String[]{"Could not login to Taeger " + getTargetFTP().getProtocol().toUpperCase() + " Server", "Check connection or login info"});
                    }else{
                        observer.showMessage(new String[]{"Could not login to Source " + getSourceFTP().getProtocol().toUpperCase() + " Server", "Check connection or login info"});
                    }
                }catch (Exception ex){
                    LogUtils.log(ex);
                    observer.showMessage(new String[]{"Error while transferring", ex.toString()});
                }

                observer.transferDone();;
            };

            Thread getPutThread = new Thread(getPutRunnable);
            getPutThread.start();

            if(threadCount > 1)
                try{
                    //Start another thread with a delay
                    Thread.sleep(20);
                }catch (Exception ex){}
        }
    }
}
