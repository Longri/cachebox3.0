/*
 * Copyright (C) 2019 team-cachebox.de
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
package de.longri.cachebox3.utils.http;

import de.longri.cachebox3.callbacks.GenericCallBack;
import de.longri.cachebox3.utils.Downloader;
import de.longri.cachebox3.utils.UnZip;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Longri on 2019-07-08.
 */
public class ZipDownloader extends Downloader {
    /**
     * Constructor. The target object should not be accessed until after calling waitUntilCompleted().
     *
     * @param url    URL of the remote resource to be downloaded
     * @param target target object to be populated (File or StringBuilder object)
     */
    public ZipDownloader(URL url, Object target) {
        super(url, target);
    }

    /**
     * number of bytes extractedded
     */
    protected int extractedLength = 0;

    @Override
    public void run() {
        super.run(); // download

        final AtomicBoolean canceld = new AtomicBoolean(false);

        // now extract
        UnZip unZip = new UnZip();
        try {
            unZip.extractFolder(this.targetFileHandle, new GenericCallBack<Double>() {
                @Override
                public void callBack(Double value) {
                    extractedLength = (int) (totalLength * (value / 100.0));
                    progressUpdated = true;
                    try {
                        checkState();
                    } catch (Exception e) {
                        canceld.set(true);
                    }
                }
            }, canceld);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * get the percentage describing the current progress.
     *
     * @return percentage describing the current progress; -1 if unknown
     */
    @Override
    public int getProgressPercent() {
        synchronized (lengthLock) {
            if ((totalLength <= 0) || (downloadedLength > totalLength)) {
                return -1;
            } else {
                return (int) (100.0 * (downloadedLength + extractedLength) / (totalLength * 2));
            }
        }
    }
}
