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
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTextField;
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
import de.longri.cachebox3.gui.widgets.*;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Table;
import de.longri.cachebox3.locator.Coordinate;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.longri.cachebox3.CB.addClickHandler;
import static de.longri.cachebox3.apis.GroundspeakAPI.searchGeoCaches;


/**
 * Created by Longri on 12.04.2017.
 */
public class ImportGCPosition extends BlockGpsActivityBase {

    private static final Logger log = LoggerFactory.getLogger(ImportGCPosition.class);
    private final CB_Button btnOK, btnCancel, btnPlus, btnMinus, tglBtnGPS, tglBtnMap, tglBtnWeb, btnBeforeAfterEqual;
    private final CB_Label lblTitle, lblRadius, lblRadiusUnit, lblImportLimit, lblCacheName, lblOwner, lblPublished, lblCategory;
    private final VisTextField edtImportLimit, edtCacheName, edtOwner, edtDate, edtCategory, txtRadius;
    private final Image gsLogo;
    private final CoordinateButton coordinateButton;
    private final CB_CheckBox checkBoxExcludeFounds, checkBoxOnlyAvailable, checkBoxExcludeHides;
    private final SimpleDateFormat simpleDateFormat;
    private final CB_Label lblCaches, lblWaypoints, lblLogs, lblImages;
    private final Image workAnimation;
    private final CB_ProgressBar progressBar;
    private final AtomicBoolean canceled = new AtomicBoolean(false);
    private Catch_Table box;
    private ScrollPane scrollPane;
    private boolean importRuns = false;
    private ClickListener cancelClickListener;
    private Coordinate actSearchPos;
    private boolean needLayout = true;
    private SearchCoordinates searchCoordinates;

    public ImportGCPosition() {
        super("searchOverPosActivity");
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        lblTitle = new CB_Label(Translation.get("importCachesOverPosition"));
        gsLogo = new Image(CB.getSkin().getMenuIcon.gc_logo);
        box = new Catch_Table(true);
        scrollPane = new ScrollPane(box);
        btnOK = new CB_Button(Translation.get("import"));
        btnCancel = new CB_Button(Translation.get("cancel"));

        coordinateButton = new CoordinateButton(EventHandler.getMyPosition());
        tglBtnGPS = new CB_Button(Translation.get("FromGps"), "toggle");
        tglBtnMap = new CB_Button(Translation.get("FromMap"), "toggle");
        tglBtnWeb = new CB_Button(Translation.get("FromWeb"), "toggle");

        lblRadius = new CB_Label(Translation.get("Radius"));
        txtRadius = new VisTextField("100");
        lblRadiusUnit = new CB_Label(Config.ImperialUnits.getValue() ? "mi" : "km");
        btnMinus = new CB_Button("-");
        btnPlus = new CB_Button("+");

        lblImportLimit = new CB_Label(Translation.get("ImportLimit"));
        edtImportLimit = new VisTextField("*" + Translation.get("ImportLimit"));
        lblCacheName = new CB_Label(Translation.get("Title"));
        edtCacheName = new VisTextField("*" + Translation.get("Title"));
        lblOwner = new CB_Label(Translation.get("Owner"));
        edtOwner = new VisTextField("*" + Translation.get("Owner"));
        btnBeforeAfterEqual = new CB_Button("<=");
        lblPublished = new CB_Label(Translation.get("published"));
        edtDate = new VisTextField("*" + Translation.get("published"));
        lblCategory = new CB_Label(Translation.get("category"));
        edtCategory = new VisTextField("*" + Translation.get("category"));

        checkBoxOnlyAvailable = new CB_CheckBox(Translation.get("SearchOnlyAvailable"));
        checkBoxExcludeHides = new CB_CheckBox(Translation.get("SearchWithoutOwns"));
        checkBoxExcludeFounds = new CB_CheckBox(Translation.get("SearchWithoutFounds"));

        Drawable animationDrawable = VisUI.getSkin().getDrawable("download-animation");
        workAnimation = new Image(animationDrawable);
        progressBar = new CB_ProgressBar(0, 100, 1, false, "default");
        lblCaches = new CB_Label("Imported Caches: 0");
        lblWaypoints = new CB_Label("Imported Waypoints: 0");
        lblLogs = new CB_Label("Imported Log's: 0");
        lblImages = new CB_Label("Imported Images: 0");

        initClickHandlersAndContent();
        setWorkAnimationVisible(false);
    }

    @Override
    public void layout() {
        if (!needLayout) {
            super.layout();
            return;
        }

        SnapshotArray<Actor> actors = getChildren();
        for (Actor actor : actors)
            removeActor(actor);

        setFillParent(true);

        setTableAndCellDefaults();
        addNext(lblTitle, 2.0f);
        addLast(gsLogo, 0.5f).fill(false).left();
        /*
        row();
        add(scrollPane).expand();
         */
        addLast(scrollPane);
        addNext(btnOK);
        addLast(btnCancel);

        box.addLast(coordinateButton);
        box.addNext(tglBtnGPS);
        box.addNext(tglBtnMap);
        box.addLast(tglBtnWeb);
        box.addNext(lblRadius, 2f);
        box.addNext(txtRadius);
        box.addNext(lblRadiusUnit);
        box.addNext(btnMinus);
        box.addLast(btnPlus);

        box.addNext(lblImportLimit);
        box.addLast(edtImportLimit, 2f);
        box.addNext(lblCacheName);
        box.addLast(edtCacheName, 2f);
        box.addNext(lblOwner);
        box.addLast(edtOwner, 2f);
        box.addNext(lblPublished, 1.5f);
        box.addNext(btnBeforeAfterEqual, 0.5f);
        box.addLast(edtDate, 1.5f);
        box.addNext(lblCategory);
        box.addLast(edtCategory, 2f);
        box.stopRow();

        box.addLast(checkBoxOnlyAvailable);
        box.addLast(checkBoxExcludeHides);
        box.addLast(checkBoxExcludeFounds);

        /*
        add(workAnimation).colspan(5).center();
        row();
        add();
        add(progressBar).colspan(3).center().expandX().fillX();
        row();
        add(lblCaches).colspan(5).left();
        row();
        add(lblWaypoints).colspan(5).left();
        row();
        add(lblLogs).colspan(5).left();
        row();
        add(lblImages).colspan(5).left();
        row().expandY().fillY().bottom();
        add();
        row();
         */

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

    private void initClickHandlersAndContent() {
        addClickHandler(btnOK, () -> CB.postAsync(new NamedRunnable("ImportGCPosition") {
            @Override
            public void run() {
                ImportNow();
            }
        }));
        cancelClickListener = addClickHandler(btnCancel, () -> {
            if (importRuns) {
                canceled.set(true);
            } else {
                finish();
            }
        });
        CB.stageManager.registerForBackKey(cancelClickListener);
        addClickHandler(btnPlus, () -> incrementRadius(1));
        addClickHandler(btnMinus, () -> incrementRadius(-1));
        addClickHandler(tglBtnGPS, () -> {
            actSearchPos = EventHandler.getMyPosition();
            setToggleBtnState(0);
        });
        addClickHandler(tglBtnMap, () -> {
            Coordinate lastStoredPos = CB.lastMapState.getFreePosition();
            if (tglBtnMap.isDisabled()) {
                actSearchPos = new Coordinate(lastStoredPos.getLatitude(), lastStoredPos.getLongitude());
                setToggleBtnState(0);
                return;
            }
            Coordinate mapCenterPos = MapView.getLastCenterPos();
            if (mapCenterPos == null) {
                actSearchPos = new Coordinate(lastStoredPos.getLatitude(), lastStoredPos.getLongitude());
            } else {
                actSearchPos = mapCenterPos;
            }
            setToggleBtnState(1);
        });
        addClickHandler(tglBtnWeb, () -> {
            actSearchPos = EventHandler.getMyPosition();
            searchCoordinates = new SearchCoordinates() {
                public void callBack(Coordinate coordinate) {
                    if (coordinate != null) {
                        actSearchPos = coordinate;
                        setToggleBtnState(2);
                    }
                }
            };
            searchCoordinates.show();
            setToggleBtnState(2);
        });

        addClickHandler(btnBeforeAfterEqual,() -> {
            switch (btnBeforeAfterEqual.getText().toString()) {
                case "X":
                    btnBeforeAfterEqual.setText("<=");
                    break;
                case "<=":
                    btnBeforeAfterEqual.setText("=");
                    break;
                case "=":
                    btnBeforeAfterEqual.setText(">=");
                    break;
                default:
                    btnBeforeAfterEqual.setText("X");
                    break;
            }
        });

        Coordinate mapCenterPos = MapView.getLastCenterPos();
        if (mapCenterPos == null || mapCenterPos.isZero() || !mapCenterPos.isValid()) {
            tglBtnMap.setDisabled(true);
            /*
            Coordinate lastStoredPos = CB.lastMapState.getFreePosition();
            actSearchPos = new Coordinate(lastStoredPos.getLatitude(), lastStoredPos.getLongitude());
             */
            actSearchPos = EventHandler.getMyPosition();
            setToggleBtnState(0);
        } else {
            actSearchPos = mapCenterPos;
            setToggleBtnState(1);
        }
        coordinateButton.setCoordinate(actSearchPos);

        checkBoxExcludeFounds.setChecked(Config.SearchWithoutFounds.getValue());
        checkBoxOnlyAvailable.setChecked(Config.SearchOnlyAvailable.getValue());
        checkBoxExcludeHides.setChecked(Config.SearchWithoutOwns.getValue());
        txtRadius.setText(String.valueOf(Config.lastSearchRadius.getValue()));

        // todo get category
        edtCategory.setText("API-Import");
        /*
        if (GlobalCore.isSetSelectedCache()) {
            long id = GlobalCore.getSelectedCache().getGPXFilename_ID();
            Category c = CoreSettingsForward.Categories.getCategoryByGpxFilenameId(id);
            if (c != null)
                edtCategory.setText(c.GpxFilename);
        }
        edtCategory.setCursorPosition(0);

        Category category = CoreSettingsForward.Categories.getCategory(edtCategory.getText());
        if (category.size() == 0)
            btnBeforeAfterEqual.setText("<=");
        else
            btnBeforeAfterEqual.setText(">=");
        edtDate.setText(simpleDateFormat.format(category.LastImported()));
        */
        edtDate.setText(simpleDateFormat.format(new Date()));
        //edtDate.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
        btnBeforeAfterEqual.setText("X");
        edtImportLimit.setText(Config.ImportLimit.getValue().toString());

        edtCacheName.setText("");
        edtOwner.setText("");

    }

    private void incrementRadius(int direction) {
        try {
            int ist = Integer.parseInt(txtRadius.getText().toString());
            ist += direction;
            if (ist > 100)
                ist = 100;
            if (ist < 1)
                ist = 1;
            txtRadius.setText(String.valueOf(ist));
        } catch (NumberFormatException e) {
        }
    }

    private void setToggleBtnState(int value) {
        // 0=GPS, 1= Map, 2= Web, 3= Manuell
        switch (value) {
            case 0:
                tglBtnGPS.setState(1);
                tglBtnMap.setState(0);
                tglBtnWeb.setState(0);
                break;
            case 1:
                tglBtnGPS.setState(0);
                tglBtnMap.setState(1);
                tglBtnWeb.setState(0);
                break;
            case 2:
                tglBtnGPS.setState(0);
                tglBtnMap.setState(0);
                tglBtnWeb.setState(1);
                break;
            case 3:
                tglBtnGPS.setState(0);
                tglBtnMap.setState(0);
                tglBtnWeb.setState(0);
                break;

        }
        coordinateButton.setCoordinate(actSearchPos);
    }

    private void ImportNow() {
        btnOK.setDisabled(true);
        importRuns = true;

        workAnimation.setVisible(true);

        final ImportProgressChangedListener progressListener = new ImportProgressChangedListener() {
            @Override
            public void progressChanged(final ImportProgressChangedEvent event) {

                if (event.progress.msg.equals("Start parsing result")) {
                    progressBar.setVisible(true);
                    lblCaches.setVisible(true);
                    lblWaypoints.setVisible(true);
                    lblLogs.setVisible(true);
                    lblImages.setVisible(true);
                }
                CB.postOnGlThread(new NamedRunnable("postOnGlThread") {
                    @Override
                    public void run() {
                        progressBar.setValue(event.progress.progress);
                        lblCaches.setText("Imported Caches: " + event.progress.caches);
                        lblWaypoints.setText("Imported Waypoints: " + event.progress.wayPoints);
                        lblLogs.setText("Imported Logs: " + event.progress.logs);
                        lblImages.setText("Imported Images: " + event.progress.images);
                    }
                });
            }
        };
        EventHandler.add(progressListener);
        final Date importStartTime = new Date();

        if (actSearchPos == null) {
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
        }

        Config.SearchWithoutFounds.setValue(checkBoxExcludeFounds.isChecked());
        Config.SearchOnlyAvailable.setValue(checkBoxOnlyAvailable.isChecked());
        Config.SearchWithoutOwns.setValue(checkBoxExcludeHides.isChecked());
        Config.AcceptChanges();

        Date tmpDate;
        try {
            tmpDate = simpleDateFormat.parse(edtDate.getText());
        } catch (Exception ex) {
            tmpDate = new Date();
        }
        final Date publishDate = tmpDate;

        // todo progressListener, importStart, cancel, ...
        Category category = CB.Categories.getCategory("API-Import");
        if (category != null) { // should not happen!!!
            GpxFilename gpxFilename = category.addGpxFilename("API-Import");
            if (gpxFilename != null) {
                GroundspeakAPI.Query q = new GroundspeakAPI.Query()
                        .resultWithFullFields()
                        //.resultWithImages(30)
                        ;
                if (!btnBeforeAfterEqual.getText().toString().equals("X")) {
                    q.publishedDate(publishDate, btnBeforeAfterEqual.getText().toString());
                }
                if (Config.numberOfLogs.getValue() > 0) {
                    q.resultWithLogs(Config.numberOfLogs.getValue());
                }
                if (txtRadius.getText().trim().length() > 0) {
                    int radius;
                    try {
                        radius = Integer.parseInt(txtRadius.getText());
                        if (Config.ImperialUnits.getValue()) radius = UnitFormatter.getKilometer(radius);
                        Config.lastSearchRadius.setValue(radius);
                        Config.AcceptChanges();
                        q.searchInCircle(actSearchPos, radius * 1000);
                    } catch (NumberFormatException nex) {
                        q.searchInCircle(actSearchPos, Config.lastSearchRadius.getValue() * 1000);
                    }
                }
                if (edtOwner.getText().trim().length() > 0) q.searchForOwner(edtOwner.getText().trim());
                if (edtCacheName.getText().trim().length() > 0) q.searchForTitle(edtCacheName.getText().trim());

                if (Config.SearchWithoutFounds.getValue()) q.excludeFinds();
                if (Config.SearchWithoutOwns.getValue()) q.excludeOwn();
                if (Config.SearchOnlyAvailable.getValue()) q.onlyActiveGeoCaches();

                int importLimit;
                try {
                    importLimit = Integer.parseInt(edtImportLimit.getText());
                } catch (Exception ex) {
                    importLimit = Config.ImportLimit.getDefaultValue();
                }
                q.setMaxToFetch(importLimit);
                Config.ImportLimit.setValue(importLimit);

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
        super.dispose();
    }
}

