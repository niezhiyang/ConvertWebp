package com.nzy.plugin;

import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;


/**
 * @author niezhiyang
 * since 2021/6/5
 */
public class LoggerUtil {
    public static Logger sLogger;
    public static void log(String tag){
        sLogger.log(LogLevel.ERROR,tag);
    }
}
