package com.davixdevelop.btemover.view.components;

import com.davixdevelop.btemover.model.Mover_Model;
import com.davixdevelop.btemover.view.UIVars;
import org.geotools.map.MapContent;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.swing.JMapPane;
import org.opengis.geometry.Envelope;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Represent the JLayeredPane that displays the map with it's toolbar, the map legend and the query JList
 *
 * @author DavixDevelop
 */
public class PanPanel extends JLayeredPane {

    private final JMapPane jMapPane;
    private final CircleButton exportButton;
    private final CircleButton osmToggleButton;
    private final CircleButton expandButton;
    private final CircleButton toggleShapefileLayer;

    public PanPanel(Mover_Model model){
        setBackground(new Color(0,0,0,0));
        setOpaque(false);

        GTRenderer gtRenderer = new StreamingRenderer();
        jMapPane = new JMapPane();
        jMapPane.setBackground(new Color(0,0,0,50));
        jMapPane.setOpaque(false);
        jMapPane.setRenderer(gtRenderer);

        jMapPane.setMapContent(model.getMapContent());
        jMapPane.addMouseListener(new ScrollWheelPanTool(jMapPane, model));

        JPanel toolbar = new JPanel();
        toolbar.setOpaque(false);
        toolbar.setBackground(UIVars.transparentColor);
        toolbar.setLayout(new GridBagLayout());

        exportButton = new CircleButton(CircleButton.SAVE_ICON, false);
        exportButton.setEnabled(false);
        exportButton.setToolTipText("Export current map to image");
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 5, 0);
        toolbar.add(exportButton, c);

        osmToggleButton = new CircleButton(CircleButton.OSM_ICON, true);
        osmToggleButton.setToolTipText("Toggle osm layer");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(0, 0, 5, 0);
        toolbar.add(osmToggleButton, c);

        expandButton = new CircleButton(CircleButton.EXPAND_ICON, false);
        expandButton.setToolTipText("Set viewport to transfer regions/shapefile layer");
        expandButton.setEnabled(false);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(0, 0, 5, 0);
        toolbar.add(expandButton, c);

        toggleShapefileLayer = new CircleButton(CircleButton.SHAPEFILE_ICON, true);
        toggleShapefileLayer.setToggledOn(true);
        toggleShapefileLayer.setToolTipText("Toggle shapefile layer visibility");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 3;
        c.insets = new Insets(0, 0, 0, 0);
        toolbar.add(toggleShapefileLayer, c);

        toolbar.setBorder(BorderFactory.createEmptyBorder(0,0,5,5));

        jMapPane.setBounds(0, 0, getWidth(), getHeight());
        add(jMapPane, new Integer(0), 1);

        toolbar.setBounds(getWidth() - toolbar.getPreferredSize().width - 5, getHeight() - toolbar.getPreferredSize().height - 5, toolbar.getPreferredSize().width, toolbar.getPreferredSize().height);
        add(toolbar, new Integer(0), 0);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                jMapPane.setBounds(0, 0, getWidth(), getHeight());
                toolbar.setBounds(getWidth() - toolbar.getPreferredSize().width, getHeight() - toolbar.getPreferredSize().height, toolbar.getPreferredSize().width, toolbar.getPreferredSize().height);
            }
        });
    }

    public void setMapContent(MapContent content){
        jMapPane.setMapContent(content);
    }

    /**
     * Set the display area of the map
     * @param envelope The area to display
     */
    public void setDisplayArea(Envelope envelope){jMapPane.setDisplayArea(envelope);}

    public CircleButton getExportButton() {
        return exportButton;
    }

    public CircleButton getOsmToggleButton() {
        return osmToggleButton;
    }

    public CircleButton getExpandButton() {
        return expandButton;
    }

    public CircleButton getToggleShapefileLayer() { return toggleShapefileLayer; }
}
