/*
 * Copyright (C) 2017 team-cachebox.de
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
package de.longri.cachebox3.gui.activities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.*;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.Utils;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.skin.styles.EditWaypointStyle;
import de.longri.cachebox3.gui.skin.styles.WayPointListItemStyle;
import de.longri.cachebox3.gui.widgets.CoordinateButton;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.CacheTypes;
import de.longri.cachebox3.types.Waypoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by Longri on 07.04.2017.
 */
public class EditWaypoint extends ActivityBase {
    private final static Logger log = LoggerFactory.getLogger(EditWaypoint.class);

    private final EditWaypointStyle style;
    private final Waypoint waypoint;
    private final VisTextButton btnOk, btnCancel;
    private final VisLabel cacheTitelLabel, titleLabel, typeLabel, descriptionLabel, clueLabel, startLabel;
    private final VisTextArea titleTextArea, descriptionTextArea, clueTextArea;
    private final VisScrollPane scrollPane;
    private final VisTable contentTable;
    private final CoordinateButton coordinateButton;
    private final VisCheckBox startCheckBox;
    private final boolean showCoordsOnShow;

    public EditWaypoint(Waypoint waypoint, boolean showCoordsOnShow) {
        super("EditWaypoint");
        style = null;
        this.waypoint = waypoint;
        this.showCoordsOnShow = showCoordsOnShow;
//        style = VisUI.getSkin().get("default", EditWaypointStyle.class);

        btnOk = new VisTextButton(Translation.Get("save"));
        btnCancel = new VisTextButton(Translation.Get("cancel"));
        cacheTitelLabel = new VisLabel(Database.Data.Query.GetCacheById(waypoint.CacheId).getName());
        typeLabel = new VisLabel(Translation.Get("type"));
        titleLabel = new VisLabel(Translation.Get("Title"));
        descriptionLabel = new VisLabel(Translation.Get("Description"));
        clueLabel = new VisLabel(Translation.Get("Clue"));
        startLabel = new VisLabel(Translation.Get("Start"));
        titleTextArea = new VisTextArea();
        descriptionTextArea = new VisTextArea();
        clueTextArea = new VisTextArea();
        contentTable = new VisTable();
        scrollPane = new VisScrollPane(contentTable);
        coordinateButton = new CoordinateButton(waypoint);
        startCheckBox = new VisCheckBox("");


        WayPointListItemStyle waypointStyle = VisUI.getSkin().get("WayPointListItems", WayPointListItemStyle.class);
        Array<Drawable> itemList = new Array<>();
        itemList.add(CacheTypes.ReferencePoint.getDrawable(waypointStyle.typeStyle));
        itemList.add(CacheTypes.MultiStage.getDrawable(waypointStyle.typeStyle));
        itemList.add(CacheTypes.MultiQuestion.getDrawable(waypointStyle.typeStyle));
        itemList.add(CacheTypes.Trailhead.getDrawable(waypointStyle.typeStyle));
        itemList.add(CacheTypes.ParkingArea.getDrawable(waypointStyle.typeStyle));
        itemList.add(CacheTypes.Final.getDrawable(waypointStyle.typeStyle));


        contentTable.setDebug(true, true);
        contentTable.add(cacheTitelLabel).colspan(2).expandX().fillX();
        contentTable.row();
        contentTable.add(coordinateButton).colspan(2);
        contentTable.row();
        contentTable.add(typeLabel, startLabel);
        contentTable.row();
        contentTable.add(null, startCheckBox);
        contentTable.row();
        contentTable.add(titleLabel).left();
        contentTable.row();
        contentTable.add(titleTextArea).colspan(2).left();
        contentTable.row();
        contentTable.add(descriptionLabel).left();
        contentTable.row();
        contentTable.add(descriptionTextArea).colspan(2).left();
        contentTable.row();
        contentTable.add(clueLabel).left();
        contentTable.row();
        contentTable.add(clueTextArea).colspan(2).left();

        // bottom fill
        contentTable.row().expandY().fillY().bottom();
        contentTable.add();
        contentTable.row();

        create();
    }

    @Override
    public void onShow() {
        if (this.showCoordsOnShow) {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    Utils.triggerButtonClicked(coordinateButton);
                }
            });
        }
    }

    private void create() {
        this.addActor(btnOk);
        this.addActor(btnCancel);
        this.addActor(scrollPane);

        btnOk.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {

                finish();
            }
        });

        btnCancel.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {

                finish();
            }
        });
    }


    @Override
    public void layout() {
        super.layout();

        float x = Gdx.graphics.getWidth() - (CB.scaledSizes.MARGIN + btnCancel.getWidth());
        float y = CB.scaledSizes.MARGIN;

        btnCancel.setPosition(x, y);
        x -= CB.scaledSizes.MARGIN + btnOk.getWidth();
        btnOk.setPosition(x, y);

        x = CB.scaledSizes.MARGIN;
        y += CB.scaledSizes.MARGIN + btnCancel.getHeight();

        scrollPane.setBounds(x, y, Gdx.graphics.getWidth() - CB.scaledSizes.MARGINx2, Gdx.graphics.getHeight() - (y + CB.scaledSizes.MARGINx2));
        contentTable.setBounds(x, y, Gdx.graphics.getWidth() - CB.scaledSizes.MARGINx2, Gdx.graphics.getHeight() - (y + CB.scaledSizes.MARGINx2));

        contentTable.layout();

    }
}
