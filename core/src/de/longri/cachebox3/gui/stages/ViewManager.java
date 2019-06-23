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
import com.kotcrab.vis.ui.widget.VisLabel;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.CacheboxMain;
import de.longri.cachebox3.events.*;
import de.longri.cachebox3.gui.actions.AbstractAction;
import de.longri.cachebox3.gui.actions.Action_NavigateExt;
import de.longri.cachebox3.gui.actions.Action_Toggle_Day_Night;
import de.longri.cachebox3.gui.actions.show_activities.*;
import de.longri.cachebox3.gui.actions.show_views.*;
import de.longri.cachebox3.gui.views.AboutView;
import de.longri.cachebox3.gui.views.AbstractView;
import de.longri.cachebox3.gui.widgets.ActionButton;
import de.longri.cachebox3.gui.widgets.ActionButton.GestureDirection;
import de.longri.cachebox3.gui.widgets.ButtonBar;
import de.longri.cachebox3.gui.widgets.GestureButton;
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

    private AbstractView actView;
    private final float width, height;
    private final ButtonBar mainButtonBar;
    private GestureButton db_button, cache_button, navButton, tool_button, misc_button;
    private VisLabel toastLabel;
    private Slider slider;
    private float sliderPos = 0;
    private final CacheboxMain main;
    private final Action_Show_DescriptionView action_show_descriptionView = new Action_Show_DescriptionView();
    private final Action_Show_WaypointView action_show_waypointView = new Action_Show_WaypointView();
    private final Action_Show_LogView action_show_logView = new Action_Show_LogView();
    private final Action_Show_MapView action_show_mapView = new Action_Show_MapView();
    private final Action_Show_CompassView action_show_compassView = new Action_Show_CompassView();
    private final Action_Show_CacheList action_show_cacheList = new Action_Show_CacheList();
    private final Action_Show_TrackListView action_show_trackListView = new Action_Show_TrackListView();
    private final Action_Show_SpoilerView action_show_spoilerView = new Action_Show_SpoilerView();
    private final Action_Show_TrackableListView action_show_trackableListView = new Action_Show_TrackableListView();
    private final Action_Show_NoteView action_show_noteView = new Action_Show_NoteView();
    private final Action_Quit action_quit = new Action_Quit();
    private final Action_Show_DraftsView action_show_DraftsView = new Action_Show_DraftsView();

    private FilterProperties actFilter = FilterInstances.ALL;
    private final AtomicBoolean isFilters = new AtomicBoolean(false);
    public final GlobalLocationReceiver locationReceiver;

    public ViewManager(final CacheboxMain main, Viewport viewport, Batch batch) {
        super("ViewManager", viewport, batch);

        log.info("ScaleFactor:" + Float.toString(CB.getScaledFloat(1)));
        log.info("Width:" + Float.toString(Gdx.graphics.getWidth()));
        log.info("Height:" + Float.toString(Gdx.graphics.getHeight()));
        log.info("PPI:" + Float.toString(Gdx.graphics.getPpiX()));

        this.main = main;

        //set this to static CB for global access
        CB.viewmanager = this;

        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();

        slider = new Slider() {
            @Override
            public void viewHeightChanged(float newPos) {
                sliderPos = height - newPos;
                setActViewBounds();
            }
        };
        slider.setBounds(0, 0, width, height);
        this.addActor(slider);
        if (Config.quickButtonLastShow.getValue())
            slider.setQuickButtonVisible();

        db_button = new GestureButton("db", this);
        cache_button = new GestureButton("cache", this);
        navButton = new GestureButton("nav", this);
        tool_button = new GestureButton("tool", this);
        misc_button = new GestureButton("misc", this);

        mainButtonBar = new ButtonBar(CB.getSkin().get("main_button_bar", ButtonBar.ButtonBarStyle.class));
        mainButtonBar.addButton(db_button);
        mainButtonBar.addButton(cache_button);
        mainButtonBar.addButton(navButton);
        mainButtonBar.addButton(tool_button);
        mainButtonBar.addButton(misc_button);
        mainButtonBar.setBounds(0, 0, width, mainButtonBar.getPrefHeight());
        this.addActor(mainButtonBar);
        mainButtonBar.layout();

        initialActionButtons();
        showView(new AboutView());


        //register SelectedCacheChangedEvent/ CacheListChangedEvent
        EventHandler.add(this);

        //set selected Cache to slider
        selectedCacheChanged(new de.longri.cachebox3.events.SelectedCacheChangedEvent(de.longri.cachebox3.events.EventHandler.getSelectedCache()));

        //initial global location receiver
        locationReceiver = new GlobalLocationReceiver();

    }

    public FilterProperties getActFilter() {
        return actFilter;
    }

    public void setNewFilter(FilterProperties filter) {
        this.setNewFilter(filter, false);
    }

    public void setNewFilter(final FilterProperties filter, boolean dontLoad) {
        if (!actFilter.equals(filter)) {

            log.debug("set New Filter: {}", filter.toString());

            actFilter = filter;
            isFilters.set(!actFilter.equals(FilterInstances.ALL));

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

    public boolean isFilters() {
        return isFilters.get();
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

        if (actView != null) {
            final AbstractView dispView = actView;
            log.debug("remove and dispose actView" + dispView.getName());
            this.getRoot().removeActor(dispView);
            CB.postAsync(new NamedRunnable("Dispose last View") {
                @Override
                public void run() {
                    dispView.onHide();
                    dispView.dispose();

                    SnapshotArray<Actor> childs = dispView.getChildren();
                    for (int i = 0, n = childs.size - 1; i < n; i++) {
                        try {
                            dispView.removeChild(childs.get(i));
                        } catch (Exception e) {

                        }
                    }
                    childs.clear();
                }
            });
        }

        this.actView = view;
        this.addActor(view);
        setActViewBounds();
        log.debug("reload view state:" + view.getName());
        this.actView.onShow();


        //bring ButtonBar to Front
        mainButtonBar.toFront();

        // and over all the slider
        slider.toFront();

        //select main button
        boolean buttonFound = false;
        for (Button button : mainButtonBar.getButtons()) {
            GestureButton gestureButton = (GestureButton) button;
            gestureButton.setChecked(false);
            gestureButton.aktActionView = null;
            if (!buttonFound) {
                for (ActionButton actionButton : gestureButton.getButtonActions()) {
                    if (actionButton.getAction() instanceof Abstract_Action_ShowView) {
                        Abstract_Action_ShowView viewAction = (Abstract_Action_ShowView) actionButton.getAction();
                        if (viewAction.viewTypeEquals(this.actView)) {
                            gestureButton.setChecked(true);
                            gestureButton.setHasContextMenu(viewAction.hasContextMenu());
                            gestureButton.aktActionView = viewAction;
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

        db_button.addAction(new ActionButton(action_show_cacheList, true, GestureDirection.Up));
        db_button.addAction(new ActionButton(new Action_ParkingDialog(), false));
        db_button.addAction(new ActionButton(action_show_trackableListView, false, GestureDirection.Right));

        cache_button.addAction(new ActionButton(action_show_descriptionView, true, GestureDirection.Up));
        cache_button.addAction(new ActionButton(action_show_waypointView, false, GestureDirection.Right));
        cache_button.addAction(new ActionButton(action_show_logView, false, GestureDirection.Down));
        cache_button.addAction(new ActionButton(new Action_HintDialog(), false));
//        cache_button.addAction(new ActionButton(actionShowDescExt, false));
        cache_button.addAction(new ActionButton(action_show_spoilerView, false));
        cache_button.addAction(new ActionButton(action_show_noteView, false));

        navButton.addAction(new ActionButton(action_show_mapView, true, GestureDirection.Up));
        navButton.addAction(new ActionButton(action_show_compassView, false, GestureDirection.Right));
        navButton.addAction(new ActionButton(new Action_NavigateExt(), false, GestureDirection.Down));
        // navButton.addAction(new ActionButton(new Action_NavigateInt(), false, GestureDirection.Left)); not implemented, obsolete?! ACB2 removed
        navButton.addAction(new ActionButton(action_show_trackListView, false, GestureDirection.Left));
        navButton.addAction(new ActionButton(new Action_MapDownload(), false)); // "MapDownload",null,()->{}); //todo ISSUE (#113 Add Map download) MapDownload.INSTANCE.show();


//
//        mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionQuickDraft, false, GestureDirection.Up));
        tool_button.addAction(new ActionButton(action_show_DraftsView, true));
        tool_button.addAction(new ActionButton(new Action_Explore(), false));
        tool_button.addAction(new ActionButton(new Action_Start_FileTransfer(), false));
//        mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionRecTrack, false));
//        mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionRecVoice, false));
//        mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionRecPicture, false, GestureDirection.Down));
//        mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionRecVideo, false));
//        mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionShowSolverView, false, GestureDirection.Left));
//        mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionShowSolverView2, false, GestureDirection.Right));
        if (CB.isTestVersion()) {
            tool_button.addAction(new ActionButton(new Action_Show_TestView(), false));
            tool_button.addAction(new ActionButton(new Action_Show_PlatformTestView(), false));
        }

        misc_button.addAction(new ActionButton(new Action_Show_AboutView(), true, GestureDirection.Up));
        misc_button.addAction(new ActionButton(new Action_Show_Credits(), false));
        misc_button.addAction(new ActionButton(new Action_Settings_Activity(), false, GestureDirection.Left));
        misc_button.addAction(new ActionButton(new Action_Toggle_Day_Night(), false));
        misc_button.addAction(new ActionButton(new Action_Help(), false));
        misc_button.addAction(new ActionButton(new Action_GetFriends(), false));
        misc_button.addAction(new ActionButton(action_quit, false, GestureDirection.Down));

//        actionShowAboutView.execute();
    }

    private void setActViewBounds() {
        if (this.actView != null) {
            actView.setBounds(0, mainButtonBar.getHeight(), width, height - (mainButtonBar.getHeight() + sliderPos));
        }

    }

    public AbstractView getActView() {
        return actView;
    }

    public CacheboxMain getMain() {
        return this.main;
    }

    @Override
    public void selectedCacheChanged(de.longri.cachebox3.events.SelectedCacheChangedEvent event) {
        setCacheName(event.cache);
    }

    @Override
    public void selectedWayPointChanged(de.longri.cachebox3.events.SelectedWayPointChangedEvent event) {
        if (event.wayPoint != null) setCacheName(Database.Data.cacheList.GetCacheById(event.wayPoint.getCacheId()));
    }

    AbstractCache lastAbstractCache = null;

    private void setCacheName(AbstractCache abstractCache) {
        // set Cache name to Slider
        if (abstractCache == null) {
            slider.setCacheName(EMPTY);
        } else {
            if (lastAbstractCache == null || !lastAbstractCache.equals(abstractCache)) {
                CharSequence text = abstractCache.getType().toShortString()
                        + terrDiffToShortString(abstractCache.getDifficulty()) + "/"
                        + terrDiffToShortString(abstractCache.getTerrain()) + abstractCache.getSize().toShortString()
                        + " " + abstractCache.getName();
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
        if (Database.Data == null | Database.Data.cacheList == null) return;
        AbstractCache abstractCache = Database.Data.cacheList.GetCacheByGcCode("CBPark");

        if (abstractCache != null)
            Database.Data.cacheList.removeValue(abstractCache, false);

        // add Parking Cache
        if (Config.ParkingLatitude.getValue() != 0) {
            abstractCache = new MutableCache(Config.ParkingLatitude.getValue(), Config.ParkingLongitude.getValue(), "My Parking area", CacheTypes.MyParking, "CBPark");
            Database.Data.cacheList.insert(0, abstractCache);
        }


        //if selected Cache not into cacheList, reset selected Cache
        AbstractCache selectedCache = EventHandler.getSelectedCache();
        if (selectedCache != null) {
            AbstractCache selectedInQuery = Database.Data.cacheList.GetCacheById(selectedCache.getId());
            if (selectedInQuery == null) {
                //reset
                EventHandler.setSelectedWaypoint(null, null);
            }
        }
    }


    // Toast pop up
    public enum ToastLength {
        SHORT(1.0f), NORMAL(1.5f), LONG(3.5f), EXTRA_LONG(6.0f), WAIT(true);

        public final float value;
        public final boolean wait;
        private CloseListener closeListener;

        public interface CloseListener {
            void close();
        }

        ToastLength(float value) {
            this.value = value;
            wait = false;
        }

        ToastLength(boolean wait) {
            this.value = 0;
            this.wait = wait;
        }

        private void setCloseListener(CloseListener listener) {
            this.closeListener = listener;
        }

        public void close() {
            if (this.closeListener != null) {
                this.closeListener.close();
                this.closeListener = null;
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
                toastLabel.setPosition((Gdx.graphics.getWidth() / 2) - (toastLabel.getWidth() / 2), mainButtonBar.getTop() + CB.scaledSizes.MARGINx2);
                toast(toastLabel, length);
            }
        });
    }

    public void toast(final Actor actor, ToastLength length) {
        CB.stageManager.addToastActor(actor);
        actor.addAction(sequence(Actions.alpha(0), Actions.fadeIn(CB.WINDOW_FADE_TIME, Interpolation.fade)));

        if (length.wait) {
            length.setCloseListener(new ToastLength.CloseListener() {
                @Override
                public void close() {
                    log.debug("Close Toast from wait");
                    actor.addAction(sequence(Actions.fadeOut(CB.WINDOW_FADE_TIME, Interpolation.fade), Actions.removeActor()));
                }
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
        if (actView != null) actView.onShow();
    }

    public void pause() {
        locationReceiver.pause();
        if (actView != null) actView.onHide();
    }

    @Override
    public void draw() {
        if (CB.DRAW_EXCEPTION_INDICATOR) this.getRoot().setDebug(true, false);
        super.draw();
    }

}
