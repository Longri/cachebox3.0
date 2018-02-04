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

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Disposable;
import com.kotcrab.vis.ui.widget.VisTable;
import de.longri.cachebox3.CB;

/**
 * Created by Longri on 03.02.18.
 */
public class ListViewItem extends VisTable implements Disposable {

    private OnDrawListener onDrawListener;
    ListViewItem next;
    ListViewItem before;
    int index;

    public ListViewItem(int index) {
        this.index = index;
    }


    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if (getOnDrawListener() != null) {
            if (CB.viewmanager != null && CB.viewmanager.isTop(this.getStage()))
                getOnDrawListener().onDraw(this);
        }
    }

    @Override
    public void dispose() {

    }

    public OnDrawListener getOnDrawListener() {
        return onDrawListener;
    }

    public void setOnDrawListener(OnDrawListener onDrawListener) {
        this.onDrawListener = onDrawListener;
    }

    public void setNext(ListViewItem next) {
        this.next = next;
    }

    public void setBefore(ListViewItem before) {
        this.before = before;
    }

    public int getListIndex() {
        return index;
    }

    @Override
    public String toString() {
        return "ListView Item: " + Integer.toString(this.index);
    }
}
