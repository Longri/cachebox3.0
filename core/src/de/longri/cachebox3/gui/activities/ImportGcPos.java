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

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextArea;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.apis.GroundspeakAPI;
import de.longri.cachebox3.events.CacheListChangedEvent;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.ImportProgressChangedEvent;
import de.longri.cachebox3.events.ImportProgressChangedListener;
import de.longri.cachebox3.gui.BlockGpsActivityBase;
import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButtons;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.stages.ViewManager;
import de.longri.cachebox3.gui.views.MapView;
import de.longri.cachebox3.gui.widgets.CB_Button;
import de.longri.cachebox3.gui.widgets.CB_CheckBox;
import de.longri.cachebox3.gui.widgets.CB_ProgressBar;
import de.longri.cachebox3.gui.widgets.CoordinateButton;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.CoordinateGPS;
import de.longri.cachebox3.locator.LatLong;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.Cache3DAO;
import de.longri.cachebox3.sqlite.dao.LogDAO;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.Category;
import de.longri.cachebox3.types.GpxFilename;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.cachebox3.utils.UnitFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.longri.cachebox3.apis.GroundspeakAPI.searchGeoCaches;


/**
 * Created by Longri on 12.04.2017.
 */
public class ImportGcPos extends BlockGpsActivityBase {

    private static final Logger log = LoggerFactory.getLogger(ImportGcPos.class);

    private final CB_Button bOK, bCancel, btnPlus, btnMinus, tglBtnGPS, tglBtnMap;
    private final VisLabel lblTitle, lblRadius, lblRadiusUnit, lblCaches, lblWaypoints, lblLogs, lblImages;
    private final Image gsLogo;
    private final CoordinateButton coordBtn;
    private final CB_CheckBox checkBoxExcludeFounds, checkBoxOnlyAvailable, checkBoxExcludeHides;
    private final VisTextArea textAreaRadius;
    private Coordinate actSearchPos;
    private boolean importRuns = false;
    private boolean needLayout = true;
    private final Image workAnimation;
    private final CB_ProgressBar CBProgressBar;
    private final AtomicBoolean canceled = new AtomicBoolean(false);

    /**
     * 0=GPS, 1= Map, 2= Manuell
     */
    private int searchState = 0;


    public ImportGcPos() {
        super("searchOverPosActivity");
        bOK = new CB_Button(Translation.get("import"));
        bCancel = new CB_Button(Translation.get("cancel"));
        gsLogo = new Image(CB.getSkin().getIcon.GC_Live);
        lblTitle = new VisLabel(Translation.get("importCachesOverPosition"));
        lblRadius = new VisLabel(Translation.get("Radius"));
        lblCaches = new VisLabel("Imported Caches: 0");
        lblWaypoints = new VisLabel("Imported Waypoints: 0");
        lblLogs = new VisLabel("Imported Log's: 0");
        lblImages = new VisLabel("Imported Images: 0");
        textAreaRadius = new VisTextArea("default");
        lblRadiusUnit = new VisLabel(Config.ImperialUnits.getValue() ? "mi" : "km");
        btnMinus = new CB_Button("-");
        btnPlus = new CB_Button("+");
        checkBoxOnlyAvailable = new CB_CheckBox(Translation.get("SearchOnlyAvailable"));
        checkBoxExcludeHides = new CB_CheckBox(Translation.get("SearchWithoutOwns"));
        checkBoxExcludeFounds = new CB_CheckBox(Translation.get("SearchWithoutFounds"));
        coordBtn = new CoordinateButton(EventHandler.getMyPosition());
        tglBtnGPS = new CB_Button(Translation.get("FromGps"), "toggle");
        tglBtnMap = new CB_Button(Translation.get("FromMap"), "toggle");

        Drawable animationDrawable = VisUI.getSkin().getDrawable("download-animation");
        workAnimation = new Image(animationDrawable);
        CBProgressBar = new CB_ProgressBar(0, 100, 1, false, "default");


        createOkCancelBtn();
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

        //check if map position actual
        Coordinate mapCenter = MapView.getLastCenterPos();
        if (mapCenter == null || mapCenter.isZero() || !mapCenter.isValid()) {
            tglBtnMap.setDisabled(true);
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
        this.add(textAreaRadius).height(new Value.Fixed((textAreaRadius.getStyle().font.getLineHeight() + CB.scaledSizes.MARGINx2) * 1.3f));
        this.add(lblRadiusUnit).left();
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
        this.add(CBProgressBar).colspan(3).center().expandX().fillX();
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
        CBProgressBar.setVisible(visible);
        lblCaches.setVisible(visible);
        lblWaypoints.setVisible(visible);
        lblLogs.setVisible(visible);
        lblImages.setVisible(visible);
    }


    private void createOkCancelBtn() {


        bOK.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                CB.postAsync(new NamedRunnable("ImportGcPos") {
                    @Override
                    public void run() {
                        startImport();
                    }
                });
            }
        });

        bCancel.addListener(cancelClickListener);
        CB.stageManager.registerForBackKey(cancelClickListener);
    }

    private final ClickListener cancelClickListener = new ClickListener() {
        public void clicked(InputEvent event, float x, float y) {
            if (importRuns) {
                canceled.set(true);
            } else {
                finish();
            }
        }
    };

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
                LatLong lastStoredPos = CB.lastMapState.getFreePosition();
                if (tglBtnMap.isDisabled()) {
                    actSearchPos = new CoordinateGPS(lastStoredPos.getLatitude(), lastStoredPos.getLongitude());
                    setToggleBtnState(0);
                    return;
                }
                Coordinate mapCenterPos = MapView.getLastCenterPos();
                if (mapCenterPos == null) {
                    actSearchPos = new CoordinateGPS(lastStoredPos.getLatitude(), lastStoredPos.getLongitude());
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
        switch (searchState) {
            case 0:
                actSearchPos = EventHandler.getMyPosition();
                break;
            case 1:
                Coordinate mapCenterPos = MapView.getLastCenterPos();
                if (mapCenterPos == null) {
                    LatLong lastStoredPos = CB.lastMapState.getFreePosition();
                    actSearchPos = new CoordinateGPS(lastStoredPos.getLatitude(), lastStoredPos.getLongitude());
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
        searchState = value;
        setToggleBtnState();
    }

    private void setToggleBtnState() {// 0=GPS, 1= Map, 2= Manuell
        switch (searchState) {
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

    private void startImport() {
        workAnimation.setVisible(true);

        final ImportProgressChangedListener progressListener = new ImportProgressChangedListener() {
            @Override
            public void progressChanged(final ImportProgressChangedEvent event) {

                if (event.progress.msg.equals("Start parsing result")) {
                    CBProgressBar.setVisible(true);
                    lblCaches.setVisible(true);
                    lblWaypoints.setVisible(true);
                    lblLogs.setVisible(true);
                    lblImages.setVisible(true);
                }
                CB.postOnGlThread(new NamedRunnable("postOnGlThread") {
                    @Override
                    public void run() {
                        CBProgressBar.setValue(event.progress.progress);
                        lblCaches.setText("Imported Caches: " + event.progress.caches);
                        lblWaypoints.setText("Imported Waypoints: " + event.progress.wayPoints);
                        lblLogs.setText("Imported Logs: " + event.progress.logs);
                        lblImages.setText("Imported Images: " + event.progress.images);
                    }
                });
            }
        };
        EventHandler.add(progressListener);

        final Date ImportStart = new Date();
        Config.SearchWithoutFounds.setValue(checkBoxExcludeFounds.isChecked());
        Config.SearchOnlyAvailable.setValue(checkBoxOnlyAvailable.isChecked());
        Config.SearchWithoutOwns.setValue(checkBoxExcludeHides.isChecked());

        int radius = 0;
        try {
            radius = Integer.parseInt(textAreaRadius.getText());
        } catch (NumberFormatException ignore) {
        }

        if (radius != 0)
            Config.lastSearchRadius.setValue(radius);

        Config.AcceptChanges();

        if (Config.ImperialUnits.getValue()) radius = UnitFormatter.getKilometer(radius);

        bOK.setDisabled(true);
        importRuns = true;


        if (actSearchPos != null) {
            importNow(progressListener, ImportStart, radius);
        } else {
            //wait for act Position
            CB.viewmanager.toast(Translation.get("waiting_for_fix"), ViewManager.ToastLength.WAIT);

            while (actSearchPos == null) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                actSearchPos = EventHandler.getMyPosition();
                //TODO react of CANCEL
                if (canceled.get()) {
                    finish();
                    return;
                }
            }
            ViewManager.ToastLength.WAIT.close();
            importNow(progressListener, ImportStart, radius);
        }
    }

    private void importNow(final ImportProgressChangedListener progressListener, final Date importStart, int radius) {
        // todo progressListener, importStart, cancel, ...
        Category category = CB.Categories.getCategory("API-Import");
        if (category != null) { // should not happen!!!
            GpxFilename gpxFilename = category.addGpxFilename("API-Import");
            if (gpxFilename != null) {
                GroundspeakAPI.Query q = new GroundspeakAPI.Query()
                        .resultWithFullFields()
                        .resultWithLogs(30)
                        //.resultWithImages(30)
                        //.publishedDate(publishDate, btnBeforeAfterEqual.getText()) // todo ACB2 extension
                        ;
                q.searchInCircle(actSearchPos, radius * 1000);

                if (Config.SearchWithoutFounds.getValue()) q.excludeFinds();
                if (Config.SearchWithoutOwns.getValue()) q.excludeOwn();
                if (Config.SearchOnlyAvailable.getValue()) q.onlyActiveGeoCaches();

                /*
                // todo implement following ACB2 extensions
                if (edtOwner.getText().trim().length() > 0) q.searchForOwner(edtOwner.getText().trim());
                if (edtCacheName.getText().trim().length() > 0) q.searchForTitle(edtCacheName.getText().trim());

                int importLimit;
                try {
                    importLimit = Integer.parseInt(edtImportLimit.getText());
                } catch (Exception ex) {
                    importLimit = Config.ImportLimit.getDefaultValue();
                }
                q.setMaxToFetch(importLimit);
                 */
                q.setMaxToFetch(Integer.MAX_VALUE);

                //dis.setAnimationType(AnimationType.Download);
                Array<GroundspeakAPI.GeoCacheRelated> fetchedCaches = searchGeoCaches(q);

                //dis.setAnimationType(AnimationType.Work);
                if (GroundspeakAPI.APIError != GroundspeakAPI.OK) {
                    MessageBox.show(GroundspeakAPI.LastAPIError, Translation.get("importCachesOverPosition"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
                } else {
                    // WriteIntoDB.CachesAndLogsAndImagesIntoDB(geoCacheRelateds, gpxFilename);
                    // todo set gpxfilename / category
                    Cache3DAO dao = new Cache3DAO();
                    for (GroundspeakAPI.GeoCacheRelated cacheEntry : fetchedCaches) {
                        dao.writeToDatabase(Database.Data, cacheEntry.cache, true);

                        LogDAO logdao = new LogDAO();
                        logdao.writeToDB(Database.Data, cacheEntry.logs);
                    }
                }

                CB.postOnNextGlThread(() -> CB.postAsync(new NamedRunnable("Reload cacheList after import") {
                    @Override
                    public void run() {
                        Database.Data.cacheList.setUnfilteredSize(Database.Data.getCacheCountOnThisDB());
                        log.debug("Call loadFilteredCacheList()");
                        CB.loadFilteredCacheList(null);
                        CB.postOnNextGlThread(() -> EventHandler.fire(new CacheListChangedEvent()));
                    }
                }));

                //close Dialog
                finish();

            }
        }
    }

    @Override
    public void dispose() {
        CB.stageManager.unRegisterForBackKey(cancelClickListener);

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
//        if (lblRadiusUnit != null)
//            lblRadiusUnit.dispose();
//        lblRadiusUnit = null;
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

