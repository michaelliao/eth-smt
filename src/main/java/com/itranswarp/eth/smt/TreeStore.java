package com.itranswarp.eth.smt;

import java.util.List;

/**
 * A tree store interface for persistence of nodes in sparse merkle tree.
 */
public interface TreeStore {

    /**
     * Load latest node by its top path and which number is less than current
     * number.
     */
    Node load(NibbleString topPath, long currentNumber);

    /**
     * Load root node by its hash.
     */
    Node loadRoot(byte[] hash);

    /**
     * Save persistable node as batch.
     */
    void save(List<PersistNode> pnodes);

}
