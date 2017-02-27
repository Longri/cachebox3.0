package com.badlogic.gdx.backends.lwjgl;

import com.badlogic.gdx.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Clipboard;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.DesktopPlatformConnector;
import de.longri.cachebox3.PlatformConnector;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.oscim.awt.AwtGraphics;

/**
 * Created by Longri on 27.02.2017.
 */
public class JUnitGdxTestApp {

    public JUnitGdxTestApp(String workFolder) {
        LwjglNativesLoader.load();
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.resizable = false;
        config.useHDPI = true;
        config.samples = 10;
        config.width = 223;
        config.height = 397;
        config.title = "Cachebox JUnitTest";

        config.stencil = 8;
        config.foregroundFPS = 30;
        config.backgroundFPS = 10;

        Gdx.graphics = new LwjglGraphics(config);

        try {
            Display.setDisplayMode(new DisplayMode(800, 600));
            Display.create();
        } catch (LWJGLException ex) {
            ex.printStackTrace();
        }

        ((LwjglGraphics) Gdx.graphics).initiateGL();

        AwtGraphics.init();
        PlatformConnector.init(new DesktopPlatformConnector());

        Gdx.files = new LwjglFiles();

        Gdx.app = new Application() {
            @Override
            public ApplicationListener getApplicationListener() {
                return null;
            }

            @Override
            public Graphics getGraphics() {
                return null;
            }

            @Override
            public Audio getAudio() {
                return null;
            }

            @Override
            public Input getInput() {
                return null;
            }

            @Override
            public Files getFiles() {
                return null;
            }

            @Override
            public Net getNet() {
                return null;
            }

            @Override
            public void log(String tag, String message) {

            }

            @Override
            public void log(String tag, String message, Throwable exception) {

            }

            @Override
            public void error(String tag, String message) {

            }

            @Override
            public void error(String tag, String message, Throwable exception) {

            }

            @Override
            public void debug(String tag, String message) {

            }

            @Override
            public void debug(String tag, String message, Throwable exception) {

            }

            @Override
            public void setLogLevel(int logLevel) {

            }

            @Override
            public int getLogLevel() {
                return 0;
            }

            @Override
            public void setApplicationLogger(ApplicationLogger applicationLogger) {

            }

            @Override
            public ApplicationLogger getApplicationLogger() {
                return null;
            }

            @Override
            public ApplicationType getType() {
                return ApplicationType.HeadlessDesktop;
            }

            @Override
            public int getVersion() {
                return 0;
            }

            @Override
            public long getJavaHeap() {
                return 0;
            }

            @Override
            public long getNativeHeap() {
                return 0;
            }

            @Override
            public Preferences getPreferences(String name) {
                return null;
            }

            @Override
            public Clipboard getClipboard() {
                return null;
            }

            @Override
            public void postRunnable(Runnable runnable) {

            }

            @Override
            public void exit() {

            }

            @Override
            public void addLifecycleListener(LifecycleListener listener) {

            }

            @Override
            public void removeLifecycleListener(LifecycleListener listener) {

            }
        };

        CB.WorkPath = workFolder;
    }


}
