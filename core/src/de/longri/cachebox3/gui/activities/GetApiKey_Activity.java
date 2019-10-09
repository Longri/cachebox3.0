package de.longri.cachebox3.gui.activities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.kotcrab.vis.ui.VisUI;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.apis.CB_Api;
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

public class GetApiKey_Activity extends Activity {

    private final static Logger log = LoggerFactory.getLogger(GetApiKey_Activity.class);

    private final WebView webView = new WebView();
    private final AtomicReference<String> accessToken = new AtomicReference<>();

    public GetApiKey_Activity() {
        //TODO replace icon with own style for GetApiKey_Activity
        super("getApiKey", VisUI.getSkin().get("ApiButton", ApiButtonStyle.class).check);
        defaults().pad(CB.scaledSizes.MARGIN_HALF);

        btnOK.setText(Translation.get("save"));
        btnOK.setDisabled(true);


//        this.setDebug(true, true);
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
        // save api key and close
        log.debug("save ApiKey: {}", accessToken.get());
    }

    @Override
    protected void runAtCancel() {
        super.runAtCancel();
    }

    @Override
    public void onShow() {
        super.onShow();
        if (webView != null) {
            webView.show();

            //set callbacks
            webView.setShouldOverrideUrlLoadingCallBack(new GenericHandleCallBack<String>() {
                @Override
                public boolean callBack(String value) {
                    return false;
                }
            });
            webView.setFinishLoadingCallBack(new GenericHandleCallBack<String>() {
                @Override
                public boolean callBack(String url) {
                    if (url.toLowerCase().contains("oauth_verifier=") && (url.toLowerCase().contains("oauth_token="))) {
                        String html = webView.getContentAsString();
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
                        return true;
                    }
                    return false;
                }
            });


            //browse to GS site to generate API Key
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
            Gdx.app.getApplicationListener().pause();
        }
    }

    @Override
    public void onHide() {
        super.onHide();
        webView.hide();
    }

    @Override
    public void dispose() {
        super.dispose();
        webView.dispose();
    }


//
//    public void callBack(String result) {
//    }

}
