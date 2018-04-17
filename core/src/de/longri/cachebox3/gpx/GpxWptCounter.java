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
package de.longri.cachebox3.gpx;

import com.badlogic.gdx.files.FileHandle;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Longri on 17.04.2018.
 */
public class GpxWptCounter extends AbstractGpxStreamImporter {

    private final AtomicInteger wptCount = new AtomicInteger();

    public GpxWptCounter() {
        super(null, null);
    }

    public void doImport(FileHandle gpxFile) {
        wptCount.set(0);
        super.doImport(gpxFile);
    }

    public int getWptCount() {
        return wptCount.get();
    }

    @Override
    protected void registerGroundspeakHandler() {

    }

    @Override
    protected void registerOpenCachingHandler() {

    }

    @Override
    protected void registerGsakHandler() {

    }

    @Override
    protected void registerGsakHandler_1_1() {

    }

    @Override
    protected void registerGenerallyHandler() {
        this.registerEndTagHandler("/gpx/wpt", new EndTagHandler() {
            @Override
            protected void handleEndTag() {
                wptCount.incrementAndGet();
            }
        });
    }

    @Override
    protected void registerCacheboxHandler() {

    }
}
