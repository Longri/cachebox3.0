/*
 * Copyright (C) 2018 team-cachebox.de
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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.apis.GroundspeakAPI;
import de.longri.cachebox3.events.CacheListChangedEvent;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.gpx.PqImport;
import de.longri.cachebox3.gui.BlockGpsActivityBase;
import de.longri.cachebox3.gui.skin.styles.PqListItemStyle;
import de.longri.cachebox3.gui.stages.ViewManager;
import de.longri.cachebox3.gui.widgets.AligmentLabel;
import de.longri.cachebox3.gui.widgets.CB_Button;
import de.longri.cachebox3.gui.widgets.CB_ProgressBar;
import de.longri.cachebox3.gui.widgets.list_view.*;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.ICancel;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.gdx.sqlite.GdxSqliteCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by Longri on 26.03.2018.
 */
public class ImportPQActivity extends BlockGpsActivityBase {

    private final static Logger log = LoggerFactory.getLogger(ImportPQActivity.class);
    private final ListView pqListView = new ListView(ListViewType.VERTICAL, false);
    private final CB_Button bOK, bCancel;
    private final DefaultListViewAdapter pqListViewItemArray = new DefaultListViewAdapter();
    private final AtomicBoolean canceled = new AtomicBoolean(false);
    private final ICancel iCancel = new ICancel() {
        @Override
        public boolean cancel() {
            return canceled.get();
        }
    };
    private final PqListItemStyle itemStyle;
    private final AligmentLabel downloadLabel;
    private final CB_ProgressBar downloadProgress;
    private final AligmentLabel extractLabel;
    private final CB_ProgressBar extractProgress;
    private final AligmentLabel importLabel;
    private final CB_ProgressBar importProgress;
    private final AligmentLabel correctedLabel;

    public ImportPQActivity() {
        super("ImportPQActivity");
        this.itemStyle = VisUI.getSkin().get(PqListItemStyle.class);

        float contentWidth = Gdx.graphics.getWidth() - CB.scaledSizes.MARGINx4;

        pqListView.setBackground(this.style.background);
        this.add(pqListView).width(new Value.Fixed(contentWidth)).expandY().fillY();
        this.row();

        // line for selected PocketQuery's
        Label.LabelStyle selectedLabelStyle = new Label.LabelStyle();
        selectedLabelStyle.font = itemStyle.infoFont;
        selectedLabelStyle.fontColor = itemStyle.infoFontColor;
        final AligmentLabel selectedLabel = new AligmentLabel(Translation.get("PQnoSelection"), selectedLabelStyle, Align.left);
        pqListView.addSelectionChangedEventListner(new SelectionChangedEvent() {
            @Override
            public void selectionChanged() {
                Array<ListViewItemInterface> selectedItems = pqListView.getSelectedItems();
                if (selectedItems == null) {
                    selectedLabel.setText(Translation.get("PQnoSelection"));
                    bOK.setDisabled(true);
                } else {
                    selectedLabel.setText(Translation.get("PQSelectionCount", Integer.toString(selectedItems.size)));
                    bOK.setDisabled(false);
                }
            }
        });
        this.add(selectedLabel).padLeft(CB.scaledSizes.MARGINx4).expandX().fillX();
        this.row();
        this.add().height(new Value.Fixed(CB.scaledSizes.MARGINx4));
        this.row();

        //download progress
        downloadLabel = new AligmentLabel(Translation.get("downloaded", "?", "?"), selectedLabelStyle, Align.left);
        downloadProgress = new CB_ProgressBar(0, 0, 1, false, "default");
        this.add(downloadLabel).padLeft(CB.scaledSizes.MARGINx4).expandX().fillX();
        this.row();
        this.add(downloadProgress).expandX().fillX();
        this.row();
        this.add().height(new Value.Fixed(CB.scaledSizes.MARGINx2));
        this.row();

        //extract progress
        extractLabel = new AligmentLabel(Translation.get("extracted", "?", "?"), selectedLabelStyle, Align.left);
        extractProgress = new CB_ProgressBar(0, 0, 1, false, "default");
        this.add(extractLabel).padLeft(CB.scaledSizes.MARGINx4).expandX().fillX();
        this.row();
        this.add(extractProgress).expandX().fillX();
        this.row();
        this.add().height(new Value.Fixed(CB.scaledSizes.MARGINx2));
        this.row();

        //import progress
        importLabel = new AligmentLabel(Translation.get("imported", "?", "?"), selectedLabelStyle, Align.left);
        importProgress = new CB_ProgressBar(0, 0, 1, false, "default");
        this.add(importLabel).padLeft(CB.scaledSizes.MARGINx4).expandX().fillX();
        this.row();
        this.add(importProgress).expandX().fillX();
        this.row();
        this.add().height(new Value.Fixed(CB.scaledSizes.MARGINx2));
        this.row();

        //corrected progress
        correctedLabel = new AligmentLabel(Translation.get("correctedCoords", "?", "?"), selectedLabelStyle, Align.left);
        this.add(correctedLabel).padLeft(CB.scaledSizes.MARGINx4).expandX().fillX();
        this.row();
        this.add().height(new Value.Fixed(CB.scaledSizes.MARGINx4));
        this.row();

        // fill and add Buttons
        this.row();
        bOK = new CB_Button(Translation.get("import"));
        bOK.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                bOK.setDisabled(true);
                importNow();
            }
        });
        bCancel = new CB_Button(Translation.get("cancel"));
        bOK.setDisabled(true);
        bCancel.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                canceled.set(true);
                ImportPQActivity.this.finish();
            }
        });
        Table nestedTable2 = new Table();
        nestedTable2.defaults().pad(CB.scaledSizes.MARGIN).bottom();
        nestedTable2.add(bOK).bottom();
        nestedTable2.add(bCancel).bottom();
        this.add(nestedTable2).colspan(5);
    }

    private void importNow() {
        PqImport pqImport = new PqImport(Database.Data);
        final long importStart = System.currentTimeMillis();
        PqImport.IReadyHandler readyHandler = new PqImport.IReadyHandler() {
            public void ready(int importedCaches, int importedWaypoints, int importedLogs) {
                // finish, close activity and notify changes
                ImportPQActivity.this.finish();
                String msg;
                if (!iCancel.cancel()) {

                    long importTime = System.currentTimeMillis() - importStart;

                    msg = "Import " + String.valueOf(importedCaches) + "Caches \n"
                            + String.valueOf(importedWaypoints) + " Waypoints \n"
                            + String.valueOf(importedLogs) + " Logs \n"
                            + "in " + String.valueOf(importTime / 1000) + " sec!";
                    CB.viewmanager.toast(msg, ViewManager.ToastLength.EXTRA_LONG);
                } else {
                    msg = "Import canceled";
                    CB.viewmanager.toast(msg);
                }
                log.debug(msg);

                CB.postOnNextGlThread(() -> CB.postAsync(new NamedRunnable("Reload cacheList after import") {
                    @Override
                    public void run() {
                        Database.Data.cacheList.setUnfilteredSize(Database.Data.getCacheCountOnThisDB());
                        log.debug("Call loadFilteredCacheList()");
                        CB.loadFilteredCacheList(null);
                        CB.postOnNextGlThread(() -> EventHandler.fire(new CacheListChangedEvent()));
                    }
                }));
            }
        };


        pqImport.importNow(pqListView.getSelectedItems(), iCancel,
                downloadLabel, downloadProgress,
                extractLabel, extractProgress,
                importLabel, importProgress,
                correctedLabel, readyHandler);
    }


    @Override
    public void onShow() {
        refreshPQList();
    }

    @Override
    public void onHide() {
    }

    private void refreshPQList() {
        pqListView.setEmptyString(Translation.get("EmptyPqList"));
        pqListView.showWorkAnimationUntilSetAdapter();

        CB.postAsync(new NamedRunnable("refreshPQList") {
            @Override
            public void run() {
                pqListViewItemArray.clear();

                int idx = 0;
                Array<GroundspeakAPI.PQ> pqList = GroundspeakAPI.fetchPocketQueryList();
                pqList.sort();
                for (GroundspeakAPI.PQ pq : pqList) {
                    //Check last import
                    GdxSqliteCursor cursor = Database.Data.myDB.rawQuery("SELECT * FROM PocketQueries WHERE PQName=\"" + pq.name + "\"");
                    if (cursor != null) {
                        cursor.moveToFirst();
                        String dateTimeString = cursor.getString(2);
                        try {
                            pq.lastImported = Database.cbDbFormat.parse(dateTimeString);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    // add only PocketQuery's their download available
                    // if (pq.downloadAvailable) // later compare last imported with last generated
                    pqListViewItemArray.add(new PqListItem(idx++, pq, itemStyle));
                }

                CB.postOnGlThread(new NamedRunnable("SetAdapter") {
                    @Override
                    public void run() {
                        if (canceled.get()) return;
                        pqListView.setAdapter(pqListViewItemArray);
                    }
                });
            }
        });
    }

    @Override
    public void dispose() {

    }
}
