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
import com.badlogic.gdx.backends.lwjgl3.CB_Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import de.longri.cachebox3.PlatformDescriptionView;
import de.longri.cachebox3.callbacks.GenericHandleCallBack;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
    private GenericHandleCallBack<String> shouldOverrideUrlLoadingCallBack;

    public DesktopDescriptionView() {
        super(null); // creates a window with no Frame as owner
        jfxPanel = new JFXPanel();
    }

    private void loopBounds() {

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {

                int yPos = (int) (Gdx.graphics.getHeight() - height);

                Lwjgl3Window window = ((CB_Lwjgl3Application) Gdx.app).currentWindow;

                DesktopDescriptionView.this.setBounds((int) (window.getPositionX() + x)
                        , (int) (window.getPositionY() + yPos - (y - 20))
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
                // this change listener will trigger when our secondary popupHandlerEngine starts to load the url ...
                engine.locationProperty().addListener(new ChangeListener<String>() {
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String location) {
                        if (!location.isEmpty()) {
                            if (shouldOverrideUrlLoadingCallBack.callBack(location)) {
                                Platform.runLater(new Runnable() {
                                    public void run() {
                                        engine.getLoadWorker().cancel();
                                        // engine.loadContent(lastValue); // stop loading and unload the url
                                        // -> does this internally: popupHandlerEngine.getLoadWorker().cancelAndReset();
                                    }
                                });
                            }
                        }
                    }

                });

                Scene scene = new Scene(webView);
                jfxPanel.setScene(scene);
            }
        });
        isInitial = true;
    }


    String lastValue;

    @Override
    public void setHtml(final String html) {
        lastValue = html;
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
    public void setShouldOverrideUrlLoadingCallBack(GenericHandleCallBack<String> shouldOverrideUrlLoadingCallBack) {
        this.shouldOverrideUrlLoadingCallBack = shouldOverrideUrlLoadingCallBack;
    }

    @Override
    public void setFinishLoadingCallBack(GenericHandleCallBack<String> finishLoadingCallBack) {
        //todo call this CallBack if Html is finish loaded
    }

    @Override
    public boolean isPageVisible() {
        return true;
    }

    @Override
    public void setBounding(final float x, final float y, final float width, final float height, final int screenHeight) {
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
    public void setScrollPosition(final float x, final float y) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                webView.getEngine().executeScript("window.scrollTo(" + x + ", " + y + ")");
            }
        });
    }

    @Override
    public float getScrollPositionX() {
        final AtomicBoolean wait = new AtomicBoolean(true);
        final AtomicInteger value = new AtomicInteger(0);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                value.set((int) webView.getEngine().executeScript("document.body.scrollLeft"));
                wait.set(false);
            }
        });

        while (wait.get()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return (float) value.get();
    }

    @Override
    public float getScrollPositionY() {
        final AtomicBoolean wait = new AtomicBoolean(true);
        final AtomicInteger value = new AtomicInteger(0);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                value.set((int) webView.getEngine().executeScript("document.body.scrollTop"));
                wait.set(false);
            }
        });

        while (wait.get()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return (float) value.get();
    }

    @Override
    public float getScale() {
        return 0;
    }

    @Override
    public void setScale(float scale) {

    }

//    /**
//     * Returns the vertical scrollbar of the webview.
//     *
//     * @param webView webview
//     * @return vertical scrollbar of the webview or {@code null} if no vertical
//     * scrollbar exists
//     */
//    private ScrollBar getVScrollBar(WebView webView, Orientation orientation) {
//
//        Set<Node> scrolls = webView.lookupAll(".scroll");
//        for (Node scrollNode : scrolls) {
//
//            if (ScrollBar.class.isInstance(scrollNode)) {
//                ScrollBar scroll = (ScrollBar) scrollNode;
//                if (scroll.getOrientation() == orientation) {
//                    return scroll;
//                }
//            }
//        }
//        return null;
//    }
}