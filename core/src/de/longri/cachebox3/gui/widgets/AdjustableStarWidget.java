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

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.CB;

/**
 * Created by Longri on 02.09.2017.
 */
public class AdjustableStarWidget extends Table {

    private final Stars starsWidget;
    private final VisTextButton plusBtn, minusBtn;
    private int value = 0;
    private final VisLabel valueLabel;


    public AdjustableStarWidget(String title) {

        starsWidget = new Stars(value);
        minusBtn = new VisTextButton("-") {
            @Override
            public float getPrefWidth() {
                return this.getPrefHeight();
            }
        };
        plusBtn = new VisTextButton("+") {
            @Override
            public float getPrefWidth() {
                return this.getPrefHeight();
            }
        };

        VisTable centerTable = new VisTable();
        VisLabel titleLabel = new VisLabel(title);
        centerTable.add(titleLabel);
        centerTable.row();
        centerTable.add(starsWidget);
        valueLabel = new VisLabel(Double.toString(value / 2));

        this.add(minusBtn);
        this.add(centerTable);
        this.add(valueLabel);
        this.add(plusBtn);

        plusBtn.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                int newValue = value + 1;
                if (newValue > 10) newValue = 0;
                setValue(newValue);
            }
        });

        minusBtn.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                int newValue = value - 1;
                if (newValue < 0) newValue = 10;
                setValue(newValue);
            }
        });
    }

    public void setValue(final int value) {
        CB.postAsync(new Runnable() {
            @Override
            public void run() {
                if (AdjustableStarWidget.this.value != value) {
                    AdjustableStarWidget.this.value = value;
                    valueLabel.setText(Double.toString((double) AdjustableStarWidget.this.value / 2.0));
                    starsWidget.setValue(AdjustableStarWidget.this.value);
                    AdjustableStarWidget.this.invalidateHierarchy();
                }
            }
        });
    }

    public int getValue() {
        return this.value;
    }
}
