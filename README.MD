<img src="https://github.com/DavixDevelop/bte-mover/raw/master/src/main/resources/com/davixdevelop/btemover/view/icon.png" width="50%" />

## BTE Mover

BTE Mover is a java application that moves/copies  2d/3d regions from a Minecraft Terra++ World (Default BTE preset) to another Minecraft Terra++ World. The regions to be moved get selected based on the ESRI Shapefile, that the user imports.
It's meant to be used on a world save which files can be accessed though ftp or through the local storage system.
In addition to ftp, the application also works with sftp, ftps and ftpes.

### Features:
 - Preview to be transferred/shared regions
 - View map of built regions on server
 - Transfer regions
 - Export map to image
 - Support for ftp/file protocol
 - Support for OpenSSH key-files (private key)

### How to use
You can either download the jar file from the latest release, or you can build the project yourself. You can read more about that [here](https://github.com/DavixDevelop/bte-mover/wiki/Build-Instructions).
To open the application you must have Java JRE installed.<br>
It's recommend to run the application with the command `java -jar bte-mover-1.3-20.jar` inside a terminal (ex. command line), from the folder
where the .jar file is located. This way, if any exceptions occur, they will be printed out to the terminal.

<img src="https://github.com/DavixDevelop/bte-mover/raw/master/docs/images/how_to_use.png" />

#### Shapefile import
As the application need's an input shapefile to work, you must either acquire one or create one inside a gis tool, such as QGIS. 
It's recommended that the shapefile uses the WGS84 projection, but it can use other projections defined in the EPSG database. For example, you can search online for `<country name> shapefile` and you will find a suitable shapefile to use.

Once you've acquired such a shapefile, you can import it with the `Import` button. 

#### Setup FTP options
After that you must set the ftp options for the source and target server, by using the `Source FTP` and `Target FTP` buttons respectively.<br>
> **If you wish to only view a map of built regions on the source server only set the Source FTP options.**

The username and password text fields are self-explanatory. In case of using sftp you can also use a key-file with a private key. In case of using a key-file It's important that It's in the OpenSHH format.
In case you are using a private key in another format, like Putty Private Key File (.ppt file) you can use PuTTYgen to convert it to the OpenSSH format.<br>
**To enable the function to add the key-file you must first input a fully correct url syntax in the server text field. More about it bellow.** 

For the server text field, you must be sure that the url is in the following format:

`(ftp|ftps|sftp|ftpes)://hostname:port(/path)`

The first round brackets group specify the chosen protocol to use (one of the four). The hostname is the IP address or the domain of the server. The port is the port of the server (ex. 21 for FTP). The second round brackets is optional, and represents the path to the world save on the server. **It's extremely important that the url doesn't end with a `/`**.

Example of an url would then be `ftp://192.168.0.26:2121/saves/BTECelje` where `saves/BTECelje` would be that path to the world once we establish a connection to `192.168.0.26` thought the `2121` port using the `ftp` protocol.

If you want to add a local source (a world saved on the local storage system) use the following syntax:

`file://path-to-world-save`

Example of such url would be `file://C:\Users\david\AppData\Roaming\.minecraft\saves\BTE Celje` or `file://C:/Users/david/AppData/Roaming/.minecraft/saves/BTE Celje`)<br>
**As before It's important that the url doesn't end with `/` or a with a backslash.**

If the entered information is correct we can than test the connection by either pressing on the `Test` button. If the dialog icon changes to a check mark, the server is a valid one, but if the changes to an X, the entered information isn't correct, or the client can't establish a connection.<br>
Pressing the `OK` button performs the same connection test and closes the dialog if the connection was a success.

If an error occurs during adding a sftp server, make sure to check if your sftp server supports the KEY, Cipher, or MAC algorithms, and host key types that the JSch library supports.
BTE Mover uses the mwiede [Fork of JSch-0.1.55](https://github.com/mwiede/jsch) of the original [JSch](http://www.jcraft.com/jsch), so make sure to take a look at the list of supported algorithms.
If you've opened BTE Mover through the terminal you can find the list of supported algorithms that your server uses there. Ex. :
> INFO: kex: server: curve25519-sha256@libssh.org,ecdh-sha2-nistp256,ecdh-sha2-nistp384,ecdh-sha2-nistp521,diffie-hellman-group14-sha1 <br>
> INFO: kex: server: ssh-ed25519<br>
> INFO: kex: server: aes128-gcm@openssh.com,chacha20-poly1305@openssh.com,aes128-ctr,aes192-ctr,aes256-ctr <br>
> INFO: kex: server: aes128-gcm@openssh.com,chacha20-poly1305@openssh.com,aes128-ctr,aes192-ctr,aes256-ctr<br>
> INFO: kex: server: hmac-sha2-256-etm@openssh.com,hmac-sha2-256,hmac-sha1,hmac-sha1-96<br>
> INFO: kex: server: hmac-sha2-256-etm@openssh.com,hmac-sha2-256,hmac-sha1,hmac-sha1-96

The first line is the key algorithms, the second line the host key type of the server, the third line is the Cipher algorithms the server supports,
and the fifth line is the MAC algorithms the server supports.

#### LIST Command
The application uses the LIST command via the FTP protocol to get the list of regions, ex. the region name and size. If your FTP server doesn't properly support this command (it uses an unknown list format that can't be parsed),
the application switches to the LEGACY mode. In this mode, the application only gets a list of region names from the FTP server. This means that 3d regions that are less than 16384 bytes big get will also get included in the preview process, and only get
skipped in the transfer process. This means, that the application first has to download the 3d region, and if It's size is less than 16384 bytes big, it gets skipped.
This greatly increases the transfer process time, so It's advisable to first check if your FTP Server support's the LIST response formats listed [here](http://www.sauronsoftware.it/projects/ftp4j/manual.php#12).
> **If your FTP Server doesn't support any of the LIST response formats listed [here](http://www.sauronsoftware.it/projects/ftp4j/manual.php#12), the application will still work, but like mentioned it will switch to LEGACY mode, greatly increasing the transfer time.**

#### Preview transfers
Next, to preview the regions to be transferred, the source/target regions, and the shared regions (regions that are on both the source and target server), we press the `Preview` button.
<br> As spinner shows to indicate that the process is running
If any error occurs during this process a message dialog gets displayed with the cause of the error, and an option to view the log (By pressing the `Show log` button).

Once the process finishes, and the spinner hides, that map displays the aforementioned region and automatically zoom's to the to be transferred regions, if it found any.

#### Map
The map enables you to zoom and pan around with a mouse. You can also right-click to copy the geographic coordinates. On the right side of the map is also a toolbar that enables you to export the current view to an image (the `Save icon`, this also creates an auxiliary file, so the exported image becomes geo-referenced and can be open in a GIS Software), display the `OSM` layer, automatically zoom back to the regions, and toggle the shapefile's layer visibility.
#### Map legend
Under the map we can also find the legend, that indicates each region type, with the count of regions in that type. Hovering over the label of each type shows the total amount of 3D regions for that type. Clicking on the count toggle's the layer's visibility.
#### Query list
On the right side we can also see the query list, that show's which regions will get transferred. The first number shows the number 3d regions in the region, following with the name of the region itself.
#### Transfer process
To transfer the marked regions, we press the `Transfer` button. During this process the region 2d files content, and it's region 3d files content get downloaded to the memory and immediately the content of each file gets uploaded to the target server remote 2d/3d region file.
If the 3d region contains only air (16384 bytes large or less) it gets skipped. 

While this is happening in the two background threads, the aforementioned query list gets constantly updated. The icon on the right side indicates the status of the region (download/upload/done), while the number of 3d region is count down to 0 for each processed region 3d file.
<br> After each region's gets processed the map, and the legend also get updated.

The application also displays an estimated time remain on the number of 2d/3d region processed on the bottom right. 
If for any reason, the transfer fails for a region, it's status in the query list gets changed to an X.

If for any reason an error occurs during the transfers, the whole process **stops**, and a message notify the user to check the logs, displays.

Once the transfer process finishes, the application display's on the bottom how long it took to transfer the files.

###Known limits
- To avoid problems, it's recommended to use a shapefile with a single feature and not multiple ones
- If you zoom out a lot have the OSM layer toggled on and specify a big resolution for the exported image, ex 8192, the OSM layer won't get rendered, due to the 128 tiles limit.

###Future plans
The application is 99% finished, therefore the only thing that I intend to improve a bit is the look of the UI, like the scroller, more detailed notifying of errors back to the user and small improvements.

#### Need help or have a question?
You can contact me on Discord, under `davixdevelop#3914`
