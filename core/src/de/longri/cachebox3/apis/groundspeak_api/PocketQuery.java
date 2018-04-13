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

import com.badlogic.gdx.files.FileHandle;
import de.longri.cachebox3.CB;
import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.utils.ICancel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Longri on 28.03.2018.
 */
public class PocketQuery {
    private final static Logger log = LoggerFactory.getLogger(PocketQuery.class);
    public String name;
    public String guid;
    public int cacheCount;
    public Date lastGenerated;
    public Date lastImported;
    public double sizeMB;
    public boolean downloadAvailable = false;


    public interface IncrementProgressBytesListener {
        void increment(int bytes);
    }


    public FileHandle download(FileHandle folder, ICancel iCancel, final IncrementProgressBytesListener listener) {

        // create filename
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String dateString = dateFormatter.format(this.lastGenerated);
        String localName = this.name + "_" + dateString + ".zip";
        FileHandle localFile = folder.child(localName);

        //check file exist and delete
        if (localFile.exists()) {
            if (!localFile.delete()) {
                log.warn("Can't delete exist PQ file: {} ", localFile.path());
                return null;
            }
        }

        final AtomicBoolean WAIT = new AtomicBoolean(true);
        GetPocketQuery getPocketQuery = new GetPocketQuery(GroundspeakAPI.getAccessToken(true),
                this.guid, localFile, listener, iCancel);
        getPocketQuery.post(new GenericCallBack<ApiResultState>() {
            @Override
            public void callBack(ApiResultState value) {
                WAIT.set(false);
            }
        });
        CB.wait(WAIT);
        if (localFile.exists()) return localFile;
        return null;
    }
}
