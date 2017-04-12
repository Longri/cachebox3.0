
package de.longri.cachebox3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import de.longri.cachebox3.api.CB_Api;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.settings.Config;
import org.robovm.apple.foundation.*;
import org.robovm.apple.uikit.UIViewController;
import org.robovm.apple.uikit.UIWebView;
import org.robovm.apple.uikit.UIWebViewDelegate;
import org.robovm.apple.uikit.UIWebViewNavigationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class demonstrates how to call into JavaScript and how to call Java from JavaScript.<p>
 * To call into Javascript, we use {@link UIWebView#evaluateJavaScript}.<p>
 * To call Java from JavaScript we have to use a trick: In JS we change the {@code window.location} to a custom url scheme.
 * We override {@link UIWebViewDelegate#didFinishLoad(UIWebView)} to intercept the url loading. We parse the incoming url and handle it as we like.
 * We return {@code false} so the web view does not try to load our custom url.
 */
public class GenerateApiKeyWebViewController extends UIViewController implements UIWebViewDelegate {

    private static final Logger log = LoggerFactory.getLogger(GenerateApiKeyWebViewController.class);


    private final UIWebView webView;
    private final GenericCallBack<String> callBack;
    private final UIViewController mainViewController;

    public GenerateApiKeyWebViewController(GenericCallBack<String> callBack, UIViewController mainViewController) {
        webView = new UIWebView(getView().getFrame());
        webView.setDelegate(this);
        getView().addSubview(webView);
        this.callBack = callBack;
        this.mainViewController = mainViewController;

        if (Config.OverrideUrl.getValue().equals("")) {
            CB_Api.getGcAuthUrl(new GenericCallBack<String>() {
                @Override
                public void callBack(final String value) {
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                    log.debug("Show web site at {}", value);
                    NSURL url = new NSURL(value);
                    NSURLRequest request = new NSURLRequest(url);
                    webView.loadRequest(request);
                }
            });
        } else {
            String GC_AuthUrl = Config.OverrideUrl.getValue();
            log.debug("Show override web site at {}", GC_AuthUrl);
            NSURL url = new NSURL(GC_AuthUrl);
            NSURLRequest request = new NSURLRequest(url);
            webView.loadRequest(request);
        }

    }


    /**
     * Override the delegate method to intercept url loading.
     */
    @Override
    public boolean shouldStartLoad(UIWebView webView, NSURLRequest request,
                                   UIWebViewNavigationType navigationType) {
        return true;
    }

    @Override
    public void didStartLoad(UIWebView webView) {
        log.debug("didStartLoad");
    }

    @Override
    public void didFinishLoad(UIWebView webView) {
        String loadedUrl = webView.getRequest().getURL().getAbsoluteString();
        log.debug("DidFinishHtmlLoad {}", loadedUrl);

        if (loadedUrl.startsWith("http://oauth.team-cachebox.de/")
                || loadedUrl.startsWith("http://staging.oauth.team-cachebox.de/")) {
            //parse content of oauth result
            String content = webView.evaluateJavaScript("document.body.textContent");

            String search = "Access token: ";
            int pos = content.indexOf(search) + search.length();
            // between pos und pos2 shut be a valid  AccessToken!!!
            final String accessToken = content.substring(pos).trim();

            log.debug("pos: {}, AccesToken= {}", pos, accessToken);
            this.callBack.callBack(accessToken);
            //reload GlView
            webView.removeFromSuperview();
            ((IOSApplication) Gdx.app).getUIWindow().setRootViewController(this.mainViewController);
            ((IOSApplication) Gdx.app).getUIWindow().makeKeyAndVisible();
        }

    }

    @Override
    public void didFailLoad(UIWebView webView, NSError error) {
        log.debug("didFailLoad");
    }
}