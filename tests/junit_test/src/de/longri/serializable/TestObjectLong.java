package de.longri.serializable;

/**
 * Created by Longri on 03.11.15.
 */
public class TestObjectLong implements Serializable {

    protected long value1 = 0;
    protected long value2 = 0;
    protected long value3 = 0;
    protected long value4 = 0;
    protected long value5 = 0;
    protected long value6 = 0;
    protected long value7 = 0;
    protected long value8 = 0;
    protected long value9 = 0;
    protected long value10 = 0;
    protected long value11 = 0;
    protected long value12 = 0;
    protected long value13 = 0;
    protected long value14 = 0;


    @Override
    public void serialize(StoreBase writer) {
        writer.write(value1);
        writer.write(value2);
        writer.write(value3);
        writer.write(value4);
        writer.write(value5);
        writer.write(value6);
        writer.write(value7);
        writer.write(value8);
        writer.write(value9);
        writer.write(value10);
        writer.write(value11);
        writer.write(value12);
        writer.write(value13);
        writer.write(value14);
    }

    @Override
    public void deserialize(StoreBase reader) {
        value1 = reader.readLong();
        value2 = reader.readLong();
        value3 = reader.readLong();
        value4 = reader.readLong();
        value5 = reader.readLong();
        value6 = reader.readLong();
        value7 = reader.readLong();
        value8 = reader.readLong();
        value9 = reader.readLong();
        value10 = reader.readLong();
        value11 = reader.readLong();
        value12 = reader.readLong();
        value13 = reader.readLong();
        value14 = reader.readLong();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof TestObjectLong) {
            TestObjectLong obj = (TestObjectLong) other;

            if (obj.value1 != this.value1) return false;
            if (obj.value2 != this.value2) return false;
            if (obj.value3 != this.value3) return false;
            if (obj.value4 != this.value4) return false;
            if (obj.value5 != this.value5) return false;
            if (obj.value6 != this.value6) return false;
            if (obj.value7 != this.value7) return false;
            if (obj.value8 != this.value8) return false;
            if (obj.value9 != this.value9) return false;
            if (obj.value10 != this.value10) return false;
            if (obj.value11 != this.value11) return false;
            if (obj.value12 != this.value12) return false;
            if (obj.value13 != this.value13) return false;
            if (obj.value14 != this.value14) return false;


            return true;
        }
        return false;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("value1=" + value1 + "\n");
        sb.append("value2=" + value2 + "\n");
        sb.append("value3=" + value3 + "\n");
        sb.append("value4=" + value4 + "\n");
        sb.append("value5=" + value5 + "\n");
        sb.append("value6=" + value6 + "\n");
        sb.append("value7=" + value7 + "\n");
        sb.append("value8=" + value8 + "\n");
        sb.append("value9=" + value9 + "\n");
        sb.append("value10=" + value10 + "\n");
        sb.append("value11=" + value11 + "\n");
        sb.append("value12=" + value12 + "\n");
        sb.append("value13=" + value13 + "\n");
        sb.append("value14=" + value14 + "\n");
        return sb.toString();
    }
}
