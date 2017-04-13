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
import de.longri.cachebox3.apis.cachebox_api.CB_Api;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.settings.Config;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.lwjgl.opengl.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import static javafx.concurrent.Worker.State.FAILED;


public class GenerateApiKeyWebView extends Window {

    static {
        //apply -Dprism.order=j2d (set VM options)
        Properties props = System.getProperties();
        props.setProperty("com.sun.prism.order", "j2d");
//TODO that dosen't work
    }

    private static final Logger log = LoggerFactory.getLogger(GenerateApiKeyWebView.class);


    //    private final UIWebView webView;
    private final GenericCallBack<String> callBack;
    private final JFXPanel jfxPanel;
    private WebEngine engine;
//    private final UIViewController mainViewController;

    public GenerateApiKeyWebView(GenericCallBack<String> callBack) {

        super(null); // creates a window with no Frame as owner

        setVisible(true);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                loopBounds();
            }
        });
        t.start();

        this.callBack = callBack;


        if (Config.OverrideUrl.getValue().equals("")) {
            CB_Api.getGcAuthUrl(new GenericCallBack<String>() {
                @Override
                public void callBack(final String value) {
                    log.debug("Show web site at {}", value);

                }
            });
        } else {
            String GC_AuthUrl = Config.OverrideUrl.getValue();
            log.debug("Show override web site at {}", GC_AuthUrl);

        }

        jfxPanel = new JFXPanel();
        this.add(jfxPanel);

        iniitialWebView();
        this.setAutoRequestFocus(true);

    }

    private boolean cancelBounds = false;

    private void loopBounds() {
        if (cancelBounds) return;
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                GenerateApiKeyWebView.this.setBounds(Display.getX(), Display.getY() + 22, Display.getWidth(), Display.getHeight());
                GenerateApiKeyWebView.this.setAlwaysOnTop(true);
                GenerateApiKeyWebView.this.setFocusable(true);
//                log.debug("loop");
                loopBounds();
            }
        });
    }

    private void callBack(String key) {
        cancelBounds = true;
        this.callBack.callBack(key);
        this.setVisible(false);
    }

    private boolean secondLoad = false;

    private void iniitialWebView() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                WebView view = new WebView();
                engine = view.getEngine();
                engine.getLoadWorker().stateProperty()
                        .addListener(new ChangeListener<State>() {
                            @Override
                            public void changed(
                                    ObservableValue<? extends State> ov,
                                    State oldState, State newState) {

                            }
                        });
                secondLoad = false;
                engine.getLoadWorker().workDoneProperty()
                        .addListener(new ChangeListener<Number>() {
                            @Override
                            public void changed(
                                    ObservableValue<? extends Number> observableValue,
                                    Number oldValue, final Number newValue) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        String loadedUrl = engine.getLocation();
                                        log.debug("DidFinishHtmlLoad {}", loadedUrl);

                                        if (loadedUrl.startsWith("http://oauth.team-cachebox.de/")
                                                || loadedUrl.startsWith("http://staging.oauth.team-cachebox.de/")) {

                                            if (!secondLoad) {
                                                secondLoad = true;
                                                return;
                                            }

                                            Platform.runLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    //parse content of oauth result
                                                    String content = (String) engine.executeScript("document.documentElement.outerHTML");

                                                    String search = "Access token: ";
                                                    String accessToken = "ERROR";
                                                    int pos = content.indexOf(search);
                                                    if (pos > -1) {
                                                        int pos2 = content.indexOf("</span>", pos);
                                                        if (pos2 > pos) {
                                                            // between pos und pos2 is the valid AccessToken!!!
                                                            accessToken = content.substring(pos + search.length(), pos2);
                                                        }
                                                    }
                                                    log.debug("pos: {}, AccesToken= {}", pos, accessToken);
                                                    callBack(accessToken);
                                                }
                                            });
                                        }
                                    }
                                });
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
                // show web site
                if (Config.OverrideUrl.getValue().equals("")) {
                    CB_Api.getGcAuthUrl(new GenericCallBack<String>() {
                        @Override
                        public void callBack(final String value) {
                            log.debug("Show web site at {}", value);
                            loadURL(value);
                        }
                    });
                } else {
                    String GC_AuthUrl = Config.OverrideUrl.getValue();
                    log.debug("Show override web site at {}", GC_AuthUrl);
                    loadURL(GC_AuthUrl);
                }

                Scene scene = new Scene(view);
                jfxPanel.setScene(scene);
            }
        });

    }

    public void loadURL(final String url) {


        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                String tmp = toURL(url);

                if (tmp == null) {
                    tmp = toURL("http://" + url);
                }
                engine.load(tmp);


//                engine.loadContent("<html>hello, world</html>", "text/html");

            }
        });

    }

    private static String toURL(String str) {
        try {
            return new URL(str).toExternalForm();
        } catch (MalformedURLException exception) {
            return null;
        }
    }

}