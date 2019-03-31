package de.longri.serializable;


import java.util.ArrayList;

/**
 * Created by Longri on 04.11.15.
 */
public class SerializableArrayList<T extends Serializable> implements Serializable {

    private ArrayList<T> list = new ArrayList<T>();
    private final Class<T> tClass;

    public SerializableArrayList(Class<T> tClass) {
        this.tClass = tClass;
    }


    @Override
    public void serialize(StoreBase writer) {
        writer.write(list.size());
        ArrayList<Byte> byteArrayList = new ArrayList<Byte>();

        for (T t : list) {
            t.serialize(writer);
        }
    }

    @Override
    public void deserialize(StoreBase reader) {
        list = reader.readList(tClass);
    }

    public void add(T object) {
        list.add(object);
    }

    public T get(int index) {
        if (list.size() < index) return null;
        return list.get(index);
    }

    public int size() {
        return list.size();
    }

}
