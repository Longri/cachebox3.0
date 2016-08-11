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
package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.SvgSkin;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Created by Hoepfner on 11.08.2016.
 */
public class HashAtlasWriter {

    /**
     * Saves the provided PixmapPacker to the provided file. The resulting file will use the standard TextureAtlas file format and
     * can be loaded by TextureAtlas as if it had been created using TexturePacker.
     *
     * @param resultHashCode
     * @param file           the file to which the atlas descriptor will be written, images will be written as siblings
     * @param packer         the PixmapPacker to be written
     * @param parameters     the SaveParameters specifying how to save the PixmapPacker
     * @throws IOException if the atlas file can not be written
     */
    public static void save(int resultHashCode, FileHandle file, PixmapPacker packer, PixmapPackerIO.SaveParameters parameters) throws IOException {
        Writer writer = file.writer(false);
        int index = 0;
        for (PixmapPacker.Page page : packer.pages) {
            if (page.rects.size > 0) {
                FileHandle pageFile = file.sibling(file.nameWithoutExtension() + "_" + (++index) + parameters.format.getExtension());
                switch (parameters.format) {
                    case CIM: {
                        PixmapIO.writeCIM(pageFile, page.image);
                        break;
                    }
                    case PNG: {
                        PixmapIO.writePNG(pageFile, page.image);
                        break;
                    }
                }
                writer.write("\n");
                writer.write(pageFile.name() + "\n");
                writer.write("size: " + page.image.getWidth() + "," + page.image.getHeight() + "\n");
                writer.write("format: " + packer.pageFormat.name() + "\n");
                writer.write("filter: " + parameters.minFilter.name() + "," + parameters.magFilter.name() + "\n");
                writer.write("repeat: none" + "\n");
                for (String name : page.rects.keys()) {
                    writer.write(name + "\n");
                    Rectangle rect = page.rects.get(name);
                    writer.write("rotate: false" + "\n");
                    writer.write("xy: " + (int) rect.x + "," + (int) rect.y + "\n");
                    writer.write("size: " + (int) rect.width + "," + (int) rect.height + "\n");
                    writer.write("orig: " + (int) rect.width + "," + (int) rect.height + "\n");
                    writer.write("offset: 0, 0" + "\n");
                    writer.write("index: -1" + "\n");
                }
            }
        }
        writer.close();

        //write hash file
        FileHandle hashFile = file.sibling(file.nameWithoutExtension() + ".hash");
        Writer hashwriter = hashFile.writer(false);
        hashwriter.write("hash: " + resultHashCode + "\n");
        hashwriter.close();
    }


    public static boolean hashEquals(FileHandle folder) {

        FileHandle file = Gdx.files.absolute(CB.WorkPath + SvgSkin.TMP_UI_ATLAS);

        FileHandle hashFile = file.sibling(file.nameWithoutExtension() + ".hash");
        int hash = -1;
        BufferedReader reader = new BufferedReader(new InputStreamReader(hashFile.read()), 64);
        try {
            String line = reader.readLine();
            int colon = line.indexOf(':');
            String sValue = line.substring(colon + 2, line.length());
            hash = Integer.parseInt(sValue);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<FileHandle> fileHandleArrayList = new ArrayList<FileHandle>();
        Utils.listFileHandels(folder, fileHandleArrayList);
        final int prime = 31;
        int resultHashCode = 1;
        // resultHashCode is the hashcode.
        for (FileHandle fileHandle : fileHandleArrayList) {

            //check for svg or png
            if (fileHandle.extension().equalsIgnoreCase("svg")) {
                resultHashCode = resultHashCode * prime + Utils.getMd5(fileHandle).hashCode();

            } else if (fileHandle.extension().equalsIgnoreCase("png")) {
                resultHashCode = resultHashCode * prime + Utils.getMd5(fileHandle).hashCode();
            }
        }
        return resultHashCode == hash;
    }
}
