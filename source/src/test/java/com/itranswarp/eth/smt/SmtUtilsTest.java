package com.itranswarp.eth.smt;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class SmtUtilsTest {

    @ParameterizedTest
    @MethodSource
    void keccakSingleData(String expected, String inputStrData) {
        byte[] hash = SmtUtils.keccak(inputStrData.getBytes());
        assertEquals(expected, SmtUtils.toHexString(hash));
    }

    static List<Arguments> keccakSingleData() {
        return List.of( //
                Arguments.of("c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470", ""), //
                Arguments.of("03783fac2efed8fbc9ad443e592ee30e61d65f471140c10ca155e937b435b760", "A"), //
                Arguments.of("1c8aff950685c2ed4bc3174f3472287b56d9517b9c948127319a09a7a36deac8", "hello"), //
                Arguments.of("18296c188ed03eb9be91af1838d87644870affae813bb3f39ca7b2eaecdae1e0", "hello, world."), //
                Arguments.of("360054265d535c5ac43c0b1467bfb4a4792e9fa2bf2d3bc451132b09e44d037b", "hi, this is a 32 bytes test data"), //
                Arguments.of("9a57067ee21cfc99ac55dbc3bc5e6f9461dbd7ffc60b79a12f3e93c9c987f0cf", "0123456789abcdef0123456789abcdef"), //
                Arguments.of("290decd9548b62a8d60345a988386fc84ba6bc95484008f6362f93160ef3e563", "\0".repeat(32)), //
                Arguments.of("899c4737ad9cc3532493e7d131fca42ec35456cc014173651e07a928cdb6d996", "0123456789abcdef0123456789abcdef0123456789abcdef-xyz"));
    }

    @ParameterizedTest
    @MethodSource
    void keccakSimpleMerkle(String expected, String left, String right) {
        assertEquals(expected, SmtUtils.keccak(left, right));
    }

    static List<Arguments> keccakSimpleMerkle() {
        final String left1 = "8cce65b2992eebfc9bcd56dc1db87a45a75cffa46617f31317a2254d14c381ce";
        final String right1 = "cf3a90558602bb70e2c935e447b60de2e392e3d7f535e35e77fe6cdc7d635d9a";

        final String left2 = "9a57067ee21cfc99ac55dbc3bc5e6f9461dbd7ffc60b79a12f3e93c9c987f0cf";
        final String right2 = "9a57067ee21cfc99ac55dbc3bc5e6f9461dbd7ffc60b79a12f3e93c9c987f0ce";

        final String hash0 = "290decd9548b62a8d60345a988386fc84ba6bc95484008f6362f93160ef3e563";
        final String hashEmpty = "c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470";

        return List.of( //
                // switch left, right:
                Arguments.of("f3fdc852d6d7df177476aafee1aab0bb3b0456e11dfd392a0bee07c545274dc4", left1, right1), //
                Arguments.of("babd05a3d64888bd6078386103d7607e2a55cda74135b5496e044e3ca090117d", right1, left1), //

                Arguments.of("a8357ed01197ecd80a59ef015206c34da2b9fe27215f7f3a1bc6130a6831239d", left2, right2), //
                Arguments.of("c6138e501daba3db3b0168ff769303450077026e6cedab4723de9869e05467c5", right2, left2), //

                // same left, right:
                Arguments.of("633dc4d7da7256660a892f8f1604a44b5432649cc8ec5cb3ced4c4e6ac94dd1d", hash0, hash0), //

                Arguments.of("9c6b2c1b0d0b25a008e6c882cc7b415f309965c72ad2b944ac0931048ca31cd5", hashEmpty, hashEmpty));
    }

    @Test
    void keccakMerkleFromLeafsToRoot() {
        String single = "458b30c2d72bfd2c6317304a4594ecbafe5f729d3111b65fdc3a33bd48e5432d";
        assertEquals(single, SmtUtils.keccakMerkle(new String[] { single }));

        String[] leafsOf2Test1 = { "0000000000000000000000000000000000000000000000000000000000000001", //
                "0000000000000000000000000000000000000000000000000000000000000002" };
        assertEquals("e90b7bceb6e7df5418fb78d8ee546e97c83a08bbccc01a0644d599ccd2a7c2e0", SmtUtils.keccakMerkle(leafsOf2Test1));

        String[] leafsOf2Test2 = { "0000000000000000000000000000000000000000000000000000000000000002", //
                "0000000000000000000000000000000000000000000000000000000000000001" };
        assertEquals("d9d16d34ffb15ba3a3d852f0d403e2ce1d691fb54de27ac87cd2f993f3ec330f", SmtUtils.keccakMerkle(leafsOf2Test2));

        String[] leafsOf8 = { "0000000000000000000000000000000000000000000000000000000000000001", //
                "0000000000000000000000000000000000000000000000000000000000000002", //
                "0000000000000000000000000000000000000000000000000000000000000003", //
                "0000000000000000000000000000000000000000000000000000000000000004", //
                "0000000000000000000000000000000000000000000000000000000000000005", //
                "0000000000000000000000000000000000000000000000000000000000000006", //
                "0000000000000000000000000000000000000000000000000000000000000007", //
                "0000000000000000000000000000000000000000000000000000000000000008" };
        assertEquals("6f4feb766c4e9e71bf038b8df02f0966e2bf98fe1eaacfd96e5d036664ca1b3c", SmtUtils.keccakMerkle(leafsOf8));

        String[] leafsOf9 = { "0000000000000000000000000000000000000000000000000000000000000001", //
                "0000000000000000000000000000000000000000000000000000000000000002", //
                "0000000000000000000000000000000000000000000000000000000000000003", //
                "0000000000000000000000000000000000000000000000000000000000000004", //
                "0000000000000000000000000000000000000000000000000000000000000005", //
                "0000000000000000000000000000000000000000000000000000000000000006", //
                "0000000000000000000000000000000000000000000000000000000000000007", //
                "0000000000000000000000000000000000000000000000000000000000000008", //
                "0000000000000000000000000000000000000000000000000000000000000009" };
        assertEquals("e8cfcbe9ef675ec92ff1a3e0e6a059007a2b4ad4fc105611229042f37b5f98b1", SmtUtils.keccakMerkle(leafsOf9));
    }

    @ParameterizedTest
    @MethodSource
    void doKeccakMerkle(String expected, String inputs) {
        byte[][] inputsBytes = Arrays.stream(inputs.split(",")).map(SmtUtils::fromHexString).toArray(byte[][]::new);
        byte[][] tops = SmtUtils.doKeccakMerkle(inputsBytes);
        String actual = String.join(",", Arrays.stream(tops).map(SmtUtils::toHexString).toArray(String[]::new));
        assertEquals(expected, actual);
    }

    static List<Arguments> doKeccakMerkle() {
        return List.of( // 2 nodes:
                Arguments.of("f3fdc852d6d7df177476aafee1aab0bb3b0456e11dfd392a0bee07c545274dc4",
                        "8cce65b2992eebfc9bcd56dc1db87a45a75cffa46617f31317a2254d14c381ce,cf3a90558602bb70e2c935e447b60de2e392e3d7f535e35e77fe6cdc7d635d9a"),
                Arguments.of("babd05a3d64888bd6078386103d7607e2a55cda74135b5496e044e3ca090117d",
                        "cf3a90558602bb70e2c935e447b60de2e392e3d7f535e35e77fe6cdc7d635d9a,8cce65b2992eebfc9bcd56dc1db87a45a75cffa46617f31317a2254d14c381ce"),

                // 3 nodes:
                Arguments.of(
                        "e90b7bceb6e7df5418fb78d8ee546e97c83a08bbccc01a0644d599ccd2a7c2e0,cbc4e5fb02c3d1de23a9f1e014b4d2ee5aeaea9505df5e855c9210bf472495af",
                        "0000000000000000000000000000000000000000000000000000000000000001,0000000000000000000000000000000000000000000000000000000000000002,0000000000000000000000000000000000000000000000000000000000000003"),

                Arguments.of(
                        "d9d16d34ffb15ba3a3d852f0d403e2ce1d691fb54de27ac87cd2f993f3ec330f,cbc4e5fb02c3d1de23a9f1e014b4d2ee5aeaea9505df5e855c9210bf472495af",
                        "0000000000000000000000000000000000000000000000000000000000000002,0000000000000000000000000000000000000000000000000000000000000001,0000000000000000000000000000000000000000000000000000000000000003"),

                // 4 nodes:
                Arguments.of(
                        "e90b7bceb6e7df5418fb78d8ee546e97c83a08bbccc01a0644d599ccd2a7c2e0,2e174c10e159ea99b867ce3205125c24a42d128804e4070ed6fcc8cc98166aa0",
                        "0000000000000000000000000000000000000000000000000000000000000001,0000000000000000000000000000000000000000000000000000000000000002,0000000000000000000000000000000000000000000000000000000000000003,0000000000000000000000000000000000000000000000000000000000000004"),

                Arguments.of(
                        "e90b7bceb6e7df5418fb78d8ee546e97c83a08bbccc01a0644d599ccd2a7c2e0,83ec6a1f0257b830b5e016457c9cf1435391bf56cc98f369a58a54fe93772465",
                        "0000000000000000000000000000000000000000000000000000000000000001,0000000000000000000000000000000000000000000000000000000000000002,0000000000000000000000000000000000000000000000000000000000000004,0000000000000000000000000000000000000000000000000000000000000003"),

                // 5 nodes:
                Arguments.of(
                        "e90b7bceb6e7df5418fb78d8ee546e97c83a08bbccc01a0644d599ccd2a7c2e0,2e174c10e159ea99b867ce3205125c24a42d128804e4070ed6fcc8cc98166aa0,458b30c2d72bfd2c6317304a4594ecbafe5f729d3111b65fdc3a33bd48e5432d",
                        "0000000000000000000000000000000000000000000000000000000000000001,0000000000000000000000000000000000000000000000000000000000000002,0000000000000000000000000000000000000000000000000000000000000003,0000000000000000000000000000000000000000000000000000000000000004,0000000000000000000000000000000000000000000000000000000000000005"),

                // 8 nodes:
                Arguments.of(
                        "e90b7bceb6e7df5418fb78d8ee546e97c83a08bbccc01a0644d599ccd2a7c2e0,2e174c10e159ea99b867ce3205125c24a42d128804e4070ed6fcc8cc98166aa0,bfd358e93f18da3ed276c3afdbdba00b8f0b6008a03476a6a86bd6320ee6938b,24cd397636bedc6cf9b490d0edd57c769c19b367fb7d5c2344ae1ddc7d21c144",
                        "0000000000000000000000000000000000000000000000000000000000000001,0000000000000000000000000000000000000000000000000000000000000002,0000000000000000000000000000000000000000000000000000000000000003,0000000000000000000000000000000000000000000000000000000000000004,0000000000000000000000000000000000000000000000000000000000000005,0000000000000000000000000000000000000000000000000000000000000006,0000000000000000000000000000000000000000000000000000000000000007,0000000000000000000000000000000000000000000000000000000000000008"));
    }

    @Test
    void keccakMerkleOf1Level() {
        String leafHash = "9999999999999999999999999999999999999999999999999999999999999999";

        assertEquals("e67841f9a215c07d39de830f7bb56576ab25433493e04ab0ca6aa5a495bd0d6a",
                SmtUtils.toHexString(SmtUtils.keccakMerkleOf1Level(160, 0, SmtUtils.fromHexString(leafHash))));

        assertEquals("bd8c943f0a7be84be54301661315add8c5f2b418f56c0c1504d95590f1f15d3e",
                SmtUtils.toHexString(SmtUtils.keccakMerkleOf1Level(160, 1, SmtUtils.fromHexString(leafHash))));

        assertEquals("5f50cfe6b03797cc81f45396a400aca81ff8d9947785ab9583133ea366c15b51",
                SmtUtils.toHexString(SmtUtils.keccakMerkleOf1Level(160, 2, SmtUtils.fromHexString(leafHash))));

        assertEquals("7469ef9ca82c77d0e77882655707ac7bd35cc76fccac50cbf9b2758f052d62f4",
                SmtUtils.toHexString(SmtUtils.keccakMerkleOf1Level(160, 9, SmtUtils.fromHexString(leafHash))));

        assertEquals("3d6ce0f60469d2ca2c7b2f8af2089a0a212292fc57dc7e177c759db926e90512",
                SmtUtils.toHexString(SmtUtils.keccakMerkleOf1Level(160, 14, SmtUtils.fromHexString(leafHash))));

        assertEquals("36873fe6ba9db2ce0ec296eafea5cc940796a0d2a846937269e3369136780628", SmtUtils.keccakMerkleOf1Level(160, 15, leafHash));

        String nodeHash = "7469ef9ca82c77d0e77882655707ac7bd35cc76fccac50cbf9b2758f052d62f4";
        assertEquals("4fab65756339c0969be5301cf3df1e7148b22ad78d5dcf428fed51133a20117d", SmtUtils.keccakMerkleOf1Level(156, 3, nodeHash));
    }

    @Test
    void keccakMerkleByRange() {
        String leafHash = "9999999999999999999999999999999999999999999999999999999999999999";

        assertEquals(leafHash, SmtUtils.keccakMerkleByRange(160, "", leafHash));

        assertEquals("e67841f9a215c07d39de830f7bb56576ab25433493e04ab0ca6aa5a495bd0d6a", SmtUtils.keccakMerkleByRange(160, "0", leafHash));
        assertEquals("bd8c943f0a7be84be54301661315add8c5f2b418f56c0c1504d95590f1f15d3e", SmtUtils.keccakMerkleByRange(160, "1", leafHash));
        assertEquals("7469ef9ca82c77d0e77882655707ac7bd35cc76fccac50cbf9b2758f052d62f4", SmtUtils.keccakMerkleByRange(160, "9", leafHash));
        assertEquals("36873fe6ba9db2ce0ec296eafea5cc940796a0d2a846937269e3369136780628", SmtUtils.keccakMerkleByRange(160, "f", leafHash));
        assertEquals("4fab65756339c0969be5301cf3df1e7148b22ad78d5dcf428fed51133a20117d", SmtUtils.keccakMerkleByRange(160, "39", leafHash));
    }
}
