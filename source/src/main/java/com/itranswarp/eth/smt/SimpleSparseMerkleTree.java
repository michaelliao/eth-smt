package com.itranswarp.eth.smt;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple sparse merkle tree used for test.
 */
public class SimpleSparseMerkleTree {

    private Map<BigInteger, LeafData> leafs = new HashMap<>(100000);

    /**
     * Get leaf data by address, or empty bytes if no data.
     * 
     * @param address Address.
     * @return Bytes array. Empty if no data.
     */
    public byte[] getLeafData(String address) {
        BigInteger index = new BigInteger(address.substring(2), 16);
        LeafData leaf = leafs.get(index);
        if (leaf == null) {
            return SmtUtils.EMPTY_DATA;
        }
        return leaf.data;
    }

    /**
     * Update an address with binary data.
     * 
     * @param address   Address.
     * @param dataValue Binary value.
     */
    public void update(String address, byte[] dataValue) {
        BigInteger index = new BigInteger(address.substring(2), 16);
        leafs.put(index, new LeafData(dataValue));
    }

    /**
     * Calculate root hash.
     * 
     * @return Root hash.
     */
    public byte[] calculateMerkleRoot() {
        if (this.leafs.isEmpty()) {
            return TreeInfo.getDefaultHash(0);
        }
        Map<BigInteger, byte[]> results = new HashMap<>(this.leafs.size());
        for (BigInteger key : this.leafs.keySet()) {
            results.put(key, this.leafs.get(key).hash);
        }
        for (int i = 160; i > 0; i--) {
            Map<BigInteger, byte[]> tops = new HashMap<>(results.size() / 2);
            for (BigInteger index : results.keySet()) {
                BigInteger leftIndex;
                BigInteger rightIndex;
                if (index.testBit(0)) {
                    // 1, 3, 5...
                    leftIndex = index.subtract(BigInteger.ONE);
                    rightIndex = index;
                } else {
                    // 0, 2, 4...
                    leftIndex = index;
                    rightIndex = index.add(BigInteger.ONE);
                }
                byte[] leftHash = results.get(leftIndex);
                byte[] rightHash = results.get(rightIndex);
                if (leftHash == null) {
                    leftHash = TreeInfo.getDefaultHash(i);
                }
                if (rightHash == null) {
                    rightHash = TreeInfo.getDefaultHash(i);
                }
                byte[] topHash = SmtUtils.keccak(leftHash, rightHash);
                BigInteger topIndex = leftIndex.shiftRight(1);
                tops.put(topIndex, topHash);
            }
            results = tops;
        }
        return results.get(BigInteger.ZERO);
    }
}

class LeafData {

    byte[] hash;
    byte[] data;

    LeafData(byte[] data) {
        setData(data);
    }

    void setData(byte[] data) {
        this.data = data;
        this.hash = SmtUtils.keccak(data);
    }
}
