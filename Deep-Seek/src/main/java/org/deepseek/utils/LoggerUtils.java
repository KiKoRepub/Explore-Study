package org.deepseek.utils;

import java.util.logging.Logger;

public class LoggerUtils {

    private static Logger log = Logger.getLogger(LoggerUtils.class.getName());



    public static void info(String message){
        log.info(message);
    }

    public static void error(Throwable e){
        error("",e);
    }
    public static void error(String message, Throwable e){
        log.log(null, message, e);
    }

    public static void info(String message,Object ... args){
        log.info(String.format(message,args));
    }


}
