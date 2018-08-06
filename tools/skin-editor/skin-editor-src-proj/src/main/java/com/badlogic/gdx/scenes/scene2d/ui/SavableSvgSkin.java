/*
 * Copyright (C) 2017 team-cachebox.de
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
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.develop.tools.skin_editor.StyleTypes;
import de.longri.cachebox3.gui.drawables.SvgNinePatchDrawable;
import de.longri.cachebox3.gui.skin.styles.*;

/**
 * Created by Longri on 12.01.2017.
 */
public class SavableSvgSkin extends SvgSkin {

    public SavableSvgSkin(String name) {
        super(name);
    }

    public SavableSvgSkin(boolean forceCreateNewAtlas, String name, StorageType storageType, FileHandle skinFolder) {
        super(forceCreateNewAtlas, name, storageType, skinFolder);
    }


    /**
     * Store all resources in the specified skin JSON file.
     */
    public boolean save(FileHandle skinFile) {
        SvgSkinUtil.saveSkin(this, StyleTypes.items, skinFile);
        return true;
    }

    public SavableSvgSkin clone(String newName) {
        SavableSvgSkin newSkin = new SavableSvgSkin(newName);
        newSkin.getIcon = getIcon;
        newSkin.getMenuIcon = getMenuIcon;
        newSkin.resources = resources;
        newSkin.atlas = atlas;

        return newSkin;
    }
}
