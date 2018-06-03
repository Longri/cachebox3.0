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
package de.longri.cachebox3.gui.map;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.gui.interfaces.SelectBoxItem;
import de.longri.cachebox3.gui.skin.styles.CacheTypeStyle;

/**
 * Created by Longri on 28.09.2016.
 */
public enum MapMode implements SelectBoxItem {

    FREE, GPS, WP, LOCK, CAR, NONE;

    public static MapMode fromOrdinal(int ordinal) {
        for (MapMode mapMode : MapMode.values())
            if (mapMode.ordinal() == ordinal) return mapMode;
        return null;
    }

    public String getName() {
        return this.name();
    }

    @Override
    public Drawable getDrawable() {
        // for select Box interface, use 'default' style
       return null;
    }
}
