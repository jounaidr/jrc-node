package com.jounaidr.jrc.node.blockchain;


import com.jounaidr.jrc.node.crypto.CryptoHashHelper;

import java.time.Instant;

public class Block {
    private String hash;
    private String previousHash;
    private String data;
    private String timeStamp;

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

    private String getHash() {
        return hash;
    }

    private void setHash(String hash) {
        this.hash = hash;
    }

    private String getPreviousHash() {
        return previousHash;
    }

    private void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    private String getData() {
        return data;
    }

    private void setData(String data) {
        this.data = data;
    }

    private String getTimeStamp() {
        return timeStamp;
    }

    private void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
