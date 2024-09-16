package com.davixdevelop.btemover.model;

public class Region3D {

    public Region3D(String name){
        region3dName = name;
    }
    private String region3dName;
    private byte[] content;

    public String getRegion3dName() {
        return region3dName;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
