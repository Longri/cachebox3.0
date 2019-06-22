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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.apis.GroundspeakAPI;
import de.longri.cachebox3.events.CacheListChangedEvent;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.ImportProgressChangedEvent;
import de.longri.cachebox3.events.ImportProgressChangedListener;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.Window;
import de.longri.cachebox3.gui.dialogs.ButtonDialog;
import de.longri.cachebox3.gui.dialogs.MessageBoxButtons;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.dialogs.OnMsgBoxClickListener;
import de.longri.cachebox3.gui.drawables.ColorDrawable;
import de.longri.cachebox3.gui.widgets.CB_Button;
import de.longri.cachebox3.gui.widgets.CB_ProgressBar;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.translation.word.CompoundCharSequence;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.gdx.sqlite.GdxSqlitePreparedStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

import static de.longri.cachebox3.apis.GroundspeakAPI.*;


/**
 * Created by Longri on 28.06.2017.
 */
public class UpdateStatusAndOthers extends ActivityBase {

    private static final Logger log = LoggerFactory.getLogger(UpdateStatusAndOthers.class);


    private final int BlockSize = 50; // API 1.0 has a limit of 50, handled in GroundspeakAPI but want to write to DB after Blocksize fetched
    private final CB_Button bCancel;
    private final VisLabel lblTitle;
    private final Image gsLogo;
    private final Image workAnimation;
    private final CB_ProgressBar CBProgressBar;
    private final AtomicBoolean canceled = new AtomicBoolean(false);
    private boolean importRuns = false;
    private final ClickListener cancelListener = new ClickListener() {
        public void clicked(InputEvent event, float x, float y) {
            if (importRuns) {
                canceled.set(true);
            }
            finish();
        }
    };
    private boolean needLayout = true;

    public UpdateStatusAndOthers() {
        super("UpdateStatusAndOthers");

        bCancel = new CB_Button(Translation.get("cancel"));
        gsLogo = new Image(CB.getSkin().getIcon.GC_Live);
        lblTitle = new VisLabel(Translation.get("chkApiState"));
        Label.LabelStyle style = new Label.LabelStyle(lblTitle.getStyle());
        style.fontColor.set(Color.WHITE);
        lblTitle.setStyle(style);
        Drawable animationDrawable = VisUI.getSkin().getDrawable("download-animation");
        workAnimation = new Image(animationDrawable);
        CBProgressBar = new CB_ProgressBar(0, 100, 1, false, "default");

        createOkCancelBtn();
        setWorkAnimationVisible(false);

        this.setStageBackground(new ColorDrawable(VisUI.getSkin().getColor("dialog_background")));
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


        this.add(workAnimation).colspan(5).center();
        this.row();
        this.add();
        this.add(CBProgressBar).colspan(3).center().expandX().fillX();
        this.row();
        Table nestedTable2 = new Table();
        nestedTable2.defaults().pad(CB.scaledSizes.MARGIN).bottom();
        nestedTable2.add(bCancel).bottom();
        this.add(nestedTable2).colspan(5);

        super.layout();
        needLayout = false;
    }

    private void setWorkAnimationVisible(boolean visible) {
        workAnimation.setVisible(visible);
        CBProgressBar.setVisible(visible);
    }

    private void createOkCancelBtn() {
        bCancel.addListener(cancelListener);
        CB.stageManager.registerForBackKey(cancelListener);
    }

    @Override
    public void onShow() {
        if (!workAnimation.isVisible()) importNow();
    }

    private void importNow() {

        setWorkAnimationVisible(true);
        CBProgressBar.setAnimateDuration(0);
        final ImportProgressChangedListener progressListener = event -> CB.postOnGlThread(new NamedRunnable("UpdateStatusAndOthers") {
            @Override
            public void run() {
                CBProgressBar.setValue(event.progress.progress);
            }
        });
        EventHandler.add(progressListener);
        importRuns = true;

        CB.postAsync(new NamedRunnable("UpdateStatusAndOthers") {
            @Override
            public void run() {
                int changedCount;
                Array<AbstractCache> chkList = new Array<>();

                synchronized (Database.Data.cacheList) {
                    if (Database.Data.cacheList == null || Database.Data.cacheList.size == 0)
                        return;
                    changedCount = 0;
                    for (int i = 0, n = Database.Data.cacheList.size; i < n; i++) {
                        chkList.add(Database.Data.cacheList.get(i));
                    }

                }
                float progressInkrement = 100.0f / (chkList.size / BlockSize); // 100% durch Anzahl Schleifen
                ImportProgressChangedEvent.ImportProgress importProgress = new ImportProgressChangedEvent.ImportProgress();
                ImportProgressChangedEvent importProgressChangedEvent = new ImportProgressChangedEvent(importProgress);

                int skip = 0;

                Array<AbstractCache> caches = new Array<>();

                float progress = 0;

                // prepare for changes to DB
                String sql = "UPDATE CacheCoreInfo SET BooleanStore = ? , NumTravelbugs = ? , FavPoints = ? WHERE id = ? ;";
                GdxSqlitePreparedStatement REPLACE_STATUS = Database.Data.myDB.prepare(sql);
                Database.Data.myDB.beginTransaction();

                try {
                    do {

                        caches.clear();

                        if (chkList == null || chkList.size == 0) {
                            break;
                        }

                        for (int i = skip; i < skip + BlockSize && i < chkList.size; i++) {
                            caches.add(chkList.get(i));
                        }
                        skip += BlockSize;

                        for (GroundspeakAPI.GeoCacheRelated ci : updateStatusOfGeoCaches(caches)) {
                            AbstractCache ca = ci.cache;
                            /*
                             todo in ACB2 the DAO checks for changes by reading the database
                             and does the setting for replication to WCB
                             todo implement: since API from 11. march 2019, we can do a API query for searchable / not searchable caches
                             Requests that only include the reference code in the fields do not count against the daily geocache limit.
                             */
                            REPLACE_STATUS.bind(
                                    ca.getBooleanStore(),
                                    ca.getNumTravelbugs(),
                                    ca.getFavoritePoints(),
                                    ca.getId()
                            ).commit().reset();
                            changedCount++; // is all without compare
                        }

                        if (APIError != OK) {
                            CB.viewmanager.toast(LastAPIError);
                            break;
                        }

                        progress += progressInkrement;
                        importProgress.progress = (int) progress;
                        EventHandler.fire(importProgressChangedEvent);

                    } while (skip < chkList.size);
                } finally {
                    Database.Data.myDB.endTransaction();
                }

                finish();

                final int completeCount = changedCount;
                CB.postOnGlThread(new NamedRunnable("UpdateStatusAndOthers:") {
                    @Override
                    public void run() {
                        //Give feedback and say what updated!
                        EventHandler.fire(new CacheListChangedEvent());
                        CharSequence title = Translation.get("chkState");
                        CharSequence msg = new CompoundCharSequence(Translation.get("CachesUpdated")
                                , " ", Integer.toString(completeCount), "/", Integer.toString(Database.Data.cacheList.size));
                        Window dialog = new ButtonDialog("chkState", msg, title, MessageBoxButtons.OK, MessageBoxIcon.None, new OnMsgBoxClickListener() {
                            @Override
                            public boolean onClick(int which, Object data) {
                                if (which == ButtonDialog.BUTTON_POSITIVE) {
                                    hide();
                                    //Reload Cachelist if any changed
                                    if (completeCount > 0) {
                                        CB.postAsync(new NamedRunnable("UpdateStatusAndOthers:finish reload cachelist") {
                                            @Override
                                            public void run() {
                                                CB.loadFilteredCacheList(null);
                                            }
                                        });
                                    }
                                }
                                return true;
                            }
                        });
                        dialog.show();
                    }
                });
            }
        });
    }


    @Override
    public void dispose() {
        super.dispose();
        CB.stageManager.unRegisterForBackKey(cancelListener);
    }
}

