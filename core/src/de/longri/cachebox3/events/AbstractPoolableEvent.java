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
package de.longri.cachebox3.events;

import com.badlogic.gdx.utils.Pool;

/**
 * Created by Longri on 28.06.2019.
 */
public abstract class AbstractPoolableEvent extends AbstractEvent implements Pool.Poolable {

    public AbstractPoolableEvent(Class clazz) {
        super(clazz);
    }
}
