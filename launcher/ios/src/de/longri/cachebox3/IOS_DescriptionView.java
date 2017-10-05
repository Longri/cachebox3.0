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
package de.longri.cachebox3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import de.longri.cachebox3.callbacks.GenericHandleCallBack;
import org.robovm.apple.coregraphics.CGPoint;
import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.foundation.NSError;
import org.robovm.apple.foundation.NSURLRequest;
import org.robovm.apple.uikit.UIViewController;
import org.robovm.apple.uikit.UIWebView;
import org.robovm.apple.uikit.UIWebViewDelegate;
import org.robovm.apple.uikit.UIWebViewNavigationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by longri on 27.04.17.
 */
public class IOS_DescriptionView extends UIViewController implements UIWebViewDelegate, PlatformDescriptionView {

    private static final Logger log = LoggerFactory.getLogger(IOS_DescriptionView.class);


    private final UIWebView webView;
    private final UIViewController mainViewController;
    private GenericHandleCallBack<String> shouldOverrideUrlLoadingCallBack;

    public IOS_DescriptionView(UIViewController mainViewController) {
        webView = new UIWebView(getView().getFrame());
        webView.setDelegate(this);
        getView().addSubview(webView);
        this.mainViewController = mainViewController;
    }

    @Override
    public boolean shouldStartLoad(UIWebView uiWebView, NSURLRequest nsurlRequest, UIWebViewNavigationType uiWebViewNavigationType) {
        String url = nsurlRequest.getURL().getAbsoluteString();
        boolean shouldOverride = shouldOverrideUrlLoadingCallBack.callBack(url);
        log.debug("Should override {} Url: {}", shouldOverride, url);
        return shouldOverride;
    }

    @Override
    public void didStartLoad(UIWebView uiWebView) {

    }

    @Override
    public void didFinishLoad(UIWebView uiWebView) {

    }

    @Override
    public void didFailLoad(UIWebView uiWebView, NSError nsError) {

    }

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
    public void setHtml(String html) {
        log.debug("show html");
        webView.loadHTML(html, null);
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


    public void disposing() {
        webView.setDelegate(null);
        webView.dispose();
        shouldOverrideUrlLoadingCallBack = null;
        super.dispose();
    }


}
