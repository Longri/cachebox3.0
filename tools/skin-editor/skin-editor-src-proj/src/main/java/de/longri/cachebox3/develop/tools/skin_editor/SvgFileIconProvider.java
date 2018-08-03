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
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import org.oscim.backend.canvas.Bitmap;

import java.io.IOException;

/**
 * Created by Longri on 18.01.2017.
 */
public class SvgFileIconProvider extends FileChooser.DefaultFileIconProvider {
    public SvgFileIconProvider(FileChooser chooser) {
        super(chooser);
    }

    @Override
    public Drawable provideIcon(FileChooser.FileItem item) {
        if (item.isDirectory()) return getDirIcon(item);
        String ext = item.getFile().extension().toLowerCase();

        if (ext.equals("svg"))
            return getSvgImageIcon(item);
        return super.provideIcon(item);
    }

    private Drawable getSvgImageIcon(FileChooser.FileItem item) {
        FileHandle fileHandle = item.getFile();
        Bitmap bitmap = null;
        try {
            bitmap = PlatformConnector.getSvg("", fileHandle.read(),
                    PlatformConnector.SvgScaleType.SCALED_TO_WIDTH_OR_HEIGHT, CB.getScaledFloat(100));
        } catch (IOException e) {
            return getImageIcon(item);
        }

        byte[] bytes = bitmap.getPngEncodedData();
        Drawable drawable = new TextureRegionDrawable(new TextureRegion(new Texture(new Pixmap(bytes, 0, bytes.length))));
        return drawable;
    }
}
