package org.dee.utlis;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerUtils {

    private static final Logger log = Logger.getLogger(LoggerUtils.class.getName());



    public static void info(String message){
        log.info(message);
    }
    public static void info(String message,Object ... args){
        log.info(String.format(message,args));
    }

    public static void error(Throwable e){
        error(e,"报错的默认回复");
    }
    public static void error( Throwable e,String message){
        log.log(Level.OFF,message, e);
    }

    public static void error(Throwable e,String message,Object ... args){
        log.log(Level.OFF,String.format(message,args),e);
    }

}
