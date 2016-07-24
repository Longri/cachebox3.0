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

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import org.oscim.backend.canvas.Bitmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Longri on 18.07.16.
 */
public class Utils {
    static final Logger log = LoggerFactory.getLogger(Utils.class);

    /**
     * Returns a @Pixmap from given Bitmap
     *
     * @param bitmap
     * @return
     */
    public static Pixmap getPixmapFromBitmap(Bitmap bitmap) {
        byte[] encodedData = bitmap.getPngEncodedData();
        return new Pixmap(encodedData, 0, encodedData.length);
    }


    public static Drawable get9PatchFromSvg(InputStream inputStream, int left, int right, int top, int bottom) {
        try {
            Bitmap svgBitmap = PlatformConnector.getSvg(inputStream, PlatformConnector.SvgScaleType.DPI_SCALED, 1f);

            //scale nine patch regions
            float scale = CB.getScaledFloat(1);
            left *= scale;
            right *= scale;
            top *= scale;
            bottom *= scale;

            NinePatchDrawable ninePatchDrawable = new NinePatchDrawable(new NinePatch(new Texture(getPixmapFromBitmap(svgBitmap)), left, right, top, bottom));
            return ninePatchDrawable;

        } catch (IOException e) {
            log.error("get9PatchFromSvg", "IOE", e);
        }
        return null;
    }


    /**
     * List all Files inside a FileHandle (Directory)
     *
     * @param begin
     * @param handles
     */
    public static void listFileHandels(FileHandle begin, ArrayList<FileHandle> handles) {
        FileHandle[] newHandles = begin.list();
        for (FileHandle f : newHandles) {
            if (f.isDirectory()) {
                listFileHandels(f, handles);
            } else {
                handles.add(f);
            }
        }
    }

}
