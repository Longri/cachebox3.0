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

import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.TestUtils;
import de.longri.cachebox3.apis.groundspeak_api.PocketQuery;
import de.longri.cachebox3.utils.ICancel;
import org.junit.jupiter.api.Test;
import travis.EXCLUDE_FROM_TRAVIS;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Longri on 28.03.2018.
 */
class GetPqParserTest {

    static {
        TestUtils.initialGdx();
    }

    final String apiKey = EXCLUDE_FROM_TRAVIS.GcAPI;
    final boolean isDummy = apiKey.equals(EXCLUDE_FROM_TRAVIS.DUMMY_API_KEY);

    @Test
    void parseTest() throws FileNotFoundException {
        if (isDummy) return;
        InputStream stream = TestUtils.getResourceRequestStream("testsResources/GetPQResult.json");
        GetPqParser parser = new GetPqParser(null);

        FileHandle targetFile = TestUtils.getResourceFileHandle("testsResources/streamedPq.zipTest");

        if (targetFile.exists()) {
            assertThat("Target file must deleted", targetFile.delete());
        }


        final AtomicInteger streamedBytes = new AtomicInteger(0);
        try {
            parser.parse(stream, targetFile, new PocketQuery.IncrementProgressBytesListener() {
                        @Override
                        public void increment(int bytes) {
                            streamedBytes.addAndGet(bytes);
                        }
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertThat("File must exist", targetFile.exists());
        assertThat("File size must equals", targetFile.length() == streamedBytes.get());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertThat("Target file must deleted", targetFile.delete());
    }

    @Test
    void parseCancelTest() throws FileNotFoundException {
        InputStream stream = TestUtils.getResourceRequestStream("testsResources/GetPQResult.json");
        final AtomicBoolean cancel = new AtomicBoolean(false);
        GetPqParser parser = new GetPqParser(new ICancel() {
            @Override
            public boolean cancel() {
                return cancel.get();
            }
        });

        FileHandle targetFile = TestUtils.getResourceFileHandle("testsResources/streamedPqCancel.zipTest");

        if (targetFile.exists()) {
            assertThat("Target file must deleted", targetFile.delete());
        }


        final AtomicInteger streamedBytes = new AtomicInteger(0);
        try {
            parser.parse(stream, targetFile, new PocketQuery.IncrementProgressBytesListener() {
                        @Override
                        public void increment(int bytes) {
                            if (streamedBytes.addAndGet(bytes) > 1000) {
                                cancel.set(true);
                            }
                        }
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertThat("File must not exist", !targetFile.exists());

    }
}