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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Longri on 19.03.2018.
 * <p>
 * Extension to simplify design by arbor95 28. July 2019 :
 * <why> Adding actors may force unpredictable layout) </why>
 * <Idea> The table is structured in subtables, each representing a line(row)</Idea>
 * <not_implemented> (or a block of lines)</not_implemented>
 * <HowTo>
 * Create a subtable explicit with startRow or  implicit with addNext, addLast
 * <not_implemented>, addNextNL, addNL, addNLNext</not_implemented>
 * These commands add an actor just like the simple .add
 * To switch to the next line/row (subtable) explicit call finishRow or addLast
 * <sizing>With using 'sizing' as second parameter to addNext/addLast
 * <p>
 * the sizing factors the default cell with = whole width devided by number of cells.
 * (factor 0.5f means half of the normal cell width, or 2 means double default cell width)
 * (a negative factor (-0.3f means 30% of a line) is for percentual sizing of a cell)
 * (a faktor < -1.0 = FIXED adds a .left().fill(false) to the cell. The decimals give the percent faktor.
 * the calculation is automatic at end of row and done by setting the colspan values correspondingly
 * This will never be exact, but gives control over the layout with only litle effort</sizing>
 * </HowTo>
 * <Implementation>actorX is temporarily used to store the sizing value, for not to extend the Cell.</Implementation>
 *
 * <Further>Common defaults can be set by a constructor with boolean parameter set to true or
 * an explicit call to setTableAndCellDefaults().</Further>
 */
public class Catch_Table extends VisTable {
    public final static int FIXED = -1;
    private final static Logger log = LoggerFactory.getLogger(Catch_Table.class);
    private final static float sizingMax = 100f;
    private final AtomicBoolean drawException = new AtomicBoolean(false);
    private final AtomicInteger drawCount = new AtomicInteger(0);
    private Catch_Table currentRow;

    public Catch_Table() {
        super();
    }

    public Catch_Table(boolean withDefaults) {
        super();
        if (withDefaults)
            setTableAndCellDefaults();
    }

    public Cell setTableAndCellDefaults() {
        top().left();
        defaults().padTop(0).padBottom(0).padLeft(CB.scaledSizes.MARGIN).padRight(CB.scaledSizes.MARGIN);
        return defaults().expandX().fill().colspan((int) sizingMax);
    }

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

    public Catch_Table newLine() {
        row();
        currentRow = new Catch_Table(true);
        currentRow.setDebug(this.getDebug());
        currentRow.row();
        add(currentRow);
        return currentRow;
    }

    public <T extends Actor> Cell<T> addNext(T actor) {
        if (currentRow == null)
            newLine();
        Cell<T> cell = currentRow.add(actor);
        cell.setActorX(1f);
        return cell;
    }

    public <T extends Actor> Cell<T> addNext(T actor, float sizing) {
        if (currentRow == null)
            newLine();
        Cell<T> cell = currentRow.add(actor);
        cell.setActorX(sizing);
        return cell;
    }

    public <T extends Actor> Cell<T> addLast(T actor) {
        if (currentRow == null)
            newLine();
        Cell<T> cell = currentRow.add(actor);
        cell.setActorX(1f);
        currentRow.prepareLayout();
        currentRow = null;
        return cell;
    }

    public <T extends Actor> Cell<T> addLast(T actor, float sizing) {
        if (currentRow == null)
            newLine();
        Cell<T> cell = currentRow.add(actor);
        cell.setActorX(sizing);
        currentRow.prepareLayout();
        currentRow = null;
        return cell;
    }

    public void finishRow() {
        if (currentRow != null) {
            currentRow.prepareLayout();
            currentRow = null;
        }
    }

    public void prepareLayout() {
        row();
        float remainingSize = sizingMax;
        float sizingSum = 0;
        for (Cell c : getCells()) {
            float sizing = c.getActorX();
            if (sizing < 0) {
                sizing = sizing - (int) sizing;
                if (sizing == 0) sizing = -1;
                float percent = sizing * (-1 * sizingMax);
                c.colspan((int) percent);
                remainingSize = remainingSize - percent;
                // c.setActorX(0f);
            }
        }
        for (Cell c : getCells()) {
            if (c.getActorX() <= FIXED) {
                c.left().fill(false);
            }
        }

        if (remainingSize > 0) {
            for (Cell c : getCells()) {
                if (c.getActorX() > 0) {
                    sizingSum = sizingSum + c.getActorX();
                }
            }
            float sizingBase = remainingSize / sizingSum;
            for (Cell c : getCells()) {
                if (c.getActorX() > 0) {
                    c.colspan((int) (c.getActorX() * sizingBase));
                    // c.setActorX(0f);
                }
            }
        }
    }

    public Catch_Table setBackgroundColor(Color color) {
        Pixmap labelColor = new Pixmap(100, 100, Pixmap.Format.RGB888);
        labelColor.setColor(color);
        labelColor.fill();
        setBackground(new Image(new Texture(labelColor)).getDrawable());
        labelColor.dispose();
        return this;
    }

    public Catch_Table setBackgroundDrawable(Drawable drawable) {
        setBackground(drawable);
        return this;
    }

    protected void drawDebugBounds(ShapeRenderer shapes) {
        super.drawDebugBounds(shapes);
        if (drawException.get()) {
            if (drawCount.incrementAndGet() > 10) {
                drawException.set(false);
            } else {
                CB.postOnNextGlThread(() -> CB.requestRendering());
            }
            shapes.set(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(CB.EXCEPTION_COLOR_DRAWING);
            shapes.rect(this.getX(), this.getY(), this.getOriginX(), this.getOriginY(),
                    this.getWidth(), this.getHeight(), this.getScaleX(), this.getScaleY(), this.getRotation());
        }
    }
}
