/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.longri;

import javax.swing.UIManager;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;

import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import de.longri.cachebox3.desktop.DesktopPlatformConnector;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.develop.tools.skin_editor.SkinEditorGame;
import org.oscim.awt.AwtGraphics;

import java.awt.*;

/**
 * Desktop launcher class
 *
 * @author Yanick Bourbeau
 */
public class SkinEditorDesktopLauncher {

    /**
     * Entry point
     */
    public static void main(String[] arg) {

        // Set look and feel for Swing dialogs
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        //initialize platform bitmap factory
        AwtGraphics.init();

        //initialize platform connector
        PlatformConnector.init(new DesktopPlatformConnector());


        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        Dimension screenDimensions = Toolkit.getDefaultToolkit().getScreenSize();
        config.width = screenDimensions.width;
        config.height = screenDimensions.height;
        config.resizable = true;
        config.title = "Skin Editor for Cachebox 3.0 (v0.1)";
        config.backgroundFPS = 1;
//		config.resizable = false;
        config.vSyncEnabled = true;

        new LwjglApplication(new SkinEditorGame(), config);
    }
}
