package de.longri.cachebox3.logging;

/**
 * Created by Longri on 22.12.16.
 */
public class EmptyLogger extends Logger {
    @Override
    public void trace(String msg) {
// do nothing
    }

    @Override
    public void trace(String msg, Exception e) {
// do nothing
    }

    @Override
    public void debug(String msg) {
// do nothing
    }

    @Override
    public void debug(String msg, Exception e) {
// do nothing
    }

    @Override
    public void info(String msg) {
// do nothing
    }

    @Override
    public void info(String msg, Exception e) {
// do nothing
    }

    @Override
    public void warn(String msg) {
// do nothing
    }

    @Override
    public void warn(String msg, Exception e) {
// do nothing
    }

    @Override
    public void error(String msg) {
// do nothing
    }

    @Override
    public void error(String msg, Exception e) {
// do nothing
    }
}
