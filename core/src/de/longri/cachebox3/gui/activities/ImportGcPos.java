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

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextArea;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.views.MapView;
import de.longri.cachebox3.gui.widgets.CoordinateButton;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.locator.CoordinateGPS;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Translation;


/**
 * Created by Longri on 12.04.2017.
 */
public class ImportGcPos extends ActivityBase {

    private VisTextButton bOK, bCancel, btnPlus, btnMinus, tglBtnGPS, tglBtnMap;
    private VisLabel lblTitle, lblRadius, lblRadiusEinheit, lblMarkerPos, lblExcludeFounds, lblOnlyAvailable, lblExcludeHides;
    private Image gsLogo;
    private CoordinateButton coordBtn;
    private VisCheckBox checkBoxExcludeFounds, checkBoxOnlyAvailable, checkBoxExcludeHides;
    private VisTextArea Radius;
    private float lineHeight;
    private Coordinate actSearchPos;
    private volatile Thread thread;
    private WidgetGroup box;
    private boolean importRuns = false;
    private boolean isCanceld = false;

    /**
     * 0=GPS, 1= Map, 2= Manuell
     */
    private int searcheState = 0;


    public ImportGcPos() {
        super("searchOverPosActivity");
        lineHeight = CB.scaledSizes.BUTTON_HEIGHT;

        createOkCancelBtn();
        createBox();
        createTitleLine();
        createRadiusLine();
        createChkBoxLines();
        createToggleButtonLine();
        createCoordButton();

        initialContent();
    }

    private void createOkCancelBtn() {
        bOK = new VisTextButton(Translation.Get("import"));
        bCancel = new VisTextButton(Translation.Get("cancel"));

        this.addActor(bOK);
        bOK.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                ImportNow();
            }
        });
        this.addActor(bCancel);

        bCancel.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (importRuns) {
                    isCanceld = true;
                } else {
                    finish();
                }
            }
        });
    }

    private void createBox() {
        box = new WidgetGroup();
        this.addActor(box);

    }

    private void createTitleLine() {
        gsLogo = new Image();
        gsLogo.setDrawable(CB.getSkin().getIcon.GC_Live);
        this.addActor(gsLogo);

        lblTitle = new VisLabel(Translation.Get("importCachesOverPosition"));
        this.addActor(lblTitle);

    }

    private void createRadiusLine() {
        lblRadius = new VisLabel(Translation.Get("Radius"));
        box.addActor(lblRadius);

        Radius = new VisTextArea();
        box.addActor(Radius);

        lblRadiusEinheit = new VisLabel(Config.ImperialUnits.getValue() ? "mi" : "km");
        box.addActor(lblRadiusEinheit);

        btnMinus = new VisTextButton("-");
        box.addActor(btnMinus);

        btnPlus = new VisTextButton("+");
        box.addActor(btnPlus);
    }

    private void createChkBoxLines() {
        checkBoxOnlyAvailable = new VisCheckBox(Translation.Get("SearchOnlyAvailable"));
        box.addActor(checkBoxOnlyAvailable);

        checkBoxExcludeHides = new VisCheckBox(Translation.Get("SearchWithoutOwns"));
        box.addActor(checkBoxExcludeHides);

        checkBoxExcludeFounds = new VisCheckBox(Translation.Get("SearchWithoutFounds"));
        box.addActor(checkBoxExcludeFounds);
    }

    private void createToggleButtonLine() {

        tglBtnGPS = new VisTextButton(Translation.Get("FromGps"));
        tglBtnMap = new VisTextButton(Translation.Get("FromMap"));
        box.addActor(tglBtnGPS);
        box.addActor(tglBtnMap);

        //TODO disable with no actual MapCenterPos
//        if (MapView.that == null)
//            tglBtnMap.disable();

    }

    private void createCoordButton() {
        lblMarkerPos = new VisLabel(Translation.Get("CurentMarkerPos"));
        box.addActor(lblMarkerPos);
        coordBtn = new CoordinateButton(EventHandler.getMyPosition());
        box.addActor(coordBtn);
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
                Coordinate mapCenterPos= MapView.getLastCenterPos();
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
        Radius.setText(String.valueOf(Config.lastSearchRadius.getValue()));

        setToggleBtnState();

    }

    private void initialCoordinates() {
        // initiate Coordinates to actual Map-Center or actual GPS Coordinate
        switch (searcheState) {
            case 0:
                actSearchPos = EventHandler.getMyPosition();
                break;
            case 1:
                Coordinate mapCenterPos= MapView.getLastCenterPos();
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
            int ist = Integer.parseInt(Radius.getText().toString());
            ist += value;

            if (ist > 100)
                ist = 100;
            if (ist < 1)
                ist = 1;

            Radius.setText(String.valueOf(ist));
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
        isCanceld = false;
        Config.SearchWithoutFounds.setValue(checkBoxExcludeFounds.isChecked());
        Config.SearchOnlyAvailable.setValue(checkBoxOnlyAvailable.isChecked());
        Config.SearchWithoutOwns.setValue(checkBoxExcludeHides.isChecked());

        int radius = 0;
        try {
            radius = Integer.parseInt(Radius.getText().toString());
        } catch (NumberFormatException e) {
            // Kein Integer
            e.printStackTrace();
        }

        if (radius != 0)
            Config.lastSearchRadius.setValue(radius);

        Config.AcceptChanges();

        bOK.setDisabled(true);


        importRuns = true;

        //TODO replace with async worker
//        thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                boolean threadCanceld = false;
//
//                try {
//                    if (actSearchPos != null) {
//
//                        // alle per API importierten Caches landen in der Category und
//                        // GpxFilename
//                        // API-Import
//                        // Category suchen, die dazu gehört
//                        Category category = CoreSettingsForward.Categories.getCategory("API-Import");
//                        if (category != null) // should not happen!!!
//                        {
//                            GpxFilename gpxFilename = category.addGpxFilename("API-Import");
//                            if (gpxFilename != null) {
//                                CB_List<Cache> apiCaches = new CB_List<Cache>();
//                                ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
//                                ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();
//                                SearchCoordinate searchC = new SearchCoordinate(50, actSearchPos, Config.lastSearchRadius.getValue() * 1000);
//
//                                searchC.excludeFounds = Config.SearchWithoutFounds.getValue();
//                                searchC.excludeHides = Config.SearchWithoutOwns.getValue();
//                                searchC.available = Config.SearchOnlyAvailable.getValue();
//
//                                dis.setAnimationType(AnimationType.Download);
//                                CB_UI.SearchForGeocaches.getInstance().SearchForGeocachesJSON(searchC, apiCaches, apiLogs, apiImages, gpxFilename.Id, icancel);
//                                dis.setAnimationType(AnimationType.Work);
//                                if (apiCaches.size() > 0) {
//                                    GroundspeakAPI.WriteCachesLogsImages_toDB(apiCaches, apiLogs, apiImages);
//                                }
//
//                            }
//                        }
//                    }
//                } catch (InterruptedException e) {
//                    // Thread abgebrochen!
//                    threadCanceld = true;
//                }
//
//                if (!threadCanceld) {
//                    CacheListChangedEventList.Call();
//                    if (dis != null) {
//                        SearchOverPosition.this.removeChildsDirekt(dis);
//                        dis.dispose();
//                        dis = null;
//                    }
//                    bOK.enable();
//                    finish();
//                } else {
//
//                    // Notify Map
//                    if (MapView.that != null)
//                        MapView.that.setNewSettings(MapView.INITIAL_WP_LIST);
//                    if (dis != null) {
//                        SearchOverPosition.this.removeChildsDirekt(dis);
//                        dis.dispose();
//                        dis = null;
//                    }
//                    bOK.enable();
//                }
//                importRuns = false;
//            }
//
//        });
//
//                thread.setPriority(Thread.MAX_PRIORITY);
//                thread.start();
//
//            }}}
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
//        if (Radius != null)
//            Radius.dispose();
//        Radius = null;
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

