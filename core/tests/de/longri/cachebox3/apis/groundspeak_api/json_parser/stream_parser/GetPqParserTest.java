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
package de.longri.cachebox3.apis.groundspeak_api.json_parser.stream_parser;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.apis.groundspeak_api.PocketQuery;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Longri on 28.03.2018.
 */
class GetPqParserTest {

    @Test
    void parse() throws FileNotFoundException {
        InputStream stream = TestUtils.getResourceRequestStream("testsResources/GetPQResult.json");
        GetPqParser parser = new GetPqParser(null);

        FileHandle targetFile = Gdx.files.local("testsResources/streamedPq.zip");

        final AtomicInteger streamedBytes = new AtomicInteger(0);
        parser.parse(stream, targetFile, new PocketQuery.IncrementProgressBytesListener() {
                    @Override
                    public void increment(int bytes) {
                        streamedBytes.addAndGet(bytes);
                    }
                }
        );

        assertThat("File must exist", targetFile.exists());
        assertThat("File size must equals", targetFile.length() == streamedBytes.get());
        
    }
}