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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.SnapshotArray;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisProgressBar;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.apis.groundspeak_api.ApiResultState;
import de.longri.cachebox3.apis.groundspeak_api.GroundspeakAPI;
import de.longri.cachebox3.apis.groundspeak_api.search.SearchGC;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.events.*;
import de.longri.cachebox3.gui.ActivityBase;
import de.longri.cachebox3.gui.drawables.ColorDrawable;
import de.longri.cachebox3.gui.stages.StageManager;
import de.longri.cachebox3.gui.stages.ViewManager;
import de.longri.cachebox3.gui.widgets.CharSequenceButton;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.utils.ICancel;
import de.longri.cachebox3.utils.NamedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Created by Longri on 14.07.2017.
 */
public class ReloadCacheActivity extends ActivityBase {

    private static final Logger log = LoggerFactory.getLogger(ReloadCacheActivity.class);

    private final CharSequenceButton bCancel;
    private final VisLabel lblTitle;
    private final Image gsLogo;
    private boolean importRuns = false;
    private boolean needLayout = true;
    private final Image workAnimation;
    private final VisProgressBar progressBar;
    private final AtomicBoolean canceled = new AtomicBoolean(false);

    public ReloadCacheActivity() {
        super("CheckStateActivity");
        bCancel = new CharSequenceButton(Translation.get("cancel"));
        gsLogo = new Image(CB.getSkin().getIcon.GC_Live);
        lblTitle = new VisLabel(Translation.get("ReloadCacheAPI"));
        Label.LabelStyle style = new Label.LabelStyle(lblTitle.getStyle());
        style.fontColor.set(Color.WHITE);
        lblTitle.setStyle(style);
        Drawable animationDrawable = VisUI.getSkin().getDrawable("download-animation");
        workAnimation = new Image(animationDrawable);
        progressBar = new VisProgressBar(0, 100, 1, false, "default");

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
        this.add(progressBar).colspan(3).center().expandX().fillX();
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
        progressBar.setVisible(visible);
    }

    private final ClickListener cancelClickListener = new ClickListener() {
        public void clicked(InputEvent event, float x, float y) {
            if (importRuns) {
                canceled.set(true);
            }
            finish();
        }
    };

    private void createOkCancelBtn() {
        bCancel.addListener(cancelClickListener);
        StageManager.registerForBackKey(cancelClickListener);
    }

    @Override
    public void onShow() {
        if (!workAnimation.isVisible()) importNow();
    }

    private void importNow() {

        setWorkAnimationVisible(true);
        progressBar.setAnimateDuration(0);
        final ImportProgressChangedListener progressListener = new ImportProgressChangedListener() {
            @Override
            public void progressChanged(final ImportProgressChangedEvent event) {
                CB.postOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setValue(event.progress.progress);
                    }
                });
            }
        };
        EventHandler.add(progressListener);
        importRuns = true;

        CB.postAsync(new NamedRunnable("ReloadCacheActivity") {
            @Override
            public void run() {
                ApiCallLimitListener limitListener = new ApiCallLimitListener() {
                    @Override
                    public void waitForCall(ApiCallLimitEvent event) {
                        int sec = (int) (event.getWaitTime() / 1000);
                        CB.viewmanager.toast(Translation.get("ApiLimit"
                                , Integer.toString(Config.apiCallLimit.getValue()), Integer.toString(sec))
                                , ViewManager.ToastLength.LONG);
                    }
                };

                EventHandler.add(limitListener);

                AbstractCache actCache = EventHandler.getSelectedCache();
                if (actCache != null) {
                    final SearchGC searchGC = new SearchGC(GroundspeakAPI.getAccessToken(), actCache.getGcCode().toString(),
                            new ICancel() {
                                @Override
                                public boolean cancel() {
                                    return canceled.get();
                                }
                            });
                    searchGC.available = false;
                    searchGC.excludeFounds = false;
                    searchGC.excludeHides = false;
                    searchGC.logCount = 10;

                    final AtomicBoolean WAIT = new AtomicBoolean(true);
                    searchGC.postRequest(new GenericCallBack<ApiResultState>() {
                        @Override
                        public void callBack(ApiResultState value) {
                            WAIT.set(false);
                        }
                    }, actCache.getGPXFilename_ID());

                    CB.wait(WAIT);
                    //fire changed event
                    EventHandler.fire(new SelectedCacheChangedEvent(actCache));

                }

                //reload Cache complete, close activity
                EventHandler.remove(limitListener);
                finish();

            }
        });
    }

    @Override
    public void dispose() {
        super.dispose();
        StageManager.unRegisterForBackKey(cancelClickListener);
    }
}

