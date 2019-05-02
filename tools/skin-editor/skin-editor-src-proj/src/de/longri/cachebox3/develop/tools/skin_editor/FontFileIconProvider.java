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
package de.longri.cachebox3.develop.tools.skin_editor;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.SkinFont;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.drawables.EmptyDrawable;

/**
 * Created by Longri on 18.01.2017.
 */
public class FontFileIconProvider extends FileChooser.DefaultFileIconProvider {
    public FontFileIconProvider(FileChooser chooser) {
        super(chooser);
    }

    @Override
    public Drawable provideIcon(FileChooser.FileItem item) {
        if (item.isDirectory()) return getDirIcon(item);
        String ext = item.getFile().extension().toLowerCase();

        if (ext.toLowerCase().equals("ttf"))
            return getFontIcon(item);
        return super.provideIcon(item);
    }

    private Drawable getFontIcon(FileChooser.FileItem item) {
        FileHandle fileHandle = item.getFile();
        BitmapFont bitmapFont;
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fileHandle);
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = CB.getScaledInt(50);
            parameter.characters = SkinFont.DEFAULT_CHARACTER;
            parameter.genMipMaps = true;
            parameter.minFilter = Texture.TextureFilter.MipMapNearestNearest;
            bitmapFont = generator.generateFont(parameter);
        } catch (Exception e) {
            return getImageIcon(item);
        }

        Drawable drawable = new BitmapFontDrawable(bitmapFont, item.getFile().nameWithoutExtension());
        return drawable;
    }

    public static class BitmapFontDrawable extends EmptyDrawable {

        final BitmapFont font;
        final String text;

        private BitmapFontDrawable(BitmapFont font, String text) {
            this.font = font;
            this.text = text;
        }

        @Override
        public void draw(Batch batch, float x, float y, float width, float height) {
            if (font == null) return;
            font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            font.draw(batch, text, x, y + this.height + 15);
        }

        @Override
        public float getMinWidth() {
            if (!ready) mesure();
            return 300;
        }

        @Override
        public float getMinHeight() {
            if (!ready) mesure();
            return height + 30;
        }

        private float width, height;
        private boolean ready = false;

        private void mesure() {
            GlyphLayout layout = new GlyphLayout(font, text);
            width = layout.width;
            height = layout.height;
            ready = true;
        }
    }
}
