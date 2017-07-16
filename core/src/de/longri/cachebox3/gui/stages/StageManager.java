/*
 * Copyright (C) 2016-2017 team-cachebox.de
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
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.CB_SpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.longri.cachebox3.gui.views.DescriptionView;
import de.longri.cachebox3.gui.views.MapView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 25.08.2016.
 */
public class StageManager {
    final static Logger log = LoggerFactory.getLogger(StageManager.class);
    final static Array<NamedStage> stageList = new Array<>(5);

    public final static Viewport viewport = new ScalingViewport(Scaling.stretch, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new OrthographicCamera());
    public final static Batch batch = new CB_SpriteBatch(CB_SpriteBatch.Mode.WARN);


    final static NamedStage toastStage = new NamedStage("toastStage", viewport, batch);

    private static boolean debug = true;
    private static boolean writeDrawSequence = debug;

    private static NamedStage mainStage;
    private static InputMultiplexer inputMultiplexer;

    public static void draw() {

        if (stageList.size < 2) {
            mainStage.act();
            mainStage.draw();
            if (writeDrawSequence) log.debug("draw mainStage");
        }

        if (stageList.size == 1) {
            Stage stage = stageList.get(0);
            stage.act();
            stage.draw();
            if (writeDrawSequence) log.debug("draw Stage level 0");
        } else if (stageList.size >= 1) {
            for (int idx = stageList.size - 1; idx < stageList.size; idx++) {
                Stage stage = stageList.get(idx);
                stage.act();
                stage.draw();
                if (writeDrawSequence) log.debug("draw Stage level " + idx);
            }
        }

        // draw toastStage at last over all
        if (toastStage.getActors().size > 0) {
            toastStage.act();
            toastStage.draw();
            if (writeDrawSequence) log.debug("draw Toast Stage");
        }
        if (writeDrawSequence) log.debug("END stage drawing");
        writeDrawSequence = false;

    }

    public static void addToastActor(Actor actor) {
        log.debug("Show Toast");
        //only one Toast Actor is visible
        if (toastStage.getActors().size > 0) {
            toastStage.clear();
        }

        toastStage.addActor(actor);
        if (debug) writeDrawSequence = true;
    }

    public static void showOnNewStage(final Actor actor) {


        if (stageList.size > 0) {
            NamedStage actStage = stageList.get(stageList.size - 1);

            String lastName = actStage.getName();
            if (lastName.equals(actor.getName())) {

                // don't show double
                return;
            }
        } else {
            if (mainStage instanceof ViewManager) {
                ViewManager viewManager = (ViewManager) mainStage;
                if (viewManager.getActView() instanceof MapView) {
                    // handle MapView on ViewManagerStage
                    MapView mapView = (MapView) viewManager.getActView();
                    mapView.setInputListener(false);
                    log.debug("remove input listener from MapView");
                } else if (viewManager.getActView() instanceof DescriptionView) {
                    // handle DescriptionView on ViewManagerStage
                    DescriptionView descriptionView = (DescriptionView) viewManager.getActView();
                    descriptionView.onHide();
                    log.debug("Call DescriptionView.onHide() for showing overlay stage");
                }
            }
        }


        NamedStage newStage = new NamedStage(actor.getName(), viewport, batch);
        newStage.addActor(actor);
        newStage.setKeyboardFocus(actor);
        newStage.setScrollFocus(actor);


        stageList.add(newStage);
        log.debug("add new Stage: " + newStage.getName());
        log.debug("Stage list: " + stageList.toString());

        //switch input processor to window stage

        if (stageList.size > 1) {
            inputMultiplexer.removeProcessor(stageList.get(stageList.size - 2));
        } else {
            inputMultiplexer.removeProcessor(mainStage);
        }


        addNonDoubleInputProzessor(newStage);

        log.debug("InputProzessors:" + inputMultiplexer.getProcessors().toString());

        if (debug) writeDrawSequence = true;
    }

    public static void showOnActStage(Actor actor) {
        Stage stage = stageList.get(stageList.size - 1);
        stage.addActor(actor);
    }

    public static void removeAllWithActStage() {
        if (stageList.size == 0) return;
        NamedStage stage = stageList.pop();

        log.debug("remove Stage: " + stage.getName());
        log.debug("Stage list: " + stageList.toString());

        //switch input processor to main stage
        inputMultiplexer.removeProcessor(stage);
        if (stageList.size > 0) {
            addNonDoubleInputProzessor(stageList.get(stageList.size - 1));
        } else {
            addNonDoubleInputProzessor(mainStage);
            {
                ViewManager viewManager = (ViewManager) mainStage;
                if (viewManager.getActView() instanceof MapView) {
                    // handle MapView on ViewManagerStage
                    MapView mapView = (MapView) viewManager.getActView();
                    mapView.setInputListener(true);
                    log.debug("Enable input listener for MapView");
                } else if (viewManager.getActView() instanceof DescriptionView) {
                    // handle DescriptionView on ViewManagerStage
                    DescriptionView descriptionView = (DescriptionView) viewManager.getActView();
                    descriptionView.onShow();
                    log.debug("Call DescriptionView.onShow() for restore view");
                }

                viewManager.getActView().onShow();
            }
        }
        log.debug("InputProzessors:" + inputMultiplexer.getProcessors().toString());
        if (debug) writeDrawSequence = true;

    }

    public static void setMainStage(NamedStage stage) {
        mainStage = stage;

        // add mainStage to input processor
        if (inputMultiplexer != null) addNonDoubleInputProzessor(mainStage);
    }

    private static void addNonDoubleInputProzessor(InputProcessor processor) {
        if (inputMultiplexer.getProcessors().contains(processor, true)) return;
        inputMultiplexer.addProcessor(processor);
    }

    public static void setInputMultiplexer(InputMultiplexer newInputMultiplexer) {
        inputMultiplexer = newInputMultiplexer;
    }

    public static Batch getBatch() {
        return mainStage.getBatch();
    }

    public static void addMapMultiplexer(InputMultiplexer mapInputHandler) {
        if (!inputMultiplexer.getProcessors().contains(mapInputHandler, true)) {
            inputMultiplexer.addProcessor(mapInputHandler);
        }
    }

    public static void removeMapMultiplexer(InputMultiplexer mapInputHandler) {
        inputMultiplexer.removeProcessor(mapInputHandler);
    }
}
