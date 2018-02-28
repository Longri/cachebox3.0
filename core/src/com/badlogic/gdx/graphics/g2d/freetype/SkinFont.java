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
package com.badlogic.gdx.graphics.g2d.freetype;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tools.bmfont.BitmapFontWriter;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.IncrementProgressEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 24.07.16.
 */
public class SkinFont extends BitmapFont {

    private static final Logger log = LoggerFactory.getLogger(SkinFont.class);
    public static final String DEFAULT_CHARACTER = getCyrilCharSet();
    private static int PAGE_SIZE = -1;
    private static boolean pageSizeCalculated = false;

    public final String font;
    public final int size;


    static String getCyrilCharSet() {
        int CharSize = 0x04ff - 0x0400;
        char[] cyril = new char[CharSize + 1];
        for (int i = 0x0400; i < 0x04ff + 1; i++) {
            cyril[i - 0x0400] = (char) i;
        }
        return FreeTypeFontGenerator.DEFAULT_CHARS + String.copyValueOf(cyril) + "—–" + "ŐőŰű√€†„”“’‘☺čěřšťůž…";
    }

    private static BitmapFont generateFont(FileHandle fileHandle, int size, FileHandle cachePath) {


        if (!pageSizeCalculated) {
            if (Gdx.graphics != null) {
                // create one Char page

                FreeTypeFontGenerator gen = new FreeTypeFontGenerator(fileHandle);
                FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
                parameter.size = CB.getScaledInt(15);
                parameter.characters = "W";
                BitmapFont font = gen.generateFont(parameter);
                int pixel = font.getRegion().getRegionHeight() * font.getRegion().getRegionHeight() * DEFAULT_CHARACTER.length();

                PAGE_SIZE = MathUtils.nextPowerOfTwo((int) Math.sqrt(pixel));
                if (PAGE_SIZE > 2048) PAGE_SIZE = 2048;
                log.debug("FontPageSize: {}", PAGE_SIZE);
                pageSizeCalculated = true;
            } else {
                PAGE_SIZE = 512;
            }
        }

        String tempName = fileHandle.name() + "_" + Integer.toString(size);

        if (cachePath != null) {
            FileHandle cacheFontPath = cachePath.child(tempName + ".fnt");
            if (cacheFontPath.exists()) {
                EventHandler.fire(new IncrementProgressEvent(1, "Init Fonts | load:" + tempName));
                log.debug("Init Fonts | load:{}", tempName);
                try {
                    BitmapFont cachedFont = new BitmapFont(cacheFontPath);
                    return cachedFont;
                } catch (Exception e) {
                    log.error("load Font {}", tempName);
                    e.printStackTrace();
                }
            }
        }

        EventHandler.fire(new IncrementProgressEvent(1, "Init Fonts | generate:" + tempName));
        log.debug("Init Fonts | generate:{}", tempName);

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fileHandle);
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = CB.getScaledInt(size);
        parameter.characters = DEFAULT_CHARACTER;
        parameter.genMipMaps = true;
        parameter.minFilter = Texture.TextureFilter.Nearest;
        parameter.renderCount = 3;
        parameter.packer = new PixmapPacker(PAGE_SIZE, PAGE_SIZE, Pixmap.Format.RGBA8888, 5
                , false, new PixmapPacker.SkylineStrategy());


        FreeTypeFontGenerator.FreeTypeBitmapFontData data = generator.generateData(parameter);
        BitmapFont font = generator.generateFont(parameter, data);

        if (cachePath != null) {
            FileHandle cacheFontPath = cachePath.child(tempName + ".fnt");
            BitmapFontWriter.FontInfo info = new BitmapFontWriter.FontInfo();
            info.padding = new BitmapFontWriter.Padding(1, 1, 1, 1);

            Array<PixmapPacker.Page> pages = parameter.packer.getPages();
            String[] names;
            if (pages.size == 1) {
                names = new String[]{tempName + ".png"};
            } else {
                names = new String[pages.size];
                for (int i = 0, n = pages.size; i < n; i++) {
                    names[i] = tempName + "_" + Integer.toString(i) + ".png";
                }
            }

            BitmapFontWriter.writeFont(data, names, cacheFontPath, info, PAGE_SIZE, PAGE_SIZE);
            BitmapFontWriter.writePixmaps(pages, cachePath, tempName);
        }
        return font;
    }


    public SkinFont(String path, FileHandle fileHandle, int size, FileHandle cachePath) {
        this(generateFont(fileHandle, size, cachePath), path, size);
    }

    private SkinFont(BitmapFont bitmapFont, String path, int size) {
        super(bitmapFont.getData(), bitmapFont.getRegions(), false);
        this.font = path;
        this.size = size;
    }

}
