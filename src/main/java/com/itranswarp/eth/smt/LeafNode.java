package com.itranswarp.eth.smt;

/**
 * Represent a leaf node.
 */
public class LeafNode extends Node {

    long number;

    /**
     * address as index of leaf node.
     */
    final NibbleString address;

    /**
     * Node top level.
     */
    int topLevel;

    byte[] topHash;

    /**
     * data hash of this node.
     */
    byte[] dataHash;

    /**
     * data of this node.
     */
    byte[] dataValue;

    @Override
    public long getNumber() {
        return this.number;
    }

    LeafNode(long number, NibbleString address, int topLevel, byte[] dataValue) {
        assert address != null && address.length() == 40 : "invalid address: " + address;
        assert dataValue != null && dataValue.length > 0 && dataValue.length % 32 == 0
                : "invalid value: " + (dataValue == null ? "null" : SmtUtils.toHexString(dataValue));
        this.number = number;
        this.address = address;
        this.dataHash = SmtUtils.keccak(dataValue);
        this.dataValue = dataValue;

        this.topLevel = topLevel;
        this.topHash = SmtUtils.keccakMerkleByRange(160, address.substring(topLevel), this.dataHash);
    }

    @Override
    public NibbleString getPath() {
        return this.address;
    }

    @Override
    public int getTopLevel() {
        return this.topLevel;
    }

    @Override
    public byte[] getTopHash() {
        return this.topHash;
    }

    @Override
    public byte[] getNodeHash() {
        return this.dataHash;
    }

    /**
     * Get data of leaf node.
     * 
     * @return Binary data.
     */
    public byte[] getDataValue() {
        return this.dataValue;
    }

    /**
     * Update leaf node's number, data value and data hash.
     * 
     * @param number    Version.
     * @param topLevel  Top level.
     * @param dataValue Binary data.
     */
    public void update(long number, int topLevel, byte[] dataValue) {
        this.number = number;
        this.dataValue = dataValue;
        this.dataHash = SmtUtils.keccak(dataValue);
        this.topLevel = topLevel;
        this.topHash = SmtUtils.keccakMerkleByRange(160, address.substring(topLevel), this.dataHash);
    }

    @Override
    public void print(int indent, boolean[] isLast) {
        StringBuilder sb = new StringBuilder(256);
        for (int i = 1; i < indent; i++) {
            sb.append(isLast[i] ? "   " : "│  ");
        }
        sb.append(isLast[indent] ? "└─ " : "├─ ");
        sb.append(this);
        System.out.println(sb.toString());
    }

    @Override
    public String toString() {
        String data = SmtUtils.toHexString(this.dataValue);
        if (data.length() > 8) {
            data = data.substring(0, 8) + "...";
        }
        return String.format("LeafNode(number=%s, nodePath=%s, %s -> %s, topHash=%s, dataHash=%s, dataValue=%s)", this.number, this.address, this.topLevel, 40,
                SmtUtils.toHexString(this.topHash).substring(0, 8), SmtUtils.toHexString(this.dataHash).substring(0, 8), data);
    }
}
