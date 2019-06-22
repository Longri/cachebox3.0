/*
 * Copyright (C) 2017 - 2018  team-cachebox.de
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
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.ImportProgressChangedEvent;
import de.longri.cachebox3.events.ImportProgressChangedListener;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButtons;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.drawables.ColorDrawable;
import de.longri.cachebox3.gui.widgets.CB_Button;
import de.longri.cachebox3.gui.widgets.CB_ProgressBar;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.Cache3DAO;
import de.longri.cachebox3.sqlite.dao.LogDAO;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.utils.NamedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by Longri on 14.07.2017.
 */
public class ReloadCacheActivity extends ActivityBase {

    private static final Logger log = LoggerFactory.getLogger(ReloadCacheActivity.class);

    private final CB_Button bCancel;
    private final VisLabel lblTitle;
    private final Image gsLogo;
    private final Image workAnimation;
    private final CB_ProgressBar CBProgressBar;
    private final AtomicBoolean canceled = new AtomicBoolean(false);
    private boolean importRuns = false;
    private final ClickListener cancelClickListener = new ClickListener() {
        public void clicked(InputEvent event, float x, float y) {
            if (importRuns) {
                canceled.set(true);
            }
            finish();
        }
    };
    private boolean needLayout = true;

    public ReloadCacheActivity() {
        super("UpdateStatusAndOthers");
        bCancel = new CB_Button(Translation.get("cancel"));
        gsLogo = new Image(CB.getSkin().getIcon.GC_Live);
        lblTitle = new VisLabel(Translation.get("ReloadCacheAPI"));
        Label.LabelStyle style = new Label.LabelStyle(lblTitle.getStyle());
        style.fontColor.set(Color.WHITE);
        lblTitle.setStyle(style);
        Drawable animationDrawable = VisUI.getSkin().getDrawable("download-animation");
        workAnimation = new Image(animationDrawable);
        CBProgressBar = new CB_ProgressBar(0, 100, 1, false, "default");

        createOkCancelBtn();
        setWorkAnimationVisible(false);

        this.setStageBackground(new ColorDrawable(VisUI.getSkin().getColor("dialog_background")));
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
        bCancel.addListener(cancelClickListener);
        CB.stageManager.registerForBackKey(cancelClickListener);
    }

    @Override
    public void onShow() {
        if (!workAnimation.isVisible()) importNow();
    }

    private void importNow() {

        setWorkAnimationVisible(true);

        CBProgressBar.setAnimateDuration(0);
        final ImportProgressChangedListener progressListener = new ImportProgressChangedListener() {
            @Override
            public void progressChanged(final ImportProgressChangedEvent event) {
                CB.postOnGlThread(new NamedRunnable("ReloadCacheActivity") {
                    @Override
                    public void run() {
                        CBProgressBar.setValue(event.progress.progress);
                    }
                });
            }
        };
        EventHandler.add(progressListener);
        importRuns = true;

        AbstractCache aktCache = EventHandler.getSelectedCache();
        if (aktCache != null) {
            Array<GroundspeakAPI.GeoCacheRelated> updatedCaches = GroundspeakAPI.updateGeoCache(aktCache);
            if (GroundspeakAPI.APIError != GroundspeakAPI.OK) {
                MessageBox.show(GroundspeakAPI.LastAPIError, Translation.get("ReloadCacheAPI"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
            } else {
                for (GroundspeakAPI.GeoCacheRelated updatedCache : updatedCaches) {
                    Cache3DAO dao = new Cache3DAO();
                    dao.writeToDatabase(Database.Data, updatedCache.cache, true);

                    LogDAO logdao = new LogDAO();
                    logdao.writeToDB(Database.Data, updatedCache.logs);
                }
            }
        }

        importRuns = false;

        finish();

    }

    @Override
    public void dispose() {
        super.dispose();
        CB.stageManager.unRegisterForBackKey(cancelClickListener);
    }
}

