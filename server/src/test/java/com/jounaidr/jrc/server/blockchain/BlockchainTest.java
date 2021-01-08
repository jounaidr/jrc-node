package com.jounaidr.jrc.server.blockchain;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.InvalidObjectException;
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
    public void testBlockchainAddsBlockCorrectly() throws InvalidObjectException {
        //Given
        Blockchain testChain = new Blockchain(new ArrayList<>());

        //When
        Block secondBlock = new Block().mineBlock(testChain.getLastBlock(), "Hi, im the second block");
        testChain.getChain().add(secondBlock);
        Block thirdBlock = new Block().mineBlock(testChain.getLastBlock(), "Sup second block im the third block");
        testChain.getChain().add(thirdBlock);
        Block forthBlock = new Block().mineBlock(testChain.getLastBlock(), "And im the fourth block =)");
        testChain.getChain().add(forthBlock);

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
    public void testAddBlockMethodValidBlock() throws InvalidObjectException {
        //Given
        Blockchain testChain = new Blockchain(new ArrayList<>());
        Block secondBlock = new Block().mineBlock(testChain.getLastBlock(), "Hi, im the second block");

        //When
        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        try {
            testChain.addBlock(secondBlock);
        } catch (NullPointerException e) { //TODO: Could replace with ReflectionTestUtils, see: https://www.baeldung.com/spring-reflection-test-utils
            //Since there is no peers bean, the peers.broadcastBlockToPeers() method call will fail,
            //Catch this and fail silently as its not relevant to this unit test and is tested during integration testing
        }

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        assertEquals("failure - last block in the chain isnt the newly added block...", secondBlock.toString(), testChain.getLastBlock().toString());
        assertEquals("failure - incorrect logging message displayed","...Block added successfully!", logsList.get(2).getMessage());
    }

    @Test
    public void testAddBlockDuplicateBlock() throws InvalidObjectException {
        //Given
        Blockchain testChain = new Blockchain(new ArrayList<>());
        Block secondBlock = new Block().mineBlock(testChain.getLastBlock(), "Hi, im the second block");
        testChain.getChain().add(secondBlock); //Add the second block to the chain directly

        //When
        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        try {
            testChain.addBlock(secondBlock);
        } catch (NullPointerException e) { //TODO: Could replace with ReflectionTestUtils, see: https://www.baeldung.com/spring-reflection-test-utils
            //Since there is no peers bean, the peers.broadcastBlockToPeers() method call will fail,
            //Catch this and fail silently as its not relevant to this unit test and is tested during integration testing
        }

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        assertEquals("failure - incorrect logging message displayed","Block has already been added to the chain!", logsList.get(1).getMessage());
    }

    @Test
    public void testAddBlockMethodInvalidBlock() {
        //Given
        Blockchain testChain = new Blockchain(new ArrayList<>());
        Block invalidBlock = new Block("I","am","an","evil","block","whos","invalid");

        //When
        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        Exception blockValidationException = null;
        try {
             blockValidationException = assertThrows(InvalidObjectException.class, () -> {
                //Catch the invalid block exception
                testChain.addBlock(invalidBlock);
            });
        } catch (NullPointerException e) { //TODO: Could replace with ReflectionTestUtils, see: https://www.baeldung.com/spring-reflection-test-utils
            //Since there is no peers bean, the peers.broadcastBlockToPeers() method call will fail,
            //Catch this and fail silently as its not relevant to this unit test and is tested during integration testing
        }

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        //check exception message
        assertTrue(blockValidationException.getMessage().contains("Block validation failed, this block doesn't reference the previous blocks hash correctly. Reference to previous hash:"));
        //check log message
        assertEquals("failure - incorrect logging message displayed","New incoming block is invalid and can't be added to the blockchain. Reason: {}", logsList.get(2).getMessage());
    }

    @Test
    public void testBlockchainValidationValidChain() throws InvalidObjectException {
        //Given
        Blockchain validChain = new Blockchain(new ArrayList<>());

        //When
        Block secondBlock = new Block().mineBlock(validChain.getLastBlock(), "Hi, im the second block");
        validChain.getChain().add(secondBlock);
        Block thirdBlock = new Block().mineBlock(validChain.getLastBlock(), "Sup second block im the third block");
        validChain.getChain().add(thirdBlock);
        Block forthBlock = new Block().mineBlock(validChain.getLastBlock(), "And im the fourth block =)");
        validChain.getChain().add(forthBlock);

        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        Boolean isChainValid = validChain.isChainValid();

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        assertTrue("failure - valid chain incorrectly flagged as invalid", isChainValid);
        assertEquals("failure - incorrect logging message displayed","Blockchain is valid...", logsList.get(0).getMessage());
    }

    @Test
    public void testBlockchainValidationInvalidGenesisBlock() throws InvalidObjectException {
        //Given
        Block genesisBlock = new Block().genesis();

        List<Block> invalidGenesisChain = new ArrayList<>(); //create a dummy chain as a new arraylist
        Block badBoyBlock = new Block().mineBlock(genesisBlock,"Im a verrrrry bad block whos going to do bad things >:)");
        invalidGenesisChain.add(badBoyBlock); //add a block that isn't the genesis block to the dummy chain

        Blockchain evilBlockchain = new Blockchain(invalidGenesisChain); //Initialise the evil blockchain with the dummy chain

        //When
        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        boolean isChainValid = evilBlockchain.isChainValid();

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

        Exception blockValidationException = assertThrows(InvalidObjectException.class, () -> {
            //Catch the invalid block exception
            evilBlockchain.isChainValid();
        });

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        //check exception message
        assertTrue(blockValidationException.getMessage().contains("Block validation failed, this block doesn't reference the previous blocks hash correctly. Reference to previous hash:"));
        //check log message
        assertEquals("Chain is invalid, the block {} in the chain is invalid.", logsList.get(0).getMessage());
    }

//    VALIDATION OF BLOCK POW DONE IN Block().validateBlock()... THEREFORE THIS TEST IS NO LONGER NECESSARY
//    @Test
//    public void testBlockchainValidationInvalidProofOfWork() throws InvalidObjectException {
//        //Given
//        List<Block> invalidPreviousHashChain = new ArrayList<>(); //create a dummy chain as a new arraylist
//
//        Block genesisBlock = new Block().genesis();
//        Block validSecondBlock = new Block().mineBlock(genesisBlock,"secondBlockData"); //A valid second block
//
//        //Generate a block based on the previously generated valid second block, but set proof of work to an invalid binary string
//        Block evilSecondBlock = new Block(validSecondBlock.getHash(), validSecondBlock.getPreviousHash(), validSecondBlock.getData(), validSecondBlock.getTimeStamp(), validSecondBlock.getNonce(), validSecondBlock.getDifficulty(), "1000101");
//
//        invalidPreviousHashChain.add(genesisBlock);
//        invalidPreviousHashChain.add(evilSecondBlock); //Add the invalid block to the dummy chain
//
//        Blockchain evilBlockchain = new Blockchain(invalidPreviousHashChain); //Initialise the evil blockchain with the dummy chain
//
//        //When
//        listAppender.start();
//        logger.addAppender(listAppender); //start log capture...
//
//        Boolean isChainValid = evilBlockchain.isChainValid();
//
//        //Then
//        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs
//
//        assertFalse("failure - blockchain that has a block with invalid proof of work has verified as valid, big ouf...", isChainValid);
//        assertEquals("failure - incorrect logging message displayed","Chain is invalid, the {}th block in the chain has an invalid proof of work...", logsList.get(0).getMessage());
//    }

    @Test
    public void testReplaceChainInvalidGenesisBlock() throws InvalidObjectException {
        //Given
        Blockchain testChain = new Blockchain(new ArrayList<>());

        Block genesisBlock = new Block().genesis();

        List<Block> invalidGenesisChain = new ArrayList<>(); //create a dummy chain as a new arraylist
        Block badBoyBlock = new Block().mineBlock(genesisBlock,"Im a verrrrry bad block whos going to do bad things >:)");
        invalidGenesisChain.add(badBoyBlock); //add a block that isn't the genesis block to the dummy chain

        Blockchain evilBlockchain = new Blockchain(invalidGenesisChain); //Initialise the evil blockchain with the dummy chain
        evilBlockchain.getChain().add(genesisBlock); //Add the genesis block as second block in the evil chain

        //When
        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        testChain.replaceChain(evilBlockchain);

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        assertEquals("failure - incorrect logging message displayed","Chain is invalid, first block in the chain is not genesis block...", logsList.get(1).getMessage());
    }

    @Test
    public void testBlockchainValidationInvalidDifficulty() throws InvalidObjectException {
        //Given
        List<Block> invalidPreviousHashChain = new ArrayList<>(); //create a dummy chain as a new arraylist

        Block genesisBlock = new Block().genesis();

        //Generate a block based on the previously generated valid second block, but set jump the difficulty more than 1
        Block evilSecondBlock = new Block("fb6bf0dd384664830a642467b184c2473fa6b21251d9c45b333e9f55505294c7", "89b76d274d54b62e56aea14299ea6feb282e5ba573cd378a42ecdfb00a772c22", "secondBlockData", "2020-11-14T23:55:10.208261300Z", "4", "69", "1111110001001111000001100000100101100111011000111000110111110100110001010100011111101001010010110000101011110101011111101111101100111110000001010110001101111111101110100101111100011110000000110001100100110110010010101100011111110010100101010100101000011101");

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
        assertEquals("failure - incorrect logging message displayed","Chain is invalid, the block {} in the chain has a difficulty jump greater than 1. Difficulty changed by: {}...", logsList.get(0).getMessage());
    }

    @Test
    public void testValidLongerIncomingBlockchainReplacesCurrentChain() throws InvalidObjectException {
        //Given
        Blockchain smolChain = new Blockchain(new ArrayList<>());
        Blockchain bigChain = new Blockchain(new ArrayList<>());

        Block smolChainBlock = new Block().mineBlock(smolChain.getLastBlock(), "im the only block in this chain =(");
        smolChain.getChain().add(smolChainBlock);

        Block secondBlock = new Block().mineBlock(bigChain.getLastBlock(), "Hi, im the second block");
        bigChain.getChain().add(secondBlock);
        Block thirdBlock = new Block().mineBlock(bigChain.getLastBlock(), "Sup second block im the third block");
        bigChain.getChain().add(thirdBlock);
        Block forthBlock = new Block().mineBlock(bigChain.getLastBlock(), "And im the fourth block =)");
        bigChain.getChain().add(forthBlock);

        //When
        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        smolChain.replaceChain(bigChain);

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        assertEquals("failure - Original blockchains chain was not replaced by the longer incoming chain", bigChain.getChain(), smolChain.getChain());
        assertEquals("failure - Original blockchains chain is incorrect length after replacement", 4, smolChain.getChain().size());

        assertEquals("failure - incorrect logging message displayed","Blockchain is valid...", logsList.get(1).getMessage());
        assertEquals("failure - incorrect logging message displayed","Chain replacement successful...", logsList.get(2).getMessage());
    }

    @Test
    public void testValidShorterIncomingBlockchainDoesntReplacesCurrentChain() throws InvalidObjectException {
        //Given
        Blockchain smolChain = new Blockchain(new ArrayList<>());
        Blockchain bigChain = new Blockchain(new ArrayList<>());

        Block smolChainBlock = new Block().mineBlock(smolChain.getLastBlock(), "im the only block in this chain =(");
        smolChain.getChain().add(smolChainBlock);

        Block secondBlock = new Block().mineBlock(bigChain.getLastBlock(), "Hi, im the second block");
        bigChain.getChain().add(secondBlock);
        Block thirdBlock = new Block().mineBlock(bigChain.getLastBlock(), "Sup second block im the third block");
        bigChain.getChain().add(thirdBlock);
        Block forthBlock = new Block().mineBlock(bigChain.getLastBlock(), "And im the fourth block =)");
        bigChain.getChain().add(forthBlock);

        //When
        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        bigChain.replaceChain(smolChain);

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        assertNotEquals("failure - Original blockchains chain was replaced by smaller chain, big ouf", bigChain.getChain(), smolChain.getChain());
        assertEquals("failure - incorrect logging message displayed","Incoming blockchain is not longer than current blockchain...", logsList.get(1).getMessage());
    }

    @Test
    public void testInvalidLongerIncomingBlockchainDoesntReplacesCurrentChain(){
        //Given
        Blockchain smolChain = new Blockchain(new ArrayList<>());
        Block smolChainBlock = new Block().mineBlock(smolChain.getLastBlock(), "im the only block in this chain =(");
        smolChain.getChain().add(smolChainBlock);

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

        Exception blockValidationException = assertThrows(InvalidObjectException.class, () -> {
            //Catch the invalid block exception
            smolChain.replaceChain(evilBigBlockchain);;
        });

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        assertNotEquals("failure - Original blockchains chain was replaced by invalid chain, big ouf", evilBigBlockchain.getChain(), smolChain.getChain());
        assertEquals("failure - Original blockchains chain is incorrect length", 2, smolChain.getChain().size());
        //check exception message
        assertTrue(blockValidationException.getMessage().contains("Block validation failed, this block doesn't reference the previous blocks hash correctly. Reference to previous hash:"));
        //check log messages
        assertEquals("Chain is invalid, the block {} in the chain is invalid.", logsList.get(1).getMessage());
    }
}