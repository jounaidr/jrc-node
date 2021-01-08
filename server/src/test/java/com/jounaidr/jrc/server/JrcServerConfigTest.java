package com.jounaidr.jrc.server;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.jounaidr.jrc.server.JrcServerConfig;
import com.jounaidr.jrc.server.api.implementation.BlockchainApiDelegateImpl;
import com.jounaidr.jrc.server.api.implementation.PeersApiDelegateImpl;
import com.jounaidr.jrc.server.blockchain.Block;
import com.jounaidr.jrc.server.blockchain.Blockchain;
import com.jounaidr.jrc.server.peers.Peers;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InvalidObjectException;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
class JrcServerConfigTest {

    Logger logger = (Logger) LoggerFactory.getLogger(Blockchain.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

    @Test
    public void testServerConfigInitialisesBlockchain() throws InvalidObjectException {
        //Given
        JrcServerConfig testConfig = new JrcServerConfig();
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

        assertEquals("failure - incorrect logging message displayed","Attempting to initialise a blockchain with the following chain array: {}...", logsList.get(0).getMessage());
        assertEquals("failure - incorrect logging message displayed","A Fresh blockchain has been initialised with genesis block...", logsList.get(1).getMessage());
    }

    @Test
    public void testServerConfigInitialisesBlockchainApiDelegateImpl(){
        //Given
        JrcServerConfig testConfig = new JrcServerConfig();

        //When
        BlockchainApiDelegateImpl testBlockchainApiDelegateImpl = testConfig.blockchainApiDelegateImpl();

        //Then
        //Not much you can unit test with the api delegated, so just make sure its not null I guess ¯\_(ツ)_/¯
        assertNotNull(testBlockchainApiDelegateImpl);
    }

    @Test
    public void testServerConfigInitialisesPeers(){
        //Given
        JrcServerConfig testConfig = new JrcServerConfig();
        ReflectionTestUtils.setField(testConfig, "NODE_SOCKET", "127.0.0.1:2323");
        ReflectionTestUtils.setField(testConfig, "PEERS_MAX", 200);
        ReflectionTestUtils.setField(testConfig, "PEERS_SOCKETS", "54.90.44.155:8080,23.444.23.145:2020");

        //When
        Peers testPeers = testConfig.peers();

        //Then
        assertEquals("failure - Node Socket value incorrect", "127.0.0.1:2323", ReflectionTestUtils.getField(testPeers, "NODE_SOCKET"));
        assertEquals("failure - Max peers value incorrect", 200, ReflectionTestUtils.getField(testPeers, "MAX_PEERS"));
        assertEquals("failure - Max peers value incorrect", "54.90.44.155:8080", testPeers.getPeerList().get(0).getPeerSocket());
        assertEquals("failure - Max peers value incorrect", "23.444.23.145:2020", testPeers.getPeerList().get(1).getPeerSocket());
    }

    @Test
    public void testServerConfigInitialisesPeersApiDelegateImpl(){
        //Given
        JrcServerConfig testConfig = new JrcServerConfig();

        //When
        PeersApiDelegateImpl testPeersApiDelegateImpl = testConfig.peersApiDelegateImpl();

        //Then
        //Not much you can unit test with the api delegated, so just make sure its not null I guess ¯\_(ツ)_/¯
        assertNotNull(testPeersApiDelegateImpl);
    }
}