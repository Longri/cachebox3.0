/*
 * Copyright (C) 2016-2020 team-cachebox.de
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

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.help.GestureHelp;
import de.longri.cachebox3.gui.skin.styles.GestureButtonStyle;
import de.longri.cachebox3.gui.utils.ClickLongClickListener;
import de.longri.cachebox3.gui.views.AbstractView;
import de.longri.cachebox3.gui.widgets.Window;
import de.longri.cachebox3.gui.widgets.menu.Menu;
import de.longri.cachebox3.gui.widgets.menu.MenuItem;
import de.longri.cachebox3.gui.widgets.menu.OnItemClickListener;
import de.longri.cachebox3.settings.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Created by Longri on 24.07.16.
 */
public class GestureButton extends Button {

    final static Logger log = LoggerFactory.getLogger(GestureButton.class);
    final static float MIN_GESTURE_VELOCITY = 100;


    private static int idCounter = 0;

    private final GestureButtonStyle style, filterStyle;
    private final ArrayList<AbstractAction> buttonActions;
    private final int ID;
    private final ViewManager viewManager;
    private AbstractShowAction<AbstractView> currentShowAction;
    private boolean hasContextMenu;
    private GestureHelp gestureHelper;
    private Drawable gestureRightIcon, gestureUpIcon, gestureLeftIcon, gestureDownIcon;
    private AbstractAction defaultAction;
    ClickLongClickListener gestureListener = new ClickLongClickListener() {

        @Override
        public boolean clicked(InputEvent event, float x, float y) {
            log.debug("on click");


            // Einfacher Click -> alle Actions durchsuchen, ob die aktActionView darin enthalten ist und diese sichtbar ist
            if ((currentShowAction != null) && (currentShowAction.hasContextMenu())) {
                for (AbstractAction ba : buttonActions) {
                    if (ba == currentShowAction) {
                        if (currentShowAction.isActVisible()) {
                            // Dieses View ist aktuell das Sichtbare
                            // -> ein Click auf den Menü-Button zeigt das Contextmenü
                            // if (aktActionView.ShowContextMenu()) return true;

                            if (currentShowAction.hasContextMenu()) {
                                // das View Context Menü mit dem LongKlick Menü zusammen führen!

                                // Menu zusammen stellen!
                                // zuerst das View Context Menu
                                final Menu compoundMenu = new Menu("");

                                final OnItemClickListener[] bothListener = new OnItemClickListener[2];
                                final OnItemClickListener bothItemClickListener = item -> {

                                    boolean handeld = false;

                                    if (bothListener[0] != null)
                                        handeld = bothListener[0].onItemClick(item);

                                    if (!handeld && bothListener[1] != null)
                                        handeld = bothListener[1].onItemClick(item);

                                    return handeld;
                                };


                                final Menu viewContextMenu = currentShowAction.getContextMenu();
                                if (viewContextMenu != null) {
                                    compoundMenu.setName(viewContextMenu.getName()); // for title translation
                                    viewContextMenu.setCompoundMenu(compoundMenu);
                                    compoundMenu.addItems(viewContextMenu.getItems());
                                    bothListener[0] = viewContextMenu.getOnItemClickListener();

                                    // add divider
                                    compoundMenu.addDivider(-1);
                                }

                                Menu longClickMenu = getLongClickMenu();
                                longClickMenu.setCompoundMenu(compoundMenu);
                                compoundMenu.addItems(longClickMenu.getItems());
                                bothListener[1] = longClickMenu.getOnItemClickListener();
                                compoundMenu.setOnItemClickListener(bothItemClickListener);
                                compoundMenu.reorganizeListIndexes();

                                Menu.OnHideListener onHideListener = compoundMenu::hide;
                                if (viewContextMenu != null) viewContextMenu.addOnHideListener(onHideListener);
                                longClickMenu.addOnHideListener(onHideListener);
                                compoundMenu.show();
                                return true;
                            }
                        }
                    }
                }
            }

            // execute default action
            boolean actionExecuted = false;
            if (defaultAction != null) {
                boolean mustExecuteDefaultAction = true;

                if (defaultAction instanceof AbstractShowAction) {
                    //check if target view not actView
                    Class clazz = ((AbstractShowAction<AbstractView>) defaultAction).getViewClass();
                    if (clazz.isAssignableFrom(CB.viewmanager.getCurrentView().getClass())) {
                        mustExecuteDefaultAction = false;
                    }
                }
                if (mustExecuteDefaultAction) {
                    defaultAction.execute();
                    if (defaultAction instanceof AbstractShowAction)
                        currentShowAction = (AbstractShowAction<AbstractView>) defaultAction;
                    actionExecuted = true;
                }
            }

            // if no default action set, show context-menu from view (like long click)
            if (!actionExecuted) {
                longPress(event.getTarget(), x, y, true);
            }
            return true;
        }

        @Override
        public boolean longClicked(Actor actor, float x, float y, float touchDownStageX, float touchDownStageY) {
            log.debug("onLongClick");
            // GL_MsgBox.show("Button " + Me.getName() + " recivet a LongClick Event");
            // Wenn diesem Button mehrere Actions zugeordnet sind dann wird nach einem Lang-Click ein Menü angezeigt aus dem eine dieser
            // Actions gewählt werden kann

            if (buttonActions.size() > 1) {
                getLongClickMenu().show();
            } else if (buttonActions.size() == 1) {
                // nur eine Action dem Button zugeordnet -> diese Action gleich ausführen
                AbstractAction action = buttonActions.get(0);
                if (action != null) {
                    action.execute();
                    if (action instanceof AbstractShowAction)
                        currentShowAction = (AbstractShowAction<AbstractView>) action;
                }
            }
            return true;
        }

        public void fling(InputEvent event, float velocityX, float velocityY, int button) {

            float maxVelocity = Math.max(Math.abs(velocityX), Math.abs(velocityY));

            if (maxVelocity < MIN_GESTURE_VELOCITY) {
                // not really a gesture
                return;
            }


            AbstractAction.GestureDirection direction = AbstractAction.GestureDirection.Up;
            if (Math.abs(velocityX) >= Math.abs(velocityY)) {
                // left or right
                if (velocityX > 0) {
                    direction = AbstractAction.GestureDirection.Right;
                } else {
                    direction = AbstractAction.GestureDirection.Left;
                }
            } else {
                // up or down
                if (velocityY > 0) {
                    direction = AbstractAction.GestureDirection.Up;
                } else {
                    direction = AbstractAction.GestureDirection.Down;
                }
            }
            executeAction(direction);
        }
    };
    private boolean isLastFiltered = false;

    public GestureButton(GestureButtonStyle style, ViewManager viewManager) {
        this.style = style;
        style.checked = style.select;
        defaultAction = null;
        filterStyle = new GestureButtonStyle();
        filterStyle.down = style.down;
        filterStyle.checked = style.checked;
        filterStyle.up = style.up;
        if (style.downFiltered != null) filterStyle.down = style.downFiltered;
        if (style.selectFilterd != null) filterStyle.checked = style.selectFilterd;
        if (style.upFiltered != null) filterStyle.up = style.upFiltered;

        this.viewManager = viewManager;
        this.setStyle(style);
        this.ID = idCounter++;
        buttonActions = new ArrayList<>();

        //remove all Listeners
        Array<EventListener> listeners = this.getListeners();
        for (EventListener listener : listeners) {
            this.removeListener(listener);
        }
        this.addCaptureListener(gestureListener);
        this.pack();
    }

    public ArrayList<AbstractAction> getButtonActions() {
        return buttonActions;
    }

    public void setHasContextMenu(boolean hasContextMenu) {
        this.hasContextMenu = hasContextMenu;
    }

    public boolean equals(Object other) {
        if (other instanceof GestureButton) {
            return ((GestureButton) other).ID == ID;
        }
        return false;
    }

    public void addDefaultAction(AbstractAction buttonAction, AbstractAction.GestureDirection gestureDirection) {
        buttonAction.setGestureDirection(gestureDirection);
        addToButtonActions(buttonAction, true);
    }

    public void addAction(AbstractAction buttonAction, AbstractAction.GestureDirection gestureDirection) {
        buttonAction.setGestureDirection(gestureDirection);
        addToButtonActions(buttonAction, false);
    }

    public void addDefaultAction(AbstractAction buttonAction) {
        addToButtonActions(buttonAction, true);
    }

    public void addAction(AbstractAction buttonAction) {
        addToButtonActions(buttonAction, false);
    }

    private void addToButtonActions(AbstractAction buttonAction, boolean isDefaultAction) {
        buttonActions.add(buttonAction);
        if (isDefaultAction) defaultAction = buttonAction;

        //check if this a gesture
        if (buttonAction.getGestureDirection() != AbstractAction.GestureDirection.None) {
            switch (buttonAction.getGestureDirection()) {
                case Right:
                    gestureRightIcon = buttonAction.getIcon();
                    break;
                case Up:
                    gestureUpIcon = buttonAction.getIcon();
                    break;
                case Left:
                    gestureLeftIcon = buttonAction.getIcon();
                    break;
                case Down:
                    gestureDownIcon = buttonAction.getIcon();
                    break;
            }
        }

    }

    public void executeDefaultAction() {
        if (defaultAction != null)
            defaultAction.execute();
        else {
            //if no default button so take the first or do nothing if no buttonAction set
            if (!buttonActions.isEmpty()) {
                AbstractAction action = buttonActions.get(0);
                if (action != null) action.execute();
            }
        }
    }

    public void executeAction(AbstractAction.GestureDirection direction) {
        for (AbstractAction action : buttonActions) {
            if (action.getGestureDirection() == direction) {
                action.execute();
                return;
            }
        }
    }

    private Menu getLongClickMenu() {
        Menu longClickMenu = new Menu("");

        longClickMenu.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public boolean onItemClick(MenuItem item) {
                int mId = item.getMenuItemId();

                for (AbstractAction ba : buttonActions) {
                    if (ba.getId() == mId) {
                        final AbstractAction action = ba;

                        //have the calling action a gesture, then show gesture helper
                        if (Config.showGestureHelp.getValue() && ba.getGestureDirection() != AbstractAction.GestureDirection.None) {
                            if (gestureHelper == null) {
                                gestureHelper = new GestureHelp(GestureHelp.getHelpEllipseFromActor(GestureButton.this),
                                        style.up, gestureRightIcon, gestureUpIcon, gestureLeftIcon, gestureDownIcon);
                            }
                            gestureHelper.setWindowCloseListener(new Window.WindowCloseListener() {
                                @Override
                                public void windowClosed() {
                                    gestureHelper.clearWindowCloseListener();
                                    action.execute();
                                    if (action instanceof AbstractShowAction)
                                        currentShowAction = (AbstractShowAction<AbstractView>) action;
                                }
                            });
                            gestureHelper.show(ba.getGestureDirection());
                            return true;
                        } else {
                            // no gesture, call direct
                            action.execute();
                            if (action instanceof AbstractShowAction)
                                currentShowAction = (AbstractShowAction<AbstractView>) action;
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        for (AbstractAction action : buttonActions) {
            // if (action == null || !action.getEnabled())
            //    continue;
            MenuItem mi = longClickMenu.addItem(action.getId(), action.getTitleTranslationId(), action.getNameExtension());
            mi.setEnabled(action.getEnabled());
            mi.setCheckable(action.getIsCheckable());
            mi.setChecked(action.getIsChecked());
            mi.setIcon(action.getIcon());
        }
        return longClickMenu;
    }

    public void draw(Batch batch, float parentAlpha) {

        //check if filter changed
        if (viewManager.isFiltered() != isLastFiltered) {
            if (viewManager.isFiltered() && style.upFiltered != null) {
                this.setStyle(filterStyle);
            } else {
                this.setStyle(style);
            }
            isLastFiltered = viewManager.isFiltered();
        }

        super.draw(batch, parentAlpha);

        if (hasContextMenu && isChecked()) {

            Vector2 stagePos = new Vector2();
            this.localToStageCoordinates(stagePos);

            boolean isFiltered = viewManager.isFiltered();

            if (!isFiltered && style.hasMenu != null) {
                style.hasMenu.draw(batch, stagePos.x, stagePos.y, this.getWidth(), this.getHeight());
            }

            if (isFiltered && style.hasFilteredMenu != null) {
                style.hasFilteredMenu.draw(batch, stagePos.x, stagePos.y, this.getWidth(), this.getHeight());
            } else {
                if (style.hasMenu != null) {
                    style.hasMenu.draw(batch, stagePos.x, stagePos.y, this.getWidth(), this.getHeight());
                }
            }
        }
    }

    /*
    public AbstractShowAction<AbstractView> getCurrentActionView() {
        return currentActionView;
    }
     */

    public void setCurrentShowAction(AbstractShowAction<AbstractView> _currentActionView) {
        currentShowAction = _currentActionView;
    }
}
