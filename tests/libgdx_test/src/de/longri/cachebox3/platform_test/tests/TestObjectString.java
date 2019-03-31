

//  Don't modify this file, it's created by tool 'extract_libgdx_test

package de.longri.cachebox3.platform_test.tests;

import de.longri.serializable.*;

/**
 * Created by Longri on 03.11.15.
 */
public class TestObjectString implements Serializable {

    protected String value1 = "";
    protected String value2 = "";
    protected String value3 = "";
    protected String value4 = "";
    protected String value5 = "";
    protected String value6 = "";
    protected String value7 = "";
    protected String value8 = "";
    protected String value9 = "";
    protected String value10 = "";
    protected String value11 = "";
    protected String value12 = "";
    protected String value13 = "";
    protected String value14 = "";


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
        value1 = reader.readString();
        value2 = reader.readString();
        value3 = reader.readString();
        value4 = reader.readString();
        value5 = reader.readString();
        value6 = reader.readString();
        value7 = reader.readString();
        value8 = reader.readString();
        value9 = reader.readString();
        value10 = reader.readString();
        value11 = reader.readString();
        value12 = reader.readString();
        value13 = reader.readString();
        value14 = reader.readString();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof TestObjectString) {
            TestObjectString obj = (TestObjectString) other;

            if (!obj.value1.equals(this.value1)) return false;
            if (!obj.value2.equals(this.value2)) return false;
            if (!obj.value3.equals(this.value3)) return false;
            if (!obj.value4.equals(this.value4)) return false;
            if (!obj.value5.equals(this.value5)) return false;
            if (!obj.value6.equals(this.value6)) return false;
            if (!obj.value7.equals(this.value7)) return false;
            if (!obj.value8.equals(this.value8)) return false;
            if (!obj.value9.equals(this.value9)) return false;
            if (!obj.value10.equals(this.value10)) return false;
            if (!obj.value11.equals(this.value11)) return false;
            if (!obj.value12.equals(this.value12)) return false;
            if (!obj.value13.equals(this.value13)) return false;
            if (!obj.value14.equals(this.value14)) return false;

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
