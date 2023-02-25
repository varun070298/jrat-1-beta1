package org.shiftone.jrat.util.log;


/**
 * @author Jeff Drost
 */
public class LoggingManager implements Constants, LoggingManagerMBean {

    public void makeLevelLoud() {
        LoggerFactory.setLevel(LEVEL_TRACE);
    }


    public void setLevel(String levelName) {
        LoggerFactory.setLevel(LoggerFactory.getLevelFromName(levelName));
    }


    public String getLevel() {
        return LEVEL_NAMES[LoggerFactory.getLevel()];
    }


    public void disableLogging() {
        LoggerFactory.disableLogging();
    }


    public void enableSystemOutLogging() {
        LoggerFactory.enableSystemOutLogging();
    }
}
