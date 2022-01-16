package com.davixdevelop.btemover.view;

import javax.swing.plaf.PanelUI;
import java.awt.*;
import java.io.InputStream;

/**
 * An static class that stores all the variables for the UI, like the theme colors, insets, font's,
 * radius's, font sizes...
 *
 * @author DavixDevelop
 */
public class UIVars {
    public static int buttonRadius = 14;
    public static int[] buttonInsets = new int[] {5,10,5,10};
    public static int circleButtonInset = 5;

    public static int textFieldRadius = 14;
    public static int[] textFieldInsets = new int[] {5,10,5,10};

    public static Float primaryFontSize = 17f;
    public static Float smallFontSize = 12f;

    public static Color primaryBg = Color.decode("#30947b"); //#6da492
    public static Color primaryColor = Color.decode("#c790b9");
    public static Color secondaryBgColor = Color.decode("#90c7b9");
    public static Color secondaryBgDarkenedColor = Color.decode("#5bad98");
    public static Color alternativeBgColor = Color.decode("#b9c790");
    public static Color disabledBgColor = Color.decode("#ffffff");
    public static Color disabledTextColor = Color.decode("#d6d6d6");
    public static Color disabledTextColor2 = Color.decode("#a3a3a3");
    public static Color errorColor = Color.decode("#8100ff");
    public static Color warningColor = Color.decode("#cde9e2");
    public static Color onSourceColor = Color.decode("#ffbbed");
    public static Color onTargetColor = Color.decode("#16ffc3");
    public static Color onSharedColor = Color.decode("#7e00ff");
    public static Color onTransferColor = Color.decode("#FF0080");
    public static Color onShapefileBdColor = Color.decode("#c0c0c0");
    public static Color onShapefileBg = Color.decode("#cacaca");
    public static Color transparentColor = new Color(0,0,0,0);
    public static Color semiTransparentColor = new Color(0,0,0, 63);
    public static Color circleButtonBgColor = new Color(123,141,65, 242);
    public static Color circleButtonDisabledColor = new Color(123,141,65, 51);

    public static int legendRadius = 14;
    public static int legendIconSize = 30;
    public static int legendPanelSpacing = 5;

    public static int[] queriedRegionItemInsets = new int[]{5,5,10,5,5};
    public static int queriedRegionItemRadius = 14;
    public static int queriedRegionItemHeight = 37;
    public static Float queriedRegionItemFontSize = 13f;
    public static Color queriedRegionItemBg = Color.decode("#f5f5f5");

    public static Font RobotoRegular;
    public static Font RobotoBold;
    public static Font RobotoLight;

    public static int meterWidth = 50;
    public static int mapWidth = 39960000 * meterWidth;
    public static int mapHeight = 19980000 * meterWidth;

    public static double layerTransparency = 0.5;

}
