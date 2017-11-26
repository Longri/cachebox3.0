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
package de.longri.cachebox3.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.types.CacheSizes;
import de.longri.cachebox3.types.IntProperty;

/**
 * Created by Longri on 02.09.2017.
 */
public class AdjustableStarWidget extends Table {

    private final AbstractIntValueChangedWidget starsWidget;
    private final IntProperty value;
    private final VisLabel valueLabel;
    private final int maxValue;
    private final int minValue;
    private final int step;
    private final Type type;

    public enum Type {
        STAR, SIZE
    }


    public AdjustableStarWidget(Type type, CharSequence title, IntProperty valueProperty) {
        this.type = type;
        this.value = valueProperty;
        maxValue = type == Type.STAR ? 10 : 6;
        minValue = type == Type.STAR ? 0 : 0;
        step = type == Type.STAR ? 1 : 1;
        starsWidget = type == Type.STAR ? new Stars(value.get()) : new CacheSizeWidget(value.get());
        VisTextButton minusBtn = new VisTextButton("-") {
            @Override
            public float getPrefWidth() {
                return this.getPrefHeight();
            }
        };
        VisTextButton plusBtn = new VisTextButton("+") {
            @Override
            public float getPrefWidth() {
                return this.getPrefHeight();
            }
        };

        VisTable centerTable = new VisTable();
        VisLabel titleLabel = new VisLabel(title);
        centerTable.add(titleLabel).left().expandX().fillX();
        centerTable.row();
        VisTable line = new VisTable();
        line.add(starsWidget).left();

        valueLabel = new VisLabel(Double.toString(value.get() / 2));
        if (type == Type.SIZE) {
            line.add((Actor) null).expandX().fillX();
            line.add(valueLabel).padRight(CB.scaledSizes.MARGINx4);
        } else {
            line.add((Actor) null).expandX().fillX();
        }

        centerTable.add(line).left().expandX().fillX();

        this.add(minusBtn).left().padRight(new Value.Fixed(CB.scaledSizes.MARGINx4));
        this.add(centerTable).left().expandX().fillX().padRight(new Value.Fixed(CB.scaledSizes.MARGIN));
        if (type == Type.STAR) this.add(valueLabel).right().padRight(new Value.Fixed(CB.scaledSizes.MARGINx4));
        this.add(plusBtn).right();

        plusBtn.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                int newValue = value.get() + step;
                if (newValue > maxValue) newValue = minValue;
                setValue(newValue);
            }
        });

        minusBtn.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                int newValue = value.get() - step;
                if (newValue < minValue) newValue = maxValue;
                setValue(newValue);
            }
        });

        setValue(valueProperty.get(), true);
    }

    public void setValue(int value) {
        setValue(value, false);
    }

    private void setValue(final int value, final boolean force) {
        CB.postAsync(new Runnable() {
            @Override
            public void run() {
                if (force || AdjustableStarWidget.this.value.get() != value) {

                    if (type == Type.SIZE) {
                        valueLabel.setText(CacheSizes.parseInt(value).toString());
                        starsWidget.setValue(value);
                    } else {
                        valueLabel.setText(Double.toString((double) value / 2.0));
                        starsWidget.setValue(value);
                    }
                    AdjustableStarWidget.this.value.set(value);
                    AdjustableStarWidget.this.invalidateHierarchy();
                }
            }
        });
    }

    public int getValue() {
        return this.value.get();
    }
}
