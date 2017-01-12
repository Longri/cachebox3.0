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
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import de.longri.cachebox3.CB;

/**
 * Created by Longri on 24.07.16.
 */
public class SkinFont extends BitmapFont {

    private static final String DEFAULT_CHARACTER = getCyrilCharSet();

    public final FileHandle fileHandle;
    public final int size;


    static String getCyrilCharSet() {
        int CharSize = 0x04ff - 0x0400;
        char[] cyril = new char[CharSize + 1];
        for (int i = 0x0400; i < 0x04ff + 1; i++) {
            cyril[i - 0x0400] = (char) i;
        }
        return FreeTypeFontGenerator.DEFAULT_CHARS + String.copyValueOf(cyril) + "—–" + "ŐőŰű√€†„”“’‘☺čěřšťůž…";
    }

    private static BitmapFont generateFont(FileHandle fileHandle, int size) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fileHandle);

        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = CB.getScaledInt(size);
        parameter.characters = DEFAULT_CHARACTER;
        parameter.genMipMaps = true;
        parameter.minFilter = Texture.TextureFilter.MipMapNearestNearest;
        BitmapFont bitmapFont = generator.generateFont(parameter);
        return bitmapFont;
    }


    public SkinFont(FileHandle fileHandle, int size) {
        this(generateFont(fileHandle, size), fileHandle, size);

    }

    private SkinFont(BitmapFont bitmapFont, FileHandle fileHandle, int size) {
        super(bitmapFont.getData(), bitmapFont.getRegions(), false);
        this.fileHandle = fileHandle;
        this.size = size;
    }

}
