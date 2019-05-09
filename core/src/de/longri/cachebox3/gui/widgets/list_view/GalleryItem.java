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

import com.badlogic.gdx.utils.StringBuilder;
import com.kotcrab.vis.ui.building.utilities.Alignment;
import de.longri.cachebox3.gui.widgets.Image;
import de.longri.cachebox3.types.ImageEntry;
import de.longri.cachebox3.utils.CB_RectF;
import de.longri.cachebox3.utils.ImageLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 23.04.2019.
 */
public class GalleryItem extends ListViewItem {

    private static final Logger log = LoggerFactory.getLogger(GalleryItem.class);

    private final ImageLoader iloader;
    private final Image img;


    public GalleryItem(int index, ImageLoader loader) {
        super(index);
        iloader = loader;
        img = new Image(iloader, "", false);
        img.setHAlignment(Alignment.CENTER);
        this.add(img);
    }


    @Override
    public void layout() {
        super.layout();

        if (this.getWidth() > 0) {
            iloader.setResizeListener((width, height) -> {
                log.debug("GalleryItem: resized");
                GalleryItem.this.invalidateHierarchy();
                GalleryItem.this.layout();
            }, this.getWidth());
        }

        CB_RectF rec = new CB_RectF(0, 0, getWidth(), getHeight()).scaleCenter(0.95f);
        img.setBounds(rec.getX(), rec.getY(), rec.getWidth(), rec.getHeight());
    }

    @Override
    public float getPrefHeight() {
        return this.hasParent() ? ((GalleryListView) this.getParent()).getPrefHeight() : super.getPrefHeight();
    }

    @Override
    public float getPrefWidth() {
        return this.hasParent() ? ((GalleryListView) this.getParent()).getPrefHeight() : super.getPrefHeight();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Item: ");
        sb.append(Integer.toString(this.index));
        sb.append(" x: ").append(Float.toString(this.getX()));
        sb.append(" w: ").append(Float.toString(this.getWidth()));
        sb.append(" v: ").append(Boolean.toString(this.isVisible()));
        return sb.toString();
    }

    public String getImagePath() {
        return this.iloader.getOriginalImagePath();
    }
}
