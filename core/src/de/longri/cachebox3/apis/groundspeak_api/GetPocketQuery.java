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
import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.apis.groundspeak_api.json_parser.stream_parser.GetPqParser;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.utils.ICancel;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Longri on 28.03.2018.
 */
public class GetPocketQuery extends GetRequest {

    private final String gcApiKey;
    private final String guid;
    private final FileHandle localFile;
    private final PocketQuery.IncrementProgressBytesListener listener;

    GetPocketQuery(String gcApiKey, String guid, FileHandle localFile,
                   PocketQuery.IncrementProgressBytesListener listener, ICancel iCancel) {
        super(iCancel);
        this.gcApiKey = gcApiKey;
        this.guid = guid;
        this.localFile = localFile;
        this.listener = listener;
    }

    @Override
    protected void handleResponse(Net.HttpResponse httpResponse, GenericCallBack<ApiResultState> readyCallBack) {

        // for debug: String resultAsString = httpResponse.getResultAsString();
        InputStream stream = httpResponse.getResultAsStream();
        GetPqParser parser = new GetPqParser(this.iCancel);
        ApiResultState state = null;
        try {
            state = parser.parse(stream, this.localFile, this.listener);
        } catch (IOException e) {
            e.printStackTrace();
        }
        readyCallBack.callBack(state);
    }

    @Override
    protected String getCallUrl() {
        return "GetPocketQueryZippedFile?accessToken="
                + this.gcApiKey
                + "&pocketQueryGuid="
                + this.guid
                + "&format=json";
    }
}
