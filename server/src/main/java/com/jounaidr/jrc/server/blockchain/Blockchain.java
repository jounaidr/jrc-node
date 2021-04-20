package com.jounaidr.jrc.server.blockchain;

import com.jounaidr.jrc.server.peers.Peers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.InvalidObjectException;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class Blockchain {
    @Lazy
    @Autowired
    Peers peers;

    private List<Block> chain;
    private ReadWriteLock rwLock = new ReentrantReadWriteLock();

    /**
     * Instantiates the Blockchain with a
     * genesis block at the start of the chain
     */
    public Blockchain(List<Block> chain) {
        log.debug("Attempting to initialise a blockchain with the following chain array: {}...", chain.toString());
        this.setChain(chain);

        if(this.getChain().size() < 1){
            this.getChain().add(new Block().genesis());
            log.info("A Fresh blockchain has been initialised with genesis block...");
            log.debug("Blockchain initialised with the following genesis block: {} ...", this.getLastBlock().toString());
        }

        Miner miner = new Miner(this);
        miner.start();
    }

    /**
     * Validate an new incoming block and if valid,
     * add the block to the chain
     *
     * @param newBlock the new incoming block
     */
    public void addBlock(Block newBlock) throws InvalidObjectException {
        log.info("A new block has been submitted to the blockchain!"); //TODO: debug
        // Check if the block is has already been added
        if(newBlock.toString().equals(this.getLastBlock().toString())){
            log.info("Block has already been added to the chain!"); //TODO: debug
            return;
        }

        log.info("Attempting to add new incoming block: {}...", newBlock.toString());
        try {
            // Validate the incoming block against this blockchains last block before adding new block
            newBlock.validateBlock(this.getLastBlock());
            this.getChain().add(newBlock);
            log.info("...Block added successfully!");
            // Then broadcast the new block to the nodes peers
            peers.broadcastBlockToPeers(newBlock);
        } catch (InvalidObjectException e) {
            log.error("New incoming block is invalid and can't be added to the blockchain. Reason: {}", e.getMessage());
            throw e;
        }
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
    public void replaceChain(Blockchain newBlockchain) throws InvalidObjectException {
        log.info("Attempting to replace the current blockchain with a new incoming blockchain");
        if(newBlockchain.getChain().size() <= this.getChain().size()){
            log.debug("Incoming blockchain is not longer than current blockchain...");
            return;
        }
        if(!newBlockchain.isChainValid()){
            log.debug("Incoming blockchain is longer than current blockchain, but is not valid...");
            return;
        }

        this.setChain(newBlockchain.getChain());
        log.info("Chain replacement successful...");
    }

    /**
     * Method used to validate the blockchain chain list.
     * The first block in the chain will be initially checked
     * to ensure that its a valid genesis block.
     * If the initial check passes, then each block in the chain
     * will be validated using Block().isBlockValid, and that the
     * difference in difficulty between blocks is not greater than 1
     *
     * @return if the chain is valid
     */
    public boolean isChainValid() throws InvalidObjectException {
        if(!(this.getChain().get(0).toString()).equals(new Block().genesis().toString())){
            log.error("Chain is invalid, first block in the chain is not genesis block...");
            return false; //Verify first block in chain is genesis block
        }

         for(int i=1; i < this.getChain().size(); i++){
             try {
                 //Verify each block is valid against the previous block
                 this.getChain().get(i).validateBlock(this.getChain().get(i-1));
             } catch (InvalidObjectException e) {
                 log.error("Chain is invalid, the block {} in the chain is invalid.",i);
                 throw e;
             }
            //Verify each block changes the difficulty by no more than 1
            int changeInDifficulty = Math.abs(Integer.parseInt(this.getChain().get(i-1).getDifficulty()) - Integer.parseInt(this.getChain().get(i).getDifficulty()));
            if(changeInDifficulty > 1){
                log.error("Chain is invalid, the block {} in the chain has a difficulty jump greater than 1. Difficulty changed by: {}...",i,changeInDifficulty);
                return false;
            }
        }
        log.debug("Blockchain is valid...");
        return true;
    }

    /**
     * Getter for the last block in the chain
     *
     * @return lastBlock the last block
     */
    public Block getLastBlock() {
        return this.getChain().get(this.getChain().size() - 1);
    }

    /**
     * Getter for the blockchains chain with
     * a read lock for thread safety
     *
     * @return List<Block> this blockchains chain
     */
    public List<Block> getChain() {
        //Read lock whilst getting chain as many threads can read/write to this
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
    private void setChain(List<Block> newChain) {
        //Write lock whilst getting chain as many threads can read/write to this
        Lock writeLock = rwLock.writeLock();
        writeLock.lock();

        try {
            this.chain = newChain;
        } finally {
            writeLock.unlock();
        }
    }
}
