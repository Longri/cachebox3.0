/*
 * Copyright (C) 2014-2016 team-cachebox.de
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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.kotcrab.vis.ui.widget.VisProgressBar;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.Utils;
import de.longri.cachebox3.gui.stages.initial_tasks.*;
import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.canvas.Bitmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;

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
    private boolean threadStarted = false;


    public interface LoadReady {
        void ready();
    }

    final LoadReady loadReadyHandler;


    VisProgressBar progress;
    Image  OSM_Logo, Route_Logo, Mapsforge_Logo, LibGdx_Logo, GC_Logo;

    Label descTextView;

    int step = 0;
    boolean switcher = false;
    boolean breakForWait = false;
    final ArrayList<AbstractInitTask> initTaskList = new ArrayList<AbstractInitTask>();


    public Splash(LoadReady loadReadyHandler) {
        super("splash");
        this.loadReadyHandler = loadReadyHandler;
        Texture backgroundTexture = new Texture("splash-back.jpg");
        CB.backgroundImage = new Image(backgroundTexture);
        CB.backgroundImage.setWidth(Gdx.graphics.getWidth());
        CB.backgroundImage.setHeight(Gdx.graphics.getHeight());
        this.addActor(CB.backgroundImage);
        InitialView();
    }


    private void InitialView() {

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


        ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle();
        int patch = 12;

        style.background = Utils.get9PatchFromSvg(Gdx.files.internal("progress_back.svg").read(),
                patch, patch, patch, patch);
        style.knob = Utils.get9PatchFromSvg(Gdx.files.internal("progress_foreground.svg").read(),
                patch, patch, patch, patch);
        style.knobBefore = Utils.get9PatchFromSvg(Gdx.files.internal("progress_foreground.svg").read(),
                patch, patch, patch, patch);
        style.background.setLeftWidth(0);
        style.background.setRightWidth(0);
        style.background.setTopHeight(0);
        style.background.setBottomHeight(0);

        style.knob.setLeftWidth(0);
        style.knob.setRightWidth(0);
        style.knob.setTopHeight(0);
        style.knob.setBottomHeight(0);

        style.knobBefore.setLeftWidth(0);
        style.knobBefore.setRightWidth(0);
        style.knobBefore.setTopHeight(0);
        style.knobBefore.setBottomHeight(0);

        progress = new VisProgressBar(0f, 100f, 1f, false, style);
        float margin = 40 * (CanvasAdapter.dpi / 240);
        float progressWidth = Gdx.graphics.getWidth() - (margin * 2);

        progress.setBounds(margin, margin, progressWidth, margin);
        this.addActor(progress);


        progress.setValue(0);

        // Init loader tasks
        initTaskList.add(new InitialWorkPathTask("InitialWorkPAth", 5));
        initTaskList.add(new SkinLoaderTask("Load UI", 30));
        initTaskList.add(new TranslationLoaderTask("Load Translations", 10));
        initTaskList.add(new GdxInitialTask("Initial GDX", 2));
        initTaskList.add(new InitialLocationListenerTask("Initial Loacation Reciver", 1));
        initTaskList.add(new LoadDbTask("Load Database", 10));

        // Use classpath for Desktop or assets for iOS and Android
        assets = (CanvasAdapter.platform.isDesktop()) ?
                new AssetManager(new ClasspathFileHandleResolver())
                : new AssetManager();

        assets.load("skins/day/3d_model/Pfeil.g3db", Model.class);
        assets.load("skins/day/3d_model/compass.g3db", Model.class);
        assets.load("skins/day/3d_model/compass_gray.g3db", Model.class);
        assets.load("skins/day/3d_model/compass_yellow.g3db", Model.class);
        loading = true;

        Gdx.graphics.requestRendering();
    }


    @Override
    public void draw() {

        Gdx.graphics.requestRendering();


        if (loading && assets.update())
            doneLoading();
        super.draw();


        if (!loading && !threadStarted) {
            //Run Loader Tasks at separate threads
            Thread runThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (AbstractInitTask task : initTaskList) {
                        task.runnable();
                        progress.setValue(progress.getValue() + task.percent);
                    }
                    loadReadyHandler.ready();
                }
            });
            threadStarted = true;
            runThread.start();
        }

    }


    boolean loading;
    AssetManager assets;

    private void doneLoading() {

        log.info("add 3DModels");

        Model myLocationModel = assets.get("skins/day/3d_model/Pfeil.g3db", Model.class);
        myLocationModel.materials.get(0).set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
        myLocationModel.nodes.get(0).parts.get(0).material.set(FloatAttribute.createAlphaTest(0.1f));
        SkinLoaderTask.myLocationModel = myLocationModel;

        Model compassModel = assets.get("skins/day/3d_model/compass.g3db", Model.class);
//        compassModel.materials.get(0).set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
//        compassModel.nodes.get(0).parts.get(0).material.set(FloatAttribute.createAlphaTest(0.1f));
        SkinLoaderTask.compassModel = compassModel;

        Model compassGrayModel = assets.get("skins/day/3d_model/compass_gray.g3db", Model.class);
//        compassGrayModel.materials.get(0).set(new BlendingAttribute(GL20.GL_SRC_COLOR, GL20.GL_ONE_MINUS_SRC_COLOR));
//        compassGrayModel.nodes.get(0).parts.get(0).material.set(FloatAttribute.createAlphaTest(0.1f));
        SkinLoaderTask.compassGrayModel = compassGrayModel;

        Model compassYellowModel = assets.get("skins/day/3d_model/compass_yellow.g3db", Model.class);
//        compassYellowModel.materials.get(0).set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
//        compassYellowModel.nodes.get(0).parts.get(0).material.set(FloatAttribute.createAlphaTest(0.1f));
        SkinLoaderTask.compassYellowModel = compassYellowModel;


        loading = false;
    }


}
