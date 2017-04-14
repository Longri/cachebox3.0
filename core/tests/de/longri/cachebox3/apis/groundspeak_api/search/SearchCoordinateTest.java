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
package de.longri.cachebox3.apis.groundspeak_api.search;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import org.apache.commons.codec.Charsets;
import org.junit.jupiter.api.Test;
import travis.EXCLUDE_FROM_TRAVIS;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static travis.EXCLUDE_FROM_TRAVIS.LONGRI_HOME_COORDS;

/**
 * Created by longri on 14.04.17.
 */
class SearchCoordinateTest {

    final String apiKey = EXCLUDE_FROM_TRAVIS.GcAPI;
    final boolean isDummy = apiKey.equals(EXCLUDE_FROM_TRAVIS.DUMMY_API_KEY);

    @Test
    void getRequest() throws IOException {
        SearchCoordinate searchCoordinate = new SearchCoordinate(apiKey,50, LONGRI_HOME_COORDS, 50000);


    }

}
