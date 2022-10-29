package com.itranswarp.eth.smt;

import java.util.List;

/**
 * A tree store interface for persistence of nodes in sparse merkle tree.
 */
public interface TreeStore {

    /**
     * Load latest node by its top path and which number is less than current
     * number.
     * 
     * @param topPath       Top path.
     * @param currentNumber Current version.
     * @return Node.
     */
    Node load(NibbleString topPath, long currentNumber);

    /**
     * Load root node by its hash.
     * 
     * @param hash Root hash.
     * @return Node.
     */
    Node loadRoot(byte[] hash);

    /**
     * Save persistable node as batch.
     * 
     * @param pnodes Node list.
     */
    void save(List<PersistNode> pnodes);

}
