package com.davixdevelop.btemover;

import com.davixdevelop.btemover.logic.Mover_Controller;
import com.davixdevelop.btemover.model.Mover_Model;
import com.davixdevelop.btemover.utils.TerraHelper;
import com.davixdevelop.btemover.view.Mover_View;
import com.davixdevelop.btemover.view.UIVars;
import net.buildtheearth.terraminusminus.generator.EarthGeneratorSettings;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;

/**
 * The main entry point of the mover application
 *
 * @author DavixDevelop
 */
public class Mover_Main {

    public Mover_Main(){
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch (Exception ex){

        }

        initVariables();
        Mover_Controller mover_controller = new Mover_Controller();
    }

    public static void main(String[] args){
        new Mover_Main();
    }

    /**
     * Set's up the various variables, like font's to use in the UI
     */
    public static void initVariables(){
        try{
            EarthGeneratorSettings bteSettings = EarthGeneratorSettings.parse(EarthGeneratorSettings.BTE_DEFAULT_SETTINGS);
            TerraHelper.projection = bteSettings.projection();

            InputStream robotoRegular_IS = Mover_Main.class.getResourceAsStream("Roboto-Regular.ttf");
            UIVars.RobotoRegular = Font.createFont(Font.TRUETYPE_FONT, robotoRegular_IS);

            InputStream robotoBold_IS = Mover_Main.class.getResourceAsStream("Roboto-Bold.ttf");
            UIVars.RobotoBold = Font.createFont(Font.TRUETYPE_FONT, robotoBold_IS);

            InputStream robotoLight_IS = Mover_Main.class.getResourceAsStream("Roboto-Light.ttf");
            UIVars.RobotoLight = Font.createFont(Font.TRUETYPE_FONT, robotoLight_IS);

        }catch (Exception ex){

        }
    }
}
