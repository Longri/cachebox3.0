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
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.settings.Config;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Created by Longri on 14.04.17.
 */
public abstract class PostRequest {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(PostRequest.class);
    final static String GS_LIVE_URL = "https://api.groundspeak.com/LiveV6/geocaching.svc/";
    final static String STAGING_GS_LIVE_URL = "https://staging.api.groundspeak.com/Live/V6Beta/geocaching.svc/";

    public final static int NO_ERROR = 0;
    public final static int ERROR = -1;
    public final static int CANCELED = -2;

    protected final String gcApiKey;

    public PostRequest(String gcApiKey) {
        if (gcApiKey == null || gcApiKey.isEmpty()) throw new RuntimeException("ApiKey is empty, can't get any result");
        this.gcApiKey = gcApiKey;
    }

    protected abstract void handleHttpResponse(Net.HttpResponse httpResponse, GenericCallBack<Integer> readyCallBack);

    protected void post(final GenericCallBack<Integer> readyCallBack) {
        CB.postAsync(new Runnable() {
            @Override
            public void run() {
                String URL = Config.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;

                StringWriter writer = new StringWriter();
                Json json = new Json(JsonWriter.OutputType.json);
                json.setWriter(writer);

                json.writeObjectStart();
                getRequest(json);
                json.writeObjectEnd();

                Net.HttpRequest httpPost = new Net.HttpRequest(Net.HttpMethods.POST);
                httpPost.setUrl(URL + getCallUrl());
                httpPost.setHeader("format", "json");
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");

                httpPost.setContent(writer.toString());
                httpPost.setIncludeCredentials(true);

                log.debug("Send Post request");
                Gdx.net.sendHttpRequest(httpPost, new Net.HttpResponseListener() {
                    @Override
                    public void handleHttpResponse(Net.HttpResponse httpResponse) {
                        log.debug("Handle Response");
                        PostRequest.this.handleHttpResponse(httpResponse, readyCallBack);
                    }

                    @Override
                    public void failed(Throwable t) {
                        log.error("Request failed", t);
                        readyCallBack.callBack(ERROR);
                    }

                    @Override
                    public void cancelled() {
                        log.debug("Request cancelled");
                        readyCallBack.callBack(CANCELED);
                    }
                });
            }
        });
    }

    protected abstract String getCallUrl();

    protected abstract void getRequest(Json json);

}
