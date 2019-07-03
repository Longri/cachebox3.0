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

import com.badlogic.gdx.utils.Array;
import de.longri.cachebox3.interfaces.ProgressCancelRunnable;
import de.longri.cachebox3.utils.Downloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Longri on 03.07.2019.
 */
public class ProgressCancelDownloader extends ProgressCancelRunnable {

    final Logger log = LoggerFactory.getLogger(ProgressCancelDownloader.class);

    final Array<Downloader> list = new Array<>();
    final AtomicBoolean running = new AtomicBoolean(false);

    public ProgressCancelDownloader() {

    }

    public void add(Downloader downloader) {
        if (running.get()) throw new RuntimeException("can't add a downloader to a running task");
        list.add(downloader);
    }

    @Override
    public void canceled() {

    }

    @Override
    public void run() {
        if (isCanceled.get()) return;
        running.set(true);

        log.debug("Start download of {} files", list.size);

        long start = System.currentTimeMillis();

        // start all downloader on own Thread to get progress infos
        int count = 0;
        for (Downloader downloader : list) {
            final int num = count++;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    downloader.run();
                    try {
                        downloader.waitUntilCompleted();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        log.debug("downloader {} are finish", num);
                    }
                }
            }).start();
        }

        boolean allReady = false;
        double allBytes = -1;
        double readyBytes = -1;

        while (!allReady && !isCanceled.get()) {
            allReady = true;
            boolean allHasLength = true;
            //check all download states
            for (Downloader downloader : list) {
                if (!downloader.isCompleted()) allReady = false;
                if (downloader.getLength() < 0) {
                    allHasLength = false;
                } else {
                    allBytes += downloader.getLength();
                }
                if (downloader.getDownloadedLength() > 0) readyBytes += downloader.getDownloadedLength();
            }

            if (allHasLength) {
                // now we can calculate progress
                double progress = (readyBytes / allBytes) * 100;
                setProgress((float) progress, progressMsg);
            }
            try {
                Thread.sleep(70);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        if (!allReady) {
            // is canceled! so cancel all downloader
            int downloadedBytes = 0;
            for (Downloader downloader : list) {
                downloadedBytes += downloader.getDownloadedLength();
                downloader.cancel();
            }
            log.debug("Task is canceld after download of {} bytes!", downloadedBytes);
        } else {
            long downTime = System.currentTimeMillis() - start;
            log.debug("Download ready after {}ms. (downloaded bytes: {})", downTime, ((int) allBytes));
        }
        running.set(false);
    }
}
