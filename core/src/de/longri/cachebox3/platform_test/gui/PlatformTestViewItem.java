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
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItem;

/**
 * Created by Longri on 18.03.2019.
 */
public class PlatformTestViewItem extends ListViewItem {

    private final TestStateWidget testStateWidget = new TestStateWidget();

    enum Type {
        CONTAINER, TEST
    }

    enum State {
        NOT_TESTED, TEST_OK, TEST_FAIL, IN_PROGRESS
    }

    private final VisTable contentTable = new VisTable();
    public final String className;
    public final String containerName;
    public final String testName;
    public final Type type;
    public final TimeLabel timeLabel = new TimeLabel(false);
    public final boolean runOnGL;
    public final String beforeAllName, afterAllName;

    private String testMsg = null;

    public PlatformTestViewItem(int index, Type type, String containerName) {
        this(index, type, containerName, null, false);
    }

    public PlatformTestViewItem(int index, Type type, String containerName, String testName, boolean runOnGL) {
        this(index, type, containerName, testName, runOnGL, null, null);
    }

    public PlatformTestViewItem(int index, Type type, String containerName, String testName, boolean runOnGL, String beforeAllName, String afterAllName) {
        super(index);
        this.type = type;
        this.runOnGL = runOnGL;
        this.testName = testName;
        this.className = containerName;
        int pos = containerName.lastIndexOf('.');
        this.containerName = containerName.substring(pos + 1, containerName.length());
        this.beforeAllName = beforeAllName;
        this.afterAllName = afterAllName;

        contentTable.setRound(false);

        VisLabel label1 = new VisLabel(testName == null ? this.containerName : testName, "AboutInfo", Color.BLACK);
        Table lineTable = new Table();
        lineTable.defaults().left().pad(CB.scaledSizes.MARGIN);

        // add whitespaces if Type.Test
        if (type == Type.TEST) {
            VisLabel label2 = new VisLabel("     ");
            lineTable.add(label2);
        }

        //add indicatorWidget
        lineTable.add(testStateWidget);

        lineTable.add(label1).left().expandX().fillX();
        lineTable.add(timeLabel);
        contentTable.add(lineTable).left().expandX().fillX();

        this.addActor(contentTable);
    }

    public void setState(State state) {
        setState(state, null);
    }

    public void setState(State state, String msg) {
        testStateWidget.setState(state);
        this.testMsg = msg;
    }


    public String getMsg() {
        return this.testMsg;
    }

    public void start() {
        timeLabel.start();
    }

    public void stop() {
        timeLabel.stop();
    }

    @Override
    protected void sizeChanged() {
        contentTable.setBounds(0, 0, this.getWidth(), this.getHeight());
    }

}
