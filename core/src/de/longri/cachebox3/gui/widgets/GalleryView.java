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
package de.longri.cachebox3.gui.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.kotcrab.vis.ui.util.value.PrefHeightIfVisibleValue;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.Utils;
import de.longri.cachebox3.gui.skin.styles.GalleryViewStyle;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Table;
import de.longri.cachebox3.gui.widgets.list_view.*;
import de.longri.cachebox3.types.ImageEntry;
import de.longri.cachebox3.utils.ImageLoader;
import de.longri.cachebox3.utils.NamedRunnable;

/**
 * Created by Longri on 23.04.2019.
 */
public class GalleryView extends Catch_Table {

    private final static int MAX_THUMB_WIDTH = 500;
    private final static int MAX_OVERVIEW_THUMB_WIDTH = 240;

    private final GalleryViewStyle style;
    private final GalleryListView overview;
    private final GalleryListView gallery;
    private final DefaultListViewAdapter overViewAdapter = new DefaultListViewAdapter();
    private final DefaultListViewAdapter galleryAdapter = new DefaultListViewAdapter();


    public GalleryView(GalleryViewStyle style) {
        if (style == null) throw new RuntimeException("style can't be NULL");
        this.style = style;
        overview = new GalleryListView(style.overviewListStyle);
        gallery = new GalleryListView(style.galleryListStyle) {
            @Override
            protected void snapIn() {
                super.snapIn();
                // get index of snap item and select at Overview
                int index = getVisibleItem().getListIndex();
                overview.setSelection(index);
                CB.postAsyncDelayd(300, new NamedRunnable("setSelectedItemVisible") {
                    @Override
                    public void run() {
                        overview.setSelectedItemVisible(true);
                    }
                });
            }
        };

        gallery.setSelectable(SelectableType.NONE);
        overview.setSelectable(SelectableType.SINGLE);

        overview.addSelectionChangedEventListner(new SelectionChangedEvent() {
            @Override
            public void selectionChanged() {
                final int idx = overview.getSelectedItem().getListIndex();
                CB.postAsync(new NamedRunnable("scroll to selected item") {
                    @Override
                    public void run() {
                        ListViewItemInterface item = gallery.getListItem(idx);
                        if (item != null) {
                            gallery.setScrollPos(item.getX(), true);
                        }

                    }
                });
            }
        });

        this.add(gallery).expandX().fillX().height(new Value.Fixed(Gdx.graphics.getWidth()));
        this.row();
        this.add(overview).expandX().fillX().fillY();

        this.setDebug(true);
    }

    public void clearGallery() {
        overViewAdapter.clear();
        galleryAdapter.clear();
    }

    public void addItem(ImageEntry imageEntry, String label) {
        int index = overViewAdapter.size;

        ImageLoader loader = new ImageLoader(true); // image loader with thumb
        loader.setThumbWidth(MAX_THUMB_WIDTH, "");
        loader.setImage(imageEntry.LocalPath);

        GalleryItem item = new GalleryItem(index, loader);
//        item.setOnDoubleClickListener(onGalleryItemDoubleClicked);
        galleryAdapter.add(item);

        ImageLoader overviewloader = new ImageLoader(true); // image loader with thumb
        overviewloader.setThumbWidth(MAX_OVERVIEW_THUMB_WIDTH, Utils.THUMB_OVERVIEW);
        overviewloader.setImage(imageEntry.LocalPath);
        GalleryItem overviewItem = new GalleryItem(index, overviewloader);
//        overviewItem.setOnClickListener(onIconClicked);

//        overview.addActor(overviewItem);
        overViewAdapter.add(overviewItem);
    }

    //    @Override
//    protected void sizeChanged() {
//        super.sizeChanged();
//        overview.setSize(this.getWidth(), this.getHeight());
//        gallery.setSize(this.getWidth(), this.getHeight());
//    }
//
    public void galleryChanged() {
        overview.setAdapter(overViewAdapter);
        gallery.setAdapter(galleryAdapter);

//        CB.postOnMainThreadDelayed(10000, new NamedRunnable("tets") {
//            @Override
//            public void run() {
//                GalleryView.this.layout();
//            }
//        });
    }
}
