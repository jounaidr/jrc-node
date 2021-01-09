package com.jounaidr.jrc.server.api.implementation;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.jounaidr.jrc.server.api.generated.model.BlockModel;
import com.jounaidr.jrc.server.api.implementation.util.BlockModelUtil;
import com.jounaidr.jrc.server.blockchain.Block;
import com.jounaidr.jrc.server.blockchain.Blockchain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

class BlockchainApiDelegateImplTest {
    Logger logger = (Logger) LoggerFactory.getLogger(Blockchain.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

    private Blockchain testChain;
    private BlockchainApiDelegateImpl testApiDelegate;

    @BeforeEach
    void setUp() {
        //Given

        //Initialise a valid blockchain
        testChain = new Blockchain(new ArrayList<>());
        Block secondBlock = new Block().mineBlock(testChain.getLastBlock(), "Hi, im the second block");
        testChain.getChain().add(secondBlock);
        Block thirdBlock = new Block().mineBlock(testChain.getLastBlock(), "Sup second block im the third block");
        testChain.getChain().add(thirdBlock);
        Block forthBlock = new Block().mineBlock(testChain.getLastBlock(), "And im the fourth block =)");
        testChain.getChain().add(forthBlock);

        //Initialise the api delegate and inject the test blockchain
        testApiDelegate = new BlockchainApiDelegateImpl();
        ReflectionTestUtils.setField(testApiDelegate, "blockchain", testChain);
    }

    @Test
    void testBlockchainApiDelegateImplGetEndpoints() {
        //When
        String blockchainResponse = testApiDelegate.getBlockchain().toString();
        String blockchainSizeResponse = testApiDelegate.getBlockchainSize().toString();
        String lastBlockResponse = testApiDelegate.getLastBlock().toString();

        //Then
        assertEquals("<200 OK OK,[class BlockModel {\n" +
                "    hash: 89b76d274d54b62e56aea14299ea6feb282e5ba573cd378a42ecdfb00a772c22\n" +
                "    previousHash: dummyhash\n" +
                "    data: dummydata\n" +
                "    timeStamp: 2020-11-07T19:40:57.585581100Z\n" +
                "    nonce: dummydata\n" +
                "    difficulty: 3\n" +
                "    proofOfWork: 1101011101110100010010011001011010101001000010001011100011111011000110111000010010111111000100000000000011100011110011000000001101011000011110011010111110001000101010111000000100001100010101001100101011001110011110010000011110001011001010000001010011011000\n" +
                "}, class BlockModel {\n" +
                "    hash: " + testChain.getChain().get(1).getHash() + "\n" +
                "    previousHash: " + testChain.getChain().get(1).getPreviousHash() + "\n" +
                "    data: " + testChain.getChain().get(1).getData() + "\n" +
                "    timeStamp: " + testChain.getChain().get(1).getTimeStamp() + "\n" +
                "    nonce: " + testChain.getChain().get(1).getNonce() + "\n" +
                "    difficulty: " + testChain.getChain().get(1).getDifficulty() + "\n" +
                "    proofOfWork: " + testChain.getChain().get(1).getProofOfWork() + "\n" +
                "}, class BlockModel {\n" +
                "    hash: " + testChain.getChain().get(2).getHash() + "\n" +
                "    previousHash: " + testChain.getChain().get(2).getPreviousHash() + "\n" +
                "    data: " + testChain.getChain().get(2).getData() + "\n" +
                "    timeStamp: " + testChain.getChain().get(2).getTimeStamp() + "\n" +
                "    nonce: " + testChain.getChain().get(2).getNonce() + "\n" +
                "    difficulty: " + testChain.getChain().get(2).getDifficulty() + "\n" +
                "    proofOfWork: " + testChain.getChain().get(2).getProofOfWork() + "\n" +
                "}, class BlockModel {\n" +
                "    hash: " + testChain.getChain().get(3).getHash() + "\n" +
                "    previousHash: " + testChain.getChain().get(3).getPreviousHash() + "\n" +
                "    data: " + testChain.getChain().get(3).getData() + "\n" +
                "    timeStamp: " + testChain.getChain().get(3).getTimeStamp() + "\n" +
                "    nonce: " + testChain.getChain().get(3).getNonce() + "\n" +
                "    difficulty: " + testChain.getChain().get(3).getDifficulty() + "\n" +
                "    proofOfWork: " + testChain.getChain().get(3).getProofOfWork() + "\n" +
                "}],[]>", blockchainResponse);

        assertEquals("<200 OK OK,class BlockModel {\n" +
                "    hash: " + testChain.getChain().get(3).getHash() + "\n" +
                "    previousHash: " + testChain.getChain().get(3).getPreviousHash() + "\n" +
                "    data: " + testChain.getChain().get(3).getData() + "\n" +
                "    timeStamp: " + testChain.getChain().get(3).getTimeStamp() + "\n" +
                "    nonce: " + testChain.getChain().get(3).getNonce() + "\n" +
                "    difficulty: " + testChain.getChain().get(3).getDifficulty() + "\n" +
                "    proofOfWork: " + testChain.getChain().get(3).getProofOfWork() + "\n" +
                "},[]>", lastBlockResponse);

        assertEquals("<200 OK OK,4,[]>", blockchainSizeResponse);
    }

    @Test //TODO: Also test 200 status successful response code
    void testBlockchainApiDelegateImplAddValidBlock() {
        //Given
        Block newBlock = new Block().mineBlock(testChain.getLastBlock(), "newww blockkkk");
        BlockModel newBlockModel = BlockModelUtil.getBlockAsModel(newBlock);

        //When
        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        try {
            testApiDelegate.addBlock(newBlockModel);
        } catch (NullPointerException e) { //TODO: Could replace with ReflectionTestUtils, see: https://www.baeldung.com/spring-reflection-test-utils
            //Since there is no peers bean, the peers.broadcastBlockToPeers() method call will fail,
            //Catch this and fail silently as its not relevant to this unit test and is tested during integration testing
        }

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        assertEquals("failure - incorrect logging message displayed","...Block added successfully!", logsList.get(2).getMessage());
        assertEquals(newBlock.toString(), testChain.getLastBlock().toString()); //New block is the last block in the chain
    }

    @Test
    void testBlockchainApiDelegateImplAddInvalidBlock() {
        //Given
        Block invalidBlock = new Block("I","am","an","evil","block","whos","invalid");
        BlockModel newBlockModel = BlockModelUtil.getBlockAsModel(invalidBlock);

        //When
        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        String badResponse = testApiDelegate.addBlock(newBlockModel).toString();

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        assertEquals("<400 BAD_REQUEST Bad Request,Block validation failed, this block doesn't reference the previous blocks hash correctly. Reference to previous hash: am, supplied previous blocks hash: " + testChain.getLastBlock().getHash() +  "...,[]>", badResponse);
        assertEquals("failure - incorrect logging message displayed","New incoming block is invalid and can't be added to the blockchain. Reason: {}", logsList.get(2).getMessage());
    }
}