/*
 * Copyright (C) 2016 - 2017 team-cachebox.de
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

import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.events.EventHandler;
import de.longri.cachebox3.gui.menu.Menu;
import de.longri.cachebox3.gui.skin.styles.FieldNoteListItemStyle;
import de.longri.cachebox3.gui.skin.styles.LogListItemStyle;
import de.longri.cachebox3.gui.views.listview.Adapter;
import de.longri.cachebox3.gui.views.listview.ListView;
import de.longri.cachebox3.gui.views.listview.ListViewItem;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.FieldNoteEntry;
import de.longri.cachebox3.types.FieldNoteList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 14.09.2016.
 */
public class FieldNotesView extends AbstractView {

    private static final Logger log = LoggerFactory.getLogger(FieldNotesView.class);
    private final ListView listView = new ListView();
    private final FieldNoteList fieldNotes;
    private final FieldNoteListItemStyle itemStyle;

    private Array<ListViewItem> items;

    public FieldNotesView() {
        super("FieldNotesView");

        itemStyle = VisUI.getSkin().get("fieldNoteListItemStyle", FieldNoteListItemStyle.class);

        fieldNotes = new FieldNoteList();
        loadFieldNotes(FieldNoteList.LoadingType.LOAD_NEW_LAST_LENGTH);

        listView.setEmptyString(Translation.Get("EmptyFieldNotes"));
        this.addActor(listView);
    }

    @Override
    public void onShow() {
        setListViewAdapter();
    }

    @Override
    public void onHide() {
        super.onHide();
    }

    private void setListViewAdapter() {
        CB.postOnMainThread(new Runnable() {
            @Override
            public void run() {
                listView.setAdapter(listViewAdapter);
            }
        });
    }

    @Override
    public void sizeChanged() {
        listView.setBounds(0, 0, this.getWidth(), this.getHeight());
    }

    private Adapter listViewAdapter = new Adapter() {

        @Override
        public int getCount() {
            return items == null ? 0 : items.size;
        }

        @Override
        public ListViewItem getView(int index) {
            return items == null ? null : items.get(index);
        }

        @Override
        public void update(ListViewItem view) {
            // nothing to do
        }

        @Override
        public float getItemSize(int index) {
            return items == null ? 0 : items.get(index).getHeight();
        }
    };

    @Override
    public void dispose() {
        EventHandler.remove(this);
    }

    private void loadFieldNotes(FieldNoteList.LoadingType type) {
        fieldNotes.loadFieldNotes("", type);

        if (items == null) {
            items = new Array<>();
        }
        items.clear();

        int idx = 0;
        for (FieldNoteEntry entry : fieldNotes) {
            items.add(new FieldNotesViewItem(idx++, entry, itemStyle));
        }

        listView.setAdapter(listViewAdapter);

    }

    public Menu getContextMenu() {
        return null;
    }
}
