package com.itranswarp.eth.smt;

class TreeInfo {

    public static int HEIGHT = 160;

    private static final byte[][] DEFAULT_HASHES;

    static {
        DEFAULT_HASHES = new byte[HEIGHT + 1][];
        DEFAULT_HASHES[HEIGHT] = SmtUtils.keccak(new byte[0]);
        for (int i = HEIGHT - 1; i >= 0; i--) {
            DEFAULT_HASHES[i] = SmtUtils.keccak(DEFAULT_HASHES[i + 1], DEFAULT_HASHES[i + 1]);
        }
    }

    public static byte[] getDefaultHash(int height) {
        assert height >= 0 && height <= HEIGHT : "invalid height: " + height;
        return DEFAULT_HASHES[height];
    }
}
