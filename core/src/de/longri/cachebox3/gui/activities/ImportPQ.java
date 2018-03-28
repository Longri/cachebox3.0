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
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.apis.groundspeak_api.*;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.skin.styles.PqListItemStyle;
import de.longri.cachebox3.gui.widgets.AligmentLabel;
import de.longri.cachebox3.gui.widgets.CharSequenceButton;
import de.longri.cachebox3.gui.widgets.ProgressBar;
import de.longri.cachebox3.gui.widgets.list_view.*;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.ICancel;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.gdx.sqlite.GdxSqliteCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by Longri on 26.03.2018.
 */
public class ImportPQ extends ActivityBase {

    private final static Logger log = LoggerFactory.getLogger(ImportPQ.class);
    private final ListView pqList = new ListView(ListViewType.VERTICAL, false);
    private final CharSequenceButton bOK, bCancel;
    private final DefaultListViewAdapter itemArray = new DefaultListViewAdapter();
    private final AtomicBoolean canceled = new AtomicBoolean(false);
    private final ICancel iCancel = new ICancel() {
        @Override
        public boolean cancel() {
            return canceled.get();
        }
    };
    private final static DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private final PqListItemStyle itemStyle;
    private final AligmentLabel downloadLabel;
    private final ProgressBar downloadProgress;
    private final AligmentLabel extractLabel;
    private final ProgressBar extractProgress;
    private final AligmentLabel importLabel;
    private final ProgressBar importProgress;
    private final AligmentLabel correctedLabel;

    public ImportPQ() {
        super("ImportPQ");
        this.itemStyle = VisUI.getSkin().get(PqListItemStyle.class);

        float contentWidth = Gdx.graphics.getWidth() - CB.scaledSizes.MARGINx4;

        pqList.setBackground(this.style.background);
        this.add(pqList).width(new Value.Fixed(contentWidth)).expandY().fillY();
        this.row();

        // line for selected PocketQuery's
        Label.LabelStyle selectedLabelStyle = new Label.LabelStyle();
        selectedLabelStyle.font = itemStyle.infoFont;
        selectedLabelStyle.fontColor = itemStyle.infoFontColor;
        final AligmentLabel selectedLabel = new AligmentLabel(Translation.get("PQnoSelection"), selectedLabelStyle, Align.left);
        pqList.addSelectionChangedEventListner(new SelectionChangedEvent() {
            @Override
            public void selectionChanged() {
                Array<ListViewItemInterface> selectedItems = pqList.getSelectedItems();
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
        downloadProgress = new ProgressBar(0, 0, 1, false, "default");
        this.add(downloadLabel).padLeft(CB.scaledSizes.MARGINx4).expandX().fillX();
        this.row();
        this.add(downloadProgress).expandX().fillX();
        this.row();
        this.add().height(new Value.Fixed(CB.scaledSizes.MARGINx2));
        this.row();

        //extract progress
        extractLabel = new AligmentLabel(Translation.get("extracted", "?", "?"), selectedLabelStyle, Align.left);
        extractProgress = new ProgressBar(0, 0, 1, false, "default");
        this.add(extractLabel).padLeft(CB.scaledSizes.MARGINx4).expandX().fillX();
        this.row();
        this.add(extractProgress).expandX().fillX();
        this.row();
        this.add().height(new Value.Fixed(CB.scaledSizes.MARGINx2));
        this.row();

        //import progress
        importLabel = new AligmentLabel(Translation.get("imported", "?", "?"), selectedLabelStyle, Align.left);
        importProgress = new ProgressBar(0, 0, 1, false, "default");
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
        bOK = new CharSequenceButton(Translation.get("import"));
        bOK.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                bOK.setDisabled(true);
                importNow();
            }
        });
        bCancel = new CharSequenceButton(Translation.get("cancel"));
        bOK.setDisabled(true);
        bCancel.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                canceled.set(true);
                ImportPQ.this.finish();
            }
        });
        Table nestedTable2 = new Table();
        nestedTable2.defaults().pad(CB.scaledSizes.MARGIN).bottom();
        nestedTable2.add(bOK).bottom();
        nestedTable2.add(bCancel).bottom();
        this.add(nestedTable2).colspan(5);
    }

    private void importNow() {
        // get import information
        final AtomicInteger downloadPqCount = new AtomicInteger(0),
                downloadedPqs = new AtomicInteger(0), extractedPQs = new AtomicInteger(0),
                downloadBytes = new AtomicInteger(0), readyDownloadBytes = new AtomicInteger(0),
                importCache = new AtomicInteger(0), readyImportedCaches = new AtomicInteger(0),
                corrected = new AtomicInteger(0), mysteries = new AtomicInteger(0);

        final Array<ListViewItemInterface> selectedItems = pqList.getSelectedItems();
        for (ListViewItemInterface item : selectedItems) {
            PqListItem pqListItem = (PqListItem) item;
            downloadPqCount.incrementAndGet();
            downloadBytes.addAndGet((int) (pqListItem.getSize() * 1048576.0));
            importCache.addAndGet(pqListItem.getCount());
        }

        //set initial progress values
        downloadLabel.setText(Translation.get("downloaded", "0", Integer.toString(downloadPqCount.get())));
        downloadProgress.setRange(0, downloadBytes.get());

        extractLabel.setText(Translation.get("extracted", "0", Integer.toString(downloadPqCount.get())));
        extractProgress.setRange(0, downloadPqCount.get());

        importLabel.setText(Translation.get("imported", "0", Integer.toString(importCache.get())));
        importProgress.setRange(0, importCache.get());

        correctedLabel.setText(Translation.get("correctedCoords", "0", "0"));


        //start download async
        final FileHandle pqFolder = Gdx.files.absolute(Config.PocketQueryFolder.getValue());
        final Array<FileHandle> downloadedFiles = new Array<>();
        CB.postAsync(new NamedRunnable("Download PQs") {
            @Override
            public void run() {
                for (ListViewItemInterface item : selectedItems) {
                    PqListItem pqListItem = (PqListItem) item;
                    FileHandle downloadedFile = pqListItem.getPocketQuery().download(pqFolder, iCancel, new PocketQuery.IncrementProgressBytesListener() {
                        @Override
                        public void increment(int bytes) {
                            downloadProgress.setValue(readyDownloadBytes.addAndGet(bytes));
                        }
                    });
                    if (downloadedFile != null) {
                        downloadedFiles.add(downloadedFile);

                        //update progress message
                        downloadLabel.setText(Translation.get("downloaded",
                                Integer.toString(downloadedPqs.incrementAndGet()),
                                Integer.toString(downloadPqCount.get())));

                    }
                }
            }
        });


    }

    @Override
    public void onShow() {
        refreshPQList();
    }

    @Override
    public void onHide() {
    }

    private void refreshPQList() {
        pqList.setEmptyString(Translation.get("EmptyPqList"));
        pqList.showWorkAnimationUntilSetAdapter();

        CB.postAsync(new NamedRunnable("refreshPQList") {
            @Override
            public void run() {
                itemArray.clear();
                Array<PocketQuery> list = new Array<>();
                GetPocketQueryList pocketQuery = new GetPocketQueryList(GroundspeakAPI.getAccessToken(true), iCancel, list);

                final AtomicBoolean WAIT = new AtomicBoolean(true);
                final ApiResultState[] state = new ApiResultState[1];
                pocketQuery.post(new GenericCallBack<ApiResultState>() {
                    @Override
                    public void callBack(ApiResultState value) {
                        state[0] = value;
                        WAIT.set(false);
                    }
                });
                CB.wait(WAIT);

                if (CB.checkApiResultState(state[0])) {
                    ImportPQ.this.finish();
                }

                if (canceled.get()) return;

                int idx = 0;
                for (PocketQuery pq : list) {
                    //Check last import
                    GdxSqliteCursor cursor = Database.Data.myDB.rawQuery("SELECT * FROM PocketQueries WHERE PQName=\"" + pq.name + "\"");
                    if (cursor != null) {
                        cursor.moveToFirst();
                        String dateTimeString = cursor.getString(2);
                        try {
                            pq.lastImported = iso8601Format.parse(dateTimeString);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    //add only PocketQuery's their download available
                    if (pq.downloadAvailable)
                        itemArray.add(new PqListItem(idx++, pq, itemStyle));
                }
                CB.postOnGlThread(new NamedRunnable("SetAdapter") {
                    @Override
                    public void run() {
                        if (canceled.get()) return;
                        pqList.setAdapter(itemArray);
                    }
                });
            }
        });
    }

    @Override
    public void dispose() {

    }
}
