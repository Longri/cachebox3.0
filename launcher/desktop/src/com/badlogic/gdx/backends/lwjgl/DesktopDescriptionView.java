/*
 * Copyright (C) 2017 team-cachebox.de
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
package com.badlogic.gdx.backends.lwjgl;

import com.badlogic.gdx.Gdx;
import de.longri.cachebox3.PlatformDescriptionView;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.lwjgl.opengl.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Properties;
import java.util.Set;

import static javafx.concurrent.Worker.State.FAILED;


public class DesktopDescriptionView extends Window implements PlatformDescriptionView {

    static {
        //apply -Dprism.order=j2d (set VM options)
        Properties props = System.getProperties();
        props.setProperty("com.sun.prism.order", "j2d");
//TODO that dosn't work
    }

    private static final Logger log = LoggerFactory.getLogger(DesktopDescriptionView.class);


    private final JFXPanel jfxPanel;
    private WebEngine engine;
    WebView webView;
    private float x, y, width, height;
    Thread t;
    boolean cancelThread = false;
    private ScrollBar vScrollbar, hScrollbar;

    public DesktopDescriptionView() {
        super(null); // creates a window with no Frame as owner
        jfxPanel = new JFXPanel();
    }

    private void loopBounds() {

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                DesktopDescriptionView.this.setBounds((int) (Display.getX() + x)
                        , (int) (Display.getY() + 22 + y)
                        , (int) width, (int) height);
                DesktopDescriptionView.this.setAlwaysOnTop(true);
                DesktopDescriptionView.this.setFocusable(true);
                if (!cancelThread) loopBounds();
            }
        });
    }

    private boolean isInitial = false;

    private void iniitialWebView() {

        if (isInitial) return;
        this.add(jfxPanel);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                webView = new WebView();
                engine = webView.getEngine();
                engine.getLoadWorker().stateProperty()
                        .addListener(new ChangeListener<State>() {
                            @Override
                            public void changed(
                                    ObservableValue<? extends State> ov,
                                    State oldState, State newState) {

                            }
                        });
                engine.getLoadWorker().exceptionProperty()
                        .addListener(new ChangeListener<Throwable>() {

                            public void changed(
                                    ObservableValue<? extends Throwable> o,
                                    Throwable old, final Throwable value) {
                                if (engine.getLoadWorker().getState() == FAILED) {

                                }
                            }
                        });

                Scene scene = new Scene(webView);
                jfxPanel.setScene(scene);
            }
        });
        isInitial = true;
    }

    @Override
    public void setHtml(final String html) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                engine.loadContent(html, "text/html");
            }
        });
    }

    @Override
    public void display() {
        setVisible(true);
        t = new Thread(new Runnable() {
            @Override
            public void run() {
                loopBounds();
            }
        });
        t.start();
        iniitialWebView();
        this.setAutoRequestFocus(true);
    }

    @Override
    public void close() {
        setVisible(false);
        cancelThread = true;
        vScrollbar = null;
        hScrollbar = null;
        this.setAutoRequestFocus(false);
    }

    @Override
    public void setBounding(final float x, final float y, final float width, final float height) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                DesktopDescriptionView.this.x = x;
                DesktopDescriptionView.this.y = y;
                DesktopDescriptionView.this.width = width;
                DesktopDescriptionView.this.height = height;
            }
        });

    }

    @Override
    public void setScrollPosition(float x, float y) {
//        if (vScrollbar == null) {
//            vScrollbar = getVScrollBar(webView, Orientation.VERTICAL);
//        }
//
//        if (hScrollbar == null) {
//            hScrollbar = getVScrollBar(webView, Orientation.HORIZONTAL);
//        }
//
//        vScrollbar.setValue(10);
//        hScrollbar.setValue(x);

    }

    @Override
    public float getScrollPositionX() {
//        if (hScrollbar == null) {
//            hScrollbar = getVScrollBar(webView, Orientation.HORIZONTAL);
//        }
//        return (float) hScrollbar.getValue();
        return 0;
    }

    @Override
    public float getScrollPositionY() {
//        if (vScrollbar == null) {
//            vScrollbar = getVScrollBar(webView, Orientation.VERTICAL);
//        }
//        return (float) vScrollbar.getValue();
        return 0;
    }

    /**
     * Returns the vertical scrollbar of the webview.
     *
     * @param webView webview
     * @return vertical scrollbar of the webview or {@code null} if no vertical
     * scrollbar exists
     */
    private ScrollBar getVScrollBar(WebView webView, Orientation orientation) {

        Set<Node> scrolls = webView.lookupAll(".scroll");
        for (Node scrollNode : scrolls) {

            if (ScrollBar.class.isInstance(scrollNode)) {
                ScrollBar scroll = (ScrollBar) scrollNode;
                if (scroll.getOrientation() == orientation) {
                    return scroll;
                }
            }
        }
        return null;
    }
}