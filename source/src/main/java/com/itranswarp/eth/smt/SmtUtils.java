package com.itranswarp.eth.smt;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.bouncycastle.jcajce.provider.digest.Keccak;

class SmtUtils {

    public static final byte[] EMPTY_DATA = new byte[0];

    private static final Pattern ADDR = Pattern.compile("^0x[a-f0-9]{40}$");

    public static boolean isValidAddress(String address) {
        return ADDR.matcher(address).matches();
    }

    public static NibbleString addressToPath(String address) {
        if (!isValidAddress(address)) {
            throw new IllegalArgumentException("invalid address: " + address);
        }
        return addressToPath(fromHexString(address.substring(2)));
    }

    public static NibbleString addressToPath(byte[] address) {
        if (address.length != 20) {
            throw new IllegalArgumentException("invalid address: " + toHexString(address));
        }
        return new NibbleString(address);
    }

    /**
     * Short cut to:
     * 
     * <code>
     * keccak(byte[])
     * </code>
     */
    public static String keccak(String inputHex) {
        byte[] input = fromHexString(inputHex);
        byte[] result = keccak(input);
        return toHexString(result);
    }

    /**
     * Do keccak merkle hash.
     * 
     * @param input Input data.
     * @return Hash data.
     */
    public static byte[] keccak(byte[] input) {
        Keccak.DigestKeccak kecc = new Keccak.Digest256();
        kecc.update(input);
        return kecc.digest();
    }

    /**
     * Short cut to:
     * 
     * <code>
     * keccak(byte[], byte[])
     * </code>
     */
    public static String keccak(String leftHex, String rightHex) {
        byte[] left = fromHexString(leftHex);
        byte[] right = fromHexString(rightHex);
        byte[] result = keccak(left, right);
        return toHexString(result);
    }

    /**
     * Do keccak hash by two sequential inputs.
     * 
     * @param left  The first input hash.
     * @param right The second input hash.
     * @return The output hash.
     */
    public static byte[] keccak(byte[] left, byte[] right) {
        Keccak.DigestKeccak kecc = new Keccak.Digest256();
        kecc.update(left);
        kecc.update(right);
        return kecc.digest();
    }

    public static String keccakMerkle(String[] leafsHex) {
        byte[][] leafs = Arrays.stream(leafsHex).map(SmtUtils::fromHexString).toArray(byte[][]::new);
        byte[] result = keccakMerkle(leafs);
        return toHexString(result);
    }

    public static byte[] keccakMerkle(byte[][] leafs) {
        assert leafs.length > 0 : "invalid length: " + leafs.length;
        byte[][] results = leafs;
        while (results.length > 1) {
            results = doKeccakMerkle(results);
        }
        return results[0];
    }

    static byte[][] doKeccakMerkle(byte[][] leafs) {
        int len = leafs.length;
        if (len == 1) {
            return leafs;
        }
        if (len % 2 == 0) {
            byte[][] tops = new byte[len / 2][];
            for (int i = 0; i < tops.length; i++) {
                int j = i * 2;
                tops[i] = keccak(leafs[j], leafs[j + 1]);
            }
            return tops;
        } else {
            byte[][] tops = new byte[len / 2 + 1][];
            for (int i = 0; i < tops.length - 1; i++) {
                int j = i * 2;
                tops[i] = keccak(leafs[j], leafs[j + 1]);
            }
            int lastLeaf = leafs.length - 1;
            tops[tops.length - 1] = keccak(leafs[lastLeaf], leafs[lastLeaf]);
            return tops;
        }
    }

    public static String keccakMerkleByRange(int leafHeight, String pathRangeStr, String leafHashHex) {
        byte[] leafHash = fromHexString(leafHashHex);
        NibbleString pathRange = new NibbleString(pathRangeStr);
        byte[] result = keccakMerkleByRange(leafHeight, pathRange, leafHash);
        return toHexString(result);
    }

    public static byte[] keccakMerkleByRange(int leafHeight, NibbleString pathRange, byte[] leafHash) {
        if (pathRange.isEmpty()) {
            return leafHash;
        }
        int height = leafHeight;
        byte[] hash = leafHash;
        for (int i = pathRange.length() - 1; i >= 0; i--) {
            int index = pathRange.valueAt(i);
            hash = keccakMerkleOf1Level(height, index, hash);
            height -= 4;
        }
        return hash;
    }

    /**
     * Shortcut to:
     * 
     * <code>
     * byte[] keccakMerkleOf1Level(int leafHeight, int leafIndex, byte[] leafHash)
     * </code>
     */
    public static String keccakMerkleOf1Level(int leafHeight, int leafIndex, String leafHashHex) {
        byte[] leafHash = fromHexString(leafHashHex);
        byte[] result = keccakMerkleOf1Level(leafHeight, leafIndex, leafHash);
        return toHexString(result);
    }

    /**
     * Leafs are 16 hashes array, given the leaf index, and all other hashes are
     * default hash of the specific level. Return the merkle root.
     * 
     * @param leafHeight The height of the leaf.
     * @param leafIndex  The index of leaf.
     * @param leafHash   The hash of leaf.
     * @return The merkle root.
     */
    public static byte[] keccakMerkleOf1Level(int leafHeight, int leafIndex, byte[] leafHash) {
        // leaf height:
        boolean isLeafEven = leafIndex % 2 == 0;
        byte[] leftLeaf = isLeafEven ? leafHash : TreeInfo.getDefaultHash(leafHeight);
        byte[] rightLeaf = isLeafEven ? TreeInfo.getDefaultHash(leafHeight) : leafHash;
        int top3Index = leafIndex / 2;
        byte[] top3Hash = keccak(leftLeaf, rightLeaf);

        // top3 height:
        boolean isTop3Even = top3Index % 2 == 0;
        byte[] leftTop3 = isTop3Even ? top3Hash : TreeInfo.getDefaultHash(leafHeight - 1);
        byte[] rightTop3 = isTop3Even ? TreeInfo.getDefaultHash(leafHeight - 1) : top3Hash;
        int top2Index = top3Index / 2;
        byte[] top2Hash = keccak(leftTop3, rightTop3);

        // top2 height:
        boolean isTop2Even = top2Index % 2 == 0;
        byte[] leftTop2 = isTop2Even ? top2Hash : TreeInfo.getDefaultHash(leafHeight - 2);
        byte[] rightTop2 = isTop2Even ? TreeInfo.getDefaultHash(leafHeight - 2) : top2Hash;
        int top1Index = top2Index / 2;
        byte[] top1Hash = keccak(leftTop2, rightTop2);

        // top1 height:
        boolean isTop1Even = top1Index % 2 == 0;
        byte[] leftTop1 = isTop1Even ? top1Hash : TreeInfo.getDefaultHash(leafHeight - 3);
        byte[] rightTop1 = isTop1Even ? TreeInfo.getDefaultHash(leafHeight - 3) : top1Hash;

        // top0:
        return keccak(leftTop1, rightTop1);
    }

    /**
     * Convert bytes to hex string (all lower-case).
     *
     * @param b Input bytes.
     * @return Hex string.
     */
    public static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte x : b) {
            int hi = (x & 0xf0) >> 4;
            int lo = x & 0x0f;
            sb.append(HEX_CHARS[hi]);
            sb.append(HEX_CHARS[lo]);
        }
        return sb.toString().trim();
    }

    public static byte[] fromHexString(String s) {
        if (s.startsWith("0x")) {
            s = s.substring(2);
        }
        if (s.length() % 2 == 1) {
            throw new IllegalArgumentException("Invalid length of string.");
        }
        byte[] data = new byte[s.length() / 2];
        for (int i = 0; i < data.length; i++) {
            char c1 = s.charAt(i * 2);
            char c2 = s.charAt(i * 2 + 1);
            int n1 = HEX_STRING.indexOf(c1);
            int n2 = HEX_STRING.indexOf(c2);
            if (n1 == (-1)) {
                throw new IllegalArgumentException("Invalid char in string: " + c1);
            }
            if (n2 == (-1)) {
                throw new IllegalArgumentException("Invalid char in string: " + c2);
            }
            int n = (n1 << 4) + n2;
            data[i] = (byte) n;
        }
        return data;
    }

    private static final String HEX_STRING = "0123456789abcdef";
    private static final char[] HEX_CHARS = HEX_STRING.toCharArray();
}
