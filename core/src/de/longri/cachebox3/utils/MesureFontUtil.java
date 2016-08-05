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
package de.longri.cachebox3.utils;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;


/**
 * Created by Longri on 05.08.16.
 */
public class MesureFontUtil {

    public static GlyphLayout Measure(BitmapFont font,String txt) {
        if (txt == null || txt.equals(""))
            txt = "text";
        BitmapFontCache measureNormalCache = new BitmapFontCache(font);
        GlyphLayout bounds = measureNormalCache.setText(txt, 0, 0);
        bounds.height = bounds.height - measureNormalCache.getFont().getDescent();
        return bounds;
    }

    public static GlyphLayout MeasureWrapped(BitmapFont font, String txt, float width) {
        if (txt == null || txt.equals(""))
            txt = "text";
        BitmapFontCache measureNormalCache = new BitmapFontCache(font);
        GlyphLayout bounds = measureNormalCache.setText(txt, 0, 0, width, 0, true);
        bounds.height = bounds.height - measureNormalCache.getFont().getDescent();
        return bounds;
    }



}
