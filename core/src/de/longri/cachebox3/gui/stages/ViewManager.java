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
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.CacheboxMain;
import de.longri.cachebox3.gui.actions.*;
import de.longri.cachebox3.gui.actions.show_vies.*;
import de.longri.cachebox3.gui.views.AboutView;
import de.longri.cachebox3.gui.views.AbstractView;
import de.longri.cachebox3.gui.widgets.ActionButton;
import de.longri.cachebox3.gui.widgets.ActionButton.GestureDirection;
import de.longri.cachebox3.gui.widgets.ButtonBar;
import de.longri.cachebox3.gui.widgets.GestureButton;
import de.longri.cachebox3.gui.widgets.Slider;
import de.longri.cachebox3.locator.events.newT.*;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.Cache;
import de.longri.cachebox3.types.CacheSizes;
import de.longri.cachebox3.types.CacheTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

/**
 * Created by Longri on 20.07.2016.
 */
public class ViewManager extends NamedStage implements SelectedCacheChangedListener, SelectedWayPointChangedListener {

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

    public ViewManager(final CacheboxMain main) {
        super("ViewManager");

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


        db_button = new GestureButton("db");
        cache_button = new GestureButton("cache");
        navButton = new GestureButton("nav");
        tool_button = new GestureButton("tool");
        misc_button = new GestureButton("misc");

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


        //register SelectedCacheChangedEvent
        EventHandler.add(this);

        //set selected Cache to slider
        selectedCacheChanged(new SelectedCacheChangedEvent(EventHandler.getSelectedCache()));
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
            log.debug("remove and dispose actView" + actView.getName());
            this.getRoot().removeActor(actView);
            actView.onHide();
            actView.dispose();
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

            if (!buttonFound) {
                for (ActionButton actionButton : gestureButton.getButtonActions()) {
                    if (actionButton.getAction() instanceof Abstract_Action_ShowView) {
                        Abstract_Action_ShowView viewAction = (Abstract_Action_ShowView) actionButton.getAction();
                        if (viewAction.viewTypeEquals(this.actView)) {
                            gestureButton.setChecked(true);
                            gestureButton.setHasContextMenu(viewAction.hasContextMenu());

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

        db_button.addAction(new ActionButton(new Action_Show_CacheList(), true, GestureDirection.Up));
        db_button.addAction(new ActionButton(new Action_Show_TrackableListView(), false, GestureDirection.Right));
        db_button.addAction(new ActionButton(new Action_Show_TrackListView(), false, GestureDirection.Down));

        cache_button.addAction(new ActionButton(new Action_Show_DescriptionView(), true, GestureDirection.Up));
        cache_button.addAction(new ActionButton(new Action_Show_WaypointView(), false, GestureDirection.Right));
        cache_button.addAction(new ActionButton(new Action_Show_LogView(), false, GestureDirection.Down));
//        cache_button.addAction(new ActionButton(actionShowHint, false));
//        cache_button.addAction(new ActionButton(actionShowDescExt, false));
        cache_button.addAction(new ActionButton(new Action_Show_SpoilerView(), false));
        cache_button.addAction(new ActionButton(new Action_Show_NoteView(), false));

        navButton.addAction(new ActionButton(new Action_Show_MapView(), true, GestureDirection.Up));
        navButton.addAction(new ActionButton(new Action_Show_CompassView(), false, GestureDirection.Right));
        navButton.addAction(new ActionButton(new Action_NavigateExt(), false, GestureDirection.Down));
        navButton.addAction(new ActionButton(new Action_NavigateInt(), false, GestureDirection.Left));
        if (CB.isTestVersion())
            navButton.addAction(new ActionButton(new Action_Show_TestView(), false));
//
//        mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionQuickFieldNote, false, GestureDirection.Up));
//        mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionShowFieldNotesView, Config.ShowFieldnotesAsDefaultView.getValue()));
//        mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionRecTrack, false));
//        mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionRecVoice, false));
//        mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionRecPicture, false, GestureDirection.Down));
//        mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionRecVideo, false));
//        mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionParking, false));
//        mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionShowSolverView, false, GestureDirection.Left));
//        mToolsButtonOnLeftTab.addAction(new CB_ActionButton(actionShowSolverView2, false, GestureDirection.Right));
        tool_button.addAction(new ActionButton(new Action_Show_TestView(), true));
//
        misc_button.addAction(new ActionButton(new Action_Show_AboutView(), true, GestureDirection.Up));
        misc_button.addAction(new ActionButton(new Action_Show_Credits(), false));
        misc_button.addAction(new ActionButton(new de.longri.cachebox3.gui.actions.show_activities.Action_Show_Settings(), false, GestureDirection.Left));
        misc_button.addAction(new ActionButton(new Action_Toggle_Day_Night(), false));
        misc_button.addAction(new ActionButton(new Action_Show_Help(), false));
        misc_button.addAction(new ActionButton(new Action_Show_Quit(), false, GestureDirection.Down));

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
    public void selectedCacheChanged(SelectedCacheChangedEvent event) {
        setCacheName(event.cache);
    }

    @Override
    public void selectedWayPointChanged(SelectedWayPointChangedEvent event) {
        setCacheName(Database.Data.Query.GetCacheById(event.wayPoint.CacheId));
    }

    Cache lastCache = null;

    private void setCacheName(Cache cache) {
        // set Cache name to Slider
        if (cache == null) {
            slider.setCacheName(EMPTY);
        } else {
            if (lastCache == null || !lastCache.equals(cache)) {
                CharSequence text = CacheTypes.toShortString(cache)
                        + terrDiffToShortString(cache.getDifficulty()) + "/"
                        + terrDiffToShortString(cache.getTerrain()) + CacheSizes.toShortString(cache)
                        + " " + cache.getName();
                slider.setCacheName(text);
                lastCache = cache;
            }
        }
    }


    // Toast pop up
    public enum ToastLength {
        SHORT(1.0f), NORMAL(1.5f), LONG(3.5f), EXTRA_LONG(6.0f);

        public final float value;

        ToastLength(float value) {
            this.value = value;
        }
    }

    public void toast(String massage) {
        toast(massage, ToastLength.NORMAL);
    }

    public void toast(String massage, ToastLength length) {
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

    public void toast(final Actor actor, ToastLength length) {
        StageManager.addToastActor(actor);
        actor.addAction(sequence(Actions.alpha(0), Actions.fadeIn(CB.WINDOW_FADE_TIME, Interpolation.fade)));

        new com.badlogic.gdx.utils.Timer().scheduleTask(new com.badlogic.gdx.utils.Timer.Task() {
            @Override
            public void run() {
                actor.addAction(sequence(Actions.fadeOut(CB.WINDOW_FADE_TIME, Interpolation.fade), Actions.removeActor()));
            }
        }, length.value);
    }
}
