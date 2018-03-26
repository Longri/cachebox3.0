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
package de.longri.cachebox3.apis.groundspeak_api;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.settings.Config;
import de.longri.cachebox3.utils.ICancel;
import de.longri.cachebox3.utils.NamedRunnable;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

import static de.longri.cachebox3.apis.groundspeak_api.GroundspeakAPI.waitApiCallLimit;

/**
 * Created by Longri on 14.04.17.
 */
public abstract class GetRequest {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(GetRequest.class);
    final static String GS_LIVE_URL = "https://api.groundspeak.com/LiveV6/geocaching.svc/";
    final static String STAGING_GS_LIVE_URL = "https://staging.api.groundspeak.com/Live/V6Beta/geocaching.svc/";

    protected final ICancel iCancel;
    protected final String gcApiKey;
    protected boolean waitLimit = true;

    public GetRequest(String gcApiKey, ICancel iCancel) {
        if (gcApiKey == null || gcApiKey.isEmpty()) throw new RuntimeException("ApiKey is empty, can't get any result");
        this.gcApiKey = gcApiKey;
        this.iCancel = iCancel;
    }

    protected abstract void handleHttpResponse(Net.HttpResponse httpResponse, GenericCallBack<ApiResultState> readyCallBack);

    public void post(final GenericCallBack<ApiResultState> readyCallBack) {
        post(readyCallBack, this.iCancel);
    }

    protected void post(final GenericCallBack<ApiResultState> readyCallBack, final ICancel iCancel) {
        CB.postAsync(new NamedRunnable("PostRequest") {
            @Override
            public void run() {
                if (waitLimit && waitApiCallLimit(iCancel) == -1) {
                    readyCallBack.callBack(ApiResultState.API_ERROR);
                    return;
                }

                if (iCancel != null && iCancel.cancel()) readyCallBack.callBack(ApiResultState.CANCELED);
                String URL = Config.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;


                final Net.HttpRequest httpPost = new Net.HttpRequest(Net.HttpMethods.GET);
                httpPost.setUrl(URL + getCallUrl());

                final AtomicBoolean checkCancel = new AtomicBoolean(iCancel != null);
                if (checkCancel.get()) {
                    //start cancel listener
                    CB.postAsync(new NamedRunnable("PostRequest cancelListener") {
                        @Override
                        public void run() {
                            while (checkCancel.get()) {
                                if (iCancel.cancel()) {
                                    Gdx.net.cancelHttpRequest(httpPost);
                                    checkCancel.set(false);
                                }

                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }

                log.debug("Send Post request");
                Gdx.net.sendHttpRequest(httpPost, new Net.HttpResponseListener() {
                    @Override
                    public void handleHttpResponse(Net.HttpResponse httpResponse) {
                        log.debug("Handle Response");
                        checkCancel.set(false);
                        GetRequest.this.handleHttpResponse(httpResponse, readyCallBack);
                    }

                    @Override
                    public void failed(Throwable t) {
                        log.error("Request failed", t);
                        checkCancel.set(false);
                        readyCallBack.callBack(ApiResultState.API_ERROR);
                    }

                    @Override
                    public void cancelled() {
                        log.debug("Request cancelled");
                        checkCancel.set(false);
                        readyCallBack.callBack(ApiResultState.CANCELED);
                    }
                });
            }
        });
    }

    protected abstract String getCallUrl();

}
