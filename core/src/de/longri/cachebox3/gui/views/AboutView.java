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

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.events.location.PositionChangedEvent;
import de.longri.cachebox3.events.location.PositionChangedListener;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.utils.BuildInfo;

/**
 * Created by Longri on 23.07.16.
 */
public class AboutView extends AbstractTableView implements PositionChangedListener, de.longri.cachebox3.events.DistanceChangedListener {


    public static final String aboutMsg1 = "Team Cachebox (2011-2017)";
    public static final String teamLink = "www.team-cachebox.de";
    public static final String aboutMsg2 = "Cache Icons Copyright 2009,\nGroundspeak Inc. Used with permission";

    public AboutView(de.longri.serializable.BitStore reader) {
        super(reader);
        create();
    }

    public AboutView() {
        super("AboutView");
        create();
    }

    @Override
    protected void create() {
//        contentTable.setDebug(true, true);
        contentTable.setBackground(CB.backgroundImage.getDrawable());

        contentTable.add().height(new Value.Fixed(CB.scaledSizes.MARGINx2 * 3));
        contentTable.row();
        contentTable.add(CB.CB_Logo).center();
        contentTable.add().height(new Value.Fixed(CB.scaledSizes.MARGINx2));
        contentTable.row();

        VisLabel versionLabel = new VisLabel("Version: " + BuildInfo.getVersion() + BuildInfo.getRevision());
        contentTable.add(versionLabel).center();
        contentTable.row();

        Label.LabelStyle style;
        try {
            style = VisUI.getSkin().get("AboutViewVersionInfoStyle", Label.LabelStyle.class);
        } catch (Exception e) {
            style = null;
        }
        VisLabel versionInfoLabel = new VisLabel("(" + BuildInfo.getBranch() + " " + BuildInfo.getBuildDate() +
                " " + BuildInfo.getSHA() + ")", style == null ? VisUI.getSkin().get(Label.LabelStyle.class) : style);

        contentTable.add(versionInfoLabel).center();
        contentTable.row();

        Label.LabelStyle aboutStyle;
        try {
            aboutStyle = VisUI.getSkin().get("AboutSmall", Label.LabelStyle.class);
        } catch (Exception ex) {
            aboutStyle = null;
        }

        VisLabel aboutLabel1 = new VisLabel(aboutMsg1, aboutStyle == null ? VisUI.getSkin().get(Label.LabelStyle.class) : aboutStyle);
        contentTable.add(aboutLabel1).center();
        contentTable.row();


        Label.LabelStyle linkStyle = null;
        try {
            linkStyle = VisUI.getSkin().get("AboutLinkLabel", Label.LabelStyle.class);
        } catch (Exception e) {
        }

        VisLabel teamLinkLabel = new VisLabel(teamLink, linkStyle == null ? VisUI.getSkin().get(Label.LabelStyle.class) : linkStyle);
        contentTable.add(teamLinkLabel).center();
        contentTable.row();

        teamLinkLabel.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                PlatformConnector._openUrlExtern(teamLink);
            }
        });


        VisLabel aboutLabel2 = new VisLabel(aboutMsg2, aboutStyle == null ? VisUI.getSkin().get(Label.LabelStyle.class) : aboutStyle);
        aboutLabel2.setAlignment(Align.center);
        contentTable.add(aboutLabel2).center();
        contentTable.row();


        // bottom fill
        contentTable.row().expandY().fillY().bottom();
        contentTable.add();
        contentTable.row();


        //register as Location receiver
        de.longri.cachebox3.events.EventHandler.add(this);
    }

    @Override
    public void dispose() {
        //register as Location receiver
        de.longri.cachebox3.events.EventHandler.remove(this);
    }

//    protected void boundsChanged(float x, float y, float width, float height) {
//        super.boundsChanged(x, y, width, height);
//
//    }


    Coordinate pos;
    float distance = -1;

    @Override
    public void positionChanged(PositionChangedEvent event) {
        pos = event.pos;

    }

    @Override
    public void distanceChanged(de.longri.cachebox3.events.DistanceChangedEvent event) {
        distance = event.distance;

    }

    public String toString() {
        return "AboutView";
    }


    //################### Context menu implementation ####################################
    @Override
    public boolean hasContextMenu() {
        return false;
    }

    @Override
    public Menu getContextMenu() {
        return null;
    }
}
