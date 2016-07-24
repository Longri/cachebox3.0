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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.kotcrab.vis.ui.VisUI;

/**
 * Static class
 * Created by Longri on 20.07.2016.
 */
public class CB {

    final static float PPI_DEFAULT = 163;
    private static float globalScale = 1;


    private CB() {
    }

    private static Skin actSkin;
    public static Color backgroundColor = new Color(0, 1, 0, 1);


    public static void setActSkin(Skin skin) {
        if (actSkin != null) {
            VisUI.dispose();
        }
        actSkin = skin;
        VisUI.load(actSkin);
    }

    public static Color getColor(String name) {
        return actSkin.getColor(name);
    }

    public static Skin getSkin() {
        return actSkin;
    }


    public static InputMultiplexer inputMultiplexer;

    private static float scalefactor = 0;

    public static float getScaledFloat(float i) {
        if (scalefactor == 0)
            calcScaleFactor();
        return (i * scalefactor);
    }

    public static int getScaledInt(int i) {
        if (scalefactor == 0)
            calcScaleFactor();
        return (int) (i * scalefactor);
    }

    private static void calcScaleFactor() {
        scalefactor = (Gdx.graphics.getPpiX() / PPI_DEFAULT) * globalScale;
    }

    public static void setGlobalScale(float scale) {
        globalScale = scale;
    }

    public float getGlobalScaleFactor() {
        return globalScale;
    }
}
