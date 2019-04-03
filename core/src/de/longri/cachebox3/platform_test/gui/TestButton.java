/*
 * Copyright (C) 2019 team-cachebox.de
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
package de.longri.cachebox3.platform_test.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.skin.styles.CircularProgressStyle;
import de.longri.cachebox3.gui.widgets.CB_ProgressBar;
import de.longri.cachebox3.gui.widgets.CircularProgressWidget;

/**
 * Created by Longri on 2019-03-24.
 */
public class TestButton extends Button {

    // Table
    // ProgressWidget  "Run Platform Tests"   TestsReady/AllTests
    // ProgressWidget      ProgressBar            elapsed Time
    // ProgressWidget    act running test         elapsed Time


    final private CircularProgressWidget circularProgressWidget;
    final TimeLabel labelTime;
    final VisLabel labelActName;
    private int testCount;
    private final VisLabel labelCount;
    private final CB_ProgressBar progressBar;


    TestButton(int size) {
        super(VisUI.getSkin().get(VisTextButton.VisTextButtonStyle.class));
        testCount = size;

        VisTable contentTable = new VisTable();
        VisTable rightContentTable = new VisTable();
        VisLabel labelTitle = new VisLabel("Run Platform Tests");
        labelCount = new VisLabel("0/" + size, "AboutInfo", Color.BLACK);
        labelTime = new TimeLabel(true);
        labelActName = new VisLabel("            ", "AboutInfo", Color.BLACK);


        progressBar = new CB_ProgressBar(0, size, 1, false, "default");

        rightContentTable.defaults().pad(CB.scaledSizes.MARGIN);
        rightContentTable.add(labelTitle);
        rightContentTable.add(labelCount);
        rightContentTable.row();
        rightContentTable.add(progressBar).expandX().fillX();
        rightContentTable.add(labelTime);
        rightContentTable.row();
        rightContentTable.add(labelActName);
        rightContentTable.add();

        CircularProgressStyle style = new CircularProgressStyle(VisUI.getSkin().get("circularProgressStyle", CircularProgressStyle.class));
        style.scaledPreferedRadius = CB.getScaledFloat(10);
        style.unknownColor = Color.BLACK;

        circularProgressWidget = new CircularProgressWidget(style);


        contentTable.add(circularProgressWidget).padRight(CB.scaledSizes.MARGIN);
        contentTable.add(rightContentTable);

        this.add(contentTable);

        circularProgressWidget.setVisible(false);

    }

    public void beginnTest() {
        circularProgressWidget.setVisible(true);
        labelTime.start();

    }

    public void testFinish(boolean anyTestFaild) {
        labelTime.stop();
        circularProgressWidget.setVisible(false);
    }

    public void setActTestName(String name) {
        labelActName.setText(name);
    }

    public void setreadyTestCount(int count) {
        labelCount.setText(count + "/" + testCount);
        progressBar.setValue(count);
    }

    public void setreadyTestCountIO(int count) {
        labelCount.setText(count + "/" + testCount);
    }
}
