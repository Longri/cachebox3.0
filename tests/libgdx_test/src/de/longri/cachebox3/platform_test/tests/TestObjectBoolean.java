

//  Don't modify this file, it's created by tool 'extract_libgdx_test

package de.longri.cachebox3.platform_test.tests;

import de.longri.serializable.*;

/**
 * Created by Longri on 03.11.15.
 */
public class TestObjectBoolean implements Serializable {

    protected boolean value1;
    protected boolean value2;
    protected boolean value3;


    @Override
    public void serialize(StoreBase writer) {
        writer.write(value1);
        writer.write(value2);
        writer.write(value3);
    }

    @Override
    public void deserialize(StoreBase reader) {
        value1 = reader.readBool();
        value2 = reader.readBool();
        value3 = reader.readBool();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof TestObjectBoolean) {
            TestObjectBoolean obj = (TestObjectBoolean) other;

            if (obj.value1 != this.value1) return false;
            if (obj.value2 != this.value2) return false;
            if (obj.value3 != this.value3) return false;

            return true;
        }
        return false;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("value1=" + value1 + "\n");
        sb.append("value2=" + value2 + "\n");
        sb.append("value3=" + value3 + "\n");
        return sb.toString();
    }
}
