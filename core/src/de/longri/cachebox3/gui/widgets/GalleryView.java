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
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DoubleClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.TimeUtils;
import com.kotcrab.vis.ui.widget.VisTextButton;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.PlatformConnector;
import de.longri.cachebox3.PlatformDescriptionView;
import de.longri.cachebox3.Utils;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.gui.skin.styles.GalleryViewStyle;
import de.longri.cachebox3.gui.widgets.catch_exception_widgets.Catch_Table;
import de.longri.cachebox3.gui.widgets.list_view.*;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.types.ImageEntry;
import de.longri.cachebox3.utils.ImageLoader;
import de.longri.cachebox3.utils.NamedRunnable;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Longri on 23.04.2019.
 */
public class GalleryView extends Catch_Table {

    private final static int MAX_THUMB_WIDTH = 500;
    private final static int MAX_OVERVIEW_THUMB_WIDTH = 240;
    private final static String HTML_1 = "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "  <meta charset=\"utf-8\">" +
            "</head>" +
            "<body>" +
            "<p>" +
            "  <img src=\"file:///";
    private final static String HTML_2 = "\">" +
            "</p>" +
            "</body>" +
            "</html>";

    private final GalleryViewStyle style;
    private final GalleryListView overview;
    private final GalleryListView gallery;
    private final DefaultListViewAdapter overViewAdapter = new DefaultListViewAdapter();
    private final DefaultListViewAdapter galleryAdapter = new DefaultListViewAdapter();

    private PlatformDescriptionView webView;
    VisTextButton btnCloseZoomView;
    private boolean webViewVisible = false;

    public GalleryView(GalleryViewStyle style) {
        if (style == null) throw new RuntimeException("style can't be NULL");
        this.style = style;
        overview = new GalleryListView(style.overviewListStyle);
        gallery = new GalleryListView(style.galleryListStyle) {
            @Override
            public void snapIn() {
                super.snapIn();
                // get index of snap item and select at Overview
                ListViewItemInterface visibleItem = getVisibleItem();
                if (visibleItem == null) return;
                int index = visibleItem.getListIndex();
                overview.setSelection(index);
                CB.postAsyncDelayd(300, new NamedRunnable("setSelectedItemVisible") {
                    @Override
                    public void run() {
                        overview.setSelectedItemVisible(true);
                    }
                });
            }
        };


        DoubleClickListener doubleClickListener = new DoubleClickListener() {

            private float touchDownX;
            private float touchDownY;

            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                touchDownX = x;
                touchDownY = y;
                return super.touchDown(event, x, y, pointer, button);
            }

            public void doubleClicked(InputEvent event, float x, float y) {
                // show a web webView with html to showing this Image
                showZoomingWebView();
            }


            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);
                //drag image if Zoomed
                gallery.zoomedDrag(x - touchDownX, y - touchDownY);
                touchDownX = x;
                touchDownY = y;
            }
        };

        gallery.addInputListener(new InputListener() {
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
//                getStage().setScrollFocus(gallery);
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor fromActor) {
//                getStage().setScrollFocus(null);
            }

            public boolean scrolled(InputEvent event, float x, float y, int amount) {
                //block scrolling of gallery items and set zoom to act visible item
                gallery.scroll(x, y, amount);
                return true;
            }

            /** Called when a mouse button or a finger touch is moved anywhere, but only if touchDown previously returned true for the
             * mouse button or touch. The touchDragged event is always {@link Event#handle() handled}.
             * @see InputEvent */
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
//                gallery.zoomedDrag(x, y);
            }
        });


        gallery.addCaptureListener(doubleClickListener);
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
        setLayout(false);

        this.setDebug(true);
    }

    private void setLayout(boolean webViewVisible) {
        this.clear();

        if (webViewVisible) {
            btnCloseZoomView = new VisTextButton(Translation.get("close").toString());
            btnCloseZoomView.addCaptureListener(new ClickListener() {
                public void clicked(InputEvent event, float x, float y) {
                    setLayout(false);
                    btnCloseZoomView = null;
                    webView.close();
                    gallery.snapIn();
                }
            });
            this.add().fill().expand();
            this.add(btnCloseZoomView).align(Align.topRight);
            this.invalidate();
            this.layout();
        } else {
            this.add(gallery).expandX().fillX().height(new Value.Fixed(Gdx.graphics.getWidth()));
            this.row();
            this.add(overview).expandX().fillX().fillY();
        }
    }

    private void showZoomingWebView() {

        ListViewItemInterface actView = gallery.getVisibleItem();

        if (actView == null) return;

        final AtomicBoolean WAIT = new AtomicBoolean(false);
        setLayout(true);
        if (webView == null) {
            WAIT.set(true);
            PlatformConnector.getDescriptionView(new GenericCallBack<PlatformDescriptionView>() {
                @Override
                public void callBack(PlatformDescriptionView descriptionView) {
                    webView = descriptionView;
                    WAIT.set(false);
                }
            });
        }

        CB.wait(WAIT);

        webView.display();
        String imagePath = ((GalleryItem) actView).getImagePath();

        String html = HTML_1 + imagePath + HTML_2;
        html = html.replace("\\", "/");
        webView.setHtml(html);
        webViewVisible = true;
        setWebViewBounds();
    }

    public void onShow() {
        if (webViewVisible) {
            setWebViewBounds();
            webView.display();
        }
    }

    private void setWebViewBounds() {
        Actor parent = this.getParent();
        //calculate height to show webViewClose button
        float height = this.getHeight() - btnCloseZoomView.getHeight();
        webView.setBounding(parent.getX() + this.getX(), parent.getY() + this.getX(), this.getWidth(), height, Gdx.graphics.getHeight());
    }

    public void onHide() {
        if (webViewVisible) {
            webView.close();
        }
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

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        if (webView != null) {
            setWebViewBounds();
        }
    }

    public void galleryChanged() {
        overview.setAdapter(overViewAdapter);
        gallery.setAdapter(galleryAdapter);
    }
}
