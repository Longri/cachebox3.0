package de.longri.serializable;

import java.util.Arrays;

/**
 * Created by Hoepfner on 12.11.2015.
 */
public class ByteArray {

    private enum operator {
        or, and
    }

    private final int fixByteCount;

    private byte[] bytes;

    public ByteArray(Byte b) {
        this(1, b);
    }

    public ByteArray(int fixByteCount, Byte b) {
        this.fixByteCount = fixByteCount;
        this.bytes = new byte[this.fixByteCount];
        this.bytes[this.fixByteCount - 1] = b;
    }

    public ByteArray(Short s) {
        this(2, s);
    }

    public ByteArray(int fixByteCount, Short s) {
        this.fixByteCount = fixByteCount;
        this.bytes = new byte[this.fixByteCount];
        this.bytes[this.fixByteCount - 2] = (byte) (s >> 8);
        this.bytes[this.fixByteCount - 1] = s.byteValue();
    }

    public ByteArray(Integer i) {
        this(4, i);
    }

    public ByteArray(int fixByteCount, Integer i) {
        this.fixByteCount = fixByteCount;
        this.bytes = new byte[this.fixByteCount];
        this.bytes[this.fixByteCount - 4] = (byte) (i >> 24);
        this.bytes[this.fixByteCount - 3] = (byte) (i >> 16);
        this.bytes[this.fixByteCount - 2] = (byte) (i >> 8);
        this.bytes[this.fixByteCount - 1] = i.byteValue();
    }


    public ByteArray(Long l) {
        this(8, l);
    }

    public ByteArray(int fixByteCount, Long l) {
        this.fixByteCount = fixByteCount;
        this.bytes = new byte[this.fixByteCount];
        this.bytes[this.fixByteCount - 8] = (byte) (l >> 56);
        this.bytes[this.fixByteCount - 7] = (byte) (l >> 48);
        this.bytes[this.fixByteCount - 6] = (byte) (l >> 40);
        this.bytes[this.fixByteCount - 5] = (byte) (l >> 32);
        this.bytes[this.fixByteCount - 4] = (byte) (l >> 24);
        this.bytes[this.fixByteCount - 3] = (byte) (l >> 16);
        this.bytes[this.fixByteCount - 2] = (byte) (l >> 8);
        this.bytes[this.fixByteCount - 1] = l.byteValue();
    }

    public ByteArray(byte[] array) {
        this.bytes = array;
        this.fixByteCount = bytes.length;
    }

    public byte[] toByteArray() {
        return this.bytes;
    }

    public int bitLength() {
        int length = 0;
        int index = 0;
        int lastNonZero = 0;

        for (int i = this.fixByteCount - 1; i > -1; i--) {
            if (this.bytes[i] != 0) {
                lastNonZero = index;
            }
            index++;
        }


        length = 8 * lastNonZero;

        //count last bit's
        int count = 0;
        byte v = this.bytes[this.fixByteCount - lastNonZero - 1];
        while (v != 0) {
            count++;
            v = (byte) ((v & 0xff) >>> 1);
        }
        return length + count;
    }

    public void or(ByteArray other) {
        bitOperation(operator.or, other);
    }

    public void and(ByteArray other) {
        bitOperation(operator.and, other);
    }

    private void bitOperation(operator op, ByteArray other) {
        byte[] result = new byte[Math.max(this.bytes.length, other.bytes.length)];
        for (int i = 0; i < result.length; i++) {
            byte b1 = this.bytes.length > i ? this.bytes[i] : 0;
            byte b2 = other.bytes.length > i ? other.bytes[i] : 0;
            if (op == operator.or) result[i] = (byte) (b1 | b2);
            if (op == operator.and) result[i] = (byte) (b1 & b2);
        }

        if (result.length > this.fixByteCount) {
            this.bytes = Arrays.copyOfRange(result, result.length - this.fixByteCount, result.length);
        } else {
            this.bytes = result;
        }
    }

    public void shiftLeft(int n) {

        int nb = n / 8;

        if (nb >= this.fixByteCount) {
            //can fill with zero bytes
            Arrays.fill(this.bytes, (byte) 0);
            return;
        }

        int n1 = (n % 8);
        int n2 = 8 - n1;

        byte[] overflow = Arrays.copyOf(Arrays.copyOfRange(this.bytes, 1, this.fixByteCount), this.fixByteCount);
        int i = 0;
        for (byte b : overflow) {
            overflow[i++] = (byte) ((b & 0xff) >>> n2);
        }
        i = 0;
        for (byte b : this.bytes) {
            this.bytes[i++] = (byte) ((b & 0xff) << n1);
        }
        or(new ByteArray(overflow));

        if (nb == 0) return;

        //Byte Shift
        System.arraycopy(this.bytes, nb, this.bytes, 0, this.bytes.length - nb);
        //fill zero bytes
        for (i = this.fixByteCount - 1; i > this.fixByteCount - nb - 1; i--)
            this.bytes[i] = 0;


        System.out.print("");

    }

    public void shiftRight(int n) {

        int nb = n / 8;
        if (nb >= this.fixByteCount) {
            //can fill with zero bytes
            Arrays.fill(this.bytes, (byte) 0);
            return;
        }

        int n1 = (n % 8);
        int n2 = 8 - n1;

        byte[] o = Arrays.copyOfRange(this.bytes, 0, this.fixByteCount - 1);
        byte[] overflow = new byte[o.length + 1];
        int i = 1;
        for (byte b : o) overflow[i++] = b;

        i = 0;
        for (byte b : overflow) {
            overflow[i++] = (byte) ((b & 0xff) << n2);
        }
        i = 0;
        for (byte b : this.bytes) {
            this.bytes[i++] = (byte) ((b & 0xff) >>> n1);
        }
        or(new ByteArray(overflow));

        if (nb == 0) return;

        //Byte Shift
        System.arraycopy(this.bytes, 0, this.bytes, nb, this.bytes.length - nb);
        //fill zero bytes
        for (i = 0; i < nb; i++)
            this.bytes[i] = 0;


    }

// Return Number values

    public long longValue() {
        byte[] fb;

        if (this.fixByteCount == 8) fb = this.bytes;
        else if (this.fixByteCount < 8) fb = groveUp(this.bytes, 8);
        else fb = Arrays.copyOfRange(bytes, bytes.length - 8, bytes.length);
        int i = 0;
        return (fb[i++] & 0xffL) << 56 | (fb[i++] & 0xffL) << 48 | (fb[i++] & 0xffL) << 40 | (fb[i++] & 0xffL) << 32
                | (fb[i++] & 0xffL) << 24 | (fb[i++] & 0xffL) << 16 | (fb[i++] & 0xffL) << 8 | (fb[i++] & 0xffL);
    }

    public int intValue() {
        byte[] fb;
        if (this.fixByteCount == 4) fb = this.bytes;
        else if (this.fixByteCount < 4) fb = groveUp(this.bytes, 4);
        else fb = Arrays.copyOfRange(bytes, bytes.length - 4, bytes.length);
        int i = 0;
        return fb[i++] << 24 | (fb[i++] & 0xff) << 16 | (fb[i++] & 0xff) << 8 | (fb[i++] & 0xff);
    }

    public short shortValue() {
        byte[] fb;
        if (this.fixByteCount == 2) fb = this.bytes;
        else if (this.fixByteCount < 2) fb = groveUp(this.bytes, 2);
        else fb = Arrays.copyOfRange(bytes, bytes.length - 2, bytes.length);
        int i = 0;
        return (short) ((fb[i++] & 0xff) << 8 | (fb[i++] & 0xff));
    }

    public short byteValue() {
        return this.bytes[this.fixByteCount - 1];
    }

    private static byte[] groveUp(byte[] array, int newSize) {
        byte[] retVal = new byte[newSize];

        int index = newSize - array.length;
        for (byte b : array) {
            retVal[index++] = b;
        }
        return retVal;
    }


// to String

    private static final byte[] TO_STRING_MASK = {-128, 64, 32, 16, 8, 4, 2, 1};

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.bytes.length + " bytes :");
        int writtenByte = 0;
        for (byte write : this.bytes) {
            int index = 0;
            for (short m : TO_STRING_MASK) {
                if (index++ == 4) builder.append(' ');
                if ((write & m) == m) {
                    builder.append('1');
                } else {
                    builder.append('0');
                }
            }
            if (writtenByte++ < this.bytes.length - 1) builder.append(" | ");
        }
        return builder.toString();
    }
}