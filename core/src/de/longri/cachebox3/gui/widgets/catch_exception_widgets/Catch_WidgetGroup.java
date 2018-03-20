/*
 * Copyright (C) 2018 team-cachebox.de
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
package de.longri.cachebox3.gui.widgets.catch_exception_widgets;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import de.longri.cachebox3.CB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Longri on 19.03.2018.
 */
public class Catch_WidgetGroup extends WidgetGroup {

    private final static Logger log = LoggerFactory.getLogger(Catch_WidgetGroup.class);
    private final AtomicBoolean drawException = new AtomicBoolean(false);
    private final AtomicInteger drawCount = new AtomicInteger(0);

    @Override
    public void draw(Batch batch, float parentAlpha) {
        try {
            super.draw(batch, parentAlpha);
        } catch (Exception e) {
            log.error("on draw: " + this.getName(), e);

            //draw red filled rec
            drawException.set(true);
            drawCount.set(0);
        }
    }

    protected void drawDebugBounds(ShapeRenderer shapes) {
        super.drawDebugBounds(shapes);
        if (drawException.get()) {
            if (drawCount.incrementAndGet() > 10) {
                drawException.set(false);
            } else {
                CB.postOnNextGlThread(new Runnable() {
                    @Override
                    public void run() {
                        CB.requestRendering();
                    }
                });
            }
            shapes.set(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(CB.EXCEPTION_COLOR_DRAWING);
            shapes.rect(this.getX(), this.getY(), this.getOriginX(), this.getOriginY(),
                    this.getWidth(), this.getHeight(), this.getScaleX(), this.getScaleY(), this.getRotation());
        }
    }
}
