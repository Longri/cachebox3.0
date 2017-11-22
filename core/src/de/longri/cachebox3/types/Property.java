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
package de.longri.cachebox3.types;

/**
 * Created by Longri on 21.11.17.
 */
public abstract class Property<T> {

    protected T value;
    private PropertyChangedListener changedListener;

    public interface PropertyChangedListener {
        void propertyChanged();
    }

    public T get() {
        return value;
    }

    public void set(T newValue) {
        if (changedListener != null && !isEquals(newValue)) {
            value = newValue;
            changedListener.propertyChanged();
        } else {
            value = newValue;
        }
    }

    protected abstract boolean isEquals(T other);

    protected abstract boolean isEquals(Property<T> other);

    public void setChangeListener(PropertyChangedListener listener) {
        changedListener = listener;
    }

}
