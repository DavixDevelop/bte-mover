package com.davixdevelop.btemover.utils;

import java.util.Date;

/**
 * Represent and static log utility class that stores all the expectations and print's the exception
 * to the system terminal
 *
 * @author DavixDevelop
 */
public class LogUtils {

    public static String messageLog = "";

    /**
     * Log the exception message to the system terminal and save it to the static variable
     * @param message The caught exception
     */
    public static void log(Object message){
        String currentLog = messageLog;
        String newLog = new Date() + " " + message.toString();
        messageLog = newLog + "\n" +  currentLog;
        System.out.println(newLog);
    }

}
