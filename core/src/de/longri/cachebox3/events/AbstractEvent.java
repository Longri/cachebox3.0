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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Longri on 23.03.2017.
 */
public abstract class AbstractEvent<T> {

    private final Class clazz;
    public final short ID;

    public AbstractEvent(Class<T> clazz) {
        this(clazz, EventHandler.getId());
    }

    public AbstractEvent(Class<T> clazz, short eventID) {
        this.clazz = clazz;
        this.ID = eventID;
    }

    public abstract Class getListenerClass();

}
