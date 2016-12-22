package de.longri.cachebox3;

import com.badlogic.gdx.ApplicationLogger;

/**
 * Created by Longri on 22.12.16.
 */
public class IOS_ApplicationLogger implements ApplicationLogger {

    @Override
    public void log(String tag, String message) {
        System.out.println(tag + " " + message);
//        Foundation.log("%@", new NSString("[info] " + tag + ": " + message));
    }

    @Override
    public void log(String tag, String message, Throwable exception) {
        System.out.println(tag + " " + message);
//        Foundation.log("%@", new NSString("[info] " + tag + ": " + message));
//        exception.printStackTrace();
    }

    @Override
    public void error(String tag, String message) {
        System.out.println(tag + " " + message);
//        Foundation.log("%@", new NSString("[error] " + tag + ": " + message));
    }

    @Override
    public void error(String tag, String message, Throwable exception) {
        System.out.println(tag + " " + message);
//        Foundation.log("%@", new NSString("[error] " + tag + ": " + message));
//        exception.printStackTrace();
    }

    @Override
    public void debug(String tag, String message) {
        System.out.println(tag + " " + message);
//        Foundation.log("%@", new NSString("[debug] " + tag + ": " + message));
    }

    @Override
    public void debug(String tag, String message, Throwable exception) {
        System.out.println(tag + " " + message);
//        Foundation.log("%@", new NSString("[debug] " + tag + ": " + message));
//        exception.printStackTrace();
    }
}
