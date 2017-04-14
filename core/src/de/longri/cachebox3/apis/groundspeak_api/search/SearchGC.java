/* 
 * Copyright (C) 2014 - 2017 team-cachebox.de
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
package de.longri.cachebox3.apis.groundspeak_api.search;

import com.badlogic.gdx.utils.Json;

import java.util.ArrayList;

/**
 * @author Hubert
 * @author Longri
 */
public class SearchGC extends Search {
    ArrayList<String> gcCodes;

    public SearchGC(String gcApiKey, String gcCode) {
        super(gcApiKey, 1);
        // einzelner Cache wird immer voll geladen
        this.gcCodes = new ArrayList<String>();
        this.gcCodes.add(gcCode);
    }

    public SearchGC(String gcApiKey, ArrayList<String> gcCodes) {
        super(gcApiKey, gcCodes.size());
        this.gcCodes = gcCodes;
    }

    @Override
    protected void getRequest(Json json) {
        json.writeObjectStart();

        //write GC codes
        json.writeObjectStart("CacheCode");
        json.writeArrayStart("CacheCodes");
        for (String gcCode : gcCodes) {
            json.writeValue(gcCode);
        }
        json.writeArrayEnd();
        json.writeObjectEnd();

        super.getRequest(json);
        json.writeObjectEnd();
    }

}