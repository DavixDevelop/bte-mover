<img src="https://github.com/DavixDevelop/bte-mover/raw/master/src/main/resources/com/davixdevelop/btemover/view/icon.png" width="50%" />

## BTE Mover

BTE Mover is a java application that moves/copies  2d/3d regions from a Minecraft Terra++ World (Default BTE preset) to another Minecraft Terra++ World. The regions to be moved get selected based on the ESRI Shapefile, that the user imports.
It's meant to be used on a world save which files can be accessed though ftp.
In addition to ftp, the application also works with sftp, ftps and ftpes.

### How to use
You can either download the jar file from the latest release, or you can build the project yourself. You can read more about that [here](https://github.com/DavixDevelop/bte-mover/wiki/Build-Instructions).
To open the application you must have Java JRE installed.<br>
It's recommend to run the application with the command `java -jar bte-mover-1.2-15.jar` inside a terminal, ex inside the command line, from the folder
where the .jar file is located. This way, if any exceptions occur, they will be printed out to the terminal.

<img src="https://github.com/DavixDevelop/bte-mover/raw/master/docs/images/how_to_use.png" />

#### Shapefile import
As the application need's an input shapefile to work, you must either acquire on, or create one inside a gis tool, such a QGIS. 
It's recommended that the shapefile uses the WGS84 projection, but it can use other projections defined in the EPSG database.

Once you've acquired such a shapefile, you can import it with the `Import` button. 

#### Setup FTP options
After that you must set the ftp options for the source and target server, by using the `Source FTP` and `Target FTP` buttons respectively. The username and password text fields are self-explanatory.
For the server text field, you must be sure that the url is in the following format:

`(ftp|ftps|sftp|ftpes)://hostname:port(/path)`

The first round brackets group specify the chosen protocol to use (one of the four). The hostname is the IP address or the domain of the server. The port is the port of the server (ex. 21 for FTP). The second round brackets is optional, and represents the path to the world save on the server. **It's extremely important that the url doesn't end with a `/`**.

Example of an url would then be `ftp://192.168.0.26:2121/saves/BTECelje` where `saves/BTECelje` would be that path to the world once we establish a connection to `192.168.0.26` thought the `2121` port using the `fpt` protocol.

If the entered information is correct we can than test the connection by either pressing on the `Test` button. If the dialog icon changes to a check mark, the server is a valid one, but if the changes to an X, the entered information isn't correct, or the client can't establish a connection.<br>
Pressing the `OK` button performs the same connection test and closes the dialog if the connection was a success.

#### Preview transfers
Next, to preview the regions to be transferred, the source/target regions, and the shared regions (regions that are on both the source and target server), we press the `Preview` button.
<br> As spinner shows to indicate that the process is running 
If any error occurs during this process a message dialog gets displayed with the cause of the error, and an option to view the log (By pressing the `Show log` button).

Once the process finishes, and the spinner hides, that map displays the aforementioned region and automatically zoom's to the to be transferred regions, if it found any. 
#### Map
The map enables you to zoom and pan around with a mouse. You can also right-click to copy the geographic coordinates. On the right side of the map is also a toolbar that enables you to export the current view to an image (the `Save icon`), display the `OSM` layer, automatically zoom back to the regions, and toggle the shapefile's layer visibility.
#### Map legend
Under the map we can also find the legend, that indicates each region type, with the count of regions in that type. Hovering over the label of each type shows the total amount of 3D regions for that type. Clicking on the count toggle's the layer's visibility.
#### Query list
On the right side we can also see the query list, that show's which regions will get transferred. The first number shows the number 3d regions in the region, following with the name of the region itself.
#### Transfer process
To transfer the marked regions, we press the `Transfer` button. During this process the region 2d files content, and it's region 3d files content get downloaded to the memory and immediately the content of each file gets uploaded to the target server remote 2d/3d region file.

While this is happening in the two background threads, the aforementioned query list gets constantly updated. The icon on the right side indicates the status of the region (download/upload/done), while the number of 3d region is count down to 0 for each processed region 3d file.
<br> After each region's gets processed the map, and the legend also get updated.

The application also displays an estimated time remain on the number of 2d/3d region processed on the bottom right. 
If for any reason, the transfer fails for a region, it's status in the query list gets changed to an X.

If for any reason an error occurs during the transfers, the whole process **stops**, and a message notify the user to check the logs, displays.

Once the transfer process finishes, the application display's on the bottom how long it took to transfer the files.

###Future plans
The application is 99% finished, therefore the only thing that I intend to improve a bit is the look of the UI, like the scroller and more detailed notifying of errors  back to the user.

#### Need help or have a question?
You can contact me on Discord, under `davixdevelop#3914`