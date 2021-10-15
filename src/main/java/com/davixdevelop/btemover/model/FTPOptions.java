package com.davixdevelop.btemover.model;

/**
 * Represents the entered information for the ftp connection
 *
 * @author DavixDevelop
 */
public class FTPOptions {

    /**
     * Creates the ftp options
     * @param _protocol The protocol of the connection
     * @param _server The hostname of the server
     * @param _port The port of the server
     * @param _path The path to which to navigate to once and connection is established. Can be null
     * @param _user The username of the account. Must not be empty
     * @param _password The password of the account. Must not be empty
     */
    public FTPOptions(String _protocol, String _server, int _port, String _path, String _user, String _password){
        protocol = _protocol;
        server = _server;
        port = _port;
        path = _path;
        user = _user;
        password = _password;
    }

    private String server;
    public String getServer(){
        return server;
    }
    public void setServer(String _server) {
        server = _server;
    }

    private int port;
    public int getPort() {
        return port;
    }
    public void setPort(int _port) {
        port = _port;
    }

    private String path;
    public String getPath() { return path; }
    public void setPath(String _path) { path = _path; }

    private String user;
    public String getUser() {
        return user;
    }
    public void setUser(String _user) {
        user = _user;
    }

    private String password;
    public String getPassword() {
        return password;
    }
    public void setPassword(String _password) {
        password = _password;
    }

    private String protocol;
    public String getProtocol() { return protocol; }
    public void setProtocol(String _protocol) { protocol = _protocol; }
}
