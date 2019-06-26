/*
 * Copyright (C) 2016 - 2018 team-cachebox.de
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
package de.longri.cachebox3.gui.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.apis.GroundspeakAPI;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.events.SelectedCacheChangedEvent;
import de.longri.cachebox3.events.SelectedWayPointChangedEvent;
import de.longri.cachebox3.events.location.OrientationChangedEvent;
import de.longri.cachebox3.events.location.OrientationChangedListener;
import de.longri.cachebox3.events.location.PositionChangedEvent;
import de.longri.cachebox3.events.location.PositionChangedListener;
import de.longri.cachebox3.gui.Window;
import de.longri.cachebox3.gui.activities.EditWaypoint;
import de.longri.cachebox3.gui.activities.ProjectionCoordinate;
import de.longri.cachebox3.gui.dialogs.ButtonDialog;
import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButtons;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.menu.MenuItem;
import de.longri.cachebox3.gui.utils.ClickLongClickListener;
import de.longri.cachebox3.gui.widgets.list_view.ListView;
import de.longri.cachebox3.gui.widgets.list_view.ListViewAdapter;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItem;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.sqlite.dao.DaoFactory;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.AbstractCache;
import de.longri.cachebox3.types.AbstractWaypoint;
import de.longri.cachebox3.types.CacheTypes;
import de.longri.cachebox3.types.MutableWaypoint;
import de.longri.cachebox3.utils.MathUtils;
import de.longri.cachebox3.utils.NamedRunnable;
import de.longri.cachebox3.utils.UnitFormatter;
import de.longri.serializable.BitStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.longri.cachebox3.gui.widgets.list_view.ListViewType.VERTICAL;
import static de.longri.cachebox3.gui.widgets.list_view.SelectableType.SINGLE;

/**
 * Created by Longri on 14.09.2016.
 */
public class WaypointView extends AbstractView implements PositionChangedListener, OrientationChangedListener {

    private static final Logger log = LoggerFactory.getLogger(WaypointView.class);
    private AbstractCache actAbstractCache;
    private AbstractWaypoint actWaypoint;
    private ListView listView;
    private final ClickLongClickListener clickLongClickListener = new ClickLongClickListener() {
        @Override
        public boolean clicked(InputEvent event, float x, float y) {
            return false;
        }

        @Override
        public boolean longClicked(Actor actor, float x, float y, float touchDownStageX, float touchDownStageY) {
            if (!(actor instanceof ListViewItem)) return false;
            int listIndex = ((ListViewItem) actor).getListIndex();

            if (listIndex > 0) {
                actWaypoint = actAbstractCache.getWaypoints().get(listIndex - 1);
                if (WaypointView.this.listView != null)
                    WaypointView.this.listView.setSelection(listIndex);
            }
            final Menu contextMenu = getContextMenu();
            Gdx.app.postRunnable(contextMenu::show);
            return true;
        }
    };

    public WaypointView(BitStore reader) {
        super(reader);
    }

    public WaypointView() {
        super("WaypointView");
        actAbstractCache = EventHandler.getSelectedCache();
    }

    @Override
    public void onShow() {
        super.onShow();
        log.debug("onShow");
        //register as positionChanged eventListener
        EventHandler.add(this);
        Gdx.graphics.requestRendering();
    }

    @Override
    public void onHide() {
        super.onHide();
        log.debug("onShow");
        EventHandler.remove(this);
        CB.requestRendering();
    }

    @Override
    public void layout() {
        log.debug("Layout");
        super.layout();
        if (listView == null) addNewListView();
        log.debug("Finish Layout");
        Gdx.app.postRunnable(() -> Gdx.graphics.requestRendering());
    }

    /**
     * Called when the actor's size has been changed.
     */
    protected void sizeChanged() {
        if (listView != null) {
            listView.setSize(this.getWidth(), this.getHeight());
        }
    }

    @Override
    public boolean removeListener(EventListener listener) {
        return super.removeListener(listener);
    }

    private void addNewListView() {

        log.debug("Start Thread add new listView");

        this.clear();
        ListViewAdapter listViewAdapter = new ListViewAdapter() {
            @Override
            public int getCount() {
                if (actAbstractCache == null || actAbstractCache.getWaypoints() == null) return 0;
                return actAbstractCache.getWaypoints().size + 1;
            }

            @Override
            public ListViewItem getView(int index) {
                if (index == 0) {
                    return CacheListItem.getListItem(index, actAbstractCache, getWidth());
                } else {
                    final WayPointListItem item;
                    try {
                        item = WayPointListItem.getListItem(index, actAbstractCache.getWaypoints().get(index - 1), getWidth());
                    } catch (Exception e) {
                        CB.postOnGlThread(new NamedRunnable("Waypoint list invalid") {
                            @Override
                            public void run() {
                                addNewListView();
                            }
                        });
                        return new ListViewItem(index);
                    }
                    return item;
                }
            }

            @Override
            public void update(final ListViewItem view) {
                // set listener on Update, because Item is remove all listener with Layout
                view.addListener(clickLongClickListener);

                //get index from item
                int idx = view.getListIndex();

                Coordinate myPosition = EventHandler.getMyPosition();
                if (myPosition == null)
                    return; // can't update without an position

                float heading = de.longri.cachebox3.events.EventHandler.getHeading();

                // get coordinate from Cache or from Waypoint
                Coordinate targetCoord = idx == 0 ? actAbstractCache : actAbstractCache.getWaypoints().get(idx - 1);

                //calculate distance and bearing
                float[] result = new float[4];
                MathUtils.computeDistanceAndBearing(MathUtils.CalculationType.FAST,
                        myPosition.getLatitude(), myPosition.getLongitude(),
                        targetCoord.getLatitude(), targetCoord.getLongitude(), result);


                //update item
                boolean changed;
                if (idx == 0) {
                    changed = ((CacheListItem) view).update(-(result[2] - heading), UnitFormatter.distanceString(result[0], true));
                } else {
                    changed = ((WayPointListItem) view).update(-(result[2] - heading), UnitFormatter.distanceString(result[0], true));
                }
                if (changed) {
                    Gdx.app.postRunnable(() -> Gdx.graphics.requestRendering());
                }
            }

        };

        if (this.listView != null) {
            disposeListView();
        }

        this.listView = new ListView(VERTICAL);

        this.listView.setEmptyString(Translation.get("NoCacheSelect"));

        this.listView.setAdapter(listViewAdapter);

        synchronized (this.listView) {
            listView.setBounds(0, 0, this.getWidth(), this.getHeight());
            addActor(listView);
            listView.setCullingArea(new Rectangle(0, 0, this.getWidth(), this.getHeight()));
            listView.setSelectable(SINGLE);
            CB.requestRendering();
        }

        // add selection changed event listener
        listView.addSelectionChangedEventListner(() -> {

            if (listView.getSelectedItem() instanceof WayPointListItem) {
                WayPointListItem selectedItem = (WayPointListItem) listView.getSelectedItem();
                int index = selectedItem.getListIndex() - 1;
                AbstractWaypoint wp = actAbstractCache.getWaypoints().get(index);

                log.debug("Waypoint selection changed to: " + wp.toString());
                //set selected Waypoint global
                EventHandler.fire(new SelectedWayPointChangedEvent(wp));
                actWaypoint = wp;

            } else {
                CacheListItem selectedItem = (CacheListItem) listView.getSelectedItem();
                AbstractCache cache = Database.Data.cacheList.getCacheById(selectedItem.getId());
                log.debug("Cache selection changed to: " + cache.toString());
                //set selected Cache global
                EventHandler.fire(new SelectedCacheChangedEvent(cache));
                actWaypoint = null;
            }
        });

        CB.postOnNextGlThread(() -> {
            AbstractWaypoint wp = EventHandler.getSelectedWaypoint();
            if (wp == null) {
                //select Cache
                listView.setSelection(0);
                listView.setSelectedItemVisible(false);
            } else {
                Array<AbstractWaypoint> waypoints = actAbstractCache.getWaypoints();
                for (int i = 0; i < waypoints.size; i++) {
                    if (waypoints.get(i).getGcCode().equals(wp.getGcCode())) {
                        listView.setSelection(i + 1);
                        listView.setSelectedItemVisible(false);
                        break;
                    }
                }
            }
        });
        log.debug("Finish Thread add new listView");
        CB.requestRendering();
    }

    private void disposeListView() {
        final ListView disposeListView = this.listView;
        Thread disposeThread = new Thread(disposeListView::dispose);
        disposeThread.start();
    }

    @Override
    public void dispose() {
        this.actAbstractCache = null;
        this.actWaypoint = null;
        disposeListView();
        this.listView = null;
    }

    private void addProjection() {
        ProjectionCoordinate projActivity = new ProjectionCoordinate(actWaypoint == null ? actAbstractCache : actWaypoint) {
            @Override
            public void callBack(Coordinate newCoord) {
                if (newCoord == null) {
                    // wrong input, create no WP!
                    return;
                }
                addWp(newCoord, false);
            }
        };
        projActivity.show();
    }

    private void addMeasure() {

    }

    private void deleteWP() {
        //name, msg, title, buttons, icon, OnMsgBoxClickListener
        Window dialog = new ButtonDialog("delete Waypoint",
                Translation.get("?DelWP") + "\n[" + actWaypoint.getTitle() + "]\n",
                Translation.get("!DelWP"), MessageBoxButtons.YesNo, MessageBoxIcon.Question,
                (which, data) -> {
                    if (which == ButtonDialog.BUTTON_POSITIVE) {
                        log.debug("Delete Waypoint");
                        // Yes button clicked
                        DaoFactory.WAYPOINT_DAO.delete(Database.Data, actWaypoint, true);
                        actAbstractCache.getWaypoints().removeValue(actWaypoint, false);
                        addNewListView();
                    }
                    return true;
                });
        dialog.show();
    }

    private void editWP(boolean onlyShow) {
        showEditWpDialog(actWaypoint, true, onlyShow);
    }

    public void addWp() {
        addWp(EventHandler.getSelectedCoord(), true);
    }

    private void addWp(Coordinate coordinate, boolean showCoords) {
        String newGcCode;
        try {
            newGcCode = Database.createFreeGcCode(Database.Data, EventHandler.getSelectedCache().getGcCode().toString());
        } catch (Exception e) {
            log.error("can't generate GcCode! can't show EditWaypoint Activity");
            return;
        }
        if (coordinate == null)
            coordinate = EventHandler.getMyPosition();
        if ((coordinate == null) || (!coordinate.isValid()))
            coordinate = EventHandler.getSelectedCache();
        AbstractWaypoint newWP = new MutableWaypoint(newGcCode, CacheTypes.ReferencePoint, "",
                coordinate.getLatitude(), coordinate.getLongitude(), EventHandler.getSelectedCache().getId(), "", newGcCode);
        newWP.setUserWaypoint(true);
        showEditWpDialog(newWP, showCoords, false);
    }

    private void showEditWpDialog(final AbstractWaypoint newWP, final boolean showCoords, final boolean onlyShow) {
        CB.postOnGlThread(new NamedRunnable("WaypointView") {
            @Override
            public void run() {
                EditWaypoint editWaypoint = new EditWaypoint(newWP, showCoords, onlyShow, value -> {
                    if (value != null) {
                        boolean update = false;
                        if (actAbstractCache.getWaypoints().contains(value, false)) {
                            int index = actAbstractCache.getWaypoints().indexOf(value, false);
                            actAbstractCache.getWaypoints().set(index, value);
                            update = true;
                        } else {
                            actAbstractCache.getWaypoints().add(value);
                        }

                        addNewListView();
                        EventHandler.fire(new SelectedWayPointChangedEvent(value));
                        if (value.isStart()) {
                            //It must be ensured here that this waypoint is the only one of these Cache,
                            //which is defined as starting point !!!
                            DaoFactory.WAYPOINT_DAO.resetStartWaypoint(EventHandler.getSelectedCache(), value);
                        }
                        if (update) {
                            DaoFactory.WAYPOINT_DAO.updateDatabase(Database.Data, value, true);
                        } else {
                            DaoFactory.WAYPOINT_DAO.writeToDatabase(Database.Data, value, true);
                        }
                        CB.requestRendering();
                    }
                });
                editWaypoint.show();
            }
        });
    }

    @Override
    public void positionChanged(PositionChangedEvent event) {
        setChangedFlagToAllItems();
    }

    @Override
    public void orientationChanged(OrientationChangedEvent event) {
        setChangedFlagToAllItems();
    }


    private void setChangedFlagToAllItems() {
        if (listView == null) return;
        SnapshotArray<Actor> allItems = listView.items();
        Object[] actors = allItems.begin();
        for (int i = 0, n = allItems.size; i < n; i++) {
            if (actors[i] instanceof CacheListItem) {
                CacheListItem item = (CacheListItem) actors[i];
                item.posOrBearingChanged();
            } else if (actors[i] instanceof WayPointListItem) {
                WayPointListItem item = (WayPointListItem) actors[i];
                item.posOrBearingChanged();
            }
        }
        allItems.end();
        CB.requestRendering();
    }

    //################### Context menu implementation ####################################
    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        Menu cm = new Menu("WaypointViewContextMenuTitle");

        if (actWaypoint != null) {
            cm.addMenuItem("show", CB.getSkin().getMenuIcon.showWp, () -> editWP(false));
            cm.addMenuItem("edit", CB.getSkin().getMenuIcon.editWp, () -> editWP(true)).setEnabled(false); // todo implement and remove disabled. See issue #252
        }
        cm.addMenuItem("AddWaypoint", CB.getSkin().getMenuIcon.addWp, this::addWp);
        if ((actWaypoint != null) && (actWaypoint.isUserWaypoint()))
            cm.addMenuItem("delete", CB.getSkin().getMenuIcon.delWp, this::deleteWP);
        if (actWaypoint != null || actAbstractCache != null)
            cm.addMenuItem("Projection", CB.getSkin().getMenuIcon.projectWp, this::addProjection);
        // todo icon for UploadCorrectedCoordinates
        MenuItem mi = cm.addMenuItem("UploadCorrectedCoordinates", null, () -> {
            if (actAbstractCache.hasCorrectedCoordinates())
                GroundspeakAPI.uploadCorrectedCoordinates(actAbstractCache.getGcCode().toString(), actAbstractCache.getLatitude(), actAbstractCache.getLongitude());
            else if (isCorrectedFinal())
                GroundspeakAPI.uploadCorrectedCoordinates(actAbstractCache.getGcCode().toString(), actWaypoint.getLatitude(), actWaypoint.getLongitude());
            if (GroundspeakAPI.APIError == GroundspeakAPI.OK) {
                MessageBox.show(Translation.get("ok"), Translation.get("UploadCorrectedCoordinates"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
            } else {
                MessageBox.show(GroundspeakAPI.LastAPIError, Translation.get("UploadCorrectedCoordinates"), MessageBoxButtons.OK, MessageBoxIcon.Information, null);
            }
        });
        mi.setEnabled(actAbstractCache.hasCorrectedCoordinates() || isCorrectedFinal());
        cm.addMenuItem("FromGps", CB.getSkin().getMenuIcon.mesureWp, this::addMeasure);

        return cm;
    }

    private boolean isCorrectedFinal() {
        // return new String(Title, (UTF_8)).equals("Final GSAK Corrected");
        if (actWaypoint == null) return false;
        return actWaypoint.getType() == CacheTypes.Final && actWaypoint.isUserWaypoint() && actWaypoint.isValid();
    }
}
