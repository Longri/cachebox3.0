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
package de.longri.cachebox3;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import org.apache.commons.cli.*;
import org.oscim.awt.AwtGraphics;
import org.oscim.backend.GLAdapter;
import org.oscim.core.Tile;
import org.oscim.gdx.GdxAssets;
import org.oscim.gdx.LwjglGL20;

public class DesktopLauncher {
    public static void main(String[] args) {

        CommandLine cmd = getCommandLine(args);

        //initialize platform bitmap factory
        AwtGraphics.init();

        //initialize platform connector
        PlatformConnector.init(new DesktopPlatformConnector());

        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.resizable = false;
        config.useHDPI = true;
        config.samples = 10;
        config.width = 223;
        config.height = 397;
        config.title = "Cachebox 3.0";

        config.stencil = 8;
        config.foregroundFPS = 30;
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

        Tile.SIZE = 360;
        initVtm();

        // Don't change this LogLevel
        // Cachebox use the slf4j implematation for LibGdx as Log engine.
        // so set LogLevel on CB.class if you wont (USED_LOG_LEVEL)
        new LwjglApplication(new CacheboxMain(), config).setLogLevel(LwjglApplication.LOG_DEBUG);
    }

    public static void initVtm() {
        // load native library
        new SharedLibraryLoader().load("vtm-jni");
        // init globals
        AwtGraphics.init();
        GdxAssets.init("assets/");
        GLAdapter.init(new LwjglGL20());
        GLAdapter.GDX_DESKTOP_QUIRKS = true;
    }

    private static CommandLine getCommandLine(String[] args) {
        Options options = new Options();

        Option scale = new Option("s", "scale", true, "scale factor");
        scale.setRequired(false);
        options.addOption(scale);

        Option note4 = new Option("n", "note", false, "force layout for Note4");
        note4.setRequired(false);
        options.addOption(note4);

        Option gpsSimulator = new Option("o", "output", false, "start with GPS simulator");
        gpsSimulator.setRequired(false);
        options.addOption(gpsSimulator);

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
