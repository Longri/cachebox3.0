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

        // store the encrypted AccessToken in the Config file
        if (Config.UseTestUrl.getValue()) {
            Config.AccessTokenForTest.setEncryptedValue(accessToken.get());
        } else {
            Config.AccessToken.setEncryptedValue(accessToken.get());
        }
        GroundspeakAPI.getInstance().setAuthorization();
        String userNameOfAuthorization = GroundspeakAPI.getInstance().fetchMyUserInfos().username;
        GcLogin.setValue(userNameOfAuthorization);
        Config.AcceptChanges();
        finish();
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
                    webView.show();
                    if (url.toLowerCase().contains("oauth_verifier=") && (url.toLowerCase().contains("oauth_token="))) {
                        String html = webView.getContentAsString();
                        if (html == null || html.isEmpty()) return false;
                        String search = "Access token: ";
                        int pos = html.indexOf(search);
                        if (pos < 0)
                            return false;
                        int pos2 = html.indexOf("</span>", pos);
                        if (pos2 < pos)
                            return false;
                        // zwischen pos und pos2 sollte ein gültiges AccessToken sein!!!
                        accessToken.set(html.substring(pos + search.length(), pos2));
                        log.debug("found API Key: {} Enable save button! ", accessToken.get());
                        btnOK.setDisabled(false);
                        CB.requestRendering();
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
