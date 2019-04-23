/*
 * Copyright (C) 2019 team-cachebox.de
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

import com.kotcrab.vis.ui.building.utilities.Alignment;
import com.kotcrab.vis.ui.widget.VisLabel;
import de.longri.cachebox3.gui.widgets.Image;
import de.longri.cachebox3.utils.CB_RectF;
import de.longri.cachebox3.utils.ImageLoader;

/**
 * Created by Longri on 23.04.2019.
 */
public class GalleryItem extends ListViewItem {

    private final ImageLoader iloader;
    private final Image img;
    private final VisLabel lbl;

    public GalleryItem(int index, ImageLoader loader) {
        this(index, loader, null);
    }

    public GalleryItem(int index, ImageLoader loader, String label) {
        super(index);
        iloader = loader;
        CB_RectF imgRec = new CB_RectF();
        img = new Image(iloader, imgRec.ScaleCenter(0.95f), "", false);
        img.setHAlignment(Alignment.CENTER);

        if (label == null) lbl = null;
        else lbl = new VisLabel(label);

    }

    private void fillContent() {
        this.clear();
        this.add(img);
        if (lbl != null) {
            this.row();
            this.add(lbl);
        }
    }

    @Override
    public void layout() {
        super.layout();
    }
}
