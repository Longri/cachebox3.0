package de.longri.cachebox3.settings.types;

/**
 * Created by Longri on 03.06.18.
 */
public class SettingsBlob extends SettingBase<byte[]> {

    public SettingsBlob(String name, SettingCategory category, SettingMode modus, SettingStoreType storeType, SettingUsage usage, boolean desired, byte[] bytes) {
        super(name, category, modus, storeType, usage, desired);
        this.value = bytes;
    }

    @Override
    public Object toDbValue() {
        return value;
    }

    @Override
    public boolean fromDbvalue(Object value) {
        this.value = (byte[]) value;
        return true;
    }

    @Override
    public SettingBase<byte[]> copy() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public void loadFromLastValue() {

        // lastValue and value can be NULL, so we can not throw a exception and overload this method!


//        if (lastValue == null)
//            throw new IllegalArgumentException("You have never saved the last value of '" + name + "'! Call SaveToLastValue()");


        value = lastValue;
    }
}
