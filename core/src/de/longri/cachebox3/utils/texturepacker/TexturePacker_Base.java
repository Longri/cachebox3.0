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
package de.longri.cachebox3.utils.texturepacker;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public abstract class TexturePacker_Base {

    protected Settings settings;
    protected MaxRectsPacker maxRectsPacker;
    protected IImageprozessor imageProcessor;

    public static void process(String input, String output, String packFileName) {
        try {
            new TexturePackerFileProcessor(new Settings(), packFileName).process(new File(input), new File(output));
        } catch (Exception ex) {
            throw new RuntimeException("Error packing files.", ex);
        }
    }

    public abstract void writeImages(File outputDir, Array<Page> pages, String packFileName);

    // abstract void writePackFile(File outputDir, Array<Page> pages, String packFileName) throws IOException;

    public abstract TexturePacker_Base getInstanz(File rootDir, Settings settings);

    public void addImage(File file) {
        imageProcessor.addImage(file);
    }

    public void pack(File outputDir, String packFileName) {
        outputDir.mkdirs();

        if (packFileName.indexOf('.') == -1)
            packFileName += ".atlas";

        Array<Page> pages = maxRectsPacker.pack(imageProcessor.getImages());
        writeImages(outputDir, pages, packFileName);
        try {
            writePackFile(outputDir, pages, packFileName);
        } catch (IOException ex) {
            throw new RuntimeException("Error writing pack file.", ex);
        }
    }

    public static void process(Settings settings, String input, String output, String packFileName) {
        try {
            new TexturePackerFileProcessor(settings, packFileName).process(new File(input), new File(output));
        } catch (Exception ex) {
            throw new RuntimeException("Error packing files.", ex);
        }
    }

    /**
     * @return true if the output file does not yet exist or its last modification date is before the last modification date of the input
     * file
     */
    public static boolean isModified(String input, String output, String packFileName) {
        String packFullFileName = output;
        if (!packFullFileName.endsWith("/"))
            packFullFileName += "/";
        packFullFileName += packFileName;
        File outputFile = new File(packFullFileName);
        if (!outputFile.exists())
            return true;

        File inputFile = new File(input);
        if (!inputFile.exists())
            throw new IllegalArgumentException("Input file does not exist: " + inputFile.getAbsolutePath());
        return inputFile.lastModified() > outputFile.lastModified();
    }

    public static void processIfModified(String input, String output, String packFileName) {
        if (isModified(input, output, packFileName))
            process(input, output, packFileName);
    }

    public static void processIfModified(Settings settings, String input, String output, String packFileName) {
        if (isModified(input, output, packFileName))
            process(settings, input, output, packFileName);
    }

    public static void main(String[] args) throws Exception {
        String input = null, output = null, packFileName = "pack.atlas";

        switch (args.length) {
            case 3:
                packFileName = args[2];
            case 2:
                output = args[1];
            case 1:
                input = args[0];
                break;
            default:
                System.out.println("Usage: inputDir [outputDir] [packFileName]");
                System.exit(0);
        }

        if (output == null) {
            File inputFile = new File(input);
            output = new File(inputFile.getParentFile(), inputFile.getName() + "-packed").getAbsolutePath();
        }

        process(input, output, packFileName);
    }

    void writePackFile(File outputDir, Array<Page> pages, String packFileName) throws IOException {
        File packFile = new File(outputDir, packFileName);

        if (packFile.exists()) {
            // Make sure there aren't duplicate names.
            TextureAtlasData textureAtlasData = new TextureAtlasData(new FileHandle(packFile.getAbsolutePath()), new FileHandle(packFile.getAbsolutePath()), false);
            for (Page page : pages) {
                for (Rect_Base rect : page.outputRects) {
                    String rectName = settings.flattenPaths ? new FileHandle(rect.name).name() : rect.name;
                    System.out.println(rectName);
                    for (Region region : textureAtlasData.getRegions()) {
                        if (region.name.equals(rectName)) {
                            throw new GdxRuntimeException("A region with the name \"" + rectName + "\" has already been packed: " + rect.name);
                        }
                    }
                }
            }
        }

        FileWriter writer = new FileWriter(packFile);
        // if (settings.jsonOutput) {
        // } else {
        for (Page page : pages) {
            writer.write("\n" + page.imageName + "\n");
            writer.write("format: " + settings.format + "\n");
            writer.write("filter: " + settings.filterMin + "," + settings.filterMag + "\n");
            writer.write("repeat: " + getRepeatValue() + "\n");

            for (Rect_Base rect : page.outputRects) {
                writeRect(writer, page, rect);
                for (Rect_Base alias : rect.aliases) {
                    alias.setSize(rect);
                    writeRect(writer, page, alias);
                }
            }
        }
        // }
        writer.close();
    }

    private void writeRect(FileWriter writer, Page page, Rect_Base rect) throws IOException {
        String rectName = settings.flattenPaths ? new FileHandle(rect.name).name() : rect.name;
        writer.write(rectName + "\n");
        writer.write(" rotate: " + rect.rotated + "\n");
        writer.write(" xy: " + (page.x + rect.x) + ", " + (page.y + page.height - rect.height - rect.y) + "\n");
        writer.write(" size: " + rect.getWidth() + ", " + rect.getHeight() + "\n");
        if (rect.splits != null) {
            writer.write(" split: " + rect.splits[0] + ", " + rect.splits[1] + ", " + rect.splits[2] + ", " + rect.splits[3] + "\n");
        }
        if (rect.pads != null) {
            if (rect.splits == null)
                writer.write(" split: 0, 0, 0, 0\n");
            writer.write(" pad: " + rect.pads[0] + ", " + rect.pads[1] + ", " + rect.pads[2] + ", " + rect.pads[3] + "\n");
        }
        writer.write(" orig: " + rect.originalWidth + ", " + rect.originalHeight + "\n");
        writer.write(" offset: " + rect.offsetX + ", " + (rect.originalHeight - rect.getHeight() - rect.offsetY) + "\n");
        writer.write(" index: " + rect.index + "\n");
    }

    private String getRepeatValue() {
        if (settings.wrapX == TextureWrap.Repeat && settings.wrapY == TextureWrap.Repeat)
            return "xy";
        if (settings.wrapX == TextureWrap.Repeat && settings.wrapY == TextureWrap.ClampToEdge)
            return "x";
        if (settings.wrapX == TextureWrap.ClampToEdge && settings.wrapY == TextureWrap.Repeat)
            return "y";
        return "none";
    }

    public static TexturePacker_Base that;

    public TexturePacker_Base() {
        super();
        that = this;
    }

}