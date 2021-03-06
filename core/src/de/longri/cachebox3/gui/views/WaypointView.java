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
import de.longri.cachebox3.gui.activities.EditWaypoint;
import de.longri.cachebox3.gui.activities.ProjectionCoordinate;
import de.longri.cachebox3.gui.dialogs.ButtonDialog;
import de.longri.cachebox3.gui.dialogs.MessageBox;
import de.longri.cachebox3.gui.dialogs.MessageBoxButton;
import de.longri.cachebox3.gui.dialogs.MessageBoxIcon;
import de.longri.cachebox3.gui.utils.ClickLongClickListener;
import de.longri.cachebox3.gui.widgets.list_view.ListView;
import de.longri.cachebox3.gui.widgets.list_view.ListViewAdapter;
import de.longri.cachebox3.gui.widgets.list_view.ListViewItem;
import de.longri.cachebox3.gui.widgets.menu.Menu;
import de.longri.cachebox3.gui.widgets.menu.MenuItem;
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
import static de.longri.cachebox3.gui.widgets.list_view.SelectionType.SINGLE;

/**
 * Created by Longri on 14.09.2016.
 */
public class WaypointView extends AbstractView implements PositionChangedListener, OrientationChangedListener {

    private static final Logger log = LoggerFactory.getLogger(WaypointView.class);
    private AbstractCache currentCache;
    private AbstractWaypoint currentWaypoint;
    private ListView listView;
    private final ClickLongClickListener clickLongClickListener = new ClickLongClickListener() {
        @Override
        public boolean clicked(InputEvent event, float x, float y) {
            // EventHandler.fireSelectedWaypointChanged(currentCache, currentWaypoint);
            return false;
        }

        @Override
        public boolean longClicked(Actor actor, float x, float y, float touchDownStageX, float touchDownStageY) {
            if (!(actor instanceof ListViewItem)) return false;
            int listIndex = ((ListViewItem) actor).getListIndex();

            if (listIndex > 0) {
                currentWaypoint = currentCache.getWaypoints().get(listIndex - 1);
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
        currentCache = EventHandler.getSelectedCache();
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
                if (currentCache == null || currentCache.getWaypoints() == null) return 0;
                return currentCache.getWaypoints().size + 1;
            }

            @Override
            public ListViewItem getView(int index) {
                if (index == 0) {
                    return new CacheListItem(index, currentCache, getWidth());
                } else {
                    final WayPointListItem listViewItem;
                    try {
                        AbstractWaypoint waypoint = currentCache.getWaypoints().get(index - 1);
                        listViewItem = new WayPointListItem(index,
                                waypoint.getType(),
                                waypoint.getGcCode().toString(),
                                waypoint.getTitle().toString(),
                                waypoint.getDescription(),
                                waypoint.formatCoordinate());
                        listViewItem.setWidth(getWidth());
                        listViewItem.invalidate();
                        listViewItem.pack();
                    } catch (Exception e) {
                        CB.postOnGlThread(new NamedRunnable("Waypoint list invalid") {
                            @Override
                            public void run() {
                                addNewListView();
                            }
                        });
                        return new ListViewItem(index);
                    }
                    return listViewItem;
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
                Coordinate targetCoord = idx == 0 ? currentCache : currentCache.getWaypoints().get(idx - 1);

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
            listView.setSelectionType(SINGLE);
            CB.requestRendering();
        }

        // add selection changed event listener
        listView.addSelectionChangedEventListner(() -> {
            if (listView.getSelectedItem() instanceof WayPointListItem) {
                WayPointListItem selectedItem = (WayPointListItem) listView.getSelectedItem();
                int index = selectedItem.getListIndex() - 1;
                AbstractWaypoint wp = currentCache.getWaypoints().get(index);
                log.debug("Waypoint selection changed to: " + wp.toString());
                //set selected Waypoint global
                EventHandler.fire(new SelectedWayPointChangedEvent(wp));
                currentWaypoint = wp;
            } else {
                CacheListItem selectedItem = (CacheListItem) listView.getSelectedItem();
                AbstractCache cache = Database.Data.cacheList.getCacheById(selectedItem.getId());
                log.debug("Cache selection changed to: " + cache.toString());
                //set selected Cache global
                EventHandler.fire(new SelectedCacheChangedEvent(cache));
                currentWaypoint = null;
            }
        });

        CB.postOnNextGlThread(() -> {
            AbstractWaypoint wp = EventHandler.getSelectedWayPoint();
            if (wp == null) {
                //select Cache
                listView.setSelection(0);
                listView.setSelectedItemVisible(false);
            } else {
                Array<AbstractWaypoint> waypoints = currentCache.getWaypoints();
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
        this.currentCache = null;
        this.currentWaypoint = null;
        disposeListView();
        this.listView = null;
    }

    private void addProjection() {
        ProjectionCoordinate projActivity = new ProjectionCoordinate(currentWaypoint == null ? currentCache : currentWaypoint) {
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
        ButtonDialog dialog = new ButtonDialog("delete Waypoint",
                Translation.get("?DelWP") + "\n[" + currentWaypoint.getTitle() + "]\n",
                Translation.get("!DelWP"), MessageBoxButton.YesNo, MessageBoxIcon.Question,
                (which, data) -> {
                    if (which == ButtonDialog.BUTTON_POSITIVE) {
                        log.debug("Delete Waypoint");
                        // Yes button clicked
                        DaoFactory.WAYPOINT_DAO.delete(Database.Data, currentWaypoint, true);
                        currentCache.getWaypoints().removeValue(currentWaypoint, false);
                        addNewListView();
                    }
                    return true;
                });
        dialog.show();
    }

    public void addWp() {
        addWp(EventHandler.getSelectedCoordinate(), true);
    }

    private void addWp(Coordinate coordinate, boolean firstShowEditCoords) {
        String newGcCode;
        try {
            newGcCode = Database.createFreeGcCode(Database.Data, EventHandler.getSelectedCache().getGeoCacheCode().toString());
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
        showEditWpDialog(newWP, firstShowEditCoords);
    }

    private void showEditWpDialog(final AbstractWaypoint newWP, final boolean firstShowEditCoords) {
        CB.postOnGlThread(new NamedRunnable("WaypointView") {
            @Override
            public void run() {
                EditWaypoint editWaypoint = new EditWaypoint(newWP, firstShowEditCoords, value -> {
                    if (value != null) {
                        boolean update = false;
                        if (currentCache.getWaypoints().contains(value, false)) {
                            int index = currentCache.getWaypoints().indexOf(value, false);
                            currentCache.getWaypoints().set(index, value);
                            update = true;
                        } else {
                            currentCache.getWaypoints().add(value);
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

        if (currentWaypoint != null) {
            cm.addMenuItem("show", CB.getSkin().menuIcon.showWp, () -> showEditWpDialog(currentWaypoint, false));
            cm.addMenuItem("edit", CB.getSkin().menuIcon.editWp, () -> showEditWpDialog(currentWaypoint, true));
        }
        cm.addMenuItem("AddWaypoint", CB.getSkin().menuIcon.addWp, this::addWp);
        if ((currentWaypoint != null) && (currentWaypoint.isUserWaypoint()))
            cm.addMenuItem("delete", CB.getSkin().menuIcon.delWp, this::deleteWP);
        if (currentWaypoint != null || currentCache != null)
            cm.addMenuItem("Projection", CB.getSkin().menuIcon.projectWp, this::addProjection);
        MenuItem mi = cm.addMenuItem("UploadCorrectedCoordinates", CB.getSkin().menuIcon.uploadCorrectedCoordinates, () -> {
            if (currentCache.hasCorrectedCoordinates())
                GroundspeakAPI.getInstance().uploadCorrectedCoordinates(currentCache.getGeoCacheCode().toString(), currentCache.getLatitude(), currentCache.getLongitude());
            else if (isCorrectedFinal())
                GroundspeakAPI.getInstance().uploadCorrectedCoordinates(currentCache.getGeoCacheCode().toString(), currentWaypoint.getLatitude(), currentWaypoint.getLongitude());
            if (GroundspeakAPI.getInstance().APIError == GroundspeakAPI.OK) {
                MessageBox.show(Translation.get("ok"), Translation.get("UploadCorrectedCoordinates"), MessageBoxButton.OK, MessageBoxIcon.Information, null);
            } else {
                MessageBox.show(GroundspeakAPI.getInstance().LastAPIError, Translation.get("UploadCorrectedCoordinates"), MessageBoxButton.OK, MessageBoxIcon.Information, null);
            }
        });
        mi.setEnabled(currentCache.hasCorrectedCoordinates() || isCorrectedFinal());
        cm.addMenuItem("FromGps", CB.getSkin().menuIcon.todo, this::addMeasure).setEnabled(false);
        // cm.addMenuItem("FromGps", CB.getSkin().menuIcon.mesureWp, this::addMeasure);

        return cm;
    }

    private boolean isCorrectedFinal() {
        // return new String(Title, (UTF_8)).equals("Final GSAK Corrected");
        if (currentWaypoint == null) return false;
        return currentWaypoint.isCorrectedFinal();
    }
}
