/*
 * Copyright (C) 2016 - 2017 team-cachebox.de
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

import de.longri.cachebox3.callbacks.GenericHandleCallBack;

/**
 * Created by Longri on 26.04.2017.
 */
public interface PlatformWebView {

    void setBounding(float x, float y, float width, float height, int screenHeight);

    void setScrollPosition(float x, float y);

    float getScrollPositionX();

    float getScrollPositionY();

    float getScale();

    void setScale(float scale);

    void setHtml(String html);

    void display();

    void close();

    // Give the host application a chance to take control when a URL is about to be loaded in the current WebView
    void setShouldOverrideUrlLoadingCallBack(GenericHandleCallBack<String> shouldOverrideUrlLoadingCallBack);

    // Notify the host application that a page has started loading.
    void setStartLoadingCallBack(GenericHandleCallBack<String> startLoadingCallBack);

    // Notify the host application that a page has finished loading.
    void setFinishLoadingCallBack(GenericHandleCallBack<String> finishLoadingCallBack);

    boolean isPageVisible();

    void loadUrl(String urlString);

    String getContentAsString();

}
