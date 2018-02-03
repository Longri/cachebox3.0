/*
 * Copyright (C) 2018 team-cachebox.de
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
package de.longri.cachebox3.gui.widgets.list_view;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Longri on 03.02.18.
 */
public class ListViewItemLinkedList {

    private final static float FACTOR = 100000;

    private ListViewItem first;


    private final ListViewAdapter adapter;
    private final AtomicLong completeSize = new AtomicLong(0);

    public ListViewItemLinkedList(ListViewAdapter adapter) {
        this.adapter = adapter;

        //create linked dummy list with size of first item
        float size = adapter.getItemSize(0);
        ListViewItem act = first = new DummyListViewItem(size);
        for (int i = 0; i < adapter.getCount(); i++) {
            ListViewItem item = new DummyListViewItem(size);
            act.setNext(item);
            item.setBefore(act);
            act = item;
        }
        calcCompleteSize();
    }

    private void calcCompleteSize() {
        float complete = 0;
        ListViewItem act = first;
        do {
            complete += act.size;
            act = act.next;
        } while (act.next != null);
        completeSize.set((long) (complete * FACTOR));
    }

    public float getCompleteSize() {
        return completeSize.floatValue() / FACTOR;
    }
}
