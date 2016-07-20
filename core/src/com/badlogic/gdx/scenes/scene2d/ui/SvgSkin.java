package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.Utils;
import org.oscim.backend.CanvasAdapter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Longri on 20.07.2016.
 */
public class SvgSkin extends Skin {


    /**
     * Create a Skin from given Jason-file!
     * The drawable resources are created from Svg-Folder and putted into a Atlas
     *
     * @param svgFolder
     * @param json
     */
    public SvgSkin(FileHandle svgFolder, FileHandle json) {
        this.addRegions(createTextureAtlasFromImages(svgFolder));
        this.load(json);
    }


    public static TextureAtlas createTextureAtlasFromImages(FileHandle folder) {

        // max texture size are 2048x2048
        int pageWidth = 2048;
        int pageHeight = 2048;
        int padding = 2;
        boolean duplicateBorder = false;

        PixmapPacker packer = new PixmapPacker(pageWidth, pageHeight, Pixmap.Format.RGBA8888, padding, duplicateBorder);

        ArrayList<FileHandle> fileHandleArrayList = new ArrayList<FileHandle>();
        Utils.listFileHandels(folder, fileHandleArrayList);

        for (FileHandle fileHandle : fileHandleArrayList) {

            Pixmap pixmap = null;
            String name = null;

            //check for svg or png
            if (fileHandle.extension().equalsIgnoreCase("svg")) {
                try {
                    pixmap = Utils.getPixmapFromBitmap(PlatformConnector.getSvg(fileHandle.read(), PlatformConnector.SvgScaleType.DPI_SCALED, 1));
                    name = fileHandle.name();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (fileHandle.extension().equalsIgnoreCase("png")) {
                pixmap = Utils.getPixmapFromBitmap(CanvasAdapter.decodeBitmap(fileHandle.read()));
                name = fileHandle.name();
            }

            if (pixmap != null) packer.pack(name, pixmap);

        }
        TextureAtlas atlas = packer.generateTextureAtlas(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear, false);
        packer.dispose();
        return atlas;
    }


}
