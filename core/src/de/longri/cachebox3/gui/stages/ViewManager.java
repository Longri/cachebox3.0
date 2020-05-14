/*
 * Copyright (C) 2016-2018 team-cachebox.de
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
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.CacheboxMain;
import de.longri.cachebox3.events.*;
import de.longri.cachebox3.gui.menu.menuBtn1.Action_ParkingDialog;
import de.longri.cachebox3.gui.menu.menuBtn1.Action_Share;
import de.longri.cachebox3.gui.menu.menuBtn1.Show_CacheList;
import de.longri.cachebox3.gui.menu.menuBtn1.todo.Show_TrackableListAction;
import de.longri.cachebox3.gui.menu.menuBtn2.*;
import de.longri.cachebox3.gui.menu.menuBtn3.*;
import de.longri.cachebox3.gui.menu.menuBtn4.*;
import de.longri.cachebox3.gui.menu.menuBtn4.todo.Action_RecVoice;
import de.longri.cachebox3.gui.menu.menuBtn5.*;
import de.longri.cachebox3.gui.menu.menuBtn5.todo.Action_Toggle_Day_Night;
import de.longri.cachebox3.gui.menu.menuBtn5.todo.Show_Credits;
import de.longri.cachebox3.gui.skin.styles.GestureButtonStyle;
import de.longri.cachebox3.gui.views.AboutView;
import de.longri.cachebox3.gui.views.AbstractView;
import de.longri.cachebox3.gui.widgets.ButtonBar;
import de.longri.cachebox3.gui.widgets.Slider;
import de.longri.cachebox3.locator.GlobalLocationReceiver;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.*;
import de.longri.cachebox3.utils.NamedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

/**
 * Created by Longri on 20.07.2016.
 */
public class ViewManager extends NamedStage
        implements SelectedCacheChangedListener, SelectedWayPointChangedListener, CacheListChangedListener {

    final static Logger log = LoggerFactory.getLogger(ViewManager.class);
    final static CharSequence EMPTY = "";
    public final GlobalLocationReceiver locationReceiver;
    private final float width, height;
    private final ButtonBar mainButtonBar;
    private final CacheboxMain main;
    private final Show_DescriptionAction action_show_descriptionView = new Show_DescriptionAction();
    private final Show_WaypointAction action_show_waypointView = new Show_WaypointAction();
    private final Show_LogAction action_show_logView = new Show_LogAction();
    private final Show_MapAction action_show_mapView = new Show_MapAction();
    private final Show_CompassAction action_show_compassView = new Show_CompassAction();
    private final Show_CacheList action_show_cacheList = new Show_CacheList();
    private final Show_TrackListAction action_show_trackListView = new Show_TrackListAction();
    private final Show_SpoilerAction action_show_spoilerView = new Show_SpoilerAction();
    private final Show_TrackableListAction action_show_trackableListView = new Show_TrackableListAction();
    private final Show_NoteAction action_show_noteView = new Show_NoteAction();
    private final Action_Quit action_quit = new Action_Quit();
    private final Show_DraftsAction action_show_DraftsView = new Show_DraftsAction();
    private final AtomicBoolean isFiltered = new AtomicBoolean(false);
    AbstractCache lastAbstractCache = null;
    private AbstractView currentView;
    private GestureButton db_button, cache_button, nav_button, tool_button, misc_button;
    private VisLabel toastLabel;
    private Slider slider;
    private float sliderPos = 0;
    private FilterProperties actFilter = FilterInstances.ALL;

    public ViewManager(final CacheboxMain cacheboxMain, Viewport viewport, Batch batch) {
        super("ViewManager", viewport, batch);

        log.info("ScaleFactor:" + CB.getScaledFloat(1));
        log.info("Width:" + Float.toString(Gdx.graphics.getWidth()));
        log.info("Height:" + Float.toString(Gdx.graphics.getHeight()));
        log.info("PPI:" + Gdx.graphics.getPpiX());

        main = cacheboxMain;

        //set this to static CB for global access
        CB.viewmanager = this;

        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();

        slider = new Slider() {
            @Override
            public void viewHeightChanged(float newPos) {
                sliderPos = height - newPos;
                setCurrentViewBounds();
            }
        };
        slider.setBounds(0, 0, width, height);
        addActor(slider);
        if (Config.quickButtonLastShow.getValue())
            slider.setQuickButtonVisible();

        db_button = new GestureButton(VisUI.getSkin().get("db", GestureButtonStyle.class), this);
        cache_button = new GestureButton(VisUI.getSkin().get("cache", GestureButtonStyle.class), this);
        nav_button = new GestureButton(VisUI.getSkin().get("nav", GestureButtonStyle.class), this);
        tool_button = new GestureButton(VisUI.getSkin().get("tool", GestureButtonStyle.class), this);
        misc_button = new GestureButton(VisUI.getSkin().get("misc", GestureButtonStyle.class), this);

        mainButtonBar = new ButtonBar(CB.getSkin().get("main_button_bar", ButtonBar.ButtonBarStyle.class));
        mainButtonBar.addButton(db_button);
        mainButtonBar.addButton(cache_button);
        mainButtonBar.addButton(nav_button);
        mainButtonBar.addButton(tool_button);
        mainButtonBar.addButton(misc_button);
        mainButtonBar.setBounds(0, 0, width, mainButtonBar.getPrefHeight());
        addActor(mainButtonBar);
        mainButtonBar.layout();

        initialActionButtons();
        showView(new AboutView());


        //register SelectedCacheChangedEvent/ CacheListChangedEvent
        EventHandler.add(this);

        //set selected Cache to slider
        selectedCacheChanged(new SelectedCacheChangedEvent(EventHandler.getSelectedCache()));

        //initial global location receiver
        locationReceiver = new GlobalLocationReceiver();

    }

    public FilterProperties getActFilter() {
        return actFilter;
    }

    public void setNewFilter(FilterProperties filter) {
        setNewFilter(filter, false);
    }

    public void setNewFilter(final FilterProperties filter, boolean dontLoad) {
        if (!actFilter.equals(filter)) {

            log.debug("set New Filter: {}", filter.toString());

            actFilter = filter;
            isFiltered.set(!actFilter.equals(FilterInstances.ALL));

            // store filter to config
            Config.FilterNew.setValue(actFilter.getJsonString());
            Config.AcceptChanges();

            if (!dontLoad) {
                CB.postAsync(new NamedRunnable("ViewManager") {
                    @Override
                    public void run() {
                        log.debug("Call loadFilteredCacheList()");
                        CB.loadFilteredCacheList(filter);
                    }
                });
            }
        }
    }

    public boolean isFiltered() {
        return isFiltered.get();
    }

    private String terrDiffToShortString(float value) {
        int intValue = (int) value;
        String retValue;
        if (value == intValue) {
            retValue = "" + intValue;
        } else {
            retValue = "" + value;
        }
        return retValue;
    }

    public void showView(AbstractView view) {

        log.debug("show view:" + view.getName());

        if (currentView != null) {
            final AbstractView dispView = currentView;
            log.debug("remove and dispose actView: " + dispView.getName());
            getRoot().removeActor(dispView);
            CB.postAsync(new NamedRunnable("Dispose last View") {
                @Override
                public void run() {
                    dispView.onHide();
                    dispView.dispose();

                    SnapshotArray<Actor> childs = dispView.getChildren();
                    for (int i = 0, n = childs.size - 1; i < n; i++) {
                        try {
                            dispView.removeChild(childs.get(i));
                        } catch (Exception ignored) {
                        }
                    }
                    childs.clear();
                }
            });
        }

        currentView = view;
        addActor(view);
        setCurrentViewBounds();
        log.debug("reload view state:" + view.getName());
        currentView.onShow();


        //bring ButtonBar to Front
        mainButtonBar.toFront();

        // and over all the slider
        slider.toFront();

        //select main button
        boolean buttonFound = false;
        for (Button button : mainButtonBar.getButtons()) {
            GestureButton gestureButton = (GestureButton) button;
            gestureButton.setChecked(false);
            gestureButton.setCurrentShowAction(null);
            if (!buttonFound) {
                for (AbstractAction buttonAction : gestureButton.getButtonActions()) {
                    if (buttonAction instanceof AbstractShowAction) {
                        AbstractShowAction<AbstractView> viewAction = (AbstractShowAction<AbstractView>) buttonAction;
                        if (viewAction.viewTypeEquals(currentView)) {
                            gestureButton.setChecked(true);
                            gestureButton.setHasContextMenu(viewAction.hasContextMenu());
                            gestureButton.setCurrentShowAction(viewAction);
                            buttonFound = true;
                            break;
                        }
                    }
                }
            }
        }
    }

    private void initialActionButtons() {
        // assign the actions to the buttons

        db_button.addDefaultAction(action_show_cacheList, AbstractAction.GestureDirection.Up);
        db_button.addAction(new Action_ParkingDialog());
        db_button.addAction(action_show_trackableListView, AbstractAction.GestureDirection.Right);
        db_button.addAction(new Action_Share(), AbstractAction.GestureDirection.Right);

        cache_button.addDefaultAction(action_show_descriptionView, AbstractAction.GestureDirection.Up);
        cache_button.addAction(action_show_waypointView, AbstractAction.GestureDirection.Right);
        cache_button.addAction(action_show_logView, AbstractAction.GestureDirection.Down);
        cache_button.addAction(new Action_HintDialog());
        cache_button.addAction(action_show_spoilerView);
        cache_button.addAction(action_show_noteView);
//        cache_button.addAction(actionShowDescExt);

        nav_button.addDefaultAction(action_show_mapView, AbstractAction.GestureDirection.Up);
        nav_button.addAction(action_show_compassView, AbstractAction.GestureDirection.Right);
        nav_button.addAction(new Action_NavigateExt(), AbstractAction.GestureDirection.Down);
        // navButton.addAction(new Action_NavigateInt(), AbstractAction.GestureDirection.Left);
        nav_button.addAction(action_show_trackListView, AbstractAction.GestureDirection.Left);
        nav_button.addAction(new Action_MapDownload());

//
//        mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionQuickDraft, AbstractAction.GestureDirection.Up);
        tool_button.addDefaultAction(action_show_DraftsView, AbstractAction.GestureDirection.Up);
//        mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionShowSolverView, AbstractAction.GestureDirection.Left);
//        mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionShowSolverView2, AbstractAction.GestureDirection.Right);
        tool_button.addAction(new Action_TakePhoto(), AbstractAction.GestureDirection.Down);
        tool_button.addAction(new Action_RecVideo());
        tool_button.addAction(new Action_RecVoice());
        tool_button.addAction(new Action_Explore());
        tool_button.addAction(new Action_Start_FileTransfer());
        if (CB.isTestVersion()) {
            tool_button.addAction(new Show_TestAction());
            tool_button.addAction(new Show_PlatformTestAction());
        }

        misc_button.addDefaultAction(new Show_AboutAction(), AbstractAction.GestureDirection.Up);
        misc_button.addAction(new Show_Credits());
        misc_button.addAction(new Action_Settings_Activity(), AbstractAction.GestureDirection.Left);
        misc_button.addAction(new Action_Toggle_Day_Night());
        misc_button.addAction(new Action_Help());
        misc_button.addAction(new Action_GetFriends());
        misc_button.addAction(action_quit, AbstractAction.GestureDirection.Down);

//        actionShowAboutView.execute();
    }

    private void setCurrentViewBounds() {
        if (currentView != null) {
            currentView.setBounds(0, mainButtonBar.getHeight(), width, height - (mainButtonBar.getHeight() + sliderPos));
        }

    }

    public AbstractView getCurrentView() {
        return currentView;
    }

    public CacheboxMain getMain() {
        return main;
    }

    @Override
    public void selectedCacheChanged(SelectedCacheChangedEvent event) {
        setCacheName(event.cache);
    }

    @Override
    public void selectedWayPointChanged(de.longri.cachebox3.events.SelectedWayPointChangedEvent event) {
        if (event.wayPoint != null) setCacheName(Database.Data.cacheList.getCacheById(event.wayPoint.getCacheId()));
    }

    private void setCacheName(AbstractCache abstractCache) {
        // set Cache name to Slider
        if (abstractCache == null) {
            slider.setCacheName(EMPTY);
        } else {
            if (lastAbstractCache == null || !lastAbstractCache.equals(abstractCache)) {
                CharSequence text = abstractCache.getType().toShortString()
                        + terrDiffToShortString(abstractCache.getDifficulty()) + "/"
                        + terrDiffToShortString(abstractCache.getTerrain()) + abstractCache.getSize().toShortString()
                        + " " + abstractCache.getGeoCacheName();
                slider.setCacheName(text);
                lastAbstractCache = abstractCache;
            }
        }
    }

    public AbstractAction getAction_Show_DescriptionView() {
        return action_show_descriptionView;
    }

    public AbstractAction getAction_Show_WaypointView() {
        return action_show_waypointView;
    }

    public AbstractAction getAction_Show_LogView() {
        return action_show_logView;
    }

    public AbstractAction getAction_Show_MapView() {
        return action_show_mapView;
    }

    public AbstractAction getAction_Show_CompassView() {
        return action_show_compassView;
    }

    public AbstractAction getAction_Show_CacheList() {
        return action_show_cacheList;
    }

    public AbstractAction getAction_Show_TrackListView() {
        return action_show_trackListView;
    }

    public AbstractAction getAction_Show_SolverView() {
        return null;
    }

    public AbstractAction getAction_Show_SpoilerView() {
        return action_show_spoilerView;
    }

    public AbstractAction getAction_Show_DraftsView() {
        return null;
    }

    public AbstractAction getAction_Show_TrackableListView() {
        return action_show_trackableListView;
    }

    public AbstractAction getAction_Show_SolverView2() {
        return null;
    }

    public AbstractAction getAction_Show_NoteView() {
        return action_show_noteView;
    }

    public AbstractAction getAction_Show_Quit() {
        return action_quit;
    }

    public boolean isTop(Stage stage) {
        return CB.stageManager.isTop(stage);
    }

    @Override
    public void cacheListChanged(CacheListChangedEvent event) {
        if (Database.Data == null) {
            return;
        } else {
            if (Database.Data.cacheList == null) return;
        }
        AbstractCache abstractCache = Database.Data.cacheList.getCacheByGcCode("CBPark");

        if (abstractCache != null)
            Database.Data.cacheList.removeValue(abstractCache, false);

        // add Parking Cache
        if (Config.ParkingLatitude.getValue() != 0) {
            abstractCache = new MutableCache(Database.Data, Config.ParkingLatitude.getValue(), Config.ParkingLongitude.getValue(), "My Parking area", CacheTypes.MyParking, "CBPark");
            Database.Data.cacheList.insert(0, abstractCache);
        }


        //if selected Cache not into cacheList, reset selected Cache
        AbstractCache selectedCache = EventHandler.getSelectedCache();
        if (selectedCache != null) {
            AbstractCache selectedInQuery = Database.Data.cacheList.getCacheById(selectedCache.getId());
            if (selectedInQuery == null) {
                //reset
                EventHandler.fireSelectedWaypointChanged(null, null);
            }
        }
    }

    public void toast(CharSequence massage) {
        toast(massage, ToastLength.NORMAL);
    }

    public void toast(final CharSequence massage, final ToastLength length) {
        CB.postOnGlThread(new NamedRunnable("postOnGlThread") {
            @Override
            public void run() {
                if (toastLabel == null) {
                    //initial ToastLabel
                    toastLabel = new VisLabel(massage, "toast");
                }
                toastLabel.setAlignment(Align.center, Align.center);
                toastLabel.setWrap(true);
                toastLabel.setText(massage);


                Drawable labelBackground = toastLabel.getStyle().background;
                float border = 0;
                if (labelBackground != null) {
                    border = labelBackground.getLeftWidth()
                            + toastLabel.getStyle().background.getRightWidth() + CB.scaledSizes.MARGINx2;
                }

                GlyphLayout bounds = toastLabel.getStyle().font.newFontCache().setText(massage, 0, 0, CB.scaledSizes.WINDOW_WIDTH - border, 0, true);

                toastLabel.setWidth(bounds.width + border);
                toastLabel.setHeight(bounds.height + border);
                toastLabel.setPosition((Gdx.graphics.getWidth() / 2f) - (toastLabel.getWidth() / 2), mainButtonBar.getTop() + CB.scaledSizes.MARGINx2);
                toast(toastLabel, length);
            }
        });
    }

    public void toast(final Actor actor, ToastLength length) {
        CB.stageManager.addToastActor(actor);
        actor.addAction(sequence(Actions.alpha(0), Actions.fadeIn(CB.WINDOW_FADE_TIME, Interpolation.fade)));

        if (length.wait) {
            length.setCloseListener(() -> {
                log.debug("Close Toast from wait");
                actor.addAction(sequence(Actions.fadeOut(CB.WINDOW_FADE_TIME, Interpolation.fade), Actions.removeActor()));
            });
        } else {
            new com.badlogic.gdx.utils.Timer().scheduleTask(new com.badlogic.gdx.utils.Timer.Task() {
                @Override
                public void run() {
                    log.debug("Close Toast from schedule task");
                    actor.addAction(sequence(Actions.fadeOut(CB.WINDOW_FADE_TIME, Interpolation.fade), Actions.removeActor()));
                }
            }, length.value);
        }
        CB.requestRendering();
    }

    public void resume() {
        locationReceiver.resume();
        if (currentView != null) currentView.onShow();
    }

    public void pause() {
        locationReceiver.pause();
        if (currentView != null) currentView.onHide();
    }

    public void quit() {
        locationReceiver.stopForegroundUpdates();
        if (currentView != null) currentView.onHide();
    }

    @Override
    public void draw() {
        if (CB.DRAW_EXCEPTION_INDICATOR) getRoot().setDebug(true, false);
        super.draw();
    }

    public Slider getSlider() {
        return slider;
    }

    // Toast pop up
    public enum ToastLength {
        SHORT(1.0f), NORMAL(1.5f), LONG(3.5f), EXTRA_LONG(6.0f), WAIT(true);

        public final float value;
        public final boolean wait;
        private CloseListener closeListener;

        ToastLength(float newValue) {
            value = newValue;
            wait = false;
        }

        ToastLength(boolean doWait) {
            value = 0;
            wait = doWait;
        }

        private void setCloseListener(CloseListener listener) {
            closeListener = listener;
        }

        public void close() {
            if (closeListener != null) {
                closeListener.close();
                closeListener = null;
            }
        }

        public interface CloseListener {
            void close();
        }
    }

}
