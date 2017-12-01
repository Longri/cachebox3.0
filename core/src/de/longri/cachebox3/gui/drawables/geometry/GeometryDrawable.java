/* 
 * Copyright (C) 2014 - 2017 team-cachebox.de
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
package de.longri.cachebox3.gui.drawables.geometry;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Disposable;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Longri
 */
public class GeometryDrawable implements Drawable, Disposable {

    protected Color color;
    protected float width, height;
    protected float[] vertices;
    protected short[] triangles;
    protected TextureRegion texReg;
    protected Texture tex;
    protected Pixmap pix;
    protected PolygonRegion po;
    private final AtomicBoolean isDisposed = new AtomicBoolean(false);
    private final IGeometry geometry;

    public GeometryDrawable(final IGeometry geometry, final Color color, float width, float height) {
        this.geometry = geometry;
        this.height = height;
        this.width = width;
        this.color = color;

        setVerticesTriangles();
    }

    public void setColor(Color color) {
        this.color.set(color);
    }

    public void setColor(float r, float g, float b, float a) {
        this.color.set(r, g, b, a);
    }

    private void setVerticesTriangles() {
        geometry.setSize(width, height);
        geometry.compute();

        this.vertices = geometry.getVertices();
        this.triangles = geometry.getTriangles();

        if (po != null) {
            po = null;
            tex = null;
            texReg = null;
        }
    }

    private void createTexRegFromPixMap() {
        if (isDisposed.get())
            return;

        if (tex == null) {
            int w = 2;
            int h = 2;
            pix = new Pixmap(w, h, Pixmap.Format.RGB565);
            pix.setColor(new Color(Color.WHITE));

            pix.fillRectangle(0, 0, w, h);

            try {
                tex = new Texture(pix);
            } catch (Exception e) {
                tex = null;
            }
        }

        if (tex != null) {
            tex.setFilter(TextureFilter.Linear, TextureFilter.MipMapLinearLinear);
            texReg = new TextureRegion(tex, (int) this.width, (int) this.height);
        }

        if (pix != null) {
            pix.dispose();
            pix = null;
        }

    }

    public boolean isDisposed() {
        return isDisposed.get();
    }

    @Override
    public void dispose() {
        synchronized (isDisposed) {
            if (isDisposed.get())
                return;
            texReg = null;
            if (tex != null)
                tex.dispose();
            tex = null;
            if (pix != null)
                pix.dispose();
            pix = null;
            po = null;

            vertices = null;
            triangles = null;
            isDisposed.set(true);
        }
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
        setVerticesTriangles();
    }

    //###########################################################
    // Drawable implementations

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {

        if (!(batch instanceof PolygonSpriteBatch)) {
            throw new RuntimeException("Can't draw a GeometryDrawable on non PolygonSpriteBatch");
        }


        synchronized (isDisposed) {

            if (isDisposed.get())
                return;

            if (po == null) {

                if (vertices == null || vertices.length == 0) return;

                if (texReg == null)
                    createTexRegFromPixMap();
                po = new PolygonRegion(texReg, vertices, triangles);
            }

            Color c = batch.getColor();
            float a = c.a;
            float r = c.r;
            float g = c.g;
            float b = c.b;

            if (po == null)
                return;

            batch.setColor(color);
            batch.flush();
            try {
                ((PolygonSpriteBatch) batch).draw(po, x, y, width, height);
            } catch (Exception e) {
                e.printStackTrace();
            }
            batch.flush();
            // reset color
            batch.setColor(r, g, b, a);
        }
    }

    @Override
    public float getLeftWidth() {
        return 0;
    }

    @Override
    public void setLeftWidth(float v) {

    }

    @Override
    public float getRightWidth() {
        return 0;
    }

    @Override
    public void setRightWidth(float v) {

    }

    @Override
    public float getTopHeight() {
        return 0;
    }

    @Override
    public void setTopHeight(float v) {

    }

    @Override
    public float getBottomHeight() {
        return 0;
    }

    @Override
    public void setBottomHeight(float v) {

    }

    @Override
    public float getMinWidth() {
        return width;
    }

    @Override
    public void setMinWidth(float v) {

    }

    @Override
    public float getMinHeight() {
        return height;
    }

    @Override
    public void setMinHeight(float v) {

    }

    public Color getColor() {
        return this.color;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public void setDirty() {
        setVerticesTriangles();
    }
}
