package com.jounaidr.jrc.node.blockchain;

import com.jounaidr.jrc.node.crypto.KeccakHashHelper;

import java.time.Instant;

//TODO: implement slf4j logging after blockchain has been fully implemented
public class Block {
    private static final String GENESIS_PREVIOUS_HASH = "dummyhash";
    private static final String GENESIS_DATA = "dummydata";
    private static final String GENESIS_TIME_STAMP = "1";

    private String hash;
    private String previousHash;
    private String data;
    private String timeStamp;

    /**
     * Use genesis constants to generate the
     * genesis block
     *
     * @return the genesis block
     */
    public Block genesis(){
        this.setPreviousHash(GENESIS_PREVIOUS_HASH);
        this.setData(GENESIS_DATA);
        this.setTimeStamp(GENESIS_TIME_STAMP);

        this.setHash(this.generateHash()); //Generate the genesis block hash

        return this;
    }

    public Block mineBlock(Block previousBlock, String data){
        //TODO: Once proof of work implemented, create javadoc for this method
        Instant ts = Instant.now();

        this.setPreviousHash(previousBlock.getHash());
        this.setData(data);
        this.setTimeStamp(ts.toString());

        this.setHash(this.generateHash());

        return this;
    }

    /**
     * Generate the block hash based on a
     * concatenated string of previousHash + data + timeStamp
     * in that specific order
     *
     * @return the generated hash using keccakHashHelper
     */
    private String generateHash(){
        String message = previousHash + data + timeStamp;
        KeccakHashHelper keccakHashHelper = new KeccakHashHelper(message);

        return keccakHashHelper.returnHash();
    }

    public String getHash() {
        return hash;
    }

    private void setHash(String hash) {
        this.hash = hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    private void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public String getData() {
        return data;
    }

    private void setData(String data) {
        this.data = data;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    private void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
