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
package de.longri.cachebox3.gui.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.StringBuilder;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.utils.BuildInfo;
import de.longri.cachebox3.utils.UnitFormatter;

/**
 * Created by Longri on 23.07.16.
 */
public class AboutView extends AbstractTableView implements de.longri.cachebox3.events.PositionChangedListener, de.longri.cachebox3.events.DistanceChangedListener {

    VisLabel coordinateLabel, versionLabel;


    public AboutView() {
        super("AboutView");
        create();
    }

    @Override
    protected void create() {
        contentTable.setDebug(true, true);
        contentTable.setBackground(CB.backgroundImage.getDrawable());

        contentTable.add().height(new Value.Fixed(CB.scaledSizes.MARGINx2*3));
        contentTable.row();
        contentTable.add(CB.CB_Logo).center();
        contentTable.add().height(new Value.Fixed(CB.scaledSizes.MARGINx2));
        contentTable.row();

        VisLabel versionLabel = new VisLabel("Version: " + BuildInfo.getVersion() + BuildInfo.getRevision());
        contentTable.add(versionLabel).center();
        contentTable.row();

        Label.LabelStyle style = null;
        try {
            style = VisUI.getSkin().get("AboutViewVersionInfoStyle", Label.LabelStyle.class);
        } catch (Exception e) {
        }
        VisLabel versionInfoLabel = new VisLabel("(" + BuildInfo.getBranch() + " " + BuildInfo.getBuildDate() +
                " " + BuildInfo.getSHA() + ")", style == null ? VisUI.getSkin().get(Label.LabelStyle.class) : style);

        contentTable.add(versionInfoLabel).center();
        contentTable.row();

        // bottom fill
        contentTable.row().expandY().fillY().bottom();
        contentTable.add();
        contentTable.row();


        coordinateLabel = new VisLabel(this.NAME);
        coordinateLabel.setAlignment(Align.center);
        coordinateLabel.setPosition(10, 10);
        this.addActor(coordinateLabel);


        //register as Location receiver
        de.longri.cachebox3.events.EventHandler.add(this);
    }


    @Override
    public void dispose() {
        //register as Location receiver
        de.longri.cachebox3.events.EventHandler.remove(this);
    }

    protected void boundsChanged(float x, float y, float width, float height) {
        super.boundsChanged(x, y, width, height);
        coordinateLabel.setBounds(0, 0, this.getWidth(), this.getHeight());
    }


    Coordinate pos;
    float distance = -1;

    @Override
    public void positionChanged(de.longri.cachebox3.events.PositionChangedEvent event) {
        pos = event.pos;
        setText();
    }

    @Override
    public void distanceChanged(de.longri.cachebox3.events.DistanceChangedEvent event) {
        distance = event.distance;
        setText();
    }

    private void setText() {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                StringBuilder sb = new StringBuilder();
                sb.append(pos != null ? pos.formatCoordinateLineBreak() : "???");
                sb.append("\n");
                sb.append(distance == -1 ? "???" : UnitFormatter.distanceString(distance, false));
                coordinateLabel.setText(sb);
                CB.requestRendering();
            }
        });
    }

    public String toString() {
        return "AboutView";
    }
}
