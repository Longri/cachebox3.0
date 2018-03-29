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
import de.longri.cachebox3.apis.groundspeak_api.ApiResultState;
import de.longri.cachebox3.apis.groundspeak_api.PocketQuery;
import de.longri.cachebox3.utils.ICancel;
import de.longri.cachebox3.utils.converter.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Longri on 28.03.2018.
 */
public class GetPqParser {

    private final Logger log = LoggerFactory.getLogger(GetPqParser.class);
    private final ICancel iCancel;

    public GetPqParser(ICancel iCancel) {
        this.iCancel = iCancel;
    }


    public ApiResultState parse(InputStream inputStream, final FileHandle localFile, PocketQuery.IncrementProgressBytesListener listener) throws IOException {
        int buffLen = 32 * 1024;
        byte[] buff = new byte[buffLen];
        int buffCount = inputStream.read(buff, 0, buffLen);
        int buffPos = 0;
        String result = ""; // now read from the response until the ZIP Informations are beginning or to the end of stream
        for (int i = 0; i < buffCount; i++) {
            byte c = buff[i];
            result += (char) c;

            if (result.contains("\"ZippedFile\":\"")) { // The stream position represents the beginning of the ZIP block // to have a correct JSON Array we must add a "}} to the
                // result
                result += "\"}}";
                buffPos = i; // Position in the buffer where the ZIP information starts
                break;
            }
        }

        final String local = localFile.file().getAbsolutePath();
        final FileOutputStream fs = new FileOutputStream(local);
        final BufferedOutputStream bfs = new BufferedOutputStream(fs);
        boolean write = Base64.decodeStreamToStream(inputStream, buff, listener, buffCount, buffPos, bfs, this.iCancel);

        bfs.flush();
        bfs.close();
        fs.close();
        System.gc();

        if (!write) {
            localFile.delete();
        }

        return ApiResultState.IO;
    }
}
