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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.HashAtlasWriter;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.PixmapPackerIO;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Longri on 11.01.17.
 */
public class SvgSkinUtil {

    private SvgSkinUtil() {
    }

    private final static Logger log = LoggerFactory.getLogger(SvgSkinUtil.class);
    public final static String TMP_UI_ATLAS_PATH = "/user/temp/";
    private final static String TMP_UI_ATLAS = "_ui_tmp.atlas";

    public static TextureAtlas createTextureAtlasFromImages(boolean forceNew, String skinName, ArrayList<ScaledSvg> scaledSvgList,
                                                            FileHandle skinFile) {
        FileHandle cachedTexturatlasFileHandle = null;
        if (!forceNew) {
            cachedTexturatlasFileHandle = Gdx.files.absolute(CB.WorkPath + TMP_UI_ATLAS_PATH + skinName + TMP_UI_ATLAS);
            if (cachedTexturatlasFileHandle.exists()) {
                if (HashAtlasWriter.hashEquals(cachedTexturatlasFileHandle, scaledSvgList, skinFile)) {
                    log.debug("Load cached TextureAtlas");
                    return new TextureAtlas(cachedTexturatlasFileHandle);
                }
            }
        }
        log.debug("Create new TextureAtlas");

        // max texture size are 2048x2048
        int pageWidth = 2048;
        int pageHeight = 2048;
        int padding = 2;
        boolean duplicateBorder = false;

        PixmapPacker packer = new PixmapPacker(pageWidth, pageHeight, Pixmap.Format.RGBA8888, padding, duplicateBorder);


        final int prime = 31;
        int resultHashCode = 1;
        resultHashCode = resultHashCode * prime + Utils.getMd5(skinFile).hashCode();
        for (ScaledSvg scaledSvg : scaledSvgList) {

            Pixmap pixmap = null;
            String name = null;
//            skinFile.parent().child(scaledSvg.path);
            FileHandle fileHandle = skinFile.parent().child(scaledSvg.path);

            try {
                resultHashCode = resultHashCode * prime + Utils.getMd5(fileHandle).hashCode();
                resultHashCode = (resultHashCode * (int) (prime * scaledSvg.scale));
                name = scaledSvg.getRegisterName();
                pixmap = Utils.getPixmapFromBitmap(PlatformConnector.getSvg(name, fileHandle.read(), PlatformConnector.SvgScaleType.DPI_SCALED, scaledSvg.scale));

            } catch (IOException e) {
                e.printStackTrace();
            }

            log.debug("Pack Svg: " + name + " Size:" + pixmap.getWidth() + "/" + pixmap.getHeight());

            if (pixmap != null) {

                packer.pack(name, pixmap);
            }

        }

        // add one pixel color for colorDrawable
        Pixmap pixmap = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        packer.pack("color", pixmap);

        TextureAtlas atlas = packer.generateTextureAtlas(Texture.TextureFilter.MipMapNearestNearest, Texture.TextureFilter.MipMapNearestNearest, true);
        PixmapPackerIO pixmapPackerIO = new PixmapPackerIO();

        PixmapPackerIO.SaveParameters parameters = new PixmapPackerIO.SaveParameters();
        parameters.magFilter = Texture.TextureFilter.MipMapNearestNearest;
        parameters.minFilter = Texture.TextureFilter.MipMapNearestNearest;

        if (cachedTexturatlasFileHandle != null) {
            try {
                HashAtlasWriter.save(resultHashCode, cachedTexturatlasFileHandle, packer, parameters);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        packer.dispose();
        pixmap.dispose();
        return atlas;
    }


}
