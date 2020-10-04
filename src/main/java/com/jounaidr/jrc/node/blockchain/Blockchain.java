package com.jounaidr.jrc.node.blockchain;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class Blockchain {
    private static List<Block> chain = new ArrayList<>();

    public Blockchain() {
        Block genesisBlock = new Block().genesis();
        this.chain.add(genesisBlock);
    }

    public void addBlock(String data){
        Block nextBlock = new Block().mineBlock(this.chain.get(this.chain.size() - 1), data);
        this.chain.add(nextBlock);
    }

    public Block getBlock(int index){
        return this.chain.get(index);
    }
}
