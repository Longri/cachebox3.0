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
package de.longri.cachebox3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import de.longri.cachebox3.callbacks.GenericHandleCallBack;
import de.longri.cachebox3.utils.NamedRunnable;
import org.robovm.apple.coregraphics.CGPoint;
import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.foundation.NSError;
import org.robovm.apple.foundation.NSURLAuthenticationChallenge;
import org.robovm.apple.foundation.NSURLCredential;
import org.robovm.apple.foundation.NSURLSessionAuthChallengeDisposition;
import org.robovm.apple.uikit.UIViewController;
import org.robovm.apple.webkit.*;
import org.robovm.objc.block.VoidBlock1;
import org.robovm.objc.block.VoidBlock2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by longri on 27.04.17.
 */
public class IOS_DescriptionView extends UIViewController implements PlatformDescriptionView, WKNavigationDelegate {

    private static final Logger log = LoggerFactory.getLogger(IOS_DescriptionView.class);


    private final WKWebView webView;
    private final UIViewController mainViewController;
    private GenericHandleCallBack<String> shouldOverrideUrlLoadingCallBack;
    private GenericHandleCallBack<String> finishLoadingCallBack;

    public IOS_DescriptionView(UIViewController mainViewController) {
        webView = new WKWebView(getView().getFrame());
        webView.setNavigationDelegate(this);
        getView().addSubview(webView);
        this.mainViewController = mainViewController;
    }

//    @Override
//    public boolean shouldStartLoad(UIWebView uiWebView, NSURLRequest nsurlRequest, UIWebViewNavigationType uiWebViewNavigationType) {
//        String url = nsurlRequest.getURL().getAbsoluteString();
//        boolean shouldOverride = !shouldOverrideUrlLoadingCallBack.callBack(url);
//        log.debug("Should override {} Url: {}", shouldOverride, url);
//        return shouldOverride;
//    }
//
//    @Override
//    public void didStartLoad(UIWebView uiWebView) {
//
//    }
//
//    @Override
//    public void didFinishLoad(UIWebView uiWebView) {
//
//    }
//
//    @Override
//    public void didFailLoad(UIWebView uiWebView, NSError nsError) {
//
//    }

    @Override
    public void setBounding(float x, float y, float width, float height, int screenHeight) {

        log.debug("SetBounds x,y width,height {},{} {},{}", x, y, width, height);

        CGRect rect = new CGRect(x, y, width / 2, height / 2);
//        webView.setAccessibilityFrame(rect);
        webView.setBounds(rect);

        float versatz = (screenHeight - height) / 2 - y;

        CGPoint point = new CGPoint(width / 4, (screenHeight / 4) + (versatz / 2));
        webView.setCenter(point);
    }

    @Override
    public void setScrollPosition(float x, float y) {
        CGPoint point = new CGPoint(x, y);
        webView.getScrollView().setContentOffset(point);
    }

    @Override
    public float getScrollPositionX() {
        return (float) webView.getScrollView().getContentOffset().getX();
    }

    @Override
    public float getScrollPositionY() {
        return (float) webView.getScrollView().getContentOffset().getY();
    }

    @Override
    public float getScale() {
        return (float)webView.getScrollView().getZoomScale();
    }

    @Override
    public void setScale(float scale) {
        CB.postAsyncDelayd(3000, new NamedRunnable("") {
            @Override
            public void run() {
                webView.getScrollView().setZoomScale(scale,false);
            }
        });
    }

    @Override
    public void setHtml(String html) {
        log.debug("setHtml");
        html = "<meta name=\"viewport\" content=\"initial-scale=1.0\" />" + html;
        webView.loadHTMLString(html, null);
    }

    @Override
    public void display() {
        CGRect rect = new CGRect(20, 20, 200, 200);
        webView.setAccessibilityFrame(rect);
        mainViewController.addChildViewController(this);
        webView.setBounds(rect);
        ((IOSApplication) Gdx.app).getUIWindow().addSubview(webView);
    }

    @Override
    public void close() {
        webView.removeFromSuperview();
    }

    @Override
    public void setShouldOverrideUrlLoadingCallBack(GenericHandleCallBack<String> shouldOverrideUrlLoadingCallBack) {
        this.shouldOverrideUrlLoadingCallBack = shouldOverrideUrlLoadingCallBack;
    }

    @Override
    public void setFinishLoadingCallBack(GenericHandleCallBack<String> finishLoadingCallBack) {
        this.finishLoadingCallBack = finishLoadingCallBack;
    }

    @Override
    public boolean isPageVisible() {
        return true;
    }


    public void disposing() {
        webView.setNavigationDelegate(null);
        webView.dispose();
        shouldOverrideUrlLoadingCallBack = null;
        super.dispose();
    }


    @Override
    public void decidePolicyForNavigationAction(WKWebView wkWebView, WKNavigationAction wkNavigationAction, VoidBlock1<WKNavigationActionPolicy> voidBlock1) {
        log.debug("decidePolicyForNavigationAction");
        voidBlock1.invoke(WKNavigationActionPolicy.Allow);
    }

    @Override
    public void decidePolicyForNavigationResponse(WKWebView wkWebView, WKNavigationResponse wkNavigationResponse, VoidBlock1<WKNavigationResponsePolicy> voidBlock1) {
        log.debug("decidePolicyForNavigationResponse");
        voidBlock1.invoke(WKNavigationResponsePolicy.Allow);
    }

    @Override
    public void didStartProvisionalNavigation(WKWebView wkWebView, WKNavigation wkNavigation) {
        log.debug("didStartProvisionalNavigation");
        wkWebView.getBounds();
    }

    @Override
    public void didReceiveServerRedirectForProvisionalNavigation(WKWebView wkWebView, WKNavigation wkNavigation) {
        log.debug("didReceiveServerRedirectForProvisionalNavigation");

        wkWebView.getBounds();
    }

    @Override
    public void didFailProvisionalNavigation(WKWebView wkWebView, WKNavigation wkNavigation, NSError nsError) {
        log.debug("didFailProvisionalNavigation");
        wkWebView.getBounds();
    }

    @Override
    public void didCommitNavigation(WKWebView wkWebView, WKNavigation wkNavigation) {
        log.debug("didCommitNavigation");
        wkWebView.getBounds();
    }

    @Override
    public void didFinishNavigation(WKWebView wkWebView, WKNavigation wkNavigation) {
        log.debug("didFinishNavigation");
        this.finishLoadingCallBack.callBack(wkNavigation.description());
    }

    @Override
    public void didFailNavigation(WKWebView wkWebView, WKNavigation wkNavigation, NSError nsError) {
        log.debug("didFailNavigation");
        wkWebView.getBounds();
    }

    @Override
    public void didReceiveAuthenticationChallenge(WKWebView wkWebView, NSURLAuthenticationChallenge nsurlAuthenticationChallenge, VoidBlock2<NSURLSessionAuthChallengeDisposition, NSURLCredential> voidBlock2) {
        log.debug("didReceiveAuthenticationChallenge");
        voidBlock2.invoke(NSURLSessionAuthChallengeDisposition.UseCredential, null);
    }

    @Override
    public void webContentProcessDidTerminate(WKWebView wkWebView) {
        log.debug("webContentProcessDidTerminate");
        wkWebView.getBounds();
    }
}
