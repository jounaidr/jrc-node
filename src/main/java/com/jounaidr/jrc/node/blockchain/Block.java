package com.jounaidr.jrc.node.blockchain;

import com.jounaidr.Cryptonight;
import com.jounaidr.jrc.node.blockchain.helpers.BlockHelper;
import com.jounaidr.jrc.node.crypto.KeccakHashHelper;

import java.time.Instant;

//TODO: implement slf4j logging after blockchain has been fully implemented
public class Block {
    private static final long MINE_RATE = 120;

    private static final String GENESIS_PREVIOUS_HASH = "dummyhash";
    private static final String GENESIS_DATA = "dummydata";
    private static final String GENESIS_TIME_STAMP = "2020-11-07T19:40:57.585581100Z";
    private static final String GENESIS_NONCE = "dummydata";
    private static final String GENESIS_DIFFICULTY = "3";
    private static final String GENESIS_PROOF_OF_WORK = "dummyPOW";

    private String hash;
    private String previousHash;
    private String data;
    private String timeStamp;
    private String nonce;
    private String difficulty;
    private String proofOfWork;

    public Block() {
        this.hash = null;
        this.previousHash = null;
        this.data = null;
        this.timeStamp = null;
        this.nonce = null;
        this.difficulty = null;
        this.proofOfWork = null;
    }

    public Block(String hash, String previousHash, String data, String timeStamp, String nonce, String difficulty, String proofOfWork) {
        this.hash = hash;
        this.previousHash = previousHash;
        this.data = data;
        this.timeStamp = timeStamp;
        this.nonce = nonce;
        this.difficulty = difficulty;
        this.proofOfWork = proofOfWork;
    }

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
        this.setDifficulty(GENESIS_DIFFICULTY);
        this.setNonce(GENESIS_NONCE);
        this.setProofOfWork(GENESIS_PROOF_OF_WORK);

        this.setHash(this.generateHash()); //Generate the genesis block hash

        return this;
    }

    public Block mineBlock(Block previousBlock, String data){
        //TODO: Once proof of work implemented, create javadoc for this method

        this.setPreviousHash(previousBlock.getHash());
        this.setData(data);

        int currentNonce = 0;
        byte[] currentProofOfWork;

        do{
            currentNonce++;

            Instant ts = Instant.now();
            this.setTimeStamp(ts.toString()); //Set timestamp to current time and current interation in loop

            adjustDifficulty(previousBlock); //Adjust the difficulty based on the new timestamp

            String proofOfWorkData = this.previousHash + this.data + this.timeStamp + this.difficulty + currentNonce;

            Cryptonight cryptonightPOW = new Cryptonight(proofOfWorkData);
            currentProofOfWork = cryptonightPOW.returnHash();

        } while(!(this.difficulty.equals(String.valueOf(BlockHelper.getBinaryStringLeadingZeros(currentProofOfWork))))); //Check if the currently calculated proof of work leading zeros meets the difficulty

        this.setNonce(String.valueOf(currentNonce)); //Set the nonce value of the block to the previously calculated value
        this.setProofOfWork(BlockHelper.getBinaryString(currentProofOfWork)); //Set the POW value of the block to the previously calculated value as binary string

        this.setHash(this.generateHash()); //Now generate the blocks hash with all data

        return this;
    }

    private void adjustDifficulty(Block previousBlock){
        int difficulty = Integer.parseInt(previousBlock.getDifficulty());

        Instant previousBlockTimeStamp = Instant.parse(previousBlock.getTimeStamp());
        Instant currentTimeStamp = Instant.parse(this.timeStamp);

        long diffSeconds = currentTimeStamp.getEpochSecond() - previousBlockTimeStamp.getEpochSecond(); //Difference in seconds between the block currently being mined, and the previously mined block

        if(diffSeconds > MINE_RATE){
            difficulty--; //Decrement difficulty if time taken is greater than MINE_RATE (120 seconds)
        }
        else{
            difficulty++; //Otherwise increase difficulty in an attempt to increase block mine time
        }

        if(difficulty < 1){
            difficulty = 1; //Set difficulty to 1 if it drops below 1
        }

        this.setDifficulty(String.valueOf(difficulty));
    }

    /**
     * Generate the block hash based on a
     * concatenated string of previousHash + data + timeStamp
     * in that specific order
     *
     * @return the generated hash using keccakHashHelper
     */
    private String generateHash(){
        String message = this.previousHash + this.data + this.timeStamp + this.difficulty + this.nonce + this.proofOfWork;
        KeccakHashHelper keccakHashHelper = new KeccakHashHelper(message);

        return keccakHashHelper.returnHash();
    }

    public Boolean isProofOfWorkValid(){
        String proofOfWorkData = this.previousHash + this.data + this.timeStamp + this.difficulty + this.nonce;
        Cryptonight cryptonightValidator = new Cryptonight(proofOfWorkData);

        String proofOfWorkBinaryString = BlockHelper.getBinaryString(cryptonightValidator.returnHash());

        if(!this.proofOfWork.equals(proofOfWorkBinaryString)) {
            return false;
        }
        return true;
    }

    /**
     * toString method auto-generated with intellij
     * tested using https://github.com/jparams/to-string-verifier
     *
     * @return String containing block information
     */
    @Override
    public String toString() {
        return "Block{" +
                "hash='" + hash + '\'' +
                ", previousHash='" + previousHash + '\'' +
                ", data='" + data + '\'' +
                ", timeStamp='" + timeStamp + '\'' +
                ", nonce='" + nonce + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", proofOfWork='" + proofOfWork + '\'' +
                '}';
    }

    public String getHash() {
        return this.generateHash(); // Rehash block everytime block hash is needed
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

    public String getNonce() {
        return nonce;
    }

    private void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getDifficulty() {
        return difficulty;
    }

    private void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getProofOfWork() {
        return proofOfWork;
    }

    private void setProofOfWork(String proofOfWork) {
        this.proofOfWork = proofOfWork;
    }
}
