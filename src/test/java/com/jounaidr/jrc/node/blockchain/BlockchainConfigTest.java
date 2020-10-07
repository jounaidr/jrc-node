package com.jounaidr.jrc.node.blockchain;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
class BlockchainConfigTest {

    Logger logger = (Logger) LoggerFactory.getLogger(Blockchain.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

    @Test
    public void testBlockchainconfigInitialisesBlockchain(){
        //Given
        BlockchainConfig testConfig = new BlockchainConfig();
        Block genesisBlock = new Block().genesis();

        //When
        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        Blockchain testChain = testConfig.blockchain();
        Boolean isChainValid = testChain.isChainValid();

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        assertEquals("failure - hash of the first block in the blockchain does not equal the genesis hash", genesisBlock.getHash(), testChain.getChain().get(0).getHash());
        assertEquals("failure - data of the first block in the blockchain does not equal the genesis data", genesisBlock.getData(), testChain.getChain().get(0).getData());
        assertEquals("failure - previous hash of the first block in the blockchain does not equal the genesis previous hash", genesisBlock.getPreviousHash(), testChain.getChain().get(0).getPreviousHash());
        assertEquals("failure - timestamp of the first block in the blockchain does not equal the genesis timestamp", genesisBlock.getTimeStamp(), testChain.getChain().get(0).getTimeStamp());

        assertTrue("failure - valid chain incorrectly flagged as invalid", isChainValid);

        assertEquals("failure - Original blockchains chain is incorrect length", 1, testChain.getChain().size());

        assertEquals("failure - incorrect logging message displayed","Initiating blockchain with genesis block...", logsList.get(0).getMessage());
    }
}