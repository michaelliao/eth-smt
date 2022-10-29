package com.itranswarp.eth.smt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Memory tree store used for test.
 * 
 * All nodes store as table:
 * 
 * number: node's number;
 * 
 * topPath: node's top path.
 * 
 * path: node's path, or address if node is leaf.
 * 
 * topLevel: node's top level.
 * 
 * topHash: node's top hash.
 * 
 * nodeHash: node's hash.
 * 
 * dataValue: leaf node's data, or null if non-leaf node.
 * 
 * Primary key: number, path and topHash.
 */
public class MemoryTreeStore implements TreeStore {

    Map<NibbleString, List<PersistNode>> topPathMap = new HashMap<>();
    Map<String, PersistNode> rootMap = new HashMap<>();
    Map<NumberAndAddress, byte[]> leafMap = new HashMap<>();

    /**
     * Make a copy of memory tree store.
     * 
     * @return Copy of memory tree store.
     */
    public MemoryTreeStore copy() {
        MemoryTreeStore copy = new MemoryTreeStore();
        copy.rootMap.putAll(this.rootMap);
        copy.leafMap.putAll(this.leafMap);
        for (NibbleString key : this.topPathMap.keySet()) {
            List<PersistNode> value = this.topPathMap.get(key);
            List<PersistNode> valueCopy = new ArrayList<>();
            valueCopy.addAll(value);
            copy.topPathMap.put(key, valueCopy);
        }
        return copy;
    }

    @Override
    public Node load(NibbleString topPath, long currentNumber) {
        List<PersistNode> pnodes = topPathMap.get(topPath);
        if (pnodes != null) {
            for (PersistNode pnode : pnodes) {
                if (pnode.number() < currentNumber) {
                    System.out.println("loaded node: " + topPath + " from store at: " + currentNumber);
                    return pnode.deserialize();
                }
            }
        }
        System.out.println("loaded null node: " + topPath + " from store at: " + currentNumber);
        return null;
    }

    @Override
    public Node loadRoot(byte[] hash) {
        String rootHash = SmtUtils.toHexString(hash);
        PersistNode pnode = rootMap.get(rootHash);
        if (pnode == null) {
            throw new IllegalStateException("Root hash not found: " + rootHash);
        }
        return pnode.deserialize();
    }

    @Override
    public void save(List<PersistNode> pnodes) {
        for (PersistNode pnode : pnodes) {
            addTopPathMap(pnode.topPath(), pnode);
            if (pnode.path().length() == 0) {
                this.rootMap.put(SmtUtils.toHexString(pnode.nodeHash()), pnode);
            }
            if (pnode.leaf()) {
                addLeafMap(pnode.number(), pnode.path(), pnode.dataValue());
            }
        }
    }

    /**
     * For debug.
     */
    public void print() {
        System.out.println("---- Begin Tree Store ----");
        for (NibbleString key : this.topPathMap.keySet()) {
            System.out.printf("top path=%s\n", key);
            List<PersistNode> pnodes = this.topPathMap.get(key);
            for (PersistNode pnode : pnodes) {
                System.out.printf("  number=%s, node=%s\n", pnode.number(), pnode.deserialize());
            }
        }
        System.out.println("---- End Tree Store ----\n");
    }

    private void addTopPathMap(NibbleString topPath, PersistNode pnode) {
        System.out.println("add top path: " + topPath + " = " + pnode);
        List<PersistNode> pnodes = topPathMap.get(topPath);
        if (pnodes == null) {
            pnodes = new ArrayList<>();
            topPathMap.put(topPath, pnodes);
        } else {
            PersistNode latest = pnodes.get(0);
            if (latest.number() >= pnode.number()) {
                throw new IllegalStateException("Invalid current node: " + pnode + ", because last node is " + latest);
            }
        }
        pnodes.add(0, pnode);
    }

    private void addLeafMap(long number, NibbleString address, byte[] dataValue) {
        NumberAndAddress key = new NumberAndAddress(number, address);
        if (leafMap.get(key) != null) {
            throw new IllegalStateException(String.format("LeafNode exist for number = %s and address = %s", number, address));
        }
        leafMap.put(key, dataValue);
    }
}

class NumberAndAddress {
    final long number;
    final NibbleString address;

    public NumberAndAddress(final long number, final NibbleString address) {
        this.number = number;
        this.address = address;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hash(number);
        result = prime * result + address.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NumberAndAddress) {
            NumberAndAddress other = (NumberAndAddress) obj;
            return number == other.number && this.address.equals(other.address);
        }
        return false;
    }
}
