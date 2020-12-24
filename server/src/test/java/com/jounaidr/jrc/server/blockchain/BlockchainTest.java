package com.jounaidr.jrc.server.blockchain;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
class BlockchainTest {

    Logger logger = (Logger) LoggerFactory.getLogger(Blockchain.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

    @Test
    public void testBlockchainInitiatesWithGenesisBlock(){
        //Given
        Blockchain testChain;
        Block genesisBlock = new Block().genesis();

        //When
        testChain = new Blockchain(new ArrayList<>()); //...a new chain is initialised...

        //Then
        assertEquals("failure - hash of the first block in the blockchain does not equal the genesis hash", genesisBlock.getHash(), testChain.getChain().get(0).getHash());
        assertEquals("failure - data of the first block in the blockchain does not equal the genesis data", genesisBlock.getData(), testChain.getChain().get(0).getData());
        assertEquals("failure - previous hash of the first block in the blockchain does not equal the genesis previous hash", genesisBlock.getPreviousHash(), testChain.getChain().get(0).getPreviousHash());
        assertEquals("failure - timestamp of the first block in the blockchain does not equal the genesis timestamp", genesisBlock.getTimeStamp(), testChain.getChain().get(0).getTimeStamp());
    }

    @Test
    public void testBlockchainAddsBlockCorrectly(){
        //Given
        Blockchain testChain = new Blockchain(new ArrayList<>());

        //When
        testChain.addBlock("Hi, im the second block");
        testChain.addBlock("Sup second block im the third block");
        testChain.addBlock("And im the fourth block =)");

        //Then
        //Check that each block in the chain references the previous block hash correctly
        assertEquals("failure - previous hash of the second block does not reference hash of genesis block", testChain.getChain().get(0).getHash(), testChain.getChain().get(1).getPreviousHash());
        assertEquals("failure - previous hash of the third block does not reference hash of second block", testChain.getChain().get(1).getHash(), testChain.getChain().get(2).getPreviousHash());
        assertEquals("failure - previous hash of the fourth block does not reference hash of third block", testChain.getChain().get(2).getHash(), testChain.getChain().get(3).getPreviousHash());
        //Check that each block has the correct data
        assertEquals("failure - data in the second block is incorrect", "Hi, im the second block", testChain.getChain().get(1).getData());
        assertEquals("failure - data in the second block is incorrect", "Sup second block im the third block", testChain.getChain().get(2).getData());
        assertEquals("failure - data in the second block is incorrect", "And im the fourth block =)", testChain.getChain().get(3).getData());
        //Check that the blockchain has the correct amount of blocks
        assertEquals("failure - blockchain has incorrect amount of blocks", 4, testChain.getChain().size());
    }

    @Test
    public void testBlockchainValidationValidChain(){
        //Given
        Blockchain validChain = new Blockchain(new ArrayList<>());

        //When
        validChain.addBlock("Hi, im the second block");
        validChain.addBlock("Sup second block im the third block");
        validChain.addBlock("And im the fourth block =)");

        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        Boolean isChainValid = validChain.isChainValid();

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        assertTrue("failure - valid chain incorrectly flagged as invalid", isChainValid);
        assertEquals("failure - incorrect logging message displayed","Blockchain is valid...", logsList.get(0).getMessage());
    }

    @Test
    public void testBlockchainValidationInvalidGenesisBlock(){
        //Given
        Block genesisBlock = new Block().genesis();

        List<Block> invalidGenesisChain = new ArrayList<>(); //create a dummy chain as a new arraylist
        Block badBoyBlock = new Block().mineBlock(genesisBlock,"Im a verrrrry bad block whos going to do bad things >:)");
        invalidGenesisChain.add(badBoyBlock); //add a block that isn't the genesis block to the dummy chain

        Blockchain evilBlockchain = new Blockchain(invalidGenesisChain); //Initial the evil blockchain with the dummy chain

        //When
        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        Boolean isChainValid = evilBlockchain.isChainValid();

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        assertFalse("failure - blockchain with invalid genesis block has verified as valid, big ouf...", isChainValid);
        assertEquals("failure - incorrect logging message displayed","Chain is invalid, first block in the chain is not genesis block...", logsList.get(0).getMessage());
    }

    @Test
    public void testBlockchainValidationInvalidBlockOrder(){
        //Given
        List<Block> invalidPreviousHashChain = new ArrayList<>(); //create a dummy chain as a new arraylist

        invalidPreviousHashChain.add(new Block().genesis()); //start the chain with a valid genesis block
        invalidPreviousHashChain.add(new Block().mineBlock(new Block().genesis(), "a valid second block")); //add a valid block after the genesis block

        Block thirdBlock = new Block().mineBlock(new Block().genesis(),"Im meant to be the second block in the chain");
        Block forthBlock = new Block().mineBlock(thirdBlock,"Im meant to be the second block in the chain");

        invalidPreviousHashChain.add(forthBlock);
        invalidPreviousHashChain.add(thirdBlock); //switch around the second and third blocks in the chain

        Blockchain evilBlockchain = new Blockchain(invalidPreviousHashChain); //Initialise the evil blockchain with the dummy chain

        //When
        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        Boolean isChainValid = evilBlockchain.isChainValid();

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        assertFalse("failure - blockchain with invalid block order has verified as valid, big ouf...", isChainValid);
        assertEquals("failure - incorrect logging message displayed","Chain is invalid, the {}th block in the chain has previousHash value {}, however the hash of the previous block is {}...", logsList.get(0).getMessage());
    }

    @Test
    public void testBlockchainValidationInvalidProofOfWork(){
        //Given
        List<Block> invalidPreviousHashChain = new ArrayList<>(); //create a dummy chain as a new arraylist

        Block genesisBlock = new Block().genesis();
        Block validSecondBlock = new Block().mineBlock(genesisBlock,"secondBlockData"); //A valid second block

        //Generate a block based on the previously generated valid second block, but set proof of work to an invalid binary string
        Block evilSecondBlock = new Block(validSecondBlock.getHash(), validSecondBlock.getPreviousHash(), validSecondBlock.getData(), validSecondBlock.getTimeStamp(), validSecondBlock.getNonce(), validSecondBlock.getDifficulty(), "1000101");

        invalidPreviousHashChain.add(genesisBlock);
        invalidPreviousHashChain.add(evilSecondBlock); //Add the invalid block to the dummy chain

        Blockchain evilBlockchain = new Blockchain(invalidPreviousHashChain); //Initialise the evil blockchain with the dummy chain

        //When
        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        Boolean isChainValid = evilBlockchain.isChainValid();

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        assertFalse("failure - blockchain that has a block with invalid proof of work has verified as valid, big ouf...", isChainValid);
        assertEquals("failure - incorrect logging message displayed","Chain is invalid, the {}th block in the chain has an invalid proof of work...", logsList.get(0).getMessage());
    }

    @Test
    public void testBlockchainValidationInvalidDifficulty(){
        //Given
        List<Block> invalidPreviousHashChain = new ArrayList<>(); //create a dummy chain as a new arraylist

        Block genesisBlock = new Block().genesis();

        //Generate a block based on the previously generated valid second block, but set jump the difficulty more than 1
        Block evilSecondBlock = new Block("This Block Jumps Difficulty", "6034f08ebe09268c00b3144673bc0a1ce787c2e992545e3ec276b38cbebd57b6", "secondBlockData", "2020-11-14T23:55:10.208261300Z", "4", "69", "1111001110010001000010000010001100000000001111101111010000000011001000100110011010001000001010100111011110111000001001011101000110000111010001110000101001010010000101011110110011011111110101111000100101011011110011110101011101001010000011100100011111010000");

        invalidPreviousHashChain.add(genesisBlock);
        invalidPreviousHashChain.add(evilSecondBlock); //Add the invalid block to the dummy chain

        Blockchain evilBlockchain = new Blockchain(invalidPreviousHashChain); //Initialise the evil blockchain with the dummy chain

        //When
        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        Boolean isChainValid = evilBlockchain.isChainValid();

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        assertFalse("failure - blockchain that has a difficulty jump has verified as valid, big ouf...", isChainValid);
        assertEquals("failure - incorrect logging message displayed","Chain is invalid, the {}th block in the chain has a difficulty jump greater than 1. Difficulty changed by: {}...", logsList.get(0).getMessage());
    }

    @Test
    public void testValidLongerIncomingBlockchainReplacesCurrentChain(){
        //Given
        Blockchain smolChain = new Blockchain(new ArrayList<>());
        Blockchain bigChain = new Blockchain(new ArrayList<>());

        smolChain.addBlock("im the only block in this chain =(");

        bigChain.addBlock("Hi, im the second block");
        bigChain.addBlock("Sup second block im the third block");
        bigChain.addBlock("And im the fourth block =)");

        //When
        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        smolChain.replaceChain(bigChain);

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        assertEquals("failure - Original blockchains chain was not replaced by the longer incoming chain", bigChain.getChain(), smolChain.getChain());
        assertEquals("failure - Original blockchains chain is incorrect length after replacement", 4, smolChain.getChain().size());

        assertEquals("failure - incorrect logging message displayed","Blockchain is valid...", logsList.get(2).getMessage());
        assertEquals("failure - incorrect logging message displayed","Attempting to replace chain...", logsList.get(5).getMessage());
        assertEquals("failure - incorrect logging message displayed","Chain replacement successful...", logsList.get(6).getMessage());
    }

    @Test
    public void testValidShorterIncomingBlockchainDoesntReplacesCurrentChain(){
        //Given
        Blockchain smolChain = new Blockchain(new ArrayList<>());
        Blockchain bigChain = new Blockchain(new ArrayList<>());

        smolChain.addBlock("im the only block in this chain =(");

        bigChain.addBlock("Hi, im the second block");
        bigChain.addBlock("Sup second block im the third block");
        bigChain.addBlock("And im the fourth block =)");

        //When
        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        bigChain.replaceChain(smolChain);

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        assertNotEquals("failure - Original blockchains chain was replaced by smaller chain, big ouf", bigChain.getChain(), smolChain.getChain());
        assertEquals("failure - incorrect logging message displayed","Incoming blockchain is not longer than current blockchain...", logsList.get(2).getMessage());
    }

    @Test
    public void testInvalidLongerIncomingBlockchainDoesntReplacesCurrentChain(){
        //Given
        Blockchain smolChain = new Blockchain(new ArrayList<>());
        smolChain.addBlock("im the only block in this chain =(");

        List<Block> invalidPreviousHashChain = new ArrayList<>(); //create a dummy chain as a new arraylist

        invalidPreviousHashChain.add(new Block().genesis()); //start the chain with a valid genesis block
        invalidPreviousHashChain.add(new Block().mineBlock(new Block().genesis(), "a valid second block")); //add a valid block after the genesis block

        Block thirdBlock = new Block().mineBlock(new Block().genesis(),"Im meant to be the second block in the chain");
        Block forthBlock = new Block().mineBlock(thirdBlock,"Im meant to be the second block in the chain");

        invalidPreviousHashChain.add(forthBlock);
        invalidPreviousHashChain.add(thirdBlock); //switch around the second and third blocks in the chain

        Blockchain evilBigBlockchain = new Blockchain(invalidPreviousHashChain); //Initialise the evil blockchain with the dummy chain

        //When
        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        smolChain.replaceChain(evilBigBlockchain);

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        assertNotEquals("failure - Original blockchains chain was replaced by invalid chain, big ouf", evilBigBlockchain.getChain(), smolChain.getChain());
        assertEquals("failure - Original blockchains chain is incorrect length", 2, smolChain.getChain().size());
        assertEquals("failure - incorrect logging message displayed","Chain is invalid, the {}th block in the chain has previousHash value {}, however the hash of the previous block is {}...", logsList.get(2).getMessage());
        assertEquals("failure - incorrect logging message displayed","Incoming blockchain is longer than current blockchain, but is not valid...", logsList.get(3).getMessage());
    }
}