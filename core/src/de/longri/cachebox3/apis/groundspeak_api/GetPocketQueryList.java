/*
 * Copyright (C) 2018 team-cachebox.de
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

import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.apis.groundspeak_api.json_parser.stream_parser.PqListParser;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.utils.ICancel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * Created by Longri on 26.03.2018.
 */
public class GetPocketQueryList extends GetRequest {
    final static Logger log = LoggerFactory.getLogger(GetPocketQueryList.class);

    private final Array<PocketQuery> pqList;
    private final String gcApiKey;

    public GetPocketQueryList(String gcApiKey, ICancel iCancel, Array<PocketQuery> pqList) {
        super(iCancel);
        this.pqList = pqList;
        this.gcApiKey = gcApiKey;
    }

    @Override
    protected void handleResponse(Net.HttpResponse httpResponse, GenericCallBack<ApiResultState> readyCallBack) {
        //parse stream and put PocketQuery to pqList
        // for debug: String resultAsString = httpResponse.getResultAsString();
        InputStream stream = httpResponse.getResultAsStream();
        PqListParser parser = new PqListParser(this.iCancel);
        ApiResultState state = parser.parsePqList(stream, this.pqList);
        readyCallBack.callBack(state);
    }

    @Override
    protected String getCallUrl() {
        return "GetPocketQueryList?AccessToken=" + this.gcApiKey + "&format=json";
    }

}
