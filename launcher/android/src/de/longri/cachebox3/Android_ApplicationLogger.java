package de.longri.cachebox3;

import com.badlogic.gdx.ApplicationLogger;

/**
 * Created by Longri on 22.12.16.
 */
public class Android_ApplicationLogger implements ApplicationLogger {

    @Override
    public void log (String tag, String message) {
        System.out.println(tag + " " + message);
//        Log.i(tag, message);
    }

    @Override
    public void log (String tag, String message, Throwable exception) {
        System.out.println(tag + " " + message);
//        Log.i(tag, message, exception);
    }

    @Override
    public void error (String tag, String message) {
        System.out.println(tag + " " + message);
//        Log.e(tag, message);
    }

    @Override
    public void error (String tag, String message, Throwable exception) {
        System.out.println(tag + " " + message);
//        Log.e(tag, message, exception);
    }

    @Override
    public void debug (String tag, String message) {
        System.out.println(tag + " " + message);
//        Log.d(tag, message);
    }

    @Override
    public void debug (String tag, String message, Throwable exception) {
        System.out.println(tag + " " + message);
//        Log.d(tag, message, exception);
    }
}