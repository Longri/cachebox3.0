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
package de.longri.cachebox3.gui.help;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.Utils;
import de.longri.cachebox3.gui.widgets.Window;
import de.longri.cachebox3.utils.CB_RectF;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Created by Longri on 18.08.2016.
 */
public class HelpWindow extends Window {

    private final String SVG_TEMPLATE = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" +
            "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" version=\"1.1\" baseProfile=\"full\" width=\"#WIDTH#\" height=\"#HEIGHT#\"   xml:space=\"preserve\">\n" +
            "\n" +
            "<g fill-rule=\"evenodd\" fill=\"#FILLCOLOR#\" fill-opacity=\"#FILLOPACITY#\" stroke=\"#BORDERCOLOR#\" stroke-opacity=\"#BORDEROPACITY#\" stroke-width=\"#BORDERSIZE#\">\n" +
            " <path d=\"M 0,0 L #WIDTH#,0 #WIDTH#,#HEIGHT# 0,#HEIGHT# 0,0 z\n" +
            "\t     M centerX,posY \n" +
            "\t     A radiusX radiusY 0 0 0 centerX,maxY \n" +
            "\t     A radiusX radiusY 0 0 0 centerX,posY \n" +
            "\t     z\"/>\n" +
            "</g>\n" +
            "</svg>";


    private HelpWindowStyle style;
    protected final CB_RectF ellipseRectangle;

    public HelpWindow(final CB_RectF ellipseRectangle) {
        super("helpWindow");
        this.ellipseRectangle = ellipseRectangle;
    }


    public void show() {

        if (this.getStageBackground() == null) {

            if (this.style == null)
                this.style = VisUI.getSkin().get("default", HelpWindowStyle.class);

            //create a background texture as stageBackground with a ellipsed hole
            //Use SVG-drawing for create => http://svg.tutorial.aptico.de/start3.php?knr=10&kname=Pfade&uknr=10.8&ukname=A%20und%20a%20-%20Bogenkurven
            final Pixmap[] pixmap = new Pixmap[1];
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {

                    //replace Template with values

                    String width = Float.toString(Gdx.graphics.getWidth());
                    String height = Float.toString(Gdx.graphics.getHeight());

                    String centerX = Float.toString(ellipseRectangle.getCenterPosX());
                    String posY = Float.toString(Gdx.graphics.getHeight() - ellipseRectangle.getY()); // Y flipped
                    String maxY = Float.toString(Gdx.graphics.getHeight() - ellipseRectangle.getMaxY()); // Y flipped
                    String radiusX = Float.toString(ellipseRectangle.getHalfWidth());
                    String radiusY = Float.toString(ellipseRectangle.getHalfHeight());
                    String borderSize = Float.toString(HelpWindow.this.style.borderSize);


                    Color color = HelpWindow.this.style.backgroundColor;
                    String value = Integer
                            .toHexString(((int) (255 * color.r) << 16) | ((int) (255 * color.g) << 8) | ((int) (255 * color.b)));
                    while (value.length() < 6)
                        value = "0" + value;
                    String fillColor = "#" + value;
                    String fillOpacity = Float.toString(color.a);

                    color = HelpWindow.this.style.borderColor;
                    value = Integer
                            .toHexString(((int) (255 * color.r) << 16) | ((int) (255 * color.g) << 8) | ((int) (255 * color.b)));
                    while (value.length() < 6)
                        value = "0" + value;
                    String borderColor = "#" + value;
                    String borderOpacity = Float.toString(color.a);

                    String svgString = SVG_TEMPLATE.replaceAll("#WIDTH#", width);
                    svgString = svgString.replaceAll("#HEIGHT#", height);
                    svgString = svgString.replaceAll("#FILLCOLOR#", fillColor);
                    svgString = svgString.replaceAll("#FILLOPACITY#", fillOpacity);
                    svgString = svgString.replaceAll("#BORDERCOLOR#", borderColor);
                    svgString = svgString.replaceAll("#BORDEROPACITY#", borderOpacity);
                    svgString = svgString.replaceAll("centerX", centerX);
                    svgString = svgString.replaceAll("posY", posY);
                    svgString = svgString.replaceAll("radiusX", radiusX);
                    svgString = svgString.replaceAll("radiusY", radiusY);
                    svgString = svgString.replaceAll("maxY", maxY);
                    svgString = svgString.replaceAll("#BORDERSIZE#", borderSize);

                    //create a InputStream from SVG-String
                    InputStream stream = new ByteArrayInputStream(svgString.getBytes(Charset.forName("UTF-8")));

                    try {
                        pixmap[0] = Utils.getPixmapFromBitmap(PlatformConnector.getSvg("",stream, PlatformConnector.SvgScaleType.NONE, 1));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // create texture on GlThread
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            Texture texture = new Texture(new PixmapTextureData(pixmap[0], Pixmap.Format.RGBA8888, false, true));
                            TextureRegion region = new TextureRegion(texture);
                            setStageBackground(new TextureRegionDrawable(region));
                        }
                    });
                }
            });
            thread.start();
        }
        super.show();
    }


    public static CB_RectF getHelpEllipseFromActor(Actor actor) {
        Vector2 stagePos = actor.localToStageCoordinates(new Vector2(0, 0));
        return new CB_RectF(stagePos.x - CB.scaledSizes.MARGIN, stagePos.y - CB.scaledSizes.MARGIN, actor.getWidth() + CB.scaledSizes.MARGINx2,
                actor.getHeight() + CB.scaledSizes.MARGINx2);
    }

    public void setStyle(HelpWindowStyle style) {
        this.style = style;
    }

    public static class HelpWindowStyle {
        public Color backgroundColor, borderColor, fontColor;
        public BitmapFont font;
        public float borderSize;
    }
}
