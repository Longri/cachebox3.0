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

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.SnapshotArray;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.*;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.apis.groundspeak_api.GroundspeakAPI;
import de.longri.cachebox3.apis.groundspeak_api.PostRequest;
import de.longri.cachebox3.apis.groundspeak_api.search.SearchCoordinate;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.ImportProgressChangedEvent;
import de.longri.cachebox3.events.ImportProgressChangedListener;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.events.CacheListChangedEventList;
import de.longri.cachebox3.gui.views.MapView;
import de.longri.cachebox3.gui.widgets.CoordinateButton;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.CoordinateGPS;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;


/**
 * Created by Longri on 12.04.2017.
 */
public class ImportGcPos extends ActivityBase {

    private static final Logger log = LoggerFactory.getLogger(ImportGcPos.class);

    private final VisTextButton bOK, bCancel, btnPlus, btnMinus, tglBtnGPS, tglBtnMap;
    private final VisLabel lblTitle, lblRadius, lblRadiusEinheit, lblCaches, lblWaypoints, lblLogs, lblImages;
    private final Image gsLogo;
    private final CoordinateButton coordBtn;
    private final VisCheckBox checkBoxExcludeFounds, checkBoxOnlyAvailable, checkBoxExcludeHides;
    private final VisTextArea textAreaRadius;
    private Coordinate actSearchPos;
    private boolean importRuns = false;
    private boolean needLayout = true;
    private final Image workAnimation;
    private final VisProgressBar progressBar;

    /**
     * 0=GPS, 1= Map, 2= Manuell
     */
    private int searcheState = 0;


    public ImportGcPos() {
        super("searchOverPosActivity");
        bOK = new VisTextButton(Translation.Get("import"));
        bCancel = new VisTextButton(Translation.Get("cancel"));
        gsLogo = new Image(CB.getSkin().getIcon.GC_Live);
        lblTitle = new VisLabel(Translation.Get("importCachesOverPosition"));
        lblRadius = new VisLabel(Translation.Get(Translation.Get("Radius")));
        lblCaches = new VisLabel("Imported Caches: 0");
        lblWaypoints = new VisLabel("Imported Waypoints: 0");
        lblLogs = new VisLabel("Imported Log's: 0");
        lblImages = new VisLabel("Imported Images: 0");
        textAreaRadius = new VisTextArea("default");
        lblRadiusEinheit = new VisLabel(Config.ImperialUnits.getValue() ? "mi" : "km");
        btnMinus = new VisTextButton("-");
        btnPlus = new VisTextButton("+");
        checkBoxOnlyAvailable = new VisCheckBox(Translation.Get("SearchOnlyAvailable"));
        checkBoxExcludeHides = new VisCheckBox(Translation.Get("SearchWithoutOwns"));
        checkBoxExcludeFounds = new VisCheckBox(Translation.Get("SearchWithoutFounds"));
        coordBtn = new CoordinateButton(EventHandler.getMyPosition());
        tglBtnGPS = new VisTextButton(Translation.Get("FromGps"), "toggle");
        tglBtnMap = new VisTextButton(Translation.Get("FromMap"), "toggle");

        Drawable animationDrawable = VisUI.getSkin().getDrawable("download-animation");
        workAnimation = new Image(animationDrawable);
        progressBar = new VisProgressBar(0, 100, 1, false, "default");


        createOkCancelBtn();
        createToggleButtonLine();

        initialContent();
        setWorkAnimationVisible(false);
//        this.setDebug(true, true);
    }

    @Override
    public void layout() {
        if (!needLayout) {
            super.layout();
            return;
        }

        SnapshotArray<Actor> actors = this.getChildren();
        for (Actor actor : actors)
            this.removeActor(actor);

        this.setFillParent(true);
        this.defaults().pad(CB.scaledSizes.MARGIN);

        this.add(lblTitle).colspan(3).center();
        this.add(gsLogo).colspan(2).center();
        this.row().padTop(new Value.Fixed(CB.scaledSizes.MARGINx2 * 2));
        this.add(lblRadius);
        this.add(textAreaRadius).height(textAreaRadius.getStyle().font.getLineHeight() + CB.scaledSizes.MARGINx2);
        this.add(lblRadiusEinheit).left();
        this.add(btnMinus).width(new Value.Fixed(textAreaRadius.getPrefHeight()));
        this.add(btnPlus).width(new Value.Fixed(textAreaRadius.getPrefHeight()));
        this.row().left();
        this.add(checkBoxOnlyAvailable).colspan(5).left();
        this.row();
        this.add(checkBoxExcludeHides).colspan(5).left();
        this.row();
        this.add(checkBoxExcludeFounds).colspan(5).left();
        this.row();
        Table nestedTable1 = new Table();
        nestedTable1.defaults().pad(CB.scaledSizes.MARGIN);
        nestedTable1.add(tglBtnGPS);
        nestedTable1.add(tglBtnMap);
        this.add(nestedTable1).colspan(5).expandX().fillX();
        this.row();
        this.add(workAnimation).colspan(5).center();
        this.row();
        this.add();
        this.add(progressBar).colspan(3).center().expandX().fillX();
        this.row();
        this.add(lblCaches).colspan(5).left();
        this.row();
        this.add(lblWaypoints).colspan(5).left();
        this.row();
        this.add(lblLogs).colspan(5).left();
        this.row();
        this.add(lblImages).colspan(5).left();
        this.row().expandY().fillY().bottom();
        this.add();
        this.row();
        Table nestedTable2 = new Table();
        nestedTable2.defaults().pad(CB.scaledSizes.MARGIN).bottom();
        nestedTable2.add(bOK).bottom();
        nestedTable2.add(bCancel).bottom();
        this.add(nestedTable2).colspan(5);

        super.layout();
        needLayout = false;
    }


    private void setWorkAnimationVisible(boolean visible) {
        workAnimation.setVisible(visible);
        progressBar.setVisible(visible);
        lblCaches.setVisible(visible);
        lblWaypoints.setVisible(visible);
        lblLogs.setVisible(visible);
        lblImages.setVisible(visible);
    }


    private void createOkCancelBtn() {


        bOK.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                CB.postAsync(new Runnable() {
                    @Override
                    public void run() {
                        ImportNow();
                    }
                });
            }
        });

        bCancel.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (importRuns) {

                } else {
                    finish();
                }
            }
        });
    }


    private void createToggleButtonLine() {
        //TODO disable with no actual MapCenterPos
//        if (MapView.that == null)
//            tglBtnMap.disable();

    }


    private void initialContent() {
        btnPlus.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                incrementRadius(1);
            }
        });

        btnMinus.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                incrementRadius(-1);
            }
        });

        tglBtnGPS.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                actSearchPos = EventHandler.getMyPosition();
                setToggleBtnState(0);
            }
        });

        tglBtnMap.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                Coordinate mapCenterPos = MapView.getLastCenterPos();
                if (mapCenterPos == null) {
                    actSearchPos = new CoordinateGPS(Config.MapInitLatitude.getValue(), Config.MapInitLongitude.getValue());
                } else {
                    actSearchPos = mapCenterPos;
                }
                setToggleBtnState(1);
            }
        });

        checkBoxExcludeFounds.setChecked(Config.SearchWithoutFounds.getValue());
        checkBoxOnlyAvailable.setChecked(Config.SearchOnlyAvailable.getValue());
        checkBoxExcludeHides.setChecked(Config.SearchWithoutOwns.getValue());
        textAreaRadius.setText(String.valueOf(Config.lastSearchRadius.getValue()));

        setToggleBtnState();

    }

    private void initialCoordinates() {
        // initiate Coordinates to actual Map-Center or actual GPS Coordinate
        switch (searcheState) {
            case 0:
                actSearchPos = EventHandler.getMyPosition();
                break;
            case 1:
                Coordinate mapCenterPos = MapView.getLastCenterPos();
                if (mapCenterPos == null) {
                    actSearchPos = new CoordinateGPS(Config.MapInitLatitude.getValue(), Config.MapInitLongitude.getValue());
                } else {
                    actSearchPos = mapCenterPos;
                }
                break;
        }
        setToggleBtnState();
    }

    private void incrementRadius(int value) {
        try {
            int ist = Integer.parseInt(textAreaRadius.getText().toString());
            ist += value;

            if (ist > 100)
                ist = 100;
            if (ist < 1)
                ist = 1;

            textAreaRadius.setText(String.valueOf(ist));
        } catch (NumberFormatException e) {

        }
    }

    /**
     * 0=GPS, 1= Map, 2= Manuell
     */
    public void setToggleBtnState(int value) {
        searcheState = value;
        setToggleBtnState();
    }

    private void setToggleBtnState() {// 0=GPS, 1= Map, 2= Manuell
        switch (searcheState) {
            case 0:
                tglBtnGPS.setChecked(true);
                tglBtnMap.setChecked(false);
                break;
            case 1:
                tglBtnGPS.setChecked(false);
                tglBtnMap.setChecked(true);
                break;
            case 2:
                tglBtnGPS.setChecked(false);
                tglBtnMap.setChecked(false);
                break;

        }
        coordBtn.setCoordinate(actSearchPos);

    }

    private void ImportNow() {
        workAnimation.setVisible(true);

        final ImportProgressChangedListener progressListener = new ImportProgressChangedListener() {
            @Override
            public void progressChanged(ImportProgressChangedEvent event) {

                if(event.progress.msg.equals("Start parsing result")){
                    progressBar.setVisible(true);
                    lblCaches.setVisible(true);
                    lblWaypoints.setVisible(true);
                    lblLogs.setVisible(true);
                    lblImages.setVisible(true);
                }


                progressBar.setValue(event.progress.progress);
                lblCaches.setText("Imported Caches: " + event.progress.caches);
                lblWaypoints.setText("Imported Waypoints: " + event.progress.wayPoints);
                lblLogs.setText("Imported Logs: " + event.progress.logs);
                lblImages.setText("Imported Caches: " + event.progress.images);
            }
        };
        EventHandler.add(progressListener);

        final Date ImportStart = new Date();
        Config.SearchWithoutFounds.setValue(checkBoxExcludeFounds.isChecked());
        Config.SearchOnlyAvailable.setValue(checkBoxOnlyAvailable.isChecked());
        Config.SearchWithoutOwns.setValue(checkBoxExcludeHides.isChecked());

        int radius = 0;
        try {
            radius = Integer.parseInt(textAreaRadius.getText().toString());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        if (radius != 0)
            Config.lastSearchRadius.setValue(radius);

        Config.AcceptChanges();

        bOK.setDisabled(true);
        importRuns = true;


        if (actSearchPos != null) {
            Category category = CB.Categories.getCategory("API-Import");
            if (category != null) // should not happen!!!
            {
                GpxFilename gpxFilename = category.addGpxFilename("API-Import");
                if (gpxFilename != null) {

                    log.debug("Ask for API state");
                    byte apiState;
                    if (GroundspeakAPI.isPremiumMember()) {
                        apiState = 2;
                    } else {
                        apiState = 1;
                    }

                    log.debug("Api state = {}", apiState);
                    log.debug("Search at Coordinate:{}", actSearchPos);
                    final SearchCoordinate searchC = new SearchCoordinate(GroundspeakAPI.getAccessToken(),
                            50, actSearchPos, Config.lastSearchRadius.getValue() * 1000,
                            apiState);
                    searchC.excludeFounds = Config.SearchWithoutFounds.getValue();
                    searchC.excludeHides = Config.SearchWithoutOwns.getValue();
                    searchC.available = Config.SearchOnlyAvailable.getValue();

                    log.debug("Request Groundspeak API");
                    searchC.postRequest(new GenericCallBack<Integer>() {
                        @Override
                        public void callBack(Integer value) {
                            if (value == PostRequest.NO_ERROR) {

                                String Msg;
                                if (ImportStart != null) {
                                    Date Importfin = new Date();
                                    long ImportZeit = Importfin.getTime() - ImportStart.getTime();
                                    Msg = "Import " + String.valueOf(searchC.cacheCount) + "C " + String.valueOf(searchC.logCount) + "L in " + String.valueOf(ImportZeit);
                                } else {
                                    Msg = "Import canceld";
                                }

                                log.debug(Msg);
                                CB.viewmanager.toast(Msg);

                                //remove Progress handler
                                EventHandler.remove(progressListener);

                                //close Dialog
                                finish();

                                //fire CacheList changed event
                                CacheListChangedEventList.Call();
                            }
                        }
                    }, gpxFilename.Id);
                }
            }
        }
    }

    @Override
    public void dispose() {


//        if (bOK != null)
//            bOK.dispose();
//        bOK = null;
//        if (bCancel != null)
//            bCancel.dispose();
//        bCancel = null;
//        if (btnPlus != null)
//            btnPlus.dispose();
//        btnPlus = null;
//        if (btnMinus != null)
//            btnMinus.dispose();
//        btnMinus = null;
//        if (lblTitle != null)
//            lblTitle.dispose();
//        lblTitle = null;
//        if (lblRadius != null)
//            lblRadius.dispose();
//        lblRadius = null;
//        if (lblRadiusEinheit != null)
//            lblRadiusEinheit.dispose();
//        lblRadiusEinheit = null;
//        if (lblMarkerPos != null)
//            lblMarkerPos.dispose();
//        lblMarkerPos = null;
//        if (lblExcludeFounds != null)
//            lblExcludeFounds.dispose();
//        lblExcludeFounds = null;
//        if (lblOnlyAvailable != null)
//            lblOnlyAvailable.dispose();
//        lblOnlyAvailable = null;
//        if (lblExcludeHides != null)
//            lblExcludeHides.dispose();
//        lblExcludeHides = null;
//        if (gsLogo != null)
//            gsLogo.dispose();
//        gsLogo = null;
//        if (coordBtn != null)
//            coordBtn.dispose();
//        coordBtn = null;
//        if (checkBoxExcludeFounds != null)
//            checkBoxExcludeFounds.dispose();
//        checkBoxExcludeFounds = null;
//        if (checkBoxOnlyAvailable != null)
//            checkBoxOnlyAvailable.dispose();
//        checkBoxOnlyAvailable = null;
//        if (checkBoxExcludeHides != null)
//            checkBoxExcludeHides.dispose();
//        checkBoxExcludeHides = null;
//        if (textAreaRadius != null)
//            textAreaRadius.dispose();
//        textAreaRadius = null;
//        if (tglBtnGPS != null)
//            tglBtnGPS.dispose();
//        tglBtnGPS = null;
//        if (tglBtnMap != null)
//            tglBtnMap.dispose();
//        tglBtnMap = null;
//        if (dis != null)
//            dis.dispose();
//        dis = null;
//        if (box != null)
//            box.dispose();
//        box = null;
//
//        actSearchPos = null;
        super.dispose();
    }
}

