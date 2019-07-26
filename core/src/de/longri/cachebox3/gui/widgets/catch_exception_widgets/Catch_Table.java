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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Longri on 19.03.2018.
 * Extension to simplify design by arbor95 25. July 2019 :
 * <why> Adding actors may force unpredictable layout) </why>
 * <Idea> The table is structured in subtables, each representing a line (or a block of lines) </Idea>
 * <HowTo>Create a subtable explicit with createSubTable
 * or  implicit with addNext, addNextNL, addNL, addNLNext, addLast
 * These commands add an actor just like the simple .add
 * To switch to the next line (subtable) explicit call endSubTable or addLast </HowTo>
 * <colspan>With these calls there is a automatic calculation for spanning of columns within the subtable.
 * For a cell to expand to a percentage width use 'cell'.colspan(-'percentvalue')
 * This will never be exact, but gives control over the layout with only liitle action</colspan>
 *
 * <Further>Common defaults can be set by a constructor with boolean parameter set to true or
 * an explicit call to setDefaults().</Further>
 *
 */
public class Catch_Table extends VisTable {

    private final static Logger log = LoggerFactory.getLogger(Catch_Table.class);
    private final AtomicBoolean drawException = new AtomicBoolean(false);
    private final AtomicInteger drawCount = new AtomicInteger(0);
    private Catch_Table currentSubTable;
    private Cell currentSubTableCell;

    public Catch_Table() {
        super();
    }

    public Catch_Table(boolean withDefaults) {
        super();
        if (withDefaults)
            setDefaults();
    }

    public Cell setDefaults() {
        top();
        return defaults().expandX().fill().pad(CB.scaledSizes.MARGIN);
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

    public Catch_Table createSubTable() {
        row();
        currentSubTable = new Catch_Table(true);
        currentSubTableCell = add(currentSubTable);
        return currentSubTable;
    }

    public <T extends Actor> Cell<T> addNext(T actor) {
        if (currentSubTable == null)
            createSubTable();
        return currentSubTable.add(actor);
    }

    public <T extends Actor> Cell<T> addNextNL(T actor) {
        if (currentSubTable == null)
            createSubTable();
        Cell<T> cell = currentSubTable.add(actor);
        currentSubTable.row();
        return cell;
    }

    public void addNL() {
        if (currentSubTable == null)
            createSubTable();
        currentSubTable.row();
    }

    public <T extends Actor> Cell<T> addNLNext(T actor) {
        if (currentSubTable == null)
            createSubTable();
        currentSubTable.row();
        return currentSubTable.add(actor);
    }

    public <T extends Actor> Cell<T> addLast(T actor) {
        if (currentSubTable == null)
            createSubTable();
        Cell<T> cell = currentSubTable.add(actor);
        currentSubTable.prepareLayout();
        currentSubTable = null;
        return cell;
    }

    public <T extends Actor> Cell<T> addLastExpand(T actor) {
        if (currentSubTable == null)
            createSubTable();
        currentSubTableCell.expand();
        Cell<T> cell = currentSubTable.add(actor);
        currentSubTable.prepareLayout();
        currentSubTable = null;
        return cell;
    }

    public void endSubTable() {
        if (currentSubTable != null) {
            currentSubTable.prepareLayout();
            currentSubTable = null;
        }
    }

    public void prepareLayout() {
        row();
        for (Cell c : getCells()) {
            if (c.getColspan() < 0) {
                c.colspan((int) (getColumns() * (c.getColspan() / -100f) + 0.5));
            }
        }
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
