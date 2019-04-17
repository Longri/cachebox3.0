/*
 * Copyright (C) 2016-2019 team-cachebox.de
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
package de.longri.cachebox3.gui.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.ClasspathFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.freetype.SkinFont;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import de.longri.cachebox3.gui.widgets.CB_ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.SvgSkinUtil;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.Utils;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.IncrementProgressEvent;
import de.longri.cachebox3.events.IncrementProgressListener;
import de.longri.cachebox3.gui.drawables.SvgNinePatchDrawable;
import de.longri.cachebox3.gui.stages.initial_tasks.*;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.serializable.BitStore;
import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.canvas.Bitmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The Splash Stage is the first Stage to show on screen
 * and load all relevant things!
 * <p>
 * Like Skin, Config, Translations ...
 * <p>
 * <p>
 *
 * @author ging-buh
 * @author Longri
 */
public class Splash extends NamedStage {
    final static Logger log = LoggerFactory.getLogger(Splash.class);
    private final AtomicBoolean initialisationStarted = new AtomicBoolean(false);


    public interface LoadReady {
        void ready();
    }

    final LoadReady loadReadyHandler;


    CB_ProgressBar progress;
    Label workLabel;
    Image OSM_Logo, Route_Logo, Mapsforge_Logo, LibGdx_Logo, GC_Logo;

    Label descTextView;

    int step = 0;
    boolean switcher = false;
    boolean breakForWait = false;
    final InitTaskList initTaskList = new InitTaskList();


    public Splash(LoadReady loadReadyHandler, Viewport viewport, Batch batch, BitStore instanceStateReader) {
        super("splash", viewport, batch);
        log.debug("Splash creating");
        this.loadReadyHandler = loadReadyHandler;
        Texture backgroundTexture = new Texture("splash-back.jpg");
        CB.backgroundImage = new Image(backgroundTexture);
        CB.backgroundImage.setWidth(Gdx.graphics.getWidth());
        CB.backgroundImage.setHeight(Gdx.graphics.getHeight());
        this.addActor(CB.backgroundImage);
        InitialView(instanceStateReader);
        log.debug("Splash creating ready");
    }


    private void InitialView(BitStore instanceStateReader) {

        // create SVG image from Cachbox Logo
        try {
            InputStream stream = Gdx.files.internal("cb_logo.svg").read();
            float targetWidth = Gdx.graphics.getWidth() * 0.8f;
            Bitmap svgBitmap = PlatformConnector.getSvg("", stream, PlatformConnector.SvgScaleType.SCALED_TO_WIDTH, targetWidth);
            if (svgBitmap != null) {
                CB.CB_Logo = new Image(new Texture(Utils.getPixmapFromBitmap(svgBitmap)));
                CB.CB_Logo.setPosition((Gdx.graphics.getWidth() - svgBitmap.getWidth()) / 2, svgBitmap.getHeight() * 2);
                this.addActor(CB.CB_Logo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        CB_ProgressBar.ProgressBarStyle style = new CB_ProgressBar.ProgressBarStyle();

        style.background = SvgSkinUtil.getSvgNinePatchDrawable(-1, -1, -1, -1
                , 0, 0, -1, -1
                , Utils.getTextureRegion(Gdx.files.internal("progress_back.svg").read()));

        style.knobBefore = SvgSkinUtil.getSvgNinePatchDrawable(-1, -1, -1, -1
                , -1, -1, -1, -1
                , Utils.getTextureRegion(Gdx.files.internal("progress_foreground.svg").read()));


        progress = new CB_ProgressBar(0f, 100f, 1f, false, style);
        float margin = 40 * (CanvasAdapter.dpi / 240);
        float progressWidth = Gdx.graphics.getWidth() - (margin * 2);

        progress.setBounds(margin, margin, progressWidth, ((SvgNinePatchDrawable) style.background).getPatch().getTotalHeight());
        this.addActor(progress);

        progress.setValue(0);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        String path = "skins/day/fonts/DroidSans.ttf";
        labelStyle.fontColor = Color.BLACK;
        labelStyle.font = new SkinFont(path, Gdx.files.internal(path), 20, null);
        workLabel = new Label(" \n ", labelStyle);
        workLabel.setBounds(margin, margin + progress.getHeight() + margin, progressWidth, workLabel.getPrefHeight());
        this.addActor(workLabel);

        // Init loader tasks
        initTaskList.add(new InitialWorkPathTask("InitialWorkPAth"));
        initTaskList.add(new SkinLoaderTask("Load UI"));
        initTaskList.add(new TranslationLoaderTask("Load Translations"));
        initTaskList.add(new GdxInitialTask("Initial GDX"));
        initTaskList.add(new LoadDbTask("Load Database",instanceStateReader));

        // Use classpath for Desktop or assets for iOS and Android
        assets = (CanvasAdapter.platform.isDesktop()) ?
                new AssetManager(new ClasspathFileHandleResolver())
                : new AssetManager();

        Gdx.graphics.requestRendering();
    }


    @Override
    public void draw() {

        Gdx.graphics.requestRendering();
        super.draw();
        if (!initialisationStarted.get()) {
            initialisationStarted.set(true);

            int progressMax = initTaskList.getProgressMax();
            progress.setRange(0, progressMax);
            progress.setStepSize(1f);

            //add progress increment listener
            final IncrementProgressListener incrementProgressListener =
                    new IncrementProgressListener() {
                        @Override
                        public void incrementProgress(final IncrementProgressEvent event) {
                            Gdx.app.postRunnable(new Runnable() {
                                @Override
                                public void run() {

                                    if (event.progressIncrement.incrementMaxValue > 0) {
                                        //increment progress max
                                        progress.setRange(0, progress.getMaxValue() + event.progressIncrement.incrementMaxValue);
                                    }

                                    workLabel.setText(event.progressIncrement.msg);
                                    progress.setValue(progress.getValue() + event.progressIncrement.incrementValue);

                                    log.debug("ProgressEvent MSG:{} increment:{}", event.progressIncrement.msg, event.progressIncrement.incrementValue);
                                }
                            });
                        }
                    };
            EventHandler.add(incrementProgressListener);

            //Run Loader Tasks at a separate thread
            CB.postAsync(new NamedRunnable("Splash") {
                @Override
                public void run() {
                    for (AbstractInitTask task : initTaskList) {
                        try {
                            task.runnable();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    loadReadyHandler.ready();
                    EventHandler.remove(incrementProgressListener);
                    log.debug("InitTask ready progress:{} / progressMax:{}", progress.getValue(), progress.getMaxValue());
                }
            });
        }

    }

    AssetManager assets;
}
