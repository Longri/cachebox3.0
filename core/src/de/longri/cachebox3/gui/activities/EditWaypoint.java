/*
 * Copyright (C) 2017 - 2018 team-cachebox.de
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
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.Utils;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.skin.styles.EditWaypointStyle;
import de.longri.cachebox3.gui.widgets.CB_Button;
import de.longri.cachebox3.gui.widgets.CoordinateButton;
import de.longri.cachebox3.gui.widgets.EditTextBox;
import de.longri.cachebox3.gui.widgets.SelectBox;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.AbstractWaypoint;
import de.longri.cachebox3.types.CacheTypes;
import de.longri.cachebox3.types.MutableWaypoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by Longri on 07.04.2017.
 */
public class EditWaypoint extends ActivityBase {
    private final static Logger log = LoggerFactory.getLogger(EditWaypoint.class);

    private final EditWaypointStyle style;
    private final AbstractWaypoint waypoint;
    private final CB_Button btnOk, btnCancel;
    private final VisLabel cacheTitelLabel, titleLabel, typeLabel, descriptionLabel, clueLabel, startLabel;
    private final ScrollPane scrollPane;
    private final EditTextBox titleTextArea, descriptionTextArea, clueTextArea;
    private final VisTable contentTable;
    private final CoordinateButton coordinateButton;
    private final VisCheckBox startCheckBox;
    private final SelectBox<CacheTypes> selectBox;
    private final boolean showCoordsOnShow;
    private final GenericCallBack<AbstractWaypoint> callBack;
    private final boolean onlyShow;//TODO implement see WaypointView context menu. See issue #252

    public EditWaypoint(final AbstractWaypoint waypoint, boolean showCoordsOnShow, boolean onlyShow, GenericCallBack<AbstractWaypoint> callBack) {
        super("EditWaypoint");
        style = null;
        this.waypoint = waypoint;
        this.showCoordsOnShow = showCoordsOnShow;
        this.callBack = callBack;
        this.onlyShow = onlyShow;

        btnOk = new CB_Button(Translation.get("save"));
        btnCancel = new CB_Button(Translation.get("cancel"));
        cacheTitelLabel = new VisLabel(Database.Data.cacheList.GetCacheById(waypoint.getCacheId()).getName());
        typeLabel = new VisLabel(Translation.get("type"));
        titleLabel = new VisLabel(Translation.get("Title"));
        descriptionLabel = new VisLabel(Translation.get("Description"));
        clueLabel = new VisLabel(Translation.get("Clue"));
        startLabel = new VisLabel(Translation.get("start"));
        titleTextArea = new EditTextBox(false);
        descriptionTextArea = new EditTextBox(true);
        clueTextArea = new EditTextBox(true);
        contentTable = new VisTable();
        coordinateButton = new CoordinateButton(waypoint);
        startCheckBox = new VisCheckBox("");

        scrollPane = new ScrollPane(contentTable);

        Array<CacheTypes> itemList = new Array<>();
        itemList.add(CacheTypes.ReferencePoint);
        itemList.add(CacheTypes.MultiStage);
        itemList.add(CacheTypes.MultiQuestion);
        itemList.add(CacheTypes.Trailhead);
        itemList.add(CacheTypes.ParkingArea);
        itemList.add(CacheTypes.Final);

        selectBox = new SelectBox();
        selectBox.setHideWithItemClick(true);
        selectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                EditWaypoint.this.waypoint.setType(selectBox.getSelected());
                showCbStartPoint(EditWaypoint.this.waypoint.getType() == CacheTypes.MultiStage);
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
        descriptionTextArea.setMinLineCount(4);
        clueTextArea.setMinLineCount(4);
        float lineHeight = descriptionTextArea.getStyle().font.getLineHeight();
        float space = Gdx.graphics.getHeight() / 1.7f;
        int lineCount = (int) ((space / lineHeight) / 2);
        descriptionTextArea.setMaxLineCount(lineCount);
        clueTextArea.setMaxLineCount(lineCount);
        titleTextArea.setText((waypoint.getTitle() == null) ? "" : waypoint.getTitle());
        descriptionTextArea.setText(waypoint.getDescription() == null ? "" : waypoint.getDescription());
        clueTextArea.setText(waypoint.getClue() == null ? "" : waypoint.getClue());
        selectBox.select(waypoint.getType());
    }


    private void showCbStartPoint(boolean visible) {
        startCheckBox.setVisible(visible);
        startLabel.setVisible(visible);
    }

    @Override
    public void onShow() {
        if (this.showCoordsOnShow) {
            Gdx.app.postRunnable(() -> Utils.triggerButtonClicked(coordinateButton));
        }
        CB.stageManager.registerForBackKey(cancelClickListener);
    }


    private void create() {
        this.addActor(btnOk);
        this.addActor(btnCancel);
        this.addActor(scrollPane);

        btnOk.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                Coordinate coor = coordinateButton.getCoordinate();
                AbstractWaypoint newWaypoint = new MutableWaypoint(Database.Data, waypoint);
                newWaypoint.setLatitude(coor.getLatitude());
                newWaypoint.setLongitude(coor.getLongitude());
                newWaypoint.setTitle(titleTextArea.getText());
                newWaypoint.setDescription(descriptionTextArea.getText());
                newWaypoint.setClue(clueTextArea.getText());
                newWaypoint.setStart(startCheckBox.isChecked());
                callBack.callBack(newWaypoint);
                finish();
            }
        });

        btnCancel.addListener(cancelClickListener);

    }

    private final ClickListener cancelClickListener = new ClickListener() {
        public void clicked(InputEvent event, float x, float y) {
            callBack.callBack(null);
            finish();
        }
    };

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

        float maxWidth = Gdx.graphics.getWidth() - CB.scaledSizes.MARGINx2;
        titleTextArea.setMaxWidth(maxWidth);
        descriptionTextArea.setMaxWidth(maxWidth);
        clueTextArea.setMaxWidth(maxWidth);

        scrollPane.setBounds(x, y, Gdx.graphics.getWidth() - CB.scaledSizes.MARGINx2, Gdx.graphics.getHeight() - (y + CB.scaledSizes.MARGINx2));
        contentTable.setBounds(x, y, Gdx.graphics.getWidth() - CB.scaledSizes.MARGINx2, Gdx.graphics.getHeight() - (y + CB.scaledSizes.MARGINx2));
        contentTable.layout();
    }

    @Override
    public void dispose() {
        super.dispose();
        CB.stageManager.unRegisterForBackKey(cancelClickListener);
    }
}
