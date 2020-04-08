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
package com.badlogic.gdx.backends.lwjgl3;

import ch.fhnw.imvs.gpssimulator.SimulatorMain;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl3.CB_Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3FileHandle;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.desktop.DesktopClipboard;
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
import org.oscim.gdx.Lwjgl3GL20;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.LibgdxLogger;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

public class TouchLwjgl3Launcher {
    public static void main(String[] args) {
        System.setProperty("org.lwjgl.util.NoChecks", "true");
        LibgdxLogger.PROPERTIES_FILE_HANDLE = new Lwjgl3FileHandle(LibgdxLogger.CONFIGURATION_FILE_XML, Files.FileType.Local);

        final Logger log = LoggerFactory.getLogger("MAIN-LOOP");

        CommandLine cmd = getCommandLine(args);

        //initialize platform bitmap factory
        AwtGraphics.init();

        //initialize platform connector
        DesktopPlatformConnector platformConnector = new DesktopPlatformConnector();
        PlatformConnector.init(platformConnector);
        platformConnector.setClipboard(new DesktopClipboard());

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();

        config.setResizable(false);
//        config.useHDPI = false;
//        config.samples = 1;
        config.windowWidth = 202;
        config.windowHeight = 337;


        config.stencil = 8;
//        config.idleFPS = 60;
        config.idleFPS = 10;

        if (cmd.hasOption("note")) {
            //force note 4 layout
            config.windowWidth = 323;
            config.windowHeight = 574;
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
            config.windowWidth *= scale;
            config.windowHeight *= scale;
        }

        JFrame f = null;
        if (cmd.hasOption("gps")) {

            try {
                f = SimulatorMain.createFrame();
                f.pack();
                f.setResizable(false);
                f.setVisible(true);
                f.setBounds(0, 0, f.getWidth(), f.getHeight());

                config.windowX = f.getX() + f.getWidth() + 10;
                config.windowY = f.getY() + 30;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        CB.locationHandler = new Desktop_LocationHandler();

        initVtm();

        // Don't change this LogLevel
        // Cachebox use the slf4j implematation for LibGdx as Log engine.
        // so set LogLevel on CB.class if you wont (USED_LOG_LEVEL)
        new CB_Lwjgl3Application(new DesktopMain(), config);

        if (f != null)
            f.setVisible(false);

        System.exit(0);

    }

    public static void initVtm() {
        // load native library
        new SharedLibraryLoader().load("vtm-jni");
        // init globals
        AwtGraphics.init();
        GdxAssets.init("assets/");
        GLAdapter.init(new Lwjgl3GL20());
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
