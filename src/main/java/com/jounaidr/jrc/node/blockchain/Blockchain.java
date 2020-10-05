package com.jounaidr.jrc.node.blockchain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@Service
public class Blockchain {
    private static List<Block> chain = new ArrayList<>();
    private ReadWriteLock rwLock = new ReentrantReadWriteLock();

    /**
     * Instantiates a new Blockchain with a
     * genesis block at the start of the chain
     */
    public Blockchain() {
        log.debug("Initiating blockchain with genesis block...");
        this.chain.add(new Block().genesis());
    }

    /**
     * Mine a new block for some given data and
     * add it to the end of the chain
     *
     * @param data transaction data to be added to the new block
     */
    public void addBlock(String data){
        log.debug("Adding new block to the chain with transaction data: {} ...", data);
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
        if(newBlockchain.getChain().size() <= this.chain.size()){
            log.debug("Incoming blockchain is not longer than current blockchain...");
            return;
        }
        if(!newBlockchain.isChainValid()){
            log.error("Incoming blockchain is longer than current blockchain, but is not valid...");
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
            log.error("Chain is invalid, first block in the chain is not genesis block...");
            return false; //Verify first block in chain is genesis block
        }

        for(int i=1; i < this.chain.size(); i++){
            if(this.chain.get(i).getPreviousHash() != this.chain.get(i-1).getHash()){
                log.error("Chain is invalid, the {}th block in the chain has previousHash value {}, however the hash of the previous block is {}...",i,this.chain.get(i).getPreviousHash(),this.chain.get(i-1).getHash());
                return false; //Verify each block in the chain references previous hash value correctly
            }
        }
        log.debug("Blockchain is valid...");
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
        log.debug("Attempting to read chain...");
        try {
            return this.chain;
        } finally {
            log.debug("Chain read successfully...");
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
        log.debug("Attempting to replace chain...");
        try {
            this.chain = newChain;
        } finally {
            log.debug("Chain replacement successful...");
            writeLock.unlock();
        }
    }
}
