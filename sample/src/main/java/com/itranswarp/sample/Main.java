package com.itranswarp.sample;

import java.math.BigInteger;

import com.itranswarp.eth.smt.MemoryTreeStore;
import com.itranswarp.eth.smt.PersistSparseMerkleTree;

public class Main {

    public static void main(String[] args) {
        String address1 = "0x1234567890123456789012345678901234567890"; // ends with 0
        String address2 = "0x123456789012345678901234567890123456789e"; // ends with e
        String address3 = "0x2234567890123456789012345678901234567890"; // starts with 2
        String address4 = "0x223456789d123456789012345678901234567890"; // share prefix 0x223456789
        MemoryTreeStore persistStore = null;
        byte[] persistRoot = null;
        {
            var store = new MemoryTreeStore();
            var psmt = new PersistSparseMerkleTree(store, null);
            psmt.update(address1, "addr-1".getBytes());
            psmt.update(address2, "addr-2".getBytes());
            persistStore = store.copy();
            persistRoot = psmt.getMerkleRoot();
            psmt.update(address3, "addr-3".getBytes());
            psmt.update(address4, "addr-4".getBytes());
            psmt.update(address1, "changed".getBytes());
            psmt.print();
            System.out.printf("%40x", new BigInteger(1, psmt.getMerkleRoot()));
        }
        {
            var psmt = new PersistSparseMerkleTree(persistStore, persistRoot);
            psmt.update(address3, "addr-3".getBytes());
            psmt.update(address4, "addr-4".getBytes());
            psmt.update(address1, "changed".getBytes());
            psmt.print();
            System.out.printf("%40x", new BigInteger(1, psmt.getMerkleRoot()));
        }
    }
}
