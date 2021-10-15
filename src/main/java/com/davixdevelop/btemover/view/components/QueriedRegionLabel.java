package com.davixdevelop.btemover.view.components;

import com.davixdevelop.btemover.model.QueriedRegion;
import com.davixdevelop.btemover.view.UIVars;
import com.davixdevelop.btemover.view.style.RegionListRenderer;

import javax.swing.*;
import java.awt.*;

/**
 * Represents the query item JLabel that is displayed in the query JList.
 * It displays the number of 3d regions, the name of the region, and the current status of the query item.
 *
 * @author DavixDevelop
 */
public class QueriedRegionLabel extends JLabel {
    private QueriedRegion region;

    public QueriedRegionLabel(){
        super();
        setOpaque(false);
        setFont(UIVars.RobotoRegular.deriveFont(UIVars.queriedRegionItemFontSize));
        //setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
    }

    public void setRegion(QueriedRegion _region){
        region = _region;
        setText(region.getName());
        Font font = UIVars.RobotoBold.deriveFont(UIVars.queriedRegionItemFontSize);
        final FontMetrics fontMetrics = getFontMetrics(font);
                //g2d.getFontMetrics ();

        setBorder(BorderFactory.createEmptyBorder(UIVars.queriedRegionItemInsets[0],UIVars.queriedRegionItemInsets[1] + 10 + fontMetrics.stringWidth("" + region.getNum3d()) + 27,UIVars.queriedRegionItemInsets[2],UIVars.queriedRegionItemInsets[3] ));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //g2.setColor(UIVars.primaryBg);
        //g2.fillRect(0,0,getWidth(), getHeight());

        g2.setColor(UIVars.queriedRegionItemBg);
        g2.fillRoundRect(0,0,getWidth(), getHeight() - UIVars.queriedRegionItemInsets[4], UIVars.queriedRegionItemRadius, UIVars.queriedRegionItemRadius);

        //Draw count of 3rd files
        final String text = "" + region.getNum3d();
        final Font oldFont = g2.getFont();
        g2.setFont(UIVars.RobotoBold.deriveFont(UIVars.queriedRegionItemFontSize));
        final FontMetrics fontMetrics = g2.getFontMetrics();
        g2.setPaint(Color.black);
        g2.drawString(text,UIVars.queriedRegionItemInsets[1] + 5, getHeight() / 2 + (fontMetrics.getAscent() - fontMetrics.getLeading() - fontMetrics.getDescent()) / 2);
        g2.setFont(oldFont);

        //Draw 3rd icon
        g2.drawImage(RegionListRenderer.reg3DIcon.getImage(), UIVars.queriedRegionItemInsets[1] + 5 + fontMetrics.stringWidth(text),
                getHeight() / 2 - RegionListRenderer.reg3DIcon.getIconHeight() / 2, null);

        //Draw status icon
        switch (region.getStatus()){
            case 1:
                g2.drawImage(RegionListRenderer.downloadIcon.getImage(), getWidth() - UIVars.queriedRegionItemInsets[3] - RegionListRenderer.downloadIcon.getIconWidth(),
                        getHeight() / 2 - RegionListRenderer.downloadIcon.getIconHeight() / 2, null);
                break;
            case 2:
                g2.drawImage(RegionListRenderer.uploadIcon.getImage(), getWidth() - UIVars.queriedRegionItemInsets[3] - RegionListRenderer.uploadIcon.getIconWidth(),
                        getHeight() / 2 - RegionListRenderer.uploadIcon.getIconHeight() / 2, null);
                break;
            case 3:
                g2.drawImage(RegionListRenderer.doneIcon.getImage(), getWidth() - UIVars.queriedRegionItemInsets[3] - RegionListRenderer.doneIcon.getIconWidth(),
                        getHeight() / 2 - RegionListRenderer.doneIcon.getIconHeight() / 2, null);
                break;
            case 4:
                g2.drawImage(RegionListRenderer.failedIcon.getImage(), getWidth() - UIVars.queriedRegionItemInsets[3] - RegionListRenderer.failedIcon.getIconWidth(),
                        getHeight() / 2 - RegionListRenderer.failedIcon.getIconHeight() / 2, null);
            default:
                break;
        }

        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension dimension = super.getPreferredSize();
        dimension.height = UIVars.queriedRegionItemHeight;
        return dimension;
    }
}
