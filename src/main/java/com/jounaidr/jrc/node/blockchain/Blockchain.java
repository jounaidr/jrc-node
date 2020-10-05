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

    /**
     * Instantiates a new Blockchain with a
     * genesis block at the start of the chain
     */
    public Blockchain() {
        this.chain.add(new Block().genesis());
    }

    /**
     * Mine a new block for some given data and
     * add it to the end of the chain
     *
     * @param data transaction data to be added to the new block
     */
    public void addBlock(String data){
        Block nextBlock = new Block().mineBlock(this.chain.get(this.chain.size() - 1), data);
        this.chain.add(nextBlock);
    }

    /**
     * First check whether another incoming blockchain
     * is first longer than this blockchain.
     * If the new chain is longer, then validate the new
     * chain.
     * If the new chain is valid, then replace this chain
     * with the new one.
     *
     * @param newBlockchain the new incoming blockchain
     */
    public void replaceChain(Blockchain newBlockchain){
        if(this.chain.size() <= newBlockchain.getChain().size()){
            return;
        }
        if(!newBlockchain.isChainValid()){
            return;
        }

        this.setChain(newBlockchain.getChain());
    }

    /**
     * Method used to validate the blockchain chain list.
     * The first block in the chain will be initially checked
     * to ensure that its a valid genesis block.
     * If the initial check passes, then each block in the chain
     * will be checked that the previous hash value matches the
     * hash value of the previous block.
     *
     * @return if the chain is valid
     */
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

    /**
     * Getter for the blockchains chain with
     * a read lock for thread safety
     *
     * @return List<Block> this blockchains chain
     */
    public List<Block> getChain(){
        Lock readLock = rwLock.readLock();
        readLock.lock();

        try {
            return this.chain;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Setter for the blockchains chain with
     * a write lock for thread safety
     *
     * @param newChain incoming chain
     */
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
