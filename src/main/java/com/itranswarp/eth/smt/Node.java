package com.itranswarp.eth.smt;

/**
 * Node for sparse merkle tree.
 */
public abstract class Node {

    /**
     * Get node's last updated block number.
     * 
     * @return block number when updated.
     */
    public abstract long getNumber();

    /**
     * Get node's path. The path of the root node is empty (""). The path of the
     * leaf node is address. The path of the other node is prefix of address.
     * 
     * @return Node's path.
     */
    public abstract NibbleString getPath();

    /**
     * Get top level of this node.
     * 
     * @return Node's top level.
     */
    public abstract int getTopLevel();

    /**
     * Get node's top hash which is equals to the parent node's child hash.
     * 
     * @return Node's top hash.
     */
    public abstract byte[] getTopHash();

    /**
     * Get node's hash. The leaf node hash is equals to the hash of leaf data.
     * 
     * @return Node's hash.
     */
    public abstract byte[] getNodeHash();

    /**
     * For debug only.
     * 
     * @param indent Indent space.
     * @param isLast Is last node.
     */
    public abstract void print(int indent, boolean[] isLast);
}
