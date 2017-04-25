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

import de.longri.cachebox3.types.Cache;

/**
 * Created by Longri on 23.03.2017.
 */
public class SelectedCacheChangedEvent extends AbstractEvent<Cache> {
    public final Cache cache;

    public SelectedCacheChangedEvent(Cache cache) {
        super(Cache.class);
        this.cache = cache;
    }

    public SelectedCacheChangedEvent(Cache cache, short id) {
        super(Cache.class, id);
        this.cache = cache;
    }

    @Override
    Class getListenerClass() {
        return SelectedCacheChangedListener.class;
    }
}
