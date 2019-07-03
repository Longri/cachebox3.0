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

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.CB_SpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.skin.styles.CompassStyle;
import de.longri.cachebox3.gui.skin.styles.CompassViewStyle;
import de.longri.cachebox3.gui.views.DescriptionView;
import de.longri.cachebox3.gui.views.MapView;
import de.longri.cachebox3.utils.NamedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Longri on 25.08.2016.
 */
public class StageManager {
    final  Logger log = LoggerFactory.getLogger(StageManager.class);
    final  Array<NamedStage> stageList = new Array<>(5);

    public final  Viewport viewport = new ScalingViewport(Scaling.stretch, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), new OrthographicCamera());
    public final  CB_SpriteBatch batch = new CB_SpriteBatch(CB_SpriteBatch.Mode.NORMAL);


    final  NamedStage toastStage = new NamedStage("toastStage", viewport, batch);

    private  boolean debug = true;
    private  boolean writeDrawSequence = debug;

    NamedStage mainStage;
    private  InputMultiplexer inputMultiplexer;

    public static final InputEvent BACK_KEY_INPUT_EVENT = new InputEvent();
    private  final Array<ClickListener> backKeyClickListener = new Array<>();
    private  NamedStage zOrderTopStage;

    public  void draw() {
        synchronized (stageList) {
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

        if (indicateCatchedException.get()) {
            if (exceptionDrawCount.incrementAndGet() > 10) {
                indicateCatchedException.set(false);
                exceptionDrawCount.set(0);
            }
            if (exceptionSprite != null) {
                if (!batch.isDrawing())
                    batch.begin();
                Color lastColor = batch.getColor();
                batch.setColor(exceptionDrawColor);
                batch.draw(exceptionSprite, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                batch.setColor(lastColor);
                batch.end();
            } else {
                Sprite sprite = CB.getSprite("color");
                if (sprite != null) {
                    exceptionSprite = new Sprite(sprite);
                    exceptionSprite.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                    exceptionSprite.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                }
            }
        }

    }

    public  void indicateException(Color color) {
        if (!CB.DRAW_EXCEPTION_INDICATOR) return;
        exceptionDrawColor = color;
        exceptionDrawCount.set(0);
        indicateCatchedException.set(true);
    }

    private final  AtomicBoolean indicateCatchedException = new AtomicBoolean(false);
    private final  AtomicInteger exceptionDrawCount = new AtomicInteger(0);
    private  Color exceptionDrawColor = Color.GREEN;
    private  Sprite exceptionSprite;

    public  void addToastActor(Actor actor) {
        log.debug("show Toast");
        //only one Toast Actor is visible
        if (toastStage.getActors().size > 0) {
            toastStage.clear();
        }

        toastStage.addActor(actor);
        if (debug) writeDrawSequence = true;
    }

    public  NamedStage showOnNewStage(final Actor actor) {
        synchronized (stageList) {
            if (stageList.size > 0) {
                NamedStage actStage = stageList.get(stageList.size - 1);

                String lastName = actStage.getName();
                if (lastName.equals(actor.getName())) {
                    // don't show double
                    return actStage;
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
                        log.debug("Call DescriptionView.onHide() for showing overlay stage");
                    }
                    viewManager.getActView().onHide();
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
                NamedStage stage = stageList.get(stageList.size - 2);
                if (stage != null) inputMultiplexer.removeProcessor(stage);
            } else {
                if (mainStage != null) inputMultiplexer.removeProcessor(mainStage);
            }


            addNonDoubleInputProzessor(newStage);

            log.debug("InputProzessors:" + inputMultiplexer.getProcessors().toString());

            if (debug) writeDrawSequence = true;
            setTopStage();
            return newStage;
        }
    }

    public  NamedStage showOnActStage(Actor actor) {
        NamedStage stage = stageList.get(stageList.size - 1);
        stage.addActor(actor);
        return stage;
    }

    public  void removeAllWithActStage(NamedStage showingStage) {
        synchronized (stageList) {
            if (stageList.size == 0) return;
            NamedStage stage = stageList.pop();

            if (showingStage != stage) {
                stageList.add(stage);
                if (showingStage.getActors().size > 1) {
                    //todo remove only actor
                } else {
                    stageList.removeValue(showingStage, true);
                }
                stage = showingStage;
            }

            log.debug("remove Stage: " + stage.getName());
            log.debug("Stage list: " + stageList.toString());

            //switch input processor to main stage
            inputMultiplexer.removeProcessor(stage);
            if (stageList.size > 0) {
                addNonDoubleInputProzessor(stageList.get(stageList.size - 1));
            } else {
                addNonDoubleInputProzessor(mainStage);
                {
                    final ViewManager viewManager = (ViewManager) mainStage;
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
                    CB.postOnGlThread(new NamedRunnable("Call act View.onShow()") {
                        @Override
                        public void run() {
                            viewManager.getActView().onShow();
                        }
                    });
                    ;
                }
            }
            log.debug("InputProzessors:" + inputMultiplexer.getProcessors().toString());
            if (debug) writeDrawSequence = true;
            setTopStage();
        }
    }

    private  void setTopStage() {
        if (stageList.size > 0) {
            zOrderTopStage = stageList.get(stageList.size - 1);
        } else {
            zOrderTopStage = mainStage;
        }
    }

    public  void setMainStage(NamedStage stage) {
        mainStage = stage;

        if (false && mainStage instanceof ViewManager) {
            // add scaled drawable to batch for non throwing exception with scaled drawing!
            CompassStyle compassStyle = VisUI.getSkin().get("compassViewStyle", CompassViewStyle.class);
            if (compassStyle.arrow != null)
                batch.registerScaledDrawable(((TextureAtlas.AtlasRegion) ((TextureRegionDrawable) compassStyle.arrow).getRegion()).name);

            if (compassStyle.scale != null)
                batch.registerScaledDrawable(((TextureAtlas.AtlasRegion) ((TextureRegionDrawable) compassStyle.scale).getRegion()).name);

            if (compassStyle.frameCompasAlign != null)
                batch.registerScaledDrawable(((TextureAtlas.AtlasRegion) ((TextureRegionDrawable) compassStyle.frameCompasAlign).getRegion()).name);

            if (compassStyle.frameNorthOrient != null)
                batch.registerScaledDrawable(((TextureAtlas.AtlasRegion) ((TextureRegionDrawable) compassStyle.frameNorthOrient).getRegion()).name);

            if (compassStyle.frameUserRotate != null)
                batch.registerScaledDrawable(((TextureAtlas.AtlasRegion) ((TextureRegionDrawable) compassStyle.frameUserRotate).getRegion()).name);

        }

        // add mainStage to input processor
        if (inputMultiplexer != null) addNonDoubleInputProzessor(mainStage);
    }

    private  void addNonDoubleInputProzessor(InputProcessor processor) {
        if (inputMultiplexer.getProcessors().contains(processor, true)) return;
        inputMultiplexer.addProcessor(processor);
    }

    public  void setInputMultiplexer(InputMultiplexer newInputMultiplexer) {
        inputMultiplexer = newInputMultiplexer;

        //add BackKey listener
        InputProcessor backProcessor = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if ((keycode == Input.Keys.ESCAPE) || (keycode == Input.Keys.BACK))
                    callBackKeyClicked();
                return false;
            }
        };
        inputMultiplexer.addProcessor(backProcessor);
    }

    public  Batch getBatch() {
        return mainStage.getBatch();
    }

    public  void addMapMultiplexer(InputMultiplexer mapInputHandler) {
        if (inputMultiplexer.getProcessors().contains(mapInputHandler, true)) {
            inputMultiplexer.removeProcessor(mapInputHandler);
        }
        inputMultiplexer.addProcessor(mapInputHandler);
    }

    public  void removeMapMultiplexer(InputMultiplexer mapInputHandler) {
        inputMultiplexer.removeProcessor(mapInputHandler);
    }

    public  void registerForBackKey(ClickListener clickListener) {
        if (!backKeyClickListener.contains(clickListener, true)) {
            backKeyClickListener.add(clickListener);
        }
    }

    public  void unRegisterForBackKey(ClickListener clickListener) {
        if (backKeyClickListener.contains(clickListener, true)) {
            backKeyClickListener.removeValue(clickListener, true);
        }
    }

    private  void callBackKeyClicked() {
        if (backKeyClickListener.size == 0) {
            // no listener registered, call Quit!
            CB.viewmanager.getAction_Show_Quit().execute();
        } else {
            // fire only last
            backKeyClickListener.peek().clicked(BACK_KEY_INPUT_EVENT, -1, -1);
        }
    }

    public  boolean isTop(Stage stage) {
        return zOrderTopStage == stage;
    }

    public  boolean isMainStageOnlyDrawing() {
        return stageList.size == 0;
    }
}
