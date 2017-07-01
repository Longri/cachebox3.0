/* 
 * Copyright (C) 2011-2017 team-cachebox.de
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
package de.longri.cachebox3.settings.types;


import de.longri.cachebox3.utils.IChanged;
import de.longri.cachebox3.utils.lists.CB_List;

import java.util.Calendar;

/**
 * @author ging-buh
 * @author Longri
 */
public abstract class SettingBase<T> implements Comparable<SettingBase<T>> {

    final public String name;

    protected CB_List<IChanged> ChangedEventList = new CB_List<IChanged>();
    protected de.longri.cachebox3.settings.types.SettingCategory category;

    protected de.longri.cachebox3.settings.types.SettingMode mode;
    protected de.longri.cachebox3.settings.types.SettingStoreType storeType;
    protected de.longri.cachebox3.settings.types.SettingUsage usage;

    protected T value;
    protected T defaultValue;
    protected T lastValue;
    protected boolean needRestart = false;

    /**
     * saves whether this setting is changed and needs to be saved
     */
    protected boolean dirty;

    private static int indexCount = 0;
    private int index = -1;

    protected long desiredTime = -1L;

    public SettingBase(String name, SettingCategory category, SettingMode modus, SettingStoreType StoreType, SettingUsage usage, boolean desired) {

        if (name.length() > 30) throw new IllegalStateException("Name length can't longer then 30 characters");

        this.name = name;
        this.category = category;
        this.mode = modus;
        this.storeType = StoreType;
        this.usage = usage;
        this.dirty = false;
        this.index = indexCount++;
        if (desired) {
            //set to zero (value -1 means that this setting has no desired value)
            //if desired time not set, so the value is desired
            desiredTime = 0;
        } else {
            desiredTime = -1;
        }
    }

    public void addChangedEventListener(IChanged listener) {
        synchronized (ChangedEventList) {
            if (!ChangedEventList.contains(listener, true))
                ChangedEventList.add(listener);
        }
    }

    public void removeChangedEventListener(IChanged listener) {
        synchronized (ChangedEventList) {
            ChangedEventList.removeValue(listener, true);
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty() {
        dirty = true;
        fireChangedEvent();
    }

    public void setDesiredTime(long time) {
        this.desiredTime = time;
        this.setDirty();
    }

    public boolean isDesired() {
        if (desiredTime == -1L) return false;
        return Calendar.getInstance().getTimeInMillis() > desiredTime;
    }

    public void clearDirty() {
        dirty = false;
    }

    public String getName() {
        return name;
    }

    public de.longri.cachebox3.settings.types.SettingCategory getCategory() {
        return category;
    }

    public SettingStoreType getStoreType() {
        return storeType;
    }

    public de.longri.cachebox3.settings.types.SettingMode getMode() {
        return mode;
    }

    public void changeSettingsModus(SettingMode mode) {
        this.mode = mode;
    }

    public abstract String toDBString();

    public abstract boolean fromDBString(String dbString);

    @Override
    public int compareTo(SettingBase<T> o) {
        return Double.compare(o.index, this.index);
    }

    private void fireChangedEvent() {
        synchronized (ChangedEventList) {
            // do this at new Thread, dont't block Ui-Thread

            Thread th = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0, n = ChangedEventList.size; i < n; i++) {
                        IChanged event = ChangedEventList.get(i);
                        event.isChanged();
                    }
                }
            });
            th.start();
        }
    }

    public T getValue() {
        return value;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    protected boolean ifValueEquals(T newValue) {
        return this.value.equals(newValue);
    }

    public void setValue(T newValue) {
        if (ifValueEquals(newValue))
            return;
        this.value = newValue;
        setDirty();
    }

    public void ForceDefaultChange(T defaultValue) {
        if (this.defaultValue.equals(defaultValue))
            return;
        this.defaultValue = defaultValue;
    }

    public void loadDefault() {
        value = defaultValue;
    }

    public void saveToLastValue() {
        lastValue = value;
    }

    public void loadFromLastValue() {
        if (lastValue == null)
            throw new IllegalArgumentException("You have never saved the last value! Call SaveToLastValue()");
        value = lastValue;
    }

    public abstract SettingBase<T> copy();

    @SuppressWarnings("unchecked")
    public void setValueFrom(SettingBase<?> cpy) {
        try {
            this.value = (T) cpy.value;
        } catch (Exception e) {

        }
    }

    @Override
    public abstract boolean equals(Object obj);

    public SettingUsage getUsage() {
        return this.usage;
    }

    public boolean isDefault() {
        return value.equals(defaultValue);
    }

    public SettingBase<T> setNeedRestart() {
        needRestart = true;
        return this;
    }

    public boolean needRestart() {
        return needRestart;
    }
}
