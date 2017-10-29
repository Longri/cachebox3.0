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
package de.longri.cachebox3.translation;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.StringBuilder;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.gui.interfaces.SelectBoxItem;
import de.longri.cachebox3.gui.skin.styles.LanguageStyle;

import java.io.IOException;

/**
 * Created by longri on 26.05.17.
 */
public enum Language implements SelectBoxItem {
    cs, de, en_GB, fr, hu, nl, pl, pt_PT;


    @Override
    public String toString() {
//        StringBuilder sb = new StringBuilder("lang/");
//        sb.append(super.toString());
//        sb.append("/strings.ini");
//        return sb.toString().replaceAll("_", "-");


        StringBuilder sb = new StringBuilder(super.toString());
        return sb.toString().replaceAll("_", "-");
    }

    public String getName() {
        String name = null;
        try {
            name = Translation.getLangNameFromFile(this.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return name == null ? this.name() : name;
    }

    @Override
    public Drawable getDrawable() {
        LanguageStyle style = VisUI.getSkin().get("settings", LanguageStyle.class);

        switch (this) {

            case cs:
                return style.cs;
            case de:
                return style.de;
            case en_GB:
                return style.en_GB;
            case fr:
                return style.fr;
            case hu:
                return style.hu;
            case nl:
                return style.nl;
            case pl:
                return style.pl;
            case pt_PT:
                return style.pt_PT;
        }

        return null;
    }
}

