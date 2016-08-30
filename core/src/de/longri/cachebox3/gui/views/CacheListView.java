package de.longri.cachebox3.gui.views;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.GlobalCore;
import de.longri.cachebox3.settings.SettingCategory;
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
            CacheWithWP nearstCacheWp = Database.Data.Query.Resort(GlobalCore.getSelectedCoord(), new CacheWithWP(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint()));

            if (nearstCacheWp != null)
                GlobalCore.setSelectedWaypoint(nearstCacheWp.getCache(), nearstCacheWp.getWaypoint());
            setSelectedCacheVisible();
        }
    }

    private void setSelectedCacheVisible() {
        //TODO
    }


    private void addNewListView() {
        this.clear();

        listView = new ListView(Database.Data.Query.size()) {
            @Override
            public VisTable createView(Integer index) {
                return getCacheItem(Database.Data.Query.get(index));
            }
        };

        listView.setBounds(0, 0, this.getWidth(), this.getHeight());
        this.addActor(listView);

    }

    private VisTable getCacheItem(final Cache cache) {
        VisTable table = new VisTable();

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
}
