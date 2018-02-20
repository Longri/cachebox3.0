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

import de.longri.cachebox3.types.AbstractCache;

/**
 * Created by Longri on 23.03.2017.
 */
public class SelectedCacheChangedEvent extends AbstractEvent<AbstractCache> {
    public final AbstractCache cache;

    public SelectedCacheChangedEvent(AbstractCache cache) {
        super(AbstractCache.class);
        this.cache = cache;
    }

    public SelectedCacheChangedEvent(AbstractCache cache, short id) {
        super(AbstractCache.class, id);
        this.cache = cache;
    }

    @Override
    public Class getListenerClass() {
        return SelectedCacheChangedListener.class;
    }
}
