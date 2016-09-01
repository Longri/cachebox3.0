package de.longri.cachebox3.gui.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.gui.views.listview.Adapter;
import de.longri.cachebox3.gui.views.listview.ListView;
import de.longri.cachebox3.gui.views.listview.ListViewItem;
import de.longri.cachebox3.sqlite.Database;
import de.longri.cachebox3.types.Cache;
import de.longri.cachebox3.types.CacheWithWP;

/**
 * Created by Longri on 24.07.16.
 */
public class CacheListView extends AbstractView {

    private ListView listView;

    public CacheListView() {
        super("CacheListView CacheCount: " + Database.Data.Query.size());
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
                        return getCacheItem(Database.Data.Query.get(index));
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
                    CacheListView.this.listView.setBounds(0, 0, CacheListView.this.getWidth(), CacheListView.this.getHeight());
                    CacheListView.this.addActor(listView);
                    CacheListView.this.listView.setCullingArea(new Rectangle(0, 0, CacheListView.this.getWidth(), CacheListView.this.getHeight()));
                    Gdx.graphics.requestRendering();
                }
            }
        });
        thread.start();
        Gdx.graphics.requestRendering();
    }

    private ListViewItem getCacheItem(final Cache cache) {
        ListViewItem table = new ListViewItem();

        // add label with category name, align left
        table.left();
        VisLabel label = new VisLabel(cache.getName());
        label.setAlignment(Align.left);
        table.add(label).pad(CB.scaledSizes.MARGIN).expandX().fillX();

//        // add next icon
//        Image next = new Image(style.nextIcon);
//        table.add(next).width(next.getWidth()).pad(CB.scaledSizes.MARGIN / 2);
//
//        // add clicklistener
//        table.addListener(new ClickListener() {
//            public void clicked(InputEvent event, float x, float y) {
//                if (event.getType() == InputEvent.Type.touchUp) {
//                    showCategory(category, true);
//                }
//            }
//        });
        return table;
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
}
