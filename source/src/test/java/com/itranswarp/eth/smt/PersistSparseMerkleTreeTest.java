package com.itranswarp.eth.smt;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

public class PersistSparseMerkleTreeTest {

    String address1 = "0x0125e02fa10caf6128207bc920ca41b85194bb79";
    String address2 = "0x0faf6128207b79e028519012f20ca41bd4b3910c";

    String address3 = "0x05e0012f20ca41bd285194b3910caf6128207b79";
    String address4 = "0x15e0012f20ca41bd285194b3910caf6128207b79";

    String address5 = "0x13579bdf0f28207b7915e0012f20ca41bd285194";
    String address6 = "0x13579bdf0f28207b7915e0012f20ca41bd285197";
    String address7 = "0x13570000d285194915e0012f21528207b70ca41b";

    String address8 = "0x123456789abcedf123456789abcdef123456789a";

    byte[] data1 = "Ab".repeat(16).getBytes();
    byte[] data2 = "Cd".repeat(16).getBytes();
    byte[] data3 = "Ef".repeat(16).getBytes();
    byte[] data4 = "Gh".repeat(16).getBytes();
    byte[] data5 = "Ij".repeat(16).getBytes();
    byte[] data6 = "Kl".repeat(16).getBytes();
    byte[] data7 = "Mn".repeat(16).getBytes();
    byte[] data8 = "Op".repeat(16).getBytes();
    byte[] dataX = "Xx".repeat(16).getBytes();
    byte[] dataY = "Yy".repeat(16).getBytes();

    byte[][] data = { data1, data2, data3, data4, data5, data6, data7, data8 };

    @Test
    void emptyTree() {
        var store = new MemoryTreeStore();
        var psmt = new PersistSparseMerkleTree(store, null);
        var ssmt = new SimpleSparseMerkleTree();
        verify(psmt, ssmt);
    }

    @Test
    void singleLeaf1() {
        var store = new MemoryTreeStore();
        var psmt = new PersistSparseMerkleTree(store, null);
        var ssmt = new SimpleSparseMerkleTree();
        update(psmt, ssmt, address1, data1);
        verify(psmt, ssmt);
        psmt.print();
        store.print();
    }

    @Test
    void singleLeaf2() {
        var store = new MemoryTreeStore();
        var psmt = new PersistSparseMerkleTree(store, null);
        var ssmt = new SimpleSparseMerkleTree();
        update(psmt, ssmt, address2, data2);
        verify(psmt, ssmt);
        psmt.print();
        store.print();
    }

    @Test
    void twoLeafsUnderRoot() {
        var store = new MemoryTreeStore();
        var psmt = new PersistSparseMerkleTree(store, null);
        var ssmt = new SimpleSparseMerkleTree();
        update(psmt, ssmt, address3, data3);
        update(psmt, ssmt, address4, data4);
        verify(psmt, ssmt);
    }

    @Test
    void twoLeafsUnderRoot2() {
        var store = new MemoryTreeStore();
        var psmt = new PersistSparseMerkleTree(store, null);
        var ssmt = new SimpleSparseMerkleTree();
        update(psmt, ssmt, address3, data3, address4, data4);
        verify(psmt, ssmt);
    }

    @Test
    void twoLeafsShare1Level() {
        var store = new MemoryTreeStore();
        var psmt = new PersistSparseMerkleTree(store, null);
        var ssmt = new SimpleSparseMerkleTree();
        update(psmt, ssmt, address1, data1);
        update(psmt, ssmt, address2, data2);
        verify(psmt, ssmt);
    }

    @Test
    void twoLeafsShare1Level2() {
        var store = new MemoryTreeStore();
        var psmt = new PersistSparseMerkleTree(store, null);
        var ssmt = new SimpleSparseMerkleTree();
        update(psmt, ssmt, address1, data1, address2, data2);
        verify(psmt, ssmt);
    }

    @Test
    void splitFullNode() {
        var store = new MemoryTreeStore();
        var psmt = new PersistSparseMerkleTree(store, null);
        var ssmt = new SimpleSparseMerkleTree();
        update(psmt, ssmt, address5, data5);
        update(psmt, ssmt, address6, data6);
        update(psmt, ssmt, address7, data7);
        verify(psmt, ssmt);
        psmt.print();
        store.print();
    }

    @Test
    void splitFullNode2() {
        var store = new MemoryTreeStore();
        var psmt = new PersistSparseMerkleTree(store, null);
        var ssmt = new SimpleSparseMerkleTree();
        update(psmt, ssmt, address5, data5, address6, data6);
        update(psmt, ssmt, address7, data7);
        verify(psmt, ssmt);
        psmt.print();
        store.print();
    }

    @Test
    void splitFullNode3() {
        var store = new MemoryTreeStore();
        var psmt = new PersistSparseMerkleTree(store, null);
        var ssmt = new SimpleSparseMerkleTree();
        update(psmt, ssmt, address5, data5);
        update(psmt, ssmt, address6, data6, address7, data7);
        verify(psmt, ssmt);
        psmt.print();
        store.print();
    }

    @Test
    void full40level() {
        var store = new MemoryTreeStore();
        var psmt = new PersistSparseMerkleTree(store, null);
        var ssmt = new SimpleSparseMerkleTree();
        update(psmt, ssmt, address8, data8);
        for (int i = 0; i <= 40; i++) {
            String addr = address8.substring(0, 42 - i);
            while (addr.length() < 42) {
                addr = addr + "0";
            }
            System.out.println(addr);
            update(psmt, ssmt, addr, data[i % 8]);
        }
        verify(psmt, ssmt);
        psmt.print();
    }

    @Test
    void getLeafs() {
        var store = new MemoryTreeStore();
        var psmt = new PersistSparseMerkleTree(store, null);
        var ssmt = new SimpleSparseMerkleTree();
        update(psmt, ssmt, address5, data5);
        update(psmt, ssmt, address6, data6);
        update(psmt, ssmt, address7, data7);
        assertArrayEquals(data5, psmt.getLeafData(address5));
        assertArrayEquals(data6, psmt.getLeafData(address6));
        assertArrayEquals(data7, psmt.getLeafData(address7));
        verify(psmt, ssmt);
        // make a copy:
        var snapshot1Copy = store.copy();
        var snapshot1Root = psmt.getMerkleRoot();

        // continue update 5, 6:
        update(psmt, ssmt, address5, dataX);
        update(psmt, ssmt, address6, dataY);

        // make a copy:
        var snapshot2Copy = store.copy();
        var snapshot2Root = psmt.getMerkleRoot();

        // check snapshot 1:
        var snapshot1 = new PersistSparseMerkleTree(snapshot1Copy, snapshot1Root);
        assertArrayEquals(data5, snapshot1.getLeafData(address5));
        assertArrayEquals(data6, snapshot1.getLeafData(address6));
        assertArrayEquals(data7, snapshot1.getLeafData(address7));

        // check snapshot 2:
        var snapshot2 = new PersistSparseMerkleTree(snapshot2Copy, snapshot2Root);
        assertArrayEquals(dataX, snapshot2.getLeafData(address5));
        assertArrayEquals(dataY, snapshot2.getLeafData(address6));
        assertArrayEquals(data7, snapshot2.getLeafData(address7));
    }

    @Test
    void randomAddresses() {
        int ADDRESSES = 100;
        String[] addresses = new String[ADDRESSES];
        PseudoRandom random = new PseudoRandom(0x123456789L);
        for (int i = 0; i < ADDRESSES; i++) {
            addresses[i] = "0x" + SmtUtils.toHexString(random.randomBytes(20));
        }
        var store = new MemoryTreeStore();
        var psmt = new PersistSparseMerkleTree(store, null);
        var ssmt = new SimpleSparseMerkleTree();
        for (int i = 0; i < 50; i++) {
            update(psmt, ssmt, addresses[i], data[i % 8]);
        }
        String root1 = SmtUtils.toHexString(psmt.getMerkleRoot());
        System.out.println("Root 50 = " + root1);
        psmt.print();
        // create snapshot of store:
        var store1 = store.copy();

        for (int i = 50; i < ADDRESSES; i++) {
            update(psmt, ssmt, addresses[i], data[i % 8]);
            System.out.println(psmt.getNumber() + " = " + SmtUtils.toHexString(psmt.getMerkleRoot()));
        }
        verify(psmt, ssmt);
        String endRoot = "be7c9cd9dd2d47eb21a690da181c30fa5c881ec587d1b5f61021687b618b428e";
        assertEquals(endRoot, SmtUtils.toHexString(psmt.getMerkleRoot()));

        // start from store1:
        store1.print();
        assertTrue(store1.topPathMap.size() < store.topPathMap.size());
        var psmt1 = new PersistSparseMerkleTree(store1, SmtUtils.fromHexString(root1));
        for (int i = 50; i < ADDRESSES; i++) {
            psmt1.update(addresses[i], data[i % 8]);
            System.out.println(psmt1.getNumber() + " = " + SmtUtils.toHexString(psmt1.getMerkleRoot()));
        }
        psmt1.print();
        assertEquals(endRoot, SmtUtils.toHexString(psmt1.getMerkleRoot()));
    }

    @Test
    void randomAddresses2() {
        int ADDRESSES = 100;
        String[] addresses = new String[ADDRESSES];
        PseudoRandom random = new PseudoRandom(0x123456789L);
        for (int i = 0; i < ADDRESSES; i++) {
            addresses[i] = "0x" + SmtUtils.toHexString(random.randomBytes(20));
        }
        var store = new MemoryTreeStore();
        var psmt = new PersistSparseMerkleTree(store, null);
        var ssmt = new SimpleSparseMerkleTree();
        for (int i = 0; i < 50; i += 2) {
            update(psmt, ssmt, addresses[i], data[i % 8], addresses[i + 1], data[(i + 1) % 8]);
        }
        String root1 = SmtUtils.toHexString(psmt.getMerkleRoot());
        System.out.println("Root 50 = " + root1);
        psmt.print();
        // create snapshot of store:
        var store1 = store.copy();

        for (int i = 50; i < ADDRESSES; i += 2) {
            update(psmt, ssmt, addresses[i], data[i % 8], addresses[i + 1], data[(i + 1) % 8]);
            System.out.println(psmt.getNumber() + " = " + SmtUtils.toHexString(psmt.getMerkleRoot()));
        }
        verify(psmt, ssmt);
        String endRoot = "be7c9cd9dd2d47eb21a690da181c30fa5c881ec587d1b5f61021687b618b428e";
        assertEquals(endRoot, SmtUtils.toHexString(psmt.getMerkleRoot()));

        // start from store1:
        store1.print();
        assertTrue(store1.topPathMap.size() < store.topPathMap.size());
        var psmt1 = new PersistSparseMerkleTree(store1, SmtUtils.fromHexString(root1));
        for (int i = 50; i < ADDRESSES; i += 2) {
            psmt1.update(addresses[i], data[i % 8], addresses[i + 1], data[(i + 1) % 8]);
            System.out.println(psmt1.getNumber() + " = " + SmtUtils.toHexString(psmt1.getMerkleRoot()));
        }
        psmt1.print();
        assertEquals(endRoot, SmtUtils.toHexString(psmt1.getMerkleRoot()));
    }

    @Test
    void largeRandomAddresses() {
        var store = new MemoryTreeStore();
        var psmt = new PersistSparseMerkleTree(store, null);
        int ADDRESSES = 10000;
        PseudoRandom random = new PseudoRandom(0x123456789abcL);
        System.out.println("generate " + ADDRESSES + " addresses...");
        List<byte[]> addresses = new ArrayList<>(ADDRESSES);
        for (int i = 0; i < ADDRESSES; i++) {
            addresses.add(random.randomBytes(20));
        }
        System.out.println("start test...");
        System.gc();
        long start = System.currentTimeMillis();
        int n = 0;
        for (byte[] address : addresses) {
            psmt.update(address, data[n % 8]);
            n++;
        }
        long end = System.currentTimeMillis();
        System.gc();
        long endFree = Runtime.getRuntime().freeMemory();
        long endTotal = Runtime.getRuntime().totalMemory() - endFree;
        System.out.println("time: " + (end - start) + " ms.");
        System.out.println("tps: " + ADDRESSES * 1000 / (end - start) + "/s.");
        System.out.println("total: " + (endTotal / (1024 * 1024)) + " mb.");
        System.out.println("free: " + (endFree / (1024 * 1024)) + " mb.");
    }

    void update(PersistSparseMerkleTree psmt, SimpleSparseMerkleTree ssmt, String address, byte[] data) {
        psmt.update(address, data);
        ssmt.update(address, data);
    }

    void update(PersistSparseMerkleTree psmt, SimpleSparseMerkleTree ssmt, String address1, byte[] data1, String address2, byte[] data2) {
        psmt.update(address1, data1, address2, data2);
        ssmt.update(address1, data1);
        ssmt.update(address2, data2);
    }

    void verify(PersistSparseMerkleTree psmt, SimpleSparseMerkleTree ssmt) {
        String actual = SmtUtils.toHexString(psmt.getMerkleRoot());
        String expected = SmtUtils.toHexString(ssmt.calculateMerkleRoot());
        if (!expected.equals(actual)) {
            System.out.println("FAILED:");
            psmt.print();
        }
        assertEquals(expected, actual);
    }
}

class PseudoRandom {

    final Random random;

    PseudoRandom(long seed) {
        random = new Random(seed);
    }

    byte[] randomBytes(int len) {
        byte[] bytes = new byte[len];
        random.nextBytes(bytes);
        return bytes;
    }
}
