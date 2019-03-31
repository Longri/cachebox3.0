package de.longri.serializable;

/**
 * Created by Longri on 03.11.15.
 */
public class ListObject implements Serializable {

    protected BooleanStore booleanStore = new BooleanStore();


    @Override
    public void serialize(StoreBase writer) {
        writer.write(booleanStore);
    }

    @Override
    public void deserialize(StoreBase reader) {
        BooleanStore Store = new BooleanStore(reader.readByte());

        booleanStore.store(BooleanStore.Bitmask.BIT_0, Store.get(BooleanStore.Bitmask.BIT_0));
        booleanStore.store(BooleanStore.Bitmask.BIT_1, Store.get(BooleanStore.Bitmask.BIT_1));
        booleanStore.store(BooleanStore.Bitmask.BIT_2, Store.get(BooleanStore.Bitmask.BIT_2));
        booleanStore.store(BooleanStore.Bitmask.BIT_3, Store.get(BooleanStore.Bitmask.BIT_3));
        booleanStore.store(BooleanStore.Bitmask.BIT_4, Store.get(BooleanStore.Bitmask.BIT_4));
        booleanStore.store(BooleanStore.Bitmask.BIT_5, Store.get(BooleanStore.Bitmask.BIT_5));
        booleanStore.store(BooleanStore.Bitmask.BIT_6, Store.get(BooleanStore.Bitmask.BIT_6));
        booleanStore.store(BooleanStore.Bitmask.BIT_7, Store.get(BooleanStore.Bitmask.BIT_7));

    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ListObject) {
            ListObject obj = (ListObject) other;
            if (obj.booleanStore.getValue() != this.booleanStore.getValue()) return false;
            return true;
        }
        return false;
    }

    public String toString() {
        return booleanStore.getValue() + " : " + booleanStore.toString();
    }

}
