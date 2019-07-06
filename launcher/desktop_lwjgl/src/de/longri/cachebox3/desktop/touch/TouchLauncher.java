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
package de.longri.cachebox3.desktop.touch;

import ch.fhnw.imvs.gpssimulator.SimulatorMain;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglFileHandle;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.desktop.DesktopPlatformConnector;
import de.longri.cachebox3.desktop.Desktop_LocationHandler;
import de.longri.cachebox3.desktop.DesktopMain;
import de.longri.cachebox3.file_transfer.MainWindow;
import javafx.application.Application;
import org.apache.commons.cli.*;
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
