package com.itranswarp.eth.smt;

import java.util.ArrayList;
import java.util.List;

/**
 * A persist sparse merkle tree.
 */
public class PersistSparseMerkleTree {

    private final TreeStore store;
    private final FullNode root;

    static void check(PersistSparseMerkleTree smt, SimpleSparseMerkleTree vsmt) {
        smt.print();
        String expected = SmtUtils.toHexString(vsmt.calculateMerkleRoot());
        String actual = SmtUtils.toHexString(smt.getMerkleRoot());
        boolean ok = expected.equals(actual);
        System.out.println((ok ? "[OK]" : "[FAILED]") + " expected = " + expected + ", actual = " + actual);
    }

    /**
     * Construct a sparse-merkle-tree.
     */
    public PersistSparseMerkleTree(TreeStore store, byte[] rootHash) {
        this.store = store;
        if (rootHash == null) {
            this.root = new FullNode(0, NibbleString.EMPTY, 0);
            this.store.save(List.of(PersistNode.serialize(this.root)));
        } else {
            this.root = (FullNode) this.store.loadRoot(rootHash);
        }
    }

    /**
     * Get root hash.
     * 
     * @return
     */
    public byte[] getMerkleRoot() {
        return this.root.getTopHash();
    }

    /**
     * Get root node.
     * 
     * @return
     */
    public Node getRootNode() {
        return this.root;
    }

    /**
     * Get number.
     * 
     * @return
     */
    public long getNumber() {
        return this.root.getNumber();
    }

    /**
     * Update single address.
     * 
     * @param address   Ethereum address like 0x1234...abcd. All lowercase.
     * @param dataValue Binary data.
     */
    public void update(String address, byte[] dataValue) {
        update(SmtUtils.fromHexString(address.substring(2)), dataValue);
    }

    /**
     * Update 2 addresses with 2 binary data.
     * 
     * @param address1   Ethereum address like 0x1234...abcd. All lowercase.
     * @param dataValue1 Binary data.
     * @param address2   Ethereum address like 0x1234...abcd. All lowercase.
     * @param dataValue2 Binary data.
     */
    public void update(String address1, byte[] dataValue1, String address2, byte[] dataValue2) {
        update(SmtUtils.fromHexString(address1.substring(2)), dataValue1, SmtUtils.fromHexString(address2.substring(2)), dataValue2);
    }

    /**
     * Update address with binary data.
     * 
     * @param address
     * @param dataValue
     */
    public void update(byte[] address, byte[] dataValue) {
        long number = getNumber() + 1;
        List<Node> collector = new ArrayList<>();
        this.root.update(collector, this.store, number, SmtUtils.addressToPath(address), dataValue);
        this.batchStore(collector);
    }

    /**
     * Update 2 addresses with 2 binary data.
     * 
     * @param address1   Ethereum address like 0x1234...abcd. All lowercase.
     * @param dataValue1 Binary data.
     * @param address2   Ethereum address like 0x1234...abcd. All lowercase.
     * @param dataValue2 Binary data.
     */
    public void update(byte[] address1, byte[] dataValue1, byte[] address2, byte[] dataValue2) {
        long number = getNumber() + 1;
        List<Node> collector = new ArrayList<>();
        this.root.update(collector, this.store, number, SmtUtils.addressToPath(address1), dataValue1);
        this.root.update(collector, this.store, number, SmtUtils.addressToPath(address2), dataValue2);
        this.batchStore(collector);
    }

    private void batchStore(List<Node> collector) {
        // remove duplicate nodes:
        int index = 0;
        List<PersistNode> pnodes = new ArrayList<>();
        for (Node node : collector) {
            int firstIndex = collector.indexOf(node);
            if (firstIndex == index) {
                pnodes.add(PersistNode.serialize(node));
            }
            index++;
        }
        this.store.save(pnodes);
    }

    /**
     * For debug.
     */
    public void print() {
        System.out.println("---- Begin Sparse Merkle Tree " + SmtUtils.toHexString(this.root.getTopHash()) + " ----");
        boolean[] isLast = new boolean[41];
        isLast[0] = true;
        this.root.print(0, isLast);
        System.out.println("---- End Sparse Merkle Tree ----\n");
    }
}
