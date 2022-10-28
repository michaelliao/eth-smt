package com.itranswarp.eth.smt;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent a full node with 16 children.
 */
public class FullNode extends Node {

    /**
     * Block number of this node.
     */
    long number;

    /**
     * Top level of this node.
     */
    int topLevel;

    /**
     * Node level of this node.
     */
    final int nodeLevel;

    /**
     * Path from root to current node level.
     */
    final NibbleString nodePath;

    /**
     * Merkle hash of this node.
     */
    byte[] nodeHash;

    /**
     * Merkle hash of this node.
     */
    byte[] topHash;

    /**
     * 16 nodes represents 4-depth sub-tree
     */
    private Node[] children;

    /**
     * 16 boolean value indicate if the child is loaded from store.
     */
    private boolean[] childrenLoaded;

    FullNode(long number, NibbleString nodePath, int topLevel) {
        this.number = number;
        this.topLevel = topLevel;
        this.nodeLevel = nodePath.length();
        this.nodePath = nodePath;
        this.nodeHash = TreeInfo.getDefaultHash(this.nodeLevel * 4);
        this.topHash = TreeInfo.getDefaultHash(this.topLevel * 4);
        this.children = new Node[16];
        this.childrenLoaded = new boolean[16];
    }

    public void updateTopLevel(long number, int topLevel) {
        assert topLevel <= this.nodeLevel;
        this.number = number;
        this.topLevel = topLevel;
        this.topHash = SmtUtils.keccakMerkleByRange(this.nodeLevel * 4, this.nodePath.substring(this.topLevel), this.nodeHash);
    }

    @Override
    public long getNumber() {
        return this.number;
    }

    @Override
    public NibbleString getPath() {
        return this.nodePath;
    }

    @Override
    public byte[] getNodeHash() {
        return nodeHash;
    }

    @Override
    public int getTopLevel() {
        return this.topLevel;
    }

    @Override
    public byte[] getTopHash() {
        return topHash;
    }

    public void update(final List<Node> collector, final TreeStore store, final long currentNumber, final NibbleString address, final byte[] dataValue) {
        this.number = currentNumber;
        NibbleString prefix = NibbleString.sharedPrefix(this.nodePath, address);
        assert prefix.length() >= this.nodeLevel
                : "wrong prefix " + prefix + " for address " + address + " with node path " + this.nodePath + " and node level " + this.nodeLevel;
        int childIndex = address.valueAt(this.nodeLevel);
        Node child = loadChild(store, currentNumber, childIndex);
        if (child == null) {
            // insert new leaf:
            LeafNode childLeaf = new LeafNode(currentNumber, address, this.nodeLevel + 1, dataValue);
            this.children[childIndex] = childLeaf;
            collector.add(childLeaf);
            this.updateHash(store, currentNumber);
            collector.add(this);
        } else if (child instanceof FullNode) {
            // child is a full node:
            final FullNode existNode = (FullNode) child;
            final NibbleString existNodePath = existNode.nodePath;
            if (address.startsWith(existNode.nodePath)) {
                existNode.update(collector, store, currentNumber, address, dataValue);
                this.updateHash(store, currentNumber);
                collector.add(this);
            } else {
                // existNode = current child, now insert a splitNode to build:
                // current child -> splitNode -> existNode
                final NibbleString sharedPrefix = NibbleString.sharedPrefix(address, existNodePath);
                final int splitNodeLevel = sharedPrefix.length();
                existNode.updateTopLevel(currentNumber, splitNodeLevel + 1);
                collector.add(existNode);
                final FullNode splitNode = new FullNode(currentNumber, sharedPrefix, this.nodeLevel + 1);
                // move exist node to split node child:
                splitNode.children[existNodePath.valueAt(splitNodeLevel)] = existNode;
                // add new:
                splitNode.update(collector, store, currentNumber, address, dataValue);
                // set split node as current child:
                this.children[childIndex] = splitNode;
                this.updateHash(store, currentNumber);
                collector.add(this);
            }
        } else {
            // child is a leaf node:
            final LeafNode existLeaf = (LeafNode) child;
            final NibbleString existLeafAddress = existLeaf.address;
            if (address.equals(existLeafAddress)) {
                // leaf node with same address:
                existLeaf.update(currentNumber, existLeaf.topLevel, dataValue);
                collector.add(existLeaf);
                this.updateHash(store, currentNumber);
                collector.add(this);
            } else {
                // leaf node with different address:
                final NibbleString sharedPrefix = NibbleString.sharedPrefix(address, existLeafAddress);
                final int splitNodeLevel = sharedPrefix.length();
                existLeaf.update(currentNumber, splitNodeLevel + 1, existLeaf.getDataValue());
                collector.add(existLeaf);
                final FullNode splitNode = new FullNode(currentNumber, sharedPrefix, this.nodeLevel + 1);
                // move exist node to split node child:
                splitNode.children[existLeafAddress.valueAt(splitNodeLevel)] = existLeaf;
                // add new:
                splitNode.update(collector, store, currentNumber, address, dataValue);
                // set split node as current child:
                this.children[childIndex] = splitNode;
                this.updateHash(store, currentNumber);
                collector.add(this);
            }
        }
    }

    private Node loadChild(TreeStore store, long currentNumber, int childIndex) {
        Node child = this.children[childIndex];
        if (child == null && !this.childrenLoaded[childIndex]) {
            // try load latest child node from store:
            NibbleString childPath = this.nodePath.join(childIndex);
            Node loadedNode = store.load(childPath, currentNumber);
            if (loadedNode != null) {
                System.out.println("loaded node: " + loadedNode);
                child = loadedNode;
                this.children[childIndex] = child;
            }
            this.childrenLoaded[childIndex] = true;
        }
        return child;
    }

    private void updateHash(TreeStore store, long currentNumber) {
        int childHeight = this.nodeLevel * 4 + 4;
        byte[][] top1Hashes = new byte[8][];
        for (int i = 0; i < 8; i++) {
            int child1Index = i * 2;
            int child2Index = child1Index + 1;
            Node child1 = loadChild(store, currentNumber, child1Index);
            Node child2 = loadChild(store, currentNumber, child2Index);
            if (child1 == null && child2 == null) {
                top1Hashes[i] = null;
            } else {
                byte[] left = child1 == null ? TreeInfo.getDefaultHash(childHeight) : child1.getTopHash();
                byte[] right = child2 == null ? TreeInfo.getDefaultHash(childHeight) : child2.getTopHash();
                top1Hashes[i] = SmtUtils.keccak(left, right);
            }
        }

        childHeight--;
        byte[][] top2Hashes = top1Hashes; // reuse top1-hash array as top2
        for (int i = 0; i < 4; i++) {
            byte[] left = top1Hashes[i * 2];
            byte[] right = top1Hashes[i * 2 + 1];
            if (left == null && right == null) {
                top2Hashes[i] = null;
            } else {
                top2Hashes[i] = SmtUtils.keccak(left != null ? left : TreeInfo.getDefaultHash(childHeight),
                        right != null ? right : TreeInfo.getDefaultHash(childHeight));
            }
        }

        childHeight--;
        byte[][] top3Hashes = top1Hashes; // reuse top1-hash array as top3
        for (int i = 0; i < 2; i++) {
            byte[] left = top2Hashes[i * 2];
            byte[] right = top2Hashes[i * 2 + 1];
            if (left == null && right == null) {
                top3Hashes[i] = null;
            } else {
                top3Hashes[i] = SmtUtils.keccak(left != null ? left : TreeInfo.getDefaultHash(childHeight),
                        right != null ? right : TreeInfo.getDefaultHash(childHeight));
            }
        }

        childHeight--;
        byte[] top3HashLeft = top3Hashes[0];
        byte[] top3HashRight = top3Hashes[1];
        this.nodeHash = SmtUtils.keccak(top3HashLeft != null ? top3HashLeft : TreeInfo.getDefaultHash(childHeight),
                top3HashRight != null ? top3HashRight : TreeInfo.getDefaultHash(childHeight));

        this.topHash = SmtUtils.keccakMerkleByRange(this.nodeLevel * 4, this.nodePath.substring(this.topLevel), this.nodeHash);
    }

    @Override
    public void print(int indent, boolean[] isLast) {
        StringBuilder sb = new StringBuilder(256);
        for (int i = 1; i < indent; i++) {
            sb.append(isLast[i] ? "   " : "│  ");
        }
        if (indent > 0) {
            sb.append(isLast[indent] ? "└─ " : "├─ ");
        }
        sb.append(this);
        System.out.println(sb);
        if (this.children != null) {
            List<Node> subs = new ArrayList<>();
            for (int i = 0; i < 16; i++) {
                Node node = this.children[i];
                if (node != null) {
                    subs.add(node);
                } else if (!this.childrenLoaded[i]) {
                    subs.add(null);
                }
            }
            for (int i = 0; i < subs.size(); i++) {
                isLast[indent + 1] = (i == subs.size() - 1);
                Node sub = subs.get(i);
                if (sub == null) {
                    StringBuilder sb2 = new StringBuilder(256);
                    for (int j = 1; j < indent + 1; j++) {
                        sb2.append(isLast[j] ? "   " : "│  ");
                    }
                    sb2.append(isLast[indent + 1] ? "└─ " : "├─ ");
                    sb2.append("???");
                    System.out.println(sb2.toString());
                } else {
                    sub.print(indent + 1, isLast);
                }
            }

        }
    }

    @Override
    public String toString() {
        return String.format("FullNode(number=%s, nodePath=%s, %s ~ %s, nodeHash=%s, topHash=%s)", this.number, this.nodePath, this.topLevel, this.nodeLevel,
                SmtUtils.toHexString(this.nodeHash).substring(0, 8), SmtUtils.toHexString(this.topHash).substring(0, 8));
    }
}
