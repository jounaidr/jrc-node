package com.jounaidr.jrc.server.blockchain;

import com.jounaidr.Cryptonight;
import com.jounaidr.jrc.server.blockchain.util.BlockUtil;
import com.jounaidr.jrc.server.blockchain.util.KeccakHashUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.InvalidObjectException;
import java.time.Instant;

@Slf4j //TODO: remove logging replace with exceptions
public class Block {
    private static final long MINE_RATE = 120;

    private static final String GENESIS_PREVIOUS_HASH = "dummyhash";
    private static final String GENESIS_DATA = "dummydata";
    private static final String GENESIS_TIME_STAMP = "2020-11-07T19:40:57.585581100Z";
    private static final String GENESIS_NONCE = "dummydata";
    private static final String GENESIS_DIFFICULTY = "3";
    private static final String GENESIS_PROOF_OF_WORK = "1101011101110100010010011001011010101001000010001011100011111011000110111000010010111111000100000000000011100011110011000000001101011000011110011010111110001000101010111000000100001100010101001100101011001110011110010000011110001011001010000001010011011000";

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

    /**
     * This method will return a new block based
     * on a provided previous blocks information
     * (previous hash, difficulty), and
     * the data to be added to the new block.
     * Inorder for the new block to be valid,
     * a proof of work binary string must be
     * generated (using CryptoNight hashing algorithm
     * on the previousHash + data + timeStamp +
     * difficulty + nonce) which has
     * an amount of leading zeros equivalent to
     * the current difficulty, which is adjusted
     * during the mining process inorder for the
     * blocks mine time to approximate the MINE_RATE
     * of 120 seconds. The blocks hash is then generated
     * using Keccak256 on all the blocks valid information
     *
     * @param previousBlock the previous block
     * @param data          the current block data
     * @return the mined block
     */
    public Block mineBlock(Block previousBlock, String data){
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

        } while(!(this.difficulty.equals(String.valueOf(BlockUtil.getByteArrayLeadingZeros(currentProofOfWork))))); //Check if the currently calculated proof of work leading zeros meets the difficulty

        log.info("Valid proof of work has been found with nonce: {}, and timestamp {} ! POW hash was found in: {} seconds...", currentNonce, this.timeStamp,
                BlockUtil.calcBlockTimeDiff(this.timeStamp,previousBlock.getTimeStamp())); //TODO: this will go in miner server!

        this.setNonce(String.valueOf(currentNonce)); //Set the nonce value of the block to the previously calculated value
        this.setProofOfWork(BlockUtil.getBinaryString(currentProofOfWork)); //Set the POW value of the block to the previously calculated value as binary string

        this.setHash(this.generateHash()); //Now generate the blocks hash with all data

        try {
            // Validate the newly mined block
            this.validateBlock(previousBlock);
        } catch (InvalidObjectException e) {
            e.printStackTrace();
        }

        return this;
    }

    /**
     * Method calculates the time diff between the
     * previous block and block currently being mined
     * and will increment the difficulty if diff is
     * less than the MINE_RATE, and decrement difficulty
     * if diff is greater than the MINE_RATE
     * Method also checks for negative difficulty and
     * will set difficulty to 1 if this should happen
     *
     * @param previousBlock the previous block
     */
    private void adjustDifficulty(Block previousBlock){
        int difficulty = Integer.parseInt(previousBlock.getDifficulty());
        long diffSeconds = BlockUtil.calcBlockTimeDiff(this.timeStamp,previousBlock.getTimeStamp()); //Difference in seconds between the block currently being mined, and the previously mined block

        if(diffSeconds > MINE_RATE){
            difficulty--; //Decrement difficulty if time taken is greater than MINE_RATE (120 seconds)
        }
        else{
            difficulty++; //Otherwise increase difficulty in an attempt to increase block mine time
        }

        if(difficulty < 1){
            log.error("Previous block difficulty is negative: {}, setting current block difficulty to 1...", difficulty);
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
    public String generateHash(){
        String message = this.previousHash + this.data + this.timeStamp + this.difficulty + this.nonce + this.proofOfWork;

        return KeccakHashUtil.returnHash(message);
    }

    /**
     * Method will regenerate the POW binary string
     * and validate it against the POW string that is
     * set for this block
     *
     * @return if POW is valid
     */
    public Boolean isProofOfWorkValid(){
        String proofOfWorkData = this.previousHash + this.data + this.timeStamp + this.difficulty + this.nonce;
        Cryptonight cryptonightValidator = new Cryptonight(proofOfWorkData);

        String proofOfWorkBinaryString = BlockUtil.getBinaryString(cryptonightValidator.returnHash());

        return this.proofOfWork.equals(proofOfWorkBinaryString);
    }

    /**
     * Method to validate a block. First checks will verify the
     * provided previousBlock has a valid hash and proof of work,
     * then will check that the block being validated has a
     * valid hash, valid proof of work, and that its previous hash
     * references the provided previousBlocks hash correctly
     *
     * @param previousBlock the previous block
     */
    public void validateBlock(Block previousBlock) throws InvalidObjectException {
        // Validation checks against the supplied previous block
        if(!previousBlock.getHash().equals(previousBlock.generateHash())){
            throw new InvalidObjectException(String.format("Block validation failed, supplied previous block has an invalid hash. Supplied previous block hash: %s, should be: %s...", previousBlock.getHash(), previousBlock.generateHash()));
        }
        if(!previousBlock.isProofOfWorkValid()){
            throw new InvalidObjectException("Block validation failed, supplied previous block has an invalid proof of work...");
        }
        // Validation checks for this block
        if(!this.getPreviousHash().equals(previousBlock.getHash())){
            throw new InvalidObjectException(String.format("Block validation failed, this block doesn't reference the previous blocks hash correctly. Reference to previous hash: %s, supplied previous blocks hash: %s...", this.getPreviousHash(), previousBlock.getHash()));
        }
        if(!this.getHash().equals(this.generateHash())){
            throw new InvalidObjectException(String.format("Block validation failed, this block has an incorrect hash value. This blocks hash: %s, should be: %s...", this.getHash(), this.generateHash()));
        }
        if(!this.isProofOfWorkValid()){
            throw new InvalidObjectException("Block validation failed, this block has an incorrect proof of work...");
        }
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
                "hash='" + this.hash + '\'' +
                ", previousHash='" + this.previousHash + '\'' +
                ", data='" + this.data + '\'' +
                ", timeStamp='" + this.timeStamp + '\'' +
                ", nonce='" + this.nonce + '\'' +
                ", difficulty='" + this.difficulty + '\'' +
                ", proofOfWork='" + this.proofOfWork + '\'' +
                '}';
    }

    public String getHash() {
        return this.hash;
    }

    private void setHash(String hash) {
        this.hash = hash;
    }

    public String getPreviousHash() {
        return this.previousHash;
    }

    private void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public String getData() {
        return this.data;
    }

    private void setData(String data) {
        this.data = data;
    }

    public String getTimeStamp() {
        return this.timeStamp;
    }

    private void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getNonce() {
        return this.nonce;
    }

    private void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getDifficulty() {
        return this.difficulty;
    }

    private void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getProofOfWork() {
        return this.proofOfWork;
    }

    private void setProofOfWork(String proofOfWork) {
        this.proofOfWork = proofOfWork;
    }
}
