package com.itranswarp.eth.smt;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class SimpleSparseMerkleTreeTest {

    final static byte[] DATA_1 = SmtUtils.fromHexString("57c65f1718e8297f4048beff2419e134656b7a856872b27ad77846e395f13ffe");
    final static byte[] DATA_2 = SmtUtils
            .fromHexString("6d91615c65c0e8f861b0fbfce2d9897fb942293e341eda10c91a6912c4f326df35023d41155193377de81fea14c4f2d4969ec7b684f6f0906b820126a699af68");
    final static byte[] DATA_3 = SmtUtils.fromHexString(
            "dab37bfba6e1ca0fb36d7f9a68deef33c72b4d5cfdc0ea73acc50cee7d09771e12ed8b009b0d73e6fd927a4bb8eab455521683a657894fe0656c004dca113c9b3fb619b966a511ac21601b194ce0d9d0644127842b5b3238c31ab55005e8b899");

    final static String ADDRESS_1 = "0x352f56a8bd2fc45c4412d0d3671e97f49eabfb93";
    final static String ADDRESS_2 = "0xd2f90decd954856a845cf49e2d0d3671417b933b";
    final static String ADDRESS_3 = "0x800fa61b0fbfc3160ef3e2d9897fb942293e341e";

    final static String DEFAULT_ROOT = "bf5c096ee07bbfcb49f3b40465e55afd1ab3c6cbeef2b8032aee65ccb216df69";

    @Test
    void calculateMerkleRootOfEmpty() {
        assertEquals(DEFAULT_ROOT, SmtUtils.toHexString(TreeInfo.getDefaultHash(0)));
        {
            var tree = new SimpleSparseMerkleTree();
            var root = tree.calculateMerkleRoot();
            assertEquals(DEFAULT_ROOT, SmtUtils.toHexString(root));
        }
        {
            var tree = new SimpleSparseMerkleTree();
            tree.update(ADDRESS_1, new byte[0]);
            tree.update(ADDRESS_2, new byte[0]);
            tree.update(ADDRESS_3, new byte[0]);
            var root = tree.calculateMerkleRoot();
            assertEquals(DEFAULT_ROOT, SmtUtils.toHexString(root));
        }
    }

    @Test
    void calculateMerkleRootOf1Leaf() {
        {
            var tree = new SimpleSparseMerkleTree();
            tree.update(ADDRESS_1, DATA_1);
            var root = tree.calculateMerkleRoot();
            assertEquals("7a582a6d28414e328910b89d69bf97869acf2cfe3eca40f3212fb59baace4c02", SmtUtils.toHexString(root));
        }
        {
            var tree = new SimpleSparseMerkleTree();
            tree.update(ADDRESS_2, DATA_1);
            var root = tree.calculateMerkleRoot();
            assertEquals("555f043ed810f712a082ac4f0f8546087064398c1066295ab6e18066ed9d5abb", SmtUtils.toHexString(root));
        }
        {
            var tree = new SimpleSparseMerkleTree();
            tree.update(ADDRESS_1, DATA_1);
            tree.update(ADDRESS_2, DATA_2);
            tree.update(ADDRESS_3, DATA_3);
            var root = tree.calculateMerkleRoot();
            assertEquals("0f57aa6ae4c0b2230a33f598c52165da734d25f2390fa508a7a95259333d2375", SmtUtils.toHexString(root));
        }
    }
}
