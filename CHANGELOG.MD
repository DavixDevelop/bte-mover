- [1.5](https://github.com/DavixDevelop/bte-mover/releases/tag/v1.5)
    - Added support for local sources (local storage via the file:// syntax)
    - Added support for key-files (OpenSSH format for usage via sftp)
- [1.4](https://github.com/DavixDevelop/bte-mover/releases/tag/v1.4)
    - Switched to mwiede actively maintained fork of [JSch](https://github.com/mwiede/jsch)
    - Added the Bouncy Castle Provider (bcprov-jdk15on)
    - Added support for `ssh-ed25519` host key types via Bouncy Castle for older release of java (Java 8)
- [1.3](https://github.com/DavixDevelop/bte-mover/releases/tag/v1.3)
    - Fixed Jsch region client not uploading the full 2d/3d region to the target sftp server
    - Fixed Jsch region client not downloading the full content of the 2d/3d region from the source sftp server
    - Added skipping of empty 3d regions (less than 16384 bytes big)
    - Added legacy mode to the ftp4j region client
    - Changed the storage of the shapefile geometry from a single geometry to an array of geometries for better performance

    #### LEGACY mode explained:
    The application uses the LIST command by default via the FTP protocol to get the list of regions, ex. the region name and size. If your FTP server doesn't properly support this command (it uses an unknown list format that can't be parsed), the application switches to the LEGACY mode. In this mode, the application only gets a list of region names from the FTP server. This means that 3d regions that are less than 16384 bytes big get will also get included in the preview process, and only get skipped in the transfer process. This means, that the application first has to download the 3d region, and if its size is less than 16384 bytes big, it gets skipped. This greatly increases the transfer process time, so It's advisable to first check if your FTP Server support's the LIST response formats listed [here](http://www.sauronsoftware.it/projects/ftp4j/manual.php#12). **If your FTP Server doesn't support any of the LIST response formats listed [here](http://www.sauronsoftware.it/projects/ftp4j/manual.php#12), the application will still work, but like mentioned it will switch to LEGACY mode, greatly increasing the transfer time.**
    

- [1.2](https://github.com/DavixDevelop/bte-mover/releases/tag/v1.2)
    - Added option to toggle layer's visibility (By clicking on the count in the map legend)
    - Added option to toggle shapefile layer visibility (Toggle button on map toolbar)
    - Added option to specify the image width for the exported image.
    - The application now asks you if you want to fetch the region again from the server, to save time, if you've added a new shapefile or imported the same shapefile, or if nothing changed in terms of the imported shapefile.
- [1.1](https://github.com/DavixDevelop/bte-mover/releases/tag/v1.1)
    - Removed the old one thread for download and one thread for upload functionality
    - Implemented getting the content (byte array) of the remote file in the two FTP clients
    - Implemented setting the content of remote files on to that of the provided content (byte array) in the two FTP clients
    - Implemented two threads for transferring the regions, where both threads simultaneously get the content and set the content to the remote file, giving an estimated performance boost of 52.18% compared to the old method.
    - Added a time done to the progress label after the transfer is done
    - Fixed issue with JAI vendor name returning null
- [1.0](https://github.com/DavixDevelop/bte-mover/releases/tag/v1.0) The first release of BTE Mover