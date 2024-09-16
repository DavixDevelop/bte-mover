package com.davixdevelop.btemover.model;

/**
 * Represents an interface which dictates what methods but the mover model implement
 *
 * @author DavixDevelop
 */
public interface IMoverModel {
    void previewTransfers();
    void transferRegions();

    void deleteRegions();
}
