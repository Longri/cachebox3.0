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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.*;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.Utils;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.skin.styles.EditWaypointStyle;
import de.longri.cachebox3.gui.widgets.CoordinateButton;
import de.longri.cachebox3.gui.widgets.SelectBox;
import de.longri.cachebox3.locator.Coordinate;
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
    private final SelectBox<CacheTypes> selectBox;
    private final boolean showCoordsOnShow;
    private final GenericCallBack<Waypoint> callBack;

    public EditWaypoint(Waypoint waypoint, boolean showCoordsOnShow, GenericCallBack<Waypoint> callBack) {
        super("EditWaypoint");
        style = null;
        this.waypoint = waypoint;
        this.showCoordsOnShow = showCoordsOnShow;
        this.callBack = callBack;

        btnOk = new VisTextButton(Translation.Get("save"));
        btnCancel = new VisTextButton(Translation.Get("cancel"));
        cacheTitelLabel = new VisLabel(Database.Data.Query.GetCacheById(waypoint.CacheId).getName());
        typeLabel = new VisLabel(Translation.Get("type"));
        titleLabel = new VisLabel(Translation.Get("Title"));
        descriptionLabel = new VisLabel(Translation.Get("Description"));
        clueLabel = new VisLabel(Translation.Get("Clue"));
        startLabel = new VisLabel(Translation.Get("start"));
        titleTextArea = new VisTextArea();
        descriptionTextArea = new VisTextArea();
        clueTextArea = new VisTextArea();
        contentTable = new VisTable();
        scrollPane = new VisScrollPane(contentTable);
        coordinateButton = new CoordinateButton(waypoint);
        startCheckBox = new VisCheckBox("");

        Array<CacheTypes> itemList = new Array<>();
        itemList.add(CacheTypes.ReferencePoint);
        itemList.add(CacheTypes.MultiStage);
        itemList.add(CacheTypes.MultiQuestion);
        itemList.add(CacheTypes.Trailhead);
        itemList.add(CacheTypes.ParkingArea);
        itemList.add(CacheTypes.Final);

        selectBox = new SelectBox();
        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                EditWaypoint.this.waypoint.Type = selectBox.getSelected();
                showCbStartPoint(EditWaypoint.this.waypoint.Type == CacheTypes.MultiStage);
            }
        });
        selectBox.set(itemList);

        contentTable.setTransform(true);
//        contentTable.setDebug(true, true);
        contentTable.add(cacheTitelLabel).colspan(2).expandX().fillX();
        contentTable.row();
        contentTable.add(coordinateButton).colspan(2);
        contentTable.row();
        contentTable.add(typeLabel, startLabel);
        contentTable.row();
        contentTable.add(selectBox).expandX().fillX().padRight(CB.scaledSizes.MARGIN);
        contentTable.add(startCheckBox).padLeft(CB.scaledSizes.MARGIN);
        contentTable.row();
        contentTable.add(titleLabel).left();
        contentTable.row();
        contentTable.add(titleTextArea).colspan(2).expandX().fillX();
        contentTable.row();
        contentTable.add(descriptionLabel).left();
        contentTable.row();
        contentTable.add(descriptionTextArea).colspan(2).expandX().fillX();
        contentTable.row();
        contentTable.add(clueLabel).left();
        contentTable.row();
        contentTable.add(clueTextArea).colspan(2).expandX().fillX();

        // bottom fill
        contentTable.row().expandY().fillY().bottom();
        contentTable.add();
        contentTable.row();

        create();
        titleTextArea.setPrefRows(1.2f);
        titleTextArea.setText((waypoint.getTitle() == null) ? "" : waypoint.getTitle());
        descriptionTextArea.addListener(textAreaChangeListener);
        clueTextArea.addListener(textAreaChangeListener);
    }


    ChangeListener textAreaChangeListener = new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
            VisTextArea textArea = (VisTextArea) actor;
            int lines = textArea.getLines();
            if (lines > 6) lines = 5;
            textArea.setPrefRows(lines + 1.5f);
            textArea.invalidate();
            contentTable.invalidate();
        }
    };


    private void showCbStartPoint(boolean visible) {
        startCheckBox.setVisible(visible);
        startLabel.setVisible(visible);
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
                Coordinate coor = coordinateButton.getCoordinate();
                Waypoint newWaypoint = new Waypoint(coor.latitude, coor.longitude, waypoint);
                newWaypoint.setTitle(titleTextArea.getText());
                newWaypoint.setDescription(descriptionTextArea.getText());
                newWaypoint.setClue(clueTextArea.getText());
                newWaypoint.IsStart = startCheckBox.isChecked();
                callBack.callBack(newWaypoint);
                finish();
            }
        });

        btnCancel.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                callBack.callBack(null);
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
