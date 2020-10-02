package com.jounaidr.jrc.node.blockchain;

import com.jounaidr.jrc.node.crypto.CryptoHashHelper;

import java.time.Instant;

public class Block {
    private static final String GENESIS_PREVIOUS_HASH = "dummyhash";
    private static final String GENESIS_DATA = "dummydata";
    private static final String GENESIS_TIME_STAMP = "1";

    private String hash;
    private String previousHash;
    private String data;
    private String timeStamp;

    public Block genesis(){
        this.setPreviousHash(GENESIS_PREVIOUS_HASH);
        this.setData(GENESIS_DATA);
        this.setTimeStamp(GENESIS_TIME_STAMP);

        this.setHash(this.generateHash());

        return this;
    }

    public Block mineBlock(Block previousBlock, String data){
        Instant ts = Instant.now();

        this.setPreviousHash(previousBlock.getHash());
        this.setData(data);
        this.setTimeStamp(ts.toString());

        this.setHash(this.generateHash());

        return this;
    }

    private String generateHash(){
        String message = previousHash + data + timeStamp;
        CryptoHashHelper cryptoHashHelper = new CryptoHashHelper(message);

        return cryptoHashHelper.returnHash();
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
