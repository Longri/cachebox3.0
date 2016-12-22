package de.longri.cachebox3.logging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static de.longri.cachebox3.logging.GdxLogger.LOG_LEVEL_INFO;

/**
 * Created by Longri on 22.12.16.
 */
public abstract class Logger {

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
