package de.longri.cachebox3.gui.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.events.CacheListChangedEventList;
import de.longri.cachebox3.gui.events.CacheListChangedEventListener;
import de.longri.cachebox3.gui.views.listview.Adapter;
import de.longri.cachebox3.gui.views.listview.ListView;
import de.longri.cachebox3.gui.views.listview.ListViewItem;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.Cache;
import de.longri.cachebox3.types.CacheWithWP;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 24.07.16.
 */
public class CacheListView extends AbstractView implements CacheListChangedEventListener {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(CacheListView.class);
    private ListView listView;

    public CacheListView() {
        super("CacheListView CacheCount: " + Database.Data.Query.size());

        //register as cacheListChanged eventListener
        CacheListChangedEventList.Add(this);
    }

    public void layout() {
        super.layout();
        if (listView == null) addNewListView();
    }

    public void resort() {
        synchronized (Database.Data.Query) {
            CacheWithWP nearstCacheWp = Database.Data.Query.Resort(CB.getSelectedCoord(), new CacheWithWP(CB.getSelectedCache(), CB.getSelectedWaypoint()));

            if (nearstCacheWp != null)
                CB.setSelectedWaypoint(nearstCacheWp.getCache(), nearstCacheWp.getWaypoint());
            setSelectedCacheVisible();
        }
    }

    private void setSelectedCacheVisible() {
        //TODO
    }


    private void addNewListView() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                CacheListView.this.clear();
                Adapter listViewAdapter = new Adapter() {
                    @Override
                    public int getCount() {
                        return Database.Data.Query.size();
                    }

                    @Override
                    public ListViewItem getView(int index) {
                        return getCacheItem(index, Database.Data.Query.get(index));
                    }

                    @Override
                    public void update(ListViewItem view) {

                    }

                    @Override
                    public float getItemSize(int position) {
                        return 0;
                    }
                };
                CacheListView.this.listView = new ListView(listViewAdapter);
                synchronized (CacheListView.this.listView) {
                    listView.setBounds(0, 0, CacheListView.this.getWidth(), CacheListView.this.getHeight());
                    addActor(listView);
                    listView.setCullingArea(new Rectangle(0, 0, CacheListView.this.getWidth(), CacheListView.this.getHeight()));
                    listView.setSelectable(ListView.SelectableType.SINGLE);
                    Gdx.graphics.requestRendering();
                }
            }
        });
        thread.start();
        Gdx.graphics.requestRendering();
    }

    private ListViewItem getCacheItem(int listIndex, final Cache cache) {
        ListViewItem listViewItem = new CacheListItem(listIndex, cache.Type, cache.getName());
        return listViewItem;
    }

    @Override
    public void dispose() {

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
    public void CacheListChangedEvent() {
        log.debug("Cachelist changed, reload listView");
        listView.dataSetChanged();
    }
}
