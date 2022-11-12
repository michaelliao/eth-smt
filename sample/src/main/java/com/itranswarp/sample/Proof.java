package com.itranswarp.sample;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.slf4j.LoggerFactory;

import com.itranswarp.eth.smt.MemoryTreeStore;
import com.itranswarp.eth.smt.PersistSparseMerkleTree;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class Proof {

    private static Random random = new Random(12345678L);

    public static void main(String[] args) {
        ch.qos.logback.classic.Logger logger = (Logger) LoggerFactory.getLogger("com.itranswarp.eth.smt");
        logger.setLevel(Level.WARN);
        Map<Long, BigDecimal> userAssets = Map.of( //
                12345L, new BigDecimal("12.34"), //
                23456L, new BigDecimal("23.45"), //
                34567L, new BigDecimal("34.56"), //
                45678L, new BigDecimal("45.67"), //
                56789L, new BigDecimal("567.89")); //

        var store = new MemoryTreeStore();
        var psmt = new PersistSparseMerkleTree(store, null);
        List<LeafItem> leafs = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        String snapshotTime = "2022-11-14T09:19:20";
        List<Long> keys = new ArrayList<>(userAssets.keySet());
        Collections.sort(keys);
        for (Long userId : keys) {
            BigDecimal balance = userAssets.get(userId);
            String secretKey = "random-" + userId + snapshotTime;
            putUserAsset(psmt, leafs, userId, secretKey, balance);
            total = total.add(balance);
        }
        System.out.println("-- generated merkle proof --");
        leafs.sort((l1, l2) -> l1.address.compareTo(l2.address));
        System.out.printf("address, balance\n");
        leafs.forEach((leaf) -> {
            System.out.printf("%s, %s\n", leaf.address, leaf.balance);
        });
        byte[] root = psmt.getMerkleRoot();
        System.out.printf("total = %s, merkle root = %64x\n", total.toPlainString(), new BigInteger(1, root));
    }

    private static void putUserAsset(PersistSparseMerkleTree merkle, List<LeafItem> leafs, Long userId, String secretKey, BigDecimal balance) {
        System.out.printf("add user asset: %d = %s\n", userId, balance.toPlainString());
        byte[] address = userHash(userId, secretKey);
        List<BigDecimal> parts = randomSplit(balance);
        for (BigDecimal part : parts) {
            LeafItem leaf = new LeafItem(HexFormat.of().formatHex(address), part.toPlainString());
            System.out.printf("  set %s: %s\n", leaf.address, leaf.balance);
            merkle.update(address, leaf.balance.getBytes());
            balance = balance.subtract(part);
            address = nextAddress(address, secretKey);
            leafs.add(leaf);
        }
    }

    private static List<BigDecimal> randomSplit(BigDecimal balance) {
        List<BigDecimal> list = new ArrayList<>();
        int split = 0;
        if (balance.compareTo(BD_1000) >= 0) {
            split = 9;
        } else if (balance.compareTo(BD_100) >= 0) {
            split = 5;
        } else if (balance.compareTo(BD_10) >= 0) {
            split = 2;
        }
        BigDecimal left = balance;
        while (list.size() < split) {
            BigDecimal r = randomBD(left);
            list.add(r);
            left = left.subtract(r);
        }
        list.add(left);
        return list;
    }

    private static BigDecimal randomBD(BigDecimal max) {
        double d = max.doubleValue() / 4;
        return new BigDecimal(random.nextDouble(d, d * 2)).setScale(8, RoundingMode.DOWN);
    }

    private static byte[] userHash(Long userId, String secretKey) {
        byte[] input = ByteBuffer.allocate(8).putLong(userId).array();
        Keccak.DigestKeccak kecc = new Keccak.Digest256();
        kecc.update(input);
        kecc.update(secretKey.getBytes());
        return Arrays.copyOfRange(kecc.digest(), 12, 32);
    }

    private static byte[] nextAddress(byte[] addr, String secretKey) {
        Keccak.DigestKeccak kecc = new Keccak.Digest256();
        kecc.update(addr);
        kecc.update(secretKey.getBytes());
        return Arrays.copyOfRange(kecc.digest(), 12, 32);
    }

    private static final BigDecimal BD_10 = new BigDecimal("10");
    private static final BigDecimal BD_100 = new BigDecimal("100");
    private static final BigDecimal BD_1000 = new BigDecimal("1000");
}

final class LeafItem {
    final String address;
    final String balance;

    public LeafItem(String address, String balance) {
        this.address = address;
        this.balance = balance;
    }
}
