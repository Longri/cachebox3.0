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
package de.longri.cachebox3.events;

/**
 * Created by Longri on 23.03.2017.
 */
public class ImportProgressChangedEvent extends AbstractEvent<ImportProgressChangedEvent.ImportProgress> {

    public static class ImportProgress {
        public int progress, caches, wayPoints, logs, images;
        public String msg = "";
    }


    public final ImportProgress progress;

    public ImportProgressChangedEvent(ImportProgress progress) {
        super(ImportProgress.class);
        this.progress = progress;
    }

    public ImportProgressChangedEvent(ImportProgress progress, short id) {
        super(ImportProgress.class, id);
        this.progress = progress;
    }

    @Override
    public Class getListenerClass() {
        return ImportProgressChangedListener.class;
    }
}
