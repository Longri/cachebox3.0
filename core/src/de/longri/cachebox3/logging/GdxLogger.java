package de.longri.cachebox3.logging;

import com.badlogic.gdx.Gdx;

import java.util.Date;

/**
 * Created by Longri on 22.12.16.
 */
public class GdxLogger extends Logger {


    private final String NAME;
    private String shortLogName;


    public GdxLogger(final String name) {
        this.NAME = name;
    }


    @Override
    public void trace(String msg) {
        log(LOG_LEVEL_TRACE, msg, null);
    }

    @Override
    public void trace(String msg, Exception e) {
        log(LOG_LEVEL_TRACE, msg, e);
    }

    @Override
    public void debug(String msg) {
        log(LOG_LEVEL_DEBUG, msg, null);
    }

    @Override
    public void debug(String msg, Exception e) {
        log(LOG_LEVEL_DEBUG, msg, e);
    }

    @Override
    public void info(String msg) {
        log(LOG_LEVEL_INFO, msg, null);
    }

    @Override
    public void info(String msg, Exception e) {
        log(LOG_LEVEL_INFO, msg, e);
    }

    @Override
    public void warn(String msg) {
        log(LOG_LEVEL_WARN, msg, null);
    }

    @Override
    public void warn(String msg, Exception e) {
        log(LOG_LEVEL_WARN, msg, e);
    }

    @Override
    public void error(String msg) {
        log(LOG_LEVEL_ERROR, msg, null);
    }

    @Override
    public void error(String msg, Exception e) {
        log(LOG_LEVEL_ERROR, msg, e);
    }

    /**
     * This is our internal implementation for logging regular
     * (non-parameterized) log messages.
     *
     * @param level   One of the LOG_LEVEL_XXX constants defining the log level
     * @param message The message itself
     * @param e       The exception whose stack trace should be logged
     */
    private void log(int level, String message, Exception e) {
        if (!isLevelEnabled(level) || Gdx.app == null) {
            return;
        }

        StringBuffer buf = new StringBuffer(32);


        // Append date-time if so configured
        if (SHOW_DATE_TIME) {
            if (DATE_FORMATTER != null) {
                buf.append(getFormattedDate());
                buf.append(' ');
            } else {
                buf.append(System.currentTimeMillis() - START_TIME);
                buf.append(' ');
            }
        }

        // Append current thread name if so configured
        if (SHOW_THREAD_NAME) {
            buf.append('[');
            buf.append(Thread.currentThread().getName());
            buf.append("] ");
        }

        if (LEVEL_IN_BRACKETS)
            buf.append('[');

        // Append a readable representation of the log level
        switch (level) {
            case LOG_LEVEL_TRACE:
                buf.append("TRACE");
                break;
            case LOG_LEVEL_DEBUG:
                buf.append("DEBUG");
                break;
            case LOG_LEVEL_INFO:
                buf.append("INFO");
                break;
            case LOG_LEVEL_WARN:
                buf.append("WARN");
                break;
            case LOG_LEVEL_ERROR:
                buf.append("ERROR");
                break;
        }
        if (LEVEL_IN_BRACKETS)
            buf.append(']');
        buf.append(' ');

        // Append the name of the log instance if so configured
        if (SHOW_SHORT_LOG_NAME) {
            if (shortLogName == null)
                shortLogName = NAME.substring(NAME.lastIndexOf(".") + 1);
            buf.append(String.valueOf(shortLogName)).append(" - ");
        } else if (SHOW_LOG_NAME) {
            buf.append(String.valueOf(NAME)).append(" - ");
        }

        // Append the message
        buf.append(message);

        switch (level) {
            case LOG_LEVEL_TRACE:
            case LOG_LEVEL_DEBUG:
                Gdx.app.debug("", buf.toString());
                break;
            case LOG_LEVEL_INFO:
                Gdx.app.log("", buf.toString());
                break;
            case LOG_LEVEL_WARN:
            case LOG_LEVEL_ERROR:
                Gdx.app.error("", buf.toString());
                break;
        }

        if (e != null) e.printStackTrace();
    }

    /**
     * Is the given log level currently enabled?
     *
     * @param logLevel is this level enabled?
     */
    public boolean isLevelEnabled(int logLevel) {
        // log level are numerically ordered so can use simple numeric
        // comparison
        return (logLevel >= currentLogLevel);
    }

    private static String getFormattedDate() {
        Date now = new Date();
        String dateText;
        synchronized (DATE_FORMATTER) {
            dateText = DATE_FORMATTER.format(now);
        }
        return dateText;
    }
}
