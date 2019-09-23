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
import de.longri.cachebox3.utils.exceptions.CancelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Longri on 2019-07-08.
 */
public class ZipDownloader extends Downloader {

    protected static Logger log = LoggerFactory.getLogger(ZipDownloader.class);

    private final double DOWNLOAD_EXTRACT_RATIO = 25.0;

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
    private int extractedLength = 0;

    /**
     * is the extraction completed?
     */
    private boolean completed = false;

    private boolean extractRunning = false;


    /**
     * Is the download completed?
     *
     * @return true if download is completed; false otherwise
     */
    public boolean isCompleted() {
        return super.isCompleted() && completed;
    }

    /**
     * get the number of bytes downloaded * 2.
     */
    @Override
    public int getDownloadedLength() {
        synchronized (lengthLock) {
            return downloadedLength + (int) (extractedLength / DOWNLOAD_EXTRACT_RATIO);
        }
    }

    /**
     * get the length of the remote resource.
     *
     * @return length of the remote resource, in number of bytes; -1 if unknown
     */
    @Override
    public int getLength() {
        synchronized (lengthLock) {
            return totalLength + (int) (totalLength / DOWNLOAD_EXTRACT_RATIO);
        }
    }

    public boolean isDownloadCompleted() {
        return super.isCompleted();
    }

    @Override
    public void run() {
        super.run(); // download

        extractRunning = true;

        final AtomicBoolean canceld = new AtomicBoolean(false);

        log.debug("Unzip downloaded file");

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
            log.error("Unzip file", e);
            e.printStackTrace();
        }
        completed = true;
        extractRunning = false;

        log.debug("Ready unzip file");
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

    /**
     * Pause the download.
     */
    public void pause() {
        super.pause();
        extractRunning = false;

    }

    /**
     * Resume the download.
     */
    public void resume() {
        super.resume();
        if (!completed) extractRunning = true;
    }

    /**
     * Check if the downloader state has been modified. This method blocks if the download has been paused, unless it is resumed or
     * cancelled. An exception is thrown if the download is cancelled.
     *
     * @throws Exception if the download is cancelled
     */
    protected void checkState() throws CancelException {
        while (true) {
            synchronized (stateLock) {
                if (cancelled) {
                    log.debug("extract download cancelled");
                    progressUpdated = true;
                    throw new CancelException("extract download cancelled");
                }

                if (running || extractRunning) {
                    return;
                }
            }
        }
    }
}
