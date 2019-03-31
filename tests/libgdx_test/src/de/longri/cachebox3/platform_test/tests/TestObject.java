

//  Don't modify this file, it's created by tool 'extract_libgdx_test

package de.longri.cachebox3.platform_test.tests;

import de.longri.serializable.*;

/**
 * Created by Longri on 03.11.15.
 */
public class TestObject implements Serializable {

    protected int IntegerValue1;
    protected int IntegerValue2;
    protected boolean bool1;
    protected boolean bool2;
    protected boolean bool3;


    @Override
    public void serialize(StoreBase writer) {


        BooleanStore booleanStore = new BooleanStore();
        booleanStore.store(BooleanStore.Bitmask.BIT_0, bool1);
        booleanStore.store(BooleanStore.Bitmask.BIT_1, bool2);
        booleanStore.store(BooleanStore.Bitmask.BIT_2, bool3);

        writer.write(booleanStore);
        writer.write(IntegerValue1);
        writer.write(IntegerValue2);

    }

    @Override
    public void deserialize(StoreBase reader) {
        BooleanStore booleanStore = new BooleanStore(reader.readByte());

        bool1 = booleanStore.get(BooleanStore.Bitmask.BIT_0);
        bool2 = booleanStore.get(BooleanStore.Bitmask.BIT_1);
        bool3 = booleanStore.get(BooleanStore.Bitmask.BIT_2);

        IntegerValue1 = reader.readInt();
        IntegerValue2 = reader.readInt();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof TestObject) {
            TestObject obj = (TestObject) other;

            if (obj.IntegerValue1 != this.IntegerValue1) return false;
            if (obj.IntegerValue2 != this.IntegerValue2) return false;
            if (obj.bool1 != this.bool1) return false;
            if (obj.bool2 != this.bool2) return false;
            if (obj.bool3 != this.bool3) return false;

            return true;
        }
        return false;
    }

    public String toString() {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(IntegerValue1);
        stringBuilder.append(" / ");
        stringBuilder.append(IntegerValue2);
        stringBuilder.append(" / ");
        if (bool3) {
            stringBuilder.append('1');
        } else {
            stringBuilder.append('0');
        }
        if (bool2) {
            stringBuilder.append('1');
        } else {
            stringBuilder.append('0');
        }
        if (bool1) {
            stringBuilder.append('1');
        } else {
            stringBuilder.append('0');
        }
        return stringBuilder.toString();
    }

}
