package com.itranswarp.eth.smt;

import java.util.Arrays;

/**
 * A PersistNode is ready to store or restore to a Node.
 */
public record PersistNode(long number, boolean leaf, NibbleString topPath, NibbleString path, int topLevel, byte[] topHash, byte[] nodeHash, byte[] dataValue) {

    /**
     * Serialize a node.
     * 
     * @param node Node.
     * @return Persist node.
     */
    public static PersistNode serialize(Node node) {
        boolean isLeaf = node instanceof LeafNode;
        if (isLeaf) {
            return serializeLeafNode((LeafNode) node);
        } else {
            return serializeFullNode((FullNode) node);
        }
    }

    private static PersistNode serializeFullNode(FullNode node) {
        NibbleString topPath = node.nodePath;
        if (node.topLevel < node.nodeLevel) {
            topPath = node.nodePath.substring(0, node.topLevel);
        }
        return new PersistNode(node.getNumber(), false, topPath, node.nodePath, node.getTopLevel(), node.getTopHash(), node.getNodeHash(), null);
    }

    private static PersistNode serializeLeafNode(LeafNode node) {
        NibbleString topPath = node.address;
        if (node.topLevel < 40) {
            topPath = node.address.substring(0, node.topLevel);
        }
        return new PersistNode(node.getNumber(), true, topPath, node.address, node.getTopLevel(), node.getTopHash(), node.getNodeHash(), node.getDataValue());
    }

    /**
     * Deserialize a node.
     * 
     * @return Node.
     */
    public Node deserialize() {
        boolean isLeaf = this.path.length() == 40;
        if (isLeaf) {
            LeafNode node = new LeafNode(this.number, this.path, this.topLevel, this.dataValue);
            if (!Arrays.equals(this.topHash, node.topHash)) {
                throw new IllegalStateException("Top hash unmatched after deserialize.");
            }
            return node;
        } else {
            FullNode node = new FullNode(this.number, this.path, this.topLevel);
            node.topHash = this.topHash;
            node.nodeHash = this.nodeHash;
            return node;
        }
    }

    @Override
    public String toString() {
        String data = "null";
        if (dataValue != null) {
            data = SmtUtils.toHexString(dataValue);
            if (data.length() > 8) {
                data = data.substring(0, 8) + "...";
            }
        }
        return String.format("PersistableNode [number=%s, topPath=%s, path=%s, %s -> %s, topHash=%s, nodeHash=%s, dataValue=%s]", number, topPath, path,
                topLevel, path.length(), SmtUtils.toHexString(topHash).substring(0, 8), SmtUtils.toHexString(nodeHash).substring(0, 8), data);
    }
}
