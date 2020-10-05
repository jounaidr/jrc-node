package com.jounaidr.jrc.node.blockchain;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class Blockchain {
    private static List<Block> chain = new ArrayList<>();
    private ReadWriteLock rwLock = new ReentrantReadWriteLock();

    public Blockchain() {
        this.chain.add(new Block().genesis());
    }

    public void addBlock(String data){
        Block nextBlock = new Block().mineBlock(this.chain.get(this.chain.size() - 1), data);
        this.chain.add(nextBlock);
    }

    public void replaceChain(Blockchain newBlockchain){
        if(this.chain.size() <= newBlockchain.getChain().size()){
            return;
        }
        if(!newBlockchain.isChainValid()){
            return;
        }

        this.setChain(newBlockchain.getChain());
    }

    public boolean isChainValid(){
        if(this.chain.get(0).toString() != new Block().genesis().toString()){
            return false; //Verify first block in chain is genesis block
        }

        for(int i=1; i < this.chain.size(); i++){
            if(this.chain.get(i).getPreviousHash() != this.chain.get(i-1).getHash()){
                return false; //Verify each block in the chain references previous hash value correctly
            }
        }

        return true;
    }

    public List<Block> getChain(){
        Lock readLock = rwLock.readLock();
        readLock.lock();

        try {
            return this.chain;
        } finally {
            readLock.unlock();
        }
    }

    private void setChain(List<Block> newChain){
        Lock writeLock = rwLock.writeLock();
        writeLock.lock();

        try {
            this.chain = newChain;
        } finally {
            writeLock.unlock();
        }
    }
}
