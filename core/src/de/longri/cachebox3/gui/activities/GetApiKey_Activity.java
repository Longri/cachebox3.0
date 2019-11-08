package de.longri.cachebox3.gui.activities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.apis.CB_Api;
import de.longri.cachebox3.apis.GroundspeakAPI;
import de.longri.cachebox3.callbacks.GenericHandleCallBack;
import de.longri.cachebox3.gui.Activity;
import de.longri.cachebox3.gui.skin.styles.ApiButtonStyle;
import de.longri.cachebox3.gui.widgets.WebView;
import de.longri.cachebox3.locator.AtomicMutableCoordinate;
import de.longri.cachebox3.locator.Coordinate;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.settings.Settings;
import de.longri.cachebox3.translation.Translation;
import de.longri.cachebox3.utils.NamedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

import static de.longri.cachebox3.settings.Settings.GcLogin;

public class GetApiKey_Activity extends Activity {

    private final static Logger log = LoggerFactory.getLogger(GetApiKey_Activity.class);

    private final BlockUiProgress_Activity BLOCK_UI = new BlockUiProgress_Activity("Loading....");
    private final WebView webView = new WebView();
    private final AtomicReference<String> accessToken = new AtomicReference<>();

    public GetApiKey_Activity() {
        //TODO replace icon with own style for GetApiKey_Activity
        super("getApiKey", VisUI.getSkin().get("ApiButton", ApiButtonStyle.class).check);
        defaults().pad(CB.scaledSizes.MARGIN_HALF);

        btnOK.setText(Translation.get("save"));
        btnOK.setDisabled(true);


//        this.setDebug(true, true);
        log.debug("constructor call ready");
    }

    @Override
    protected void createMainContent() {
        CB.postOnNextGlThread(new Runnable() {
            @Override
            public void run() {
                mainContent.invalidateHierarchy();
                mainContent.layout();
            }
        });
        mainContent.add(webView);
        mainContent.setDebug(true, true);
    }

    @Override
    public void layout() {
        super.layout();
        float calculatedContentHeight = this.getHeight();
        float calculatedContentWidth = this.getWidth();
        calculatedContentWidth -= mainContent.defaults().getPadLeft() * 6;
        calculatedContentHeight -= Math.max(imgTitle.getHeight(), lblTitle.getHeight());
        calculatedContentHeight -= btnCancel.getHeight();
        calculatedContentHeight -= mainContent.defaults().getPadTop() * 14;
        webView.setSize(calculatedContentWidth, calculatedContentHeight);
        mainContent.invalidateHierarchy();
        mainContent.layout();
    }

    /**
     * Draws this actor's debug lines if {@link #getDebug()} is true and, regardless of {@link #getDebug()}, calls
     * {@link Actor#drawDebug(ShapeRenderer)} on each child.
     */
    public void drawDebug(ShapeRenderer shapes) {
        super.drawDebug(shapes);
    }


    @Override
    protected void runAtOk() {
        log.debug("runAtOk()");
        // save api key and close
        log.debug("save ApiKey: {}", accessToken.get());

        if (Config.AccessToken == null) {
            log.warn("Config.AccessToken are NULL!");

            //try to initial
            log.debug("try to initial: {}", Settings.AccessToken == null ? "are null" : "is initial");
        }


        if (Config.AccessToken == null) {
            log.error("Config.AccessToken are NULL again! Can't store API key");
            return;
        }

        // store the encrypted AccessToken in the Config file
        if (Config.UseTestUrl.getValue()) {
            Config.AccessTokenForTest.setEncryptedValue(accessToken.get());
        } else {
            Config.AccessToken.setEncryptedValue(accessToken.get());
        }
        Config.AcceptChanges();


        //Config is storing values.
        // wait for finishing
        CB.postAsyncDelayd(200, new NamedRunnable("wait for Store API Key") {
            @Override
            public void run() {
                boolean cancelWait = false;
                int waitCount = 0;
                log.debug("wait for store API key");
                while (!Config.ifInWrite() && !cancelWait) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (waitCount++ > 100) cancelWait = true;
                }

                String userNameOfAuthorization = null;
                try {
                    log.debug("set Authorisation and get UserName");
                    GroundspeakAPI.getInstance().setAuthorization();
                    userNameOfAuthorization = GroundspeakAPI.getInstance().fetchMyUserInfos().username;
                    log.debug("fetched user name: {}", userNameOfAuthorization);
                } catch (Exception e) {
                    log.error("Set Authorisation", e);
                }
                GcLogin.setValue(userNameOfAuthorization);
                Config.AcceptChanges();
                finish();
            }
        });
    }

    @Override
    protected void runAtCancel() {
        log.debug("runAtCancel()");
        super.runAtCancel();
    }

    @Override
    public void onShow() {
        log.debug("onShow()");
        super.onShow();
        if (webView != null) {
            webView.show();

            //set callbacks
            webView.setShouldOverrideUrlLoadingCallBack(new GenericHandleCallBack<String>() {
                @Override
                public boolean callBack(String value) {
                    // show progress dialog for block UI
                    // until site is loaded
                    BLOCK_UI.show();
                    webView.hide();
                    return false;
                }
            });
            webView.setStartLoadingCallBack(new GenericHandleCallBack<String>() {
                @Override
                public boolean callBack(String url) {
                    return true;
                }
            });
            webView.setFinishLoadingCallBack(new GenericHandleCallBack<String>() {
                @Override
                public boolean callBack(String url) {
                    BLOCK_UI.finish();
                    log.debug("FinishLoadingCallBack on URL: {}", url);
                    webView.show();
                    if (url.toLowerCase().contains("oauth_verifier=") && (url.toLowerCase().contains("oauth_token="))) {
                        CB.postAsync(new NamedRunnable("get HTML content") {
                            @Override
                            public void run() {
                                String html = webView.getContentAsString();
                                if (html == null || html.isEmpty()) {
                                    log.warn("Html string are NULL or empty. Can't extract key");
                                    return;
                                }
                                String search = "Access token: ";
                                int pos = html.indexOf(search);
                                if (pos < 0) {
                                    log.warn("can't found 'Access token: ' on HTML string;=>");
                                    log.warn(html);
                                    return;
                                }

                                int pos2 = html.indexOf("</span>", pos);
                                if (pos2 < pos) {
                                    log.warn("can't found '</span>' on HTML string;=>");
                                    log.warn(html);
                                    return;
                                }
                                // between pos and pos2 must a valid AccessToken!!!
                                String fondApiKey = html.substring(pos + search.length(), pos2);
                                accessToken.set(fondApiKey);
                                log.debug("found API Key: {} Enable save button! ({})", accessToken.get(), fondApiKey);
                                btnOK.setDisabled(false);
                                CB.requestRendering();
                                CB.postOnNextGlThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        CB.requestRendering();
                                    }
                                });
                            }
                        });
                        return true;
                    }
                    return false;
                }
            });

            CB.postOnMainThread(new NamedRunnable("browse to GS site to generate API Key,on main thread") {
                @Override
                public void run() {
                    if (Config.OverrideUrl.getValue().equals("")) {
                        webView.loadUrl(CB_Api.getGcAuthUrl());
                    } else {
                        CB.postOnMainThread(new NamedRunnable("load gc url") {
                            @Override
                            public void run() {
                                String GC_AuthUrl = Config.OverrideUrl.getValue();
                                if (GC_AuthUrl.equals("")) {
                                    finish();
                                }
                                webView.loadUrl(GC_AuthUrl);
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    public void onHide() {
        log.debug("hide()");
        super.onHide();
        webView.hide();
    }

    @Override
    public void dispose() {
        log.debug("dispose()");
        super.dispose();
        webView.dispose();
    }
}
