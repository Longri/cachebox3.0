

//  Don't modify this file, it's created by tool 'extract_libgdx_test

package de.longri.cachebox3.platform_test.tests;

import de.longri.serializable.*;

import de.longri.cachebox3.platform_test.PlatformAssertionError;
import de.longri.cachebox3.platform_test.Test;

import static de.longri.cachebox3.platform_test.Assert.assertEquals;


/**
 * Created by Longri on 03.11.15.
 */
public class BitStoreTest {

    @Test
    public void testBooleanObject() throws Exception, PlatformAssertionError {

        TestObjectBoolean obj = new TestObjectBoolean();
        obj.value1 = true;
        obj.value2 = false;
        obj.value3 = true;

        TestObjectBoolean obj2 = new TestObjectBoolean();

        BitStore writer = new BitStore();
        obj.serialize(writer);
        obj2.deserialize(new BitStore(writer.getArray()));
        System.out.println("BitStore-Boolean: " + writer.getArray().length + " Bytes!");
        assertEquals(obj, obj2);
    }


    @Test
    public void testByteObject() throws Exception, PlatformAssertionError {
        TestObjectByte obj = new TestObjectByte();

        obj.value1 = -36;
        obj.value2 = 117;
        obj.value3 = 36;
        obj.value4 = Byte.MIN_VALUE;
        obj.value5 = Byte.MAX_VALUE;
        obj.value6 = 0;
        obj.value7 = Byte.MIN_VALUE + 1;
        obj.value8 = Byte.MAX_VALUE - 1;

        obj.value9 = Byte.MIN_VALUE + (Byte.MAX_VALUE / 2);
        obj.value10 = Byte.MAX_VALUE - (Byte.MAX_VALUE / 2);

        obj.value11 = Byte.MIN_VALUE + (Byte.MAX_VALUE / 4);
        obj.value12 = Byte.MAX_VALUE - (Byte.MAX_VALUE / 4);

        obj.value13 = Byte.MIN_VALUE + (Byte.MAX_VALUE / 8);
        obj.value14 = Byte.MAX_VALUE - (Byte.MAX_VALUE / 8);


        TestObjectByte obj2 = new TestObjectByte();

        BitStore writer = new BitStore();
        obj.serialize(writer);
        obj2.deserialize(new BitStore(writer.getArray()));
        System.out.println("BitStore-Byte: " + writer.getArray().length + " Bytes!");
        assertEquals(obj, obj2);
    }


    @Test
    public void testShortObject() throws Exception, PlatformAssertionError {

        TestObjectShort obj = new TestObjectShort();

        obj.value1 = -36;
        obj.value2 = 117;
        obj.value3 = 36;
        obj.value4 = Short.MIN_VALUE;
        obj.value5 = Short.MAX_VALUE;
        obj.value6 = 0;
        obj.value7 = Short.MIN_VALUE + 1;
        obj.value8 = Short.MAX_VALUE - 1;

        obj.value9 = Short.MIN_VALUE + (Short.MAX_VALUE / 2);
        obj.value10 = Short.MAX_VALUE - (Short.MAX_VALUE / 2);

        obj.value11 = Short.MIN_VALUE + (Short.MAX_VALUE / 4);
        obj.value12 = Short.MAX_VALUE - (Short.MAX_VALUE / 4);

        obj.value13 = Short.MIN_VALUE + (Short.MAX_VALUE / 8);
        obj.value14 = Short.MAX_VALUE - (Short.MAX_VALUE / 8);


        TestObjectShort obj2 = new TestObjectShort();

        BitStore writer = new BitStore();
        obj.serialize(writer);
        obj2.deserialize(new BitStore(writer.getArray()));
        System.out.println("BitStore-Short: " + writer.getArray().length + " Bytes!");
        assertEquals(obj, obj2);
    }


    @Test
    public void testIntegerObject() throws Exception, PlatformAssertionError {

        TestObjectInteger obj = new TestObjectInteger();

        obj.value1 = -36;
        obj.value2 = 117;
        obj.value3 = 36;
        obj.value4 = Integer.MIN_VALUE;
        obj.value5 = Integer.MAX_VALUE;
        obj.value6 = 0;
        obj.value7 = Integer.MIN_VALUE + 1;
        obj.value8 = Integer.MAX_VALUE - 1;

        obj.value9 = Integer.MIN_VALUE + (Integer.MAX_VALUE / 2);
        obj.value10 = Integer.MAX_VALUE - (Integer.MAX_VALUE / 2);

        obj.value11 = Integer.MIN_VALUE + (Integer.MAX_VALUE / 4);
        obj.value12 = Integer.MAX_VALUE - (Integer.MAX_VALUE / 4);

        obj.value13 = Integer.MIN_VALUE + (Integer.MAX_VALUE / 8);
        obj.value14 = Integer.MAX_VALUE - (Integer.MAX_VALUE / 8);

        TestObjectInteger obj2 = new TestObjectInteger();

        BitStore writer = new BitStore();
        obj.serialize(writer);
        obj2.deserialize(new BitStore(writer.getArray()));
        System.out.println("BitStore-Integer: " + writer.getArray().length + " Bytes!");
        assertEquals(obj, obj2);
    }

    @Test
    public void testLongObject() throws Exception, PlatformAssertionError {


        TestObjectLong obj = new TestObjectLong();

        obj.value1 = -36;
        obj.value2 = 117;
        obj.value3 = 36;
        obj.value4 = Long.MIN_VALUE;
        obj.value5 = Long.MAX_VALUE;
        obj.value6 = 0;
        obj.value7 = Long.MIN_VALUE + 1;
        obj.value8 = Long.MAX_VALUE - 1;

        obj.value9 = Long.MIN_VALUE + (Long.MAX_VALUE / 2);
        obj.value10 = Long.MAX_VALUE - (Long.MAX_VALUE / 2);

        obj.value11 = Long.MIN_VALUE + (Long.MAX_VALUE / 4);
        obj.value12 = Long.MAX_VALUE - (Long.MAX_VALUE / 4);

        obj.value13 = Long.MIN_VALUE + (Long.MAX_VALUE / 8);
        obj.value14 = Long.MAX_VALUE - (Long.MAX_VALUE / 8);


        TestObjectLong obj2 = new TestObjectLong();

        BitStore writer = new BitStore();
        obj.serialize(writer);
        obj2.deserialize(new BitStore(writer.getArray()));
        System.out.println("BitStore-Long: " + writer.getArray().length + " Bytes!");
        assertEquals(obj, obj2);
    }


    @Test
    public void testArray() throws Exception, PlatformAssertionError {
        ListObject o1 = new ListObject();
        o1.booleanStore.store(BooleanStore.Bitmask.BIT_0, true);

        ListObject o2 = new ListObject();
        o2.booleanStore.store(BooleanStore.Bitmask.BIT_1, true);

        ListObject o3 = new ListObject();
        o3.booleanStore.store(BooleanStore.Bitmask.BIT_2, true);

        SerializableArrayList<ListObject> serializeList = new SerializableArrayList<ListObject>(ListObject.class);

        serializeList.add(o1);
        serializeList.add(o2);
        serializeList.add(o3);

        BitStore writer = new BitStore();

        serializeList.serialize(writer);

        byte[] byteArray = writer.getArray();

        SerializableArrayList<ListObject> deserializeList = new SerializableArrayList<ListObject>(ListObject.class);

        BitStore reader = new BitStore(byteArray);
        deserializeList.deserialize(reader);

        assertEquals(o1, deserializeList.get(0));
        assertEquals(o2, deserializeList.get(1));
        assertEquals(o3, deserializeList.get(2));


    }


    @Test
    public void testArrayObject() throws Exception, PlatformAssertionError {

        TestObject obj = new TestObject();
        obj.IntegerValue1 = 16310;
        obj.IntegerValue2 = 3242341;
        obj.bool1 = true;
        obj.bool2 = true;

        TestObject obj2 = new TestObject();
        obj.IntegerValue1 = 16;
        obj.IntegerValue2 = 322342341;
        obj.bool1 = true;
        obj.bool3 = true;

        TestObject obj3 = new TestObject();
        obj.IntegerValue1 = 845321123;
        obj.IntegerValue2 = 324212;
        obj.bool2 = true;


        SerializableArrayList<TestObject> serializeList = new SerializableArrayList<TestObject>(TestObject.class);

        serializeList.add(obj);
        serializeList.add(obj2);
        serializeList.add(obj3);

        BitStore writer = new BitStore();

        serializeList.serialize(writer);

        byte[] byteArray = writer.getArray();

        SerializableArrayList<TestObject> deserializeList = new SerializableArrayList<TestObject>(TestObject.class);

        BitStore reader = new BitStore(byteArray);
        deserializeList.deserialize(reader);

        assertEquals(obj, deserializeList.get(0));
        assertEquals(obj2, deserializeList.get(1));
        assertEquals(obj3, deserializeList.get(2));

    }


    @Test
    public void testString() throws Exception, PlatformAssertionError {

        TestObjectString obj = new TestObjectString();
        obj.value1 = "Test";
        obj.value2 = "Object";
        obj.value3 = "String";
        obj.value4 = "Test Object String";
        obj.value5 = "";
        obj.value6 = "Java is a general-purpose computer programming language that is concurrent, class-based, object-oriented, and specifically designed to have as few implementation dependencies as possible.";
        obj.value7 = "It is intended to let application developers \"write once, run anywhere\" (WORA), meaning that compiled Java code can run on all platforms that support Java without the need for recompilation.";
        obj.value8 = "";
        obj.value9 = "";
        obj.value10 = "Serialize";

        TestObjectString obj2 = new TestObjectString();

        BitStore writer = new BitStore();
        obj.serialize(writer);
        obj2.deserialize(new BitStore(writer.getArray()));

        System.out.println("BitStore-String: " + writer.getArray().length + " Bytes!");

        assertEquals(obj, obj2);
    }

    @Test
    public void testDoubleObject() throws Exception, PlatformAssertionError {

        TestObjectDouble obj = new TestObjectDouble();

        obj.value1 = -36.5;
        obj.value2 = 117.5;
        obj.value3 = 36.5;
        obj.value4 = Double.MIN_VALUE;
        obj.value5 = Double.MAX_VALUE;
        obj.value6 = 0.0;
        obj.value7 = Double.MIN_VALUE + 1;
        obj.value8 = Double.MAX_VALUE - 1;

        obj.value9 = Double.MIN_VALUE + (Double.MAX_VALUE / 2);
        obj.value10 = Double.MAX_VALUE - (Double.MAX_VALUE / 2);

        obj.value11 = Double.MIN_VALUE + (Double.MAX_VALUE / 4);
        obj.value12 = Double.MAX_VALUE - (Double.MAX_VALUE / 4);

        obj.value13 = Double.MIN_VALUE + (Double.MAX_VALUE / 8);
        obj.value14 = Double.MAX_VALUE - (Double.MAX_VALUE / 8);

        TestObjectDouble obj2 = new TestObjectDouble();

        BitStore writer = new BitStore();
        obj.serialize(writer);
        obj2.deserialize(new BitStore(writer.getArray()));
        System.out.println("BitStore-Double: " + writer.getArray().length + " Bytes!");
        assertEquals(obj, obj2);
    }

    @Test
    public void testFloatObject() throws Exception, PlatformAssertionError {

        TestObjectFloat obj = new TestObjectFloat();

        obj.value1 = -36.5f;
        obj.value2 = 117.5f;
        obj.value3 = 36.5f;
        obj.value4 = Float.MIN_VALUE;
        obj.value5 = Float.MAX_VALUE;
        obj.value6 = 0.0f;
        obj.value7 = Float.MIN_VALUE + 1;
        obj.value8 = Float.MAX_VALUE - 1;

        obj.value9 = Float.MIN_VALUE + (Float.MAX_VALUE / 2);
        obj.value10 = Float.MAX_VALUE - (Float.MAX_VALUE / 2);

        obj.value11 = Float.MIN_VALUE + (Float.MAX_VALUE / 4);
        obj.value12 = Float.MAX_VALUE - (Float.MAX_VALUE / 4);

        obj.value13 = Float.MIN_VALUE + (Float.MAX_VALUE / 8);
        obj.value14 = Float.MAX_VALUE - (Float.MAX_VALUE / 8);

        TestObjectFloat obj2 = new TestObjectFloat();

        BitStore writer = new BitStore();
        obj.serialize(writer);
        obj2.deserialize(new BitStore(writer.getArray()));
        System.out.println("BitStore-Float: " + writer.getArray().length + " Bytes!");
        assertEquals(obj, obj2);
    }
}
