/*
 * Copyright (C) 2016 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.badlogic.gdx.backends.lwjgl;

import ch.fhnw.imvs.gpssimulator.SimulatorMain;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglFileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.badlogic.gdx.utils.SnapshotArray;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.desktop.DesktopPlatformConnector;
import de.longri.cachebox3.desktop.Desktop_LocationHandler;
import de.longri.cachebox3.desktop.DesktopMain;
import de.longri.cachebox3.file_transfer.MainWindow;
import javafx.application.Application;
import org.apache.commons.cli.*;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.oscim.awt.AwtGraphics;
import org.oscim.backend.DateTime;
import org.oscim.backend.DateTimeAdapter;
import org.oscim.backend.GLAdapter;
import org.oscim.gdx.GdxAssets;
import org.oscim.gdx.LwjglGL20;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.LibgdxLogger;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class TouchLauncher {
    public static void main(String[] args) {
        System.setProperty("org.lwjgl.util.NoChecks", "true");
        LibgdxLogger.PROPERTIES_FILE_HANDLE = new LwjglFileHandle(LibgdxLogger.CONFIGURATION_FILE_XML, Files.FileType.Local);

        final Logger log = LoggerFactory.getLogger("MAIN-LOOP");

        CommandLine cmd = getCommandLine(args);

        //initialize platform bitmap factory
        AwtGraphics.init();

        //initialize platform connector
        PlatformConnector.init(new DesktopPlatformConnector());

        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.resizable = false;
        config.useHDPI = false;
        config.samples = 1;
        config.width = 202;
        config.height = 337;
        config.title = "Cachebox 3.0";

        config.stencil = 8;
        config.foregroundFPS = 60;
        config.backgroundFPS = 10;

        if (cmd.hasOption("note")) {
            //force note 4 layout
            config.width = 323;
            config.height = 574;
        }


        if (cmd.hasOption("scale")) {
            String value = cmd.getOptionValue("scale");


            // maybe it exist a local.properties file with individual scale factor
            try {
                File file = new File("../../../local.properties");
                FileInputStream fileInput = new FileInputStream(file);
                Properties properties = new Properties();
                properties.load(fileInput);
                fileInput.close();
                if (properties.containsKey("desktop.scale"))
                    value = properties.getProperty("desktop.scale");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            float scale = Float.parseFloat(value);
            CB.setGlobalScale(scale);
            config.width *= scale;
            config.height *= scale;
        }

        if (cmd.hasOption("gps")) {
            JFrame f;
            try {
                f = SimulatorMain.createFrame();
                f.pack();
                f.setResizable(false);
                f.setVisible(true);
                f.setBounds(0, 0, f.getWidth(), f.getHeight());

                config.x = f.getX() + f.getWidth() + 10;
                config.y = f.getY();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        CB.locationHandler = new Desktop_LocationHandler();

        initVtm();

        // Don't change this LogLevel
        // Cachebox use the slf4j implematation for LibGdx as Log engine.
        // so set LogLevel on CB.class if you wont (USED_LOG_LEVEL)
        LwjglApplication application = new LwjglApplication(new DesktopMain(), config) {
            public boolean executeRunnables() {
                long start = System.currentTimeMillis();
                synchronized (runnables) {
                    for (int i = runnables.size - 1; i >= 0; i--)
                        executedRunnables.add(runnables.get(i));
                    runnables.clear();
                }
                if (executedRunnables.size == 0) return false;
                final String arryString = executedRunnables.toString();
                do
                    executedRunnables.pop().run();
                while (executedRunnables.size > 0);

                long executionTime = System.currentTimeMillis() - start;

                if (executionTime > 200) {
                    log.warn("Blocked MAIN-LOOp for {}ms => {}", executionTime, arryString);
                }

                return true;
            }

            protected void mainLoop () {
                SnapshotArray<LifecycleListener> lifecycleListeners = this.lifecycleListeners;

                try {
                    graphics.setupDisplay();
                } catch (LWJGLException e) {
                    throw new GdxRuntimeException(e);
                }

                listener.create();
                graphics.resize = true;

                int lastWidth = graphics.getWidth();
                int lastHeight = graphics.getHeight();

                graphics.lastTime = System.nanoTime();
                boolean wasPaused = false;
                while (running) {
                    Display.processMessages();
                    if (Display.isCloseRequested()) exit();

                    boolean isMinimized = graphics.config.pauseWhenMinimized && !Display.isVisible();
                    boolean isBackground = !Display.isActive();
                    boolean paused = isMinimized || (isBackground && graphics.config.pauseWhenBackground);
                    if (!wasPaused && paused) { // just been minimized
                        wasPaused = true;
                        synchronized (lifecycleListeners) {
                            LifecycleListener[] listeners = lifecycleListeners.begin();
                            for (int i = 0, n = lifecycleListeners.size; i < n; ++i)
                                listeners[i].pause();
                            lifecycleListeners.end();
                        }
                        listener.pause();
                    }
                    if (wasPaused && !paused) { // just been restore from being minimized
                        wasPaused = false;
                        synchronized (lifecycleListeners) {
                            LifecycleListener[] listeners = lifecycleListeners.begin();
                            for (int i = 0, n = lifecycleListeners.size; i < n; ++i)
                                listeners[i].resume();
                            lifecycleListeners.end();
                        }
                        listener.resume();
                    }

                    boolean shouldRender = false;

                    if (graphics.canvas != null) {
                        int width = graphics.canvas.getWidth();
                        int height = graphics.canvas.getHeight();
                        if (lastWidth != width || lastHeight != height) {
                            lastWidth = width;
                            lastHeight = height;
                            Gdx.gl.glViewport(0, 0, lastWidth, lastHeight);
                            listener.resize(lastWidth, lastHeight);
                            shouldRender = true;
                        }
                    } else {
                        graphics.config.x = Display.getX();
                        graphics.config.y = Display.getY();
                        if (graphics.resize || Display.wasResized()
                                || (int)(Display.getWidth() * Display.getPixelScaleFactor()) != graphics.config.width
                                || (int)(Display.getHeight() * Display.getPixelScaleFactor()) != graphics.config.height) {
                            graphics.resize = false;
                            graphics.config.width = (int)(Display.getWidth() * Display.getPixelScaleFactor());
                            graphics.config.height = (int)(Display.getHeight() * Display.getPixelScaleFactor());
                            Gdx.gl.glViewport(0, 0, graphics.config.width, graphics.config.height);
                            if (listener != null) listener.resize(graphics.config.width, graphics.config.height);
                            shouldRender = true;
                        }
                    }

                    if (executeRunnables()) shouldRender = true;

                    // If one of the runnables set running to false, for example after an exit().
                    if (!running) break;

                    input.update();
                    if (graphics.shouldRender()) shouldRender = true;
                    input.processEvents();
                    if (audio != null) audio.update();

                    if (isMinimized)
                        shouldRender = false;
                    else if (isBackground && graphics.config.backgroundFPS == -1) //
                        shouldRender = false;

                    int frameRate = isBackground ? graphics.config.backgroundFPS : graphics.config.foregroundFPS;
                    if (shouldRender) {
                        graphics.updateTime();
                        graphics.frameId++;
                        listener.render();
                        Display.update(false);
                    } else {
                        // Sleeps to avoid wasting CPU in an empty loop.
                        if (frameRate == -1) frameRate = 10;
                        if (frameRate == 0) frameRate = graphics.config.backgroundFPS;
                        if (frameRate == 0) frameRate = 30;
                    }
                    if (frameRate > 0) Display.sync(frameRate);
                }

                synchronized (lifecycleListeners) {
                    LifecycleListener[] listeners = lifecycleListeners.begin();
                    for (int i = 0, n = lifecycleListeners.size; i < n; ++i) {
                        listeners[i].pause();
                        listeners[i].dispose();
                    }
                    lifecycleListeners.end();
                }
                listener.pause();
                listener.dispose();
                Display.destroy();
                if (audio != null) audio.dispose();
                if (graphics.config.forceExit) System.exit(-1);
            }
        };

        application.setLogLevel(LwjglApplication.LOG_DEBUG);


        if (cmd.hasOption("transfer")) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Application.launch(MainWindow.class);
                }
            }).start();
        }
    }

    public static void initVtm() {
        // load native library
        new SharedLibraryLoader().load("vtm-jni");
        // init globals
        AwtGraphics.init();
        GdxAssets.init("assets/");
        GLAdapter.init(new LwjglGL20());
        GLAdapter.GDX_DESKTOP_QUIRKS = true;
        DateTimeAdapter.init(new DateTime());
    }

    private static CommandLine getCommandLine(String[] args) {
        Options options = new Options();

        Option scale = new Option("s", "scale", true, "scale factor");
        scale.setRequired(false);
        options.addOption(scale);

        Option note4 = new Option("n", "note", false, "force layout for Note4");
        note4.setRequired(false);
        options.addOption(note4);

        Option gpsSimulator = new Option("g", "gps", false, "start with GPS simulator");
        gpsSimulator.setRequired(false);
        options.addOption(gpsSimulator);

        Option fileTransfer = new Option("t", "transfer", false, "start with FileTransfer");
        fileTransfer.setRequired(false);
        options.addOption(fileTransfer);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("CacheBoxStarter", options);

            System.exit(1);
            return null;
        }
        return cmd;
    }
}
