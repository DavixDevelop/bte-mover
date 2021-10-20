package com.davixdevelop.btemover.logic;

import org.geotools.swing.event.MapMouseEvent;

import java.awt.event.MouseEvent;

public interface IMouseObserver {
    public void rightClicked(MapMouseEvent event);
}
