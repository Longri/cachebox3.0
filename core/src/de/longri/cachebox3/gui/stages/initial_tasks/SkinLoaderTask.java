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
package de.longri.cachebox3.gui.stages.initial_tasks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.scenes.scene2d.ui.SvgSkin;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.utils.DevicesSizes;
import de.longri.cachebox3.utils.SizeF;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Longri on 02.08.16.
 */
public final class SkinLoaderTask extends AbstractInitTask {

    private static final String INTERNAL_SKIN_DEFAULT_NAME = "internalDefault";

    public static Model myLocationModel, compassModel, compassGrayModel, compassYellowModel;

    public SkinLoaderTask(String name, int percent) {
        super(name, percent);
    }

    @Override
    public void runable() {

        //initial sizes
        DevicesSizes ui = new DevicesSizes();
        ui.Window = new SizeF(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        ui.Density = CB.getScalefactor();
        ui.isLandscape = false;

//        Get selected skin name and check if available
        

        // the SvgSkin must create in a OpenGL context. so we post a runnable and wait!
        final AtomicBoolean wait = new AtomicBoolean(true);
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                loadInternaleDefaultSkin();
                wait.set(false);
            }
        });

        while (wait.get()) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
        }

        //add myLocationModel to skin
        SvgSkin skin = CB.getSkin();
        skin.add("MyLocationModel", myLocationModel, Model.class);
        skin.add("compassModel", compassModel, Model.class);
        skin.add("compassGrayModel", compassGrayModel, Model.class);
        skin.add("compassYellowModel", compassYellowModel, Model.class);
    }


    private void loadInternaleDefaultSkin() {
        FileHandle svgFolder = Gdx.files.internal("skins/day/svg");
        FileHandle skinJson = Gdx.files.internal("skins/day/skin.json");
        CB.setActSkin(new SvgSkin(INTERNAL_SKIN_DEFAULT_NAME, SvgSkin.StorageType.INTERNAL, svgFolder, skinJson));
        CB.backgroundColor = CB.getColor("background");
    }

}
