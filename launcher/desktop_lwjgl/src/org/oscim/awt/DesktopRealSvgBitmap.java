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
package org.oscim.awt;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.GetName;
import com.badlogic.gdx.scenes.scene2d.ui.StoreSvg;
import com.kitfox.svg.SVGCache;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.app.beans.SVGIcon;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import org.oscim.backend.canvas.Bitmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Created by Longri on 19.07.16.
 */
public class DesktopRealSvgBitmap extends AwtBitmap implements GetName,StoreSvg {

    private final static Logger log = LoggerFactory.getLogger(DesktopRealSvgBitmap.class);

    public String name;

    public String getName() {
        return name;
    }


    private static BufferedImage getBufferdImage(InputStream inputStream, PlatformConnector.SvgScaleType scaleType, float scaleValue) throws IOException {
        synchronized (SVGCache.getSVGUniverse()) {
            try {
                URI uri = SVGCache.getSVGUniverse().loadSVG(inputStream, Integer.toString(inputStream.hashCode()));
                SVGDiagram diagram = SVGCache.getSVGUniverse().getDiagram(uri);

                float scale = 1;

                switch (scaleType) {

                    case SCALED_TO_WIDTH:
                        scale = scaleValue / diagram.getWidth();
                        break;
                    case SCALED_TO_HEIGHT:
                        scale = scaleValue / diagram.getHeight();
                        break;
                    case DPI_SCALED:
                        scale = CB.getScaledFloat(scaleValue);
                        break;
                    case SCALED_TO_WIDTH_OR_HEIGHT:
                        scale = Math.min(scaleValue / diagram.getHeight(), scaleValue / diagram.getWidth());
                        break;
                }

                float bitmapWidth = diagram.getWidth() * scale;
                float bitmapHeight = diagram.getHeight() * scale;

                SVGIcon icon = new SVGIcon();
                icon.setAntiAlias(true);
                icon.setPreferredSize(new Dimension((int)Math.ceil( bitmapWidth),(int) Math.ceil( bitmapHeight)));
                icon.setScaleToFit(true);
                icon.setSvgURI(uri);
                BufferedImage bufferedImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
                try {
                    icon.paintIcon(null, bufferedImage.createGraphics(), 0, 0);
                } catch (Exception e) {

                    log.error("Create SVG ",e);

                    //return empty image
                    bufferedImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
                }

                return bufferedImage;
            } catch (Exception e) {
                e.printStackTrace();
                throw new IOException();
            }
        }
    }


    public DesktopRealSvgBitmap(InputStream inputStream, PlatformConnector.SvgScaleType scaleType, float scaleValue) throws IOException {
        super(getBufferdImage(inputStream, scaleType, scaleValue));
    }

    public DesktopRealSvgBitmap (Bitmap bmp){
        super(((AwtBitmap)bmp).bitmap);
    }

    @Override
    public void store(FileHandle child) {
        try {
            ImageIO.write(this.bitmap, "png", child.file());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
