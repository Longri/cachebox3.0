package de.longri.cachebox3.logging;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by Longri on 22.12.16.
 */
public abstract class Logger {

    public static final int LOG_LEVEL_TRACE = 00;
    public static final int LOG_LEVEL_DEBUG = 10;
    public static final int LOG_LEVEL_INFO = 20;
    public static final int LOG_LEVEL_WARN = 30;
    public static final int LOG_LEVEL_ERROR = 40;

    /**
     * The current log level
     */
    static int currentLogLevel = LOG_LEVEL_INFO;

    static final long START_TIME = System.currentTimeMillis();

    static DateFormat DATE_FORMATTER = new SimpleDateFormat("hh:mm:ss:SSS");

    static boolean LEVEL_IN_BRACKETS = true;
    static boolean SHOW_DATE_TIME = true;
    static boolean SHOW_THREAD_NAME = true;
    static boolean SHOW_SHORT_LOG_NAME = false;
    static boolean SHOW_LOG_NAME = true;

    public static void setCurrentLogLevel(int level) {
        currentLogLevel = level;

        //must also set Gdx.LogLevel
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
    }

    /**
     * Log a message at the TRACE level.
     *
     * @param msg the message string to be logged
     */
    public abstract void trace(String msg);

    /**
     * Log a message at the TRACE level.
     *
     * @param msg the message string to be logged
     */
    public abstract void trace(String msg, Exception e);


    /**
     * Log a message at the DEBUG level.
     *
     * @param msg the message string to be logged
     */
    public abstract void debug(String msg);

    /**
     * Log an exception (throwable) at the DEBUG level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param e   the exception to log
     */
    public abstract void debug(String msg, Exception e);

    /**
     * Log a message at the INFO level.
     *
     * @param msg the message string to be logged
     */
    public abstract void info(String msg);

    /**
     * Log an exception (throwable) at the INFO level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param e   the exception to log
     */
    public abstract void info(String msg, Exception e);

    /**
     * Log a message at the WARN level.
     *
     * @param msg the message string to be logged
     */
    public abstract void warn(String msg);

    /**
     * Log an exception (throwable) at the WARN level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param e   the exception to log
     */
    public abstract void warn(String msg, Exception e);

    /**
     * Log a message at the ERROR level.
     *
     * @param msg the message string to be logged
     */
    public abstract void error(String msg);

    /**
     * Log an exception (throwable) at the ERROR level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param e   the exception to log
     */
    public abstract void error(String msg, Exception e);


}
