package de.longri.serializable;

/**
 * Created by Longri on 03.11.15.
 */
public class TestObjectDouble implements Serializable {

    protected double value1 = 0;
    protected double value2 = 0;
    protected double value3 = 0;
    protected double value4 = 0;
    protected double value5 = 0;
    protected double value6 = 0;
    protected double value7 = 0;
    protected double value8 = 0;
    protected double value9 = 0;
    protected double value10 = 0;
    protected double value11 = 0;
    protected double value12 = 0;
    protected double value13 = 0;
    protected double value14 = 0;


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
        value1 = reader.readDouble();
        value2 = reader.readDouble();
        value3 = reader.readDouble();
        value4 = reader.readDouble();
        value5 = reader.readDouble();
        value6 = reader.readDouble();
        value7 = reader.readDouble();
        value8 = reader.readDouble();
        value9 = reader.readDouble();
        value10 = reader.readDouble();
        value11 = reader.readDouble();
        value12 = reader.readDouble();
        value13 = reader.readDouble();
        value14 = reader.readDouble();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof TestObjectDouble) {
            TestObjectDouble obj = (TestObjectDouble) other;

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
