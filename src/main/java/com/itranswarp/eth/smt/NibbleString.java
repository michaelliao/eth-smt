package com.itranswarp.eth.smt;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable half-byte-string. Each element is in range of 0 ~ 0xf.
 */
public final class NibbleString {

    private static final int[] EMPTY_ARRAY = new int[0];
    private static final String HEX_STRING = "0123456789abcdef";
    private static final char[] HEX_CHARS = HEX_STRING.toCharArray();

    private final int[] value;
    private final int offset;
    private final int count;
    private int hash;

    /**
     * Empty nibble string.
     */
    public static final NibbleString EMPTY = new NibbleString(EMPTY_ARRAY, 0, 0);

    /**
     * Build by hex string.
     * 
     * <code>
     * "a1b2" -> NibbleString { 0xa, 1, 0xb, 2 }
     * </code>
     * 
     * @param hexString The hex string.
     */
    public NibbleString(String hexString) {
        if (hexString.isEmpty()) {
            this.value = EMPTY_ARRAY;
            this.offset = 0;
            this.count = 0;
        } else {
            int len = hexString.length();
            int[] val = new int[len];
            for (int i = 0; i < len; i++) {
                char ch = hexString.charAt(i);
                if (ch >= '0' && ch <= '9') {
                    val[i] = ch - '0';
                } else if (ch >= 'a' && ch <= 'f') {
                    val[i] = ch + 10 - 'a';
                } else {
                    throw new IllegalArgumentException("Invalid hex string char: " + ch);
                }
            }
            this.value = val;
            this.offset = 0;
            this.count = len;
        }
    }

    /**
     * Build by bytes array.
     * 
     * <code>
     * byte[] {0x2f, 0x3c}" -> NibbleString { 2, 0xf, 3, 0xc }
     * </code>
     * 
     * @param hexString The hex string.
     */
    public NibbleString(byte[] original) {
        if (original.length == 0) {
            this.value = EMPTY_ARRAY;
            this.offset = 0;
            this.count = 0;
        } else {
            int len = original.length;
            int[] val = new int[len << 1];
            for (int i = 0; i < len; i++) {
                int offset = i << 1;
                byte b = original[i];
                val[offset] = (b & 0xf0) >>> 4;
                val[offset + 1] = b & 0x0f;
            }
            this.value = val;
            this.offset = 0;
            this.count = val.length;
        }
    }

    /**
     * Append a nibble.
     */
    public NibbleString join(int next) {
        if (next < 0 || next > 15) {
            throw new IllegalArgumentException("Next nibble must between 0 ~ 15.");
        }
        int[] copy = new int[this.count + 1];
        System.arraycopy(this.value, this.offset, copy, 0, this.count);
        copy[this.count] = next;
        return new NibbleString(copy, 0, this.count + 1);
    }

    private NibbleString(int[] original, int offset, int count) {
        this.value = original;
        this.offset = offset;
        this.count = count;
    }

    /**
     * Substring from beginIndex.
     */
    public NibbleString substring(int beginIndex) {
        return substring(beginIndex, this.count);
    }

    /**
     * Substring from beginIndex and endIndex (exclusive).
     */
    public NibbleString substring(int beginIndex, int endIndex) {
        if (beginIndex == 0 && endIndex == this.count) {
            return this;
        }
        Objects.checkFromToIndex(beginIndex, endIndex, this.count);
        return new NibbleString(this.value, this.offset + beginIndex, endIndex - beginIndex);
    }

    /**
     * Length of this nibble string.
     */
    public int length() {
        return this.count;
    }

    /**
     * Nibble value at index.
     */
    public int valueAt(int index) {
        Objects.checkIndex(index, this.count);
        return this.value[this.offset + index];
    }

    /**
     * equals() implementation.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof NibbleString) {
            NibbleString bs = (NibbleString) o;
            return Arrays.equals(this.value, this.offset, this.offset + this.count, bs.value, bs.offset, bs.offset + bs.count);
        }
        return false;
    }

    /**
     * hashCode() implementation.
     */
    @Override
    public int hashCode() {
        int h = this.hash;
        if (h == 0 && this.count > 0) {
            h = 1;
            int off = this.offset;
            for (int i = 0; i < this.count; i++) {
                h = 31 * h + this.value[off++];
            }
            this.hash = h;
        }
        return h;
    }

    /**
     * toString() implementation.
     */
    @Override
    public String toString() {
        if (this.count == 0) {
            return "";
        }
        var sb = new StringBuilder(this.count);
        int off = this.offset;
        for (int i = 0; i < this.count; i++) {
            sb.append(HEX_CHARS[this.value[off++]]);
        }
        return sb.toString();
    }

    /**
     * Is empty nibble string.
     */
    public boolean isEmpty() {
        return this.count == 0;
    }

    /**
     * Tests if this string starts with the specified prefix.
     *
     * @param prefix the prefix.
     * @return <code>true</code> if the character sequence represented by the
     *         argument is a prefix of the character sequence represented by this
     *         string; <code>false</code> otherwise.
     */
    public boolean startsWith(NibbleString prefix) {
        return startsWith(prefix, 0);
    }

    /**
     * Tests if the substring of this string beginning at the specified index starts
     * with the specified prefix.
     * 
     * @param prefix  prefix the prefix.
     * @param toffset toffset where to begin looking in this string.
     * @return <code>true</code> if the character sequence represented by the
     *         argument is a prefix of the substring of this object starting at
     *         index <code>toffset</code>; <code>false</code> otherwise.
     */
    public boolean startsWith(NibbleString prefix, int toffset) {
        int ta[] = this.value;
        int to = this.offset + toffset;
        int pa[] = prefix.value;
        int po = prefix.offset;
        int pc = prefix.count;
        // Note: toffset might be near -1>>>1.
        if ((toffset < 0) || (toffset > count - pc)) {
            return false;
        }
        while (--pc >= 0) {
            if (ta[to++] != pa[po++]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get shared prefix as many as possible. Example: "1a2b3c" and "1a2f4d" share
     * "1a2".
     */
    public static NibbleString sharedPrefix(NibbleString s1, NibbleString s2) {
        int max = Math.min(s1.length(), s2.length());
        if (max == 0) {
            return EMPTY;
        }
        int n = 0;
        while (n < max) {
            if (s1.valueAt(n) != s2.valueAt(n)) {
                return s1.substring(0, n);
            }
            n++;
        }
        return s1.substring(0, max);
    }
}
