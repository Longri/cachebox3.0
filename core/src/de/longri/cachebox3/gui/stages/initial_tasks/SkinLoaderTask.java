/*
 * Copyright (C) 2016 - 2017 team-cachebox.de
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
package de.longri.cachebox3.gui.stages.initial_tasks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.SvgSkin;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.settings.Settings;
import de.longri.cachebox3.utils.DevicesSizes;
import de.longri.cachebox3.utils.SizeF;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Longri on 02.08.16.
 */
public final class SkinLoaderTask extends AbstractInitTask {

    private static final String INTERNAL_SKIN_DEFAULT_NAME = "internalDefault";

    public SkinLoaderTask(String name, int percent) {
        super(name, percent);
    }

    @Override
    public void runnable() {

        //initial sizes
        DevicesSizes ui = new DevicesSizes();
        ui.Window = new SizeF(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        ui.Density = CB.getScalefactor();
        ui.isLandscape = false;


        String skinName = null;
        SvgSkin.StorageType storageType = null;
        FileHandle skinFileHandle = null;


        //Get selected skin name and check if available
        if (Settings.nightMode.getValue()) {
            if (!Settings.nightSkinName.isDefault()) {
                // check if skin exist into skin folder
                FileHandle skinFolder = Gdx.files.absolute(Settings.SkinFolder.getValue());
                if (skinFolder.exists()) {
                    FileHandle skin = skinFolder.child(Settings.nightSkinName.getValue());
                    if (skin.exists()) {
                        skinName = Settings.nightSkinName.getValue();
                        storageType = SvgSkin.StorageType.LOCAL;
                        skinFileHandle = skin;
                    }
                }
            }

            if (skinName == null) {
                // use default internal night skin
                skinName = Settings.nightSkinName.getDefaultValue();
                storageType = SvgSkin.StorageType.INTERNAL;
                skinFileHandle = Gdx.files.internal("skins/night");
            }
        } else {
            if (!Settings.daySkinName.isDefault()) {
                // check if skin exist into skin folder
                FileHandle skinFolder = Gdx.files.absolute(Settings.SkinFolder.getValue());
                if (skinFolder.exists()) {
                    FileHandle skin = skinFolder.child(Settings.daySkinName.getValue());
                    if (skin.exists()) {
                        skinName = Settings.daySkinName.getValue();
                        storageType = SvgSkin.StorageType.LOCAL;
                        skinFileHandle = skin;
                    }
                }
            }

            if (skinName == null) {
                // use default internal day skin
                skinName = Settings.daySkinName.getDefaultValue();
                storageType = SvgSkin.StorageType.INTERNAL;
                skinFileHandle = Gdx.files.internal("skins/day");
            }
        }


        // the SvgSkin must create in a OpenGL context. so we post a runnable and wait!
        final AtomicBoolean wait = new AtomicBoolean(true);

        final String finalSkinName = skinName;
        final SvgSkin.StorageType finalType = storageType;
        final FileHandle finalSkinFileHandle = skinFileHandle;
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                CB.setActSkin(new SvgSkin(false, finalSkinName, finalType, finalSkinFileHandle));
                CB.backgroundColor = CB.getColor("background");
                wait.set(false);
            }
        });

        while (wait.get()) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
        }
    }


    private void loadInternaleDefaultSkin() {


    }

}
