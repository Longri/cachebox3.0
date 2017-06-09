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

import de.longri.cachebox3.utils.lists.CB_List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SettingEnum<EnumTyp extends Enum<?>> extends SettingString {
    final static Logger log = LoggerFactory.getLogger(SettingEnum.class);
    private final CB_List<String> values;

    private EnumTyp myEnum;

    @SuppressWarnings("rawtypes")
    public SettingEnum(String name, SettingCategory category, SettingMode modus, EnumTyp defaultValue, SettingStoreType StoreType, SettingUsage usage, EnumTyp enu) {
        super(name, category, modus, defaultValue.name(), StoreType, usage);
        myEnum = enu;

        values = new CB_List<String>();

        // hier bekommst du die Klasse TestEnum
        Class c = enu.getDeclaringClass();
        // hier kannst du alle Zust�nde abfragen
        Object[] oo = c.getEnumConstants();
        // hier kannst du dann �ber alle Zust�nde iterieren
        for (Object o : oo) {
            // und von jedem den Namen abfragen (in unserem Beispiel "wert1",
            // "wert2", "wert3"
            values.add(((Enum) o).name());
        }

    }

    public CB_List<String> getValues() {
        return values;
    }

    @Override
    public void setValue(String value) {
        if (this.value.equals(value))
            return;

        if (value == null || value.isEmpty()) {
            myEnum = getEnumFromString(defaultValue);
            setDirty();
            return;
        }

        this.value = value;
        myEnum = getEnumFromString(value);
        setDirty();
    }


    public EnumTyp getEnumValue() {
        return getEnumFromString(value);
    }

    public EnumTyp getEnumDefaultValue() {
        return getEnumFromString(defaultValue);
    }

    @SuppressWarnings("unchecked")
    private EnumTyp getEnumFromString(String stringValue) {
        EnumTyp ret = null;
        try {
            ret = (EnumTyp) Enum.valueOf(myEnum.getDeclaringClass(), stringValue);
        } catch (Exception e) {
            log.error("Wrong ENUM value:" + stringValue, e);
            ret = getEnumFromString(defaultValue);
        }

        return ret;
    }

//    public void setEnumValue(EnumTyp value) {
//        if (this.myEnum == value)
//            return;
//        this.value = value.name();
//        myEnum = value;
//        setDirty();
//    }

    public void setEnumValue( Enum<?> value) {
        this.value = value.name();
        myEnum = getEnumFromString(this.value);
        setDirty();
    }


}
