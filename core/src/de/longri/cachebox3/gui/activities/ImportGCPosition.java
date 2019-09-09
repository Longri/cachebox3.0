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

import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.apis.GroundspeakAPI;
import de.longri.cachebox3.events.CacheListChangedEvent;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.gui.Activity;
import de.longri.cachebox3.gui.dialogs.InfoBox;
import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButtons;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.stages.ViewManager;
import de.longri.cachebox3.gui.views.MapView;
import de.longri.cachebox3.gui.widgets.*;
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

import static de.longri.cachebox3.CB.addClickHandler;
import static de.longri.cachebox3.apis.GroundspeakAPI.searchGeoCaches;


/**
 * Created by Longri on 12.04.2017.
 */
public class ImportGCPosition extends Activity {

    private static final Logger log = LoggerFactory.getLogger(ImportGCPosition.class);
    private final CB_Button btnPlus, btnMinus, tglBtnGPS, tglBtnMap, tglBtnWeb, btnBeforeAfterEqual;
    private final CB_Label lblRadius, lblRadiusUnit, lblImportLimit, lblCacheName, lblOwner, lblPublished, lblCategory;
    private final EditTextField edtImportLimit, edtCacheName, edtOwner, edtDate, edtCategory, txtRadius;
    private final CoordinateButton coordinateButton;
    private final CB_CheckBox checkBoxExcludeFounds, checkBoxOnlyAvailable, checkBoxExcludeHides;
    private final SimpleDateFormat simpleDateFormat;

    private Coordinate actSearchPos;
    private SearchCoordinates searchCoordinates;

    public ImportGCPosition() {
        super("importCachesOverPosition", CB.getSkin().getMenuIcon.target);
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        coordinateButton = new CoordinateButton(EventHandler.getMyPosition());
        tglBtnGPS = new CB_Button(Translation.get("FromGps"), "toggle");
        tglBtnMap = new CB_Button(Translation.get("FromMap"), "toggle");
        tglBtnWeb = new CB_Button(Translation.get("FromWeb"), "toggle");

        lblRadius = new CB_Label(Translation.get("Radius"));
        txtRadius = new EditTextField("100");
        txtRadius.setInputType(InputType.TYPE_CLASS_NUMBER);
        lblRadiusUnit = new CB_Label(Config.ImperialUnits.getValue() ? "mi" : "km");
        btnMinus = new CB_Button("-");
        btnPlus = new CB_Button("+");

        lblImportLimit = new CB_Label(Translation.get("ImportLimit"));
        edtImportLimit = new EditTextField("*" + Translation.get("ImportLimit"));
        edtImportLimit.setInputType(InputType.TYPE_CLASS_NUMBER);
        lblCacheName = new CB_Label(Translation.get("Title"));
        edtCacheName = new EditTextField("*" + Translation.get("Title"));
        lblOwner = new CB_Label(Translation.get("Owner"));
        edtOwner = new EditTextField("*" + Translation.get("Owner"));
        btnBeforeAfterEqual = new CB_Button("<=");
        lblPublished = new CB_Label(Translation.get("published"));
        edtDate = new EditTextField("*" + Translation.get("published"));
        edtDate.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
        lblCategory = new CB_Label(Translation.get("category"));
        edtCategory = new EditTextField("*" + Translation.get("category"));

        checkBoxOnlyAvailable = new CB_CheckBox(Translation.get("SearchOnlyAvailable"));
        checkBoxExcludeHides = new CB_CheckBox(Translation.get("SearchWithoutOwns"));
        checkBoxExcludeFounds = new CB_CheckBox(Translation.get("SearchWithoutFounds"));

        initClickHandlersAndContent();
    }

    @Override
    protected void createMainContent() {

        mainContent.addLast(coordinateButton);
        mainContent.addNext(tglBtnGPS);
        mainContent.addNext(tglBtnMap);
        mainContent.addLast(tglBtnWeb);
        mainContent.addNext(lblRadius, -0.4f);
        mainContent.addNext(txtRadius, -0.3f);
        mainContent.addNext(lblRadiusUnit);
        mainContent.addNext(btnMinus);
        mainContent.addLast(btnPlus);

        mainContent.addNext(lblImportLimit, -0.4f);
        mainContent.addLast(edtImportLimit);
        mainContent.addNext(lblCacheName, -0.4f);
        mainContent.addLast(edtCacheName);
        mainContent.addNext(lblOwner, -0.4f);
        mainContent.addLast(edtOwner);
        mainContent.addNext(lblPublished, -0.4f);
        mainContent.addNext(btnBeforeAfterEqual, -0.1f);
        mainContent.addLast(edtDate);
        mainContent.addNext(lblCategory, -0.4f);
        mainContent.addLast(edtCategory);
        mainContent.stopRow();

        mainContent.addLast(checkBoxOnlyAvailable);
        mainContent.addLast(checkBoxExcludeHides);
        mainContent.addLast(checkBoxExcludeFounds);
    }

    @Override
    protected void runAtOk() {
        final InfoBox infoBox = new InfoBox(InfoBox.Infotype.PROGRESS, "Import").open();
        CB.postAsync(new NamedRunnable("ImportGCPosition") {
            @Override
            public void run() {
                ImportNow(infoBox);
            }
        });
        // finish() or not
    }

    private void initClickHandlersAndContent() {
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

        addClickHandler(btnBeforeAfterEqual, () -> {
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
        if (mapCenterPos.isZero() || !mapCenterPos.isValid()) {
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
        int maxRadius = 160;
        if (Config.ImperialUnits.getValue()) {
            maxRadius = 100;
        }
        try {
            int ist = Integer.parseInt(txtRadius.getText());
            ist += direction;
            if (ist > maxRadius)
                ist = maxRadius;
            if (ist < 1)
                ist = 1;
            txtRadius.setText(String.valueOf(ist));
        } catch (NumberFormatException ignored) {
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

    private void ImportNow(InfoBox infobox) {
        btnOK.setDisabled(true);

        if (actSearchPos == null) {
            //wait for act Position
            CB.viewmanager.toast(Translation.get("waiting_for_fix"), ViewManager.ToastLength.WAIT);
            while (actSearchPos == null) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ignored) {
                }
                actSearchPos = EventHandler.getMyPosition();
            }
            ViewManager.ToastLength.WAIT.close();
        }

        Config.SearchWithoutFounds.setValue(checkBoxExcludeFounds.isChecked());
        Config.SearchOnlyAvailable.setValue(checkBoxOnlyAvailable.isChecked());
        Config.SearchWithoutOwns.setValue(checkBoxExcludeHides.isChecked());

        Date tmpDate;
        try {
            tmpDate = simpleDateFormat.parse(edtDate.getText());
        } catch (Exception ex) {
            tmpDate = new Date();
        }
        final Date publishDate = tmpDate;

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
                        if (Config.ImperialUnits.getValue()) {
                            radius = UnitFormatter.getKilometer(radius);
                            if (radius > 100) {
                                radius = 100; // max 100 miles
                                txtRadius.setText(String.valueOf(radius));
                            }
                        } else {
                            if (radius > 160) {
                                radius = 160; // max 100 miles
                                txtRadius.setText(String.valueOf(radius));
                            }
                        }
                        Config.lastSearchRadius.setValue(radius);
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

                Config.AcceptChanges();

                q.infoBox = infobox;

                Array<GroundspeakAPI.GeoCacheRelated> fetchedCaches = searchGeoCaches(q);
                if (GroundspeakAPI.APIError != GroundspeakAPI.OK) {
                    MessageBox.show(GroundspeakAPI.LastAPIError, Translation.get("importCachesOverPosition"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
                } else {
                    if (!infobox.isCanceled()) {
                        // WriteIntoDB.CachesAndLogsAndImagesIntoDB(geoCacheRelateds, gpxFilename);
                        // todo set gpxfilename / category
                        Cache3DAO dao = new Cache3DAO();
                        int count = 0;
                        infobox.setTitle("Import to database");
                        for (GroundspeakAPI.GeoCacheRelated cacheEntry : fetchedCaches) {
                            count++;
                            infobox.setProgress(100 * count / fetchedCaches.size, cacheEntry.cache.getGcCode());
                            // this sleep is simply for demonstration
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            dao.writeToDatabase(Database.Data, cacheEntry.cache, true);

                            LogDAO logdao = new LogDAO();
                            logdao.writeToDB(Database.Data, cacheEntry.logs);
                        }
                    }
                }
                infobox.close();

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

    public void show() {
        super.show();
        CB.viewmanager.locationReceiver.stopForegroundUpdates();
    }

    public void finish() {
        super.finish();
        CB.viewmanager.locationReceiver.resume();
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}

