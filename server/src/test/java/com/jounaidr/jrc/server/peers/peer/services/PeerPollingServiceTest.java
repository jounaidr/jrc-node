package com.jounaidr.jrc.server.peers.peer.services;


import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.jounaidr.jrc.server.blockchain.Blockchain;
import com.jounaidr.jrc.server.peers.Peers;
import com.jounaidr.jrc.server.peers.peer.Peer;
import com.jounaidr.jrc.server.peers.peer.PeerClient;
import com.jounaidr.jrc.server.peers.peer.util.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.ConnectException;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

class PeerPollingServiceTest {
    Logger logger = (Logger) LoggerFactory.getLogger(PeerPollingService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

    PeerPollingService testPeerPollingService;

    @Mock
    Peer mockPeer;

    @Mock
    ScheduledThreadPoolExecutor mockPeersExecutor;

    @Mock
    PeerClient mockPeerClient;

    @Mock
    Blockchain mockBlockchain;

    @Mock
    Peers mockPeers;

    @BeforeEach
    void setUp() {
        //Given
        MockitoAnnotations.initMocks(this);

        testPeerPollingService = new PeerPollingService(mockPeer, mockPeersExecutor);
        ReflectionTestUtils.setField(testPeerPollingService, "peerClient", mockPeerClient);
        ReflectionTestUtils.setField(testPeerPollingService, "blockchain", mockBlockchain);
        ReflectionTestUtils.setField(testPeerPollingService, "peers", mockPeers);
    }

    @Test
    public void testStartMethodScheduling(){
        //When
        testPeerPollingService.start();

        //Then
        //Verify executor schedules the task correctly
        Mockito.verify(mockPeersExecutor, times(1)).scheduleAtFixedRate(
                any(PeerPollingService.class),
                any(long.class), //This covers the random delay method as well
                any(long.class),
                any(TimeUnit.class));
    }

    @Test
    public void testRunPeerHealthCheckUp() throws IOException, JSONException {
        //Given
        Mockito.when(mockPeerClient.getPeerHealth()).thenReturn("UP");

        //When
        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        testPeerPollingService.run();

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        //Check the peer status is set to UP once
        Mockito.verify(mockPeer, times(1)).setPeerStatus(Status.UP);
        //Verify log message
        assertEquals("failure - incorrect logging message displayed","Connection reestablished with peer: [{}] ! Setting peer status to UP", logsList.get(1).getMessage());
    }

    @Test
    public void testRunConnectionException() throws IOException, JSONException {
        //Given
        Mockito.when(mockPeerClient.getPeerHealth()).thenThrow(new ConnectException());

        //When
        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        testPeerPollingService.run();

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        //Check the peer status is set to DOWN on connection exception
        Mockito.verify(mockPeer, times(1)).setPeerStatus(Status.DOWN);
        //Verify log message
        assertEquals("failure - incorrect logging message displayed","Could not poll the following peer: [{}]. Reason: {}. Setting peer status to DOWN", logsList.get(1).getMessage());
    }

    @Test
    public void testRunUnknownException() throws IOException, JSONException {
        //Given
        Mockito.when(mockPeerClient.getPeerHealth()).thenThrow(new NullPointerException()); //Throw an 'unknown' exception

        //When
        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        testPeerPollingService.run();

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        //Check the peer status is set to UNKNOWN
        Mockito.verify(mockPeer, times(1)).setPeerStatus(Status.UNKNOWN);
        //Verify log message
        assertEquals("failure - incorrect logging message displayed","Could not poll the following peer: [{}]. Reason: {}. Setting peer status to UNKNOWN", logsList.get(1).getMessage());
    }

    @Test
    public void testRunPeerHealthCheckUnknown() throws IOException, JSONException {
        //Given
        Mockito.when(mockPeerClient.getPeerHealth()).thenReturn("this is an invalid satus response");

        //When
        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        testPeerPollingService.run();

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        //Check the peer status is set to UP once
        Mockito.verify(mockPeer, times(1)).setPeerStatus(Status.UNKNOWN);
        //Verify log message
        assertEquals("failure - incorrect logging message displayed","Peer health check returned invalid response: {}. Setting peer [{}] status to UNKNOWN", logsList.get(1).getMessage());
    }

    @Test
    public void testRunGetPeersSocketList() throws IOException, JSONException {
        //Given
        Mockito.when(mockPeer.getPeerStatus()).thenReturn(Status.UP);
        Mockito.when(mockPeerClient.getHealthySocketsList()).thenReturn("10.10.10.10:8080");

        //When
        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        testPeerPollingService.run();

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        //Verify the new socketlist is added to peers
        Mockito.verify(mockPeers, times(1)).addSocketsList("10.10.10.10:8080");
        //Verify the cashed sockets list is update
        assertEquals("10.10.10.10:8080", ReflectionTestUtils.getField(testPeerPollingService, "cashedPeerSocketsList"));
        //Verify log message
        assertEquals("failure - incorrect logging message displayed","New peers have been detected from the following peer: [{}] !", logsList.get(1).getMessage());
    }

    @Test
    public void testRunPeersInSync_ZeroChainDiff() throws IOException, JSONException {
        //Given
        Mockito.when(mockPeer.getPeerStatus()).thenReturn(Status.UP);

        Mockito.when(mockPeerClient.getHealthySocketsList()).thenReturn("");
        Mockito.when(mockPeerClient.getPeerBlockchainSize()).thenReturn(0); //will result in 0 diff

        //When
        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        testPeerPollingService.run();

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        //Verify log message
        assertEquals("failure - incorrect logging message displayed","Peer [{}] is in sync with this node", logsList.get(2).getMessage());
    }

    @Test
    public void testRunNewValidPeerBlock_ChainDiffOne() throws IOException, JSONException {
        //Given
        Mockito.when(mockPeer.getPeerStatus()).thenReturn(Status.UP);

        Mockito.when(mockPeerClient.getHealthySocketsList()).thenReturn("");
        Mockito.when(mockPeerClient.getPeerBlockchainSize()).thenReturn(1); //Will result in a diff on 1

        //When
        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        testPeerPollingService.run();

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        //Verify the new block is added to the blockchain
        Mockito.verify(mockBlockchain, times(1)).addBlock(any());
        //Verify log message
        assertEquals("failure - incorrect logging message displayed","A new block was detected in the following peer: [{}] !", logsList.get(2).getMessage());
    }

    @Test
    public void testRunNewInvalidPeerBlock_ChainDiffOne() throws IOException, JSONException {
        //Given
        Mockito.when(mockPeer.getPeerStatus()).thenReturn(Status.UP);

        Mockito.when(mockPeerClient.getHealthySocketsList()).thenReturn("");
        Mockito.when(mockPeerClient.getPeerBlockchainSize()).thenReturn(1); //Will result in a diff on 1
        Mockito.when(mockPeerClient.getPeerLastBlock()).thenThrow(new InvalidObjectException("test exception"));

        //When
        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        testPeerPollingService.run();

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        //Verify log message
        assertEquals("failure - incorrect logging message displayed","Could not add the new block from peer: [{}]. Reason: {}", logsList.get(3).getMessage());
    }

    @Test
    public void testRunBlockchainOutOfSync_ChainDiffGreaterThanOne() throws IOException, JSONException {
        //Given
        Mockito.when(mockPeer.getPeerStatus()).thenReturn(Status.UP);

        Mockito.when(mockPeerClient.getHealthySocketsList()).thenReturn("");
        Mockito.when(mockPeerClient.getPeerBlockchainSize()).thenReturn(3); //Will result in a diff greater than 1

        //When
        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        testPeerPollingService.run();

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        //Verify peer blockchain call to client, and replace of the node blockchain...
        Mockito.verify(mockPeerClient, times(1)).getPeerBlockchain();
        Mockito.verify(mockBlockchain, times(1)).replaceChain(any());
        //Verify log message
        assertEquals("failure - incorrect logging message displayed","Attempting to synchronize with the following peer: [{}]", logsList.get(2).getMessage());
        assertEquals("failure - incorrect logging message displayed","Successfully synchronized blockchain with the following peer: [{}] !", logsList.get(3).getMessage());
    }

    @Test
    public void testRunBlockchainOutOfSyncInvalidBlock_ChainDiffGreaterThanOne() throws IOException, JSONException {
        //Given
        Mockito.when(mockPeer.getPeerStatus()).thenReturn(Status.UP);

        Mockito.when(mockPeerClient.getHealthySocketsList()).thenReturn("");
        Mockito.when(mockPeerClient.getPeerBlockchainSize()).thenReturn(3); //Will result in a diff greater than 1

        Mockito.doThrow(new InvalidObjectException("test exception")).when(mockBlockchain).replaceChain(any());

        //When
        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        testPeerPollingService.run();

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        //Verify peer blockchain call to client, and replace of the node blockchain...
        Mockito.verify(mockPeerClient, times(1)).getPeerBlockchain();
        Mockito.verify(mockBlockchain, times(1)).replaceChain(any());
        //Verify log message
        assertEquals("failure - incorrect logging message displayed","Attempting to synchronize with the following peer: [{}]", logsList.get(2).getMessage());
        assertEquals("failure - incorrect logging message displayed","Could not synchronize with the following peer: [{}]. Reason: {}", logsList.get(3).getMessage());
    }

    @Test
    public void testRunPeerIsBehindNode_NegativeChainDiff() throws IOException, JSONException {
        //Given
        Mockito.when(mockPeer.getPeerStatus()).thenReturn(Status.UP);

        Mockito.when(mockPeerClient.getHealthySocketsList()).thenReturn("");
        Mockito.when(mockPeerClient.getPeerBlockchainSize()).thenReturn(-1); //will result in negative diff

        //When
        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        testPeerPollingService.run();

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        //Verify log message
        assertEquals("failure - incorrect logging message displayed","Peer [{}] is behind this node", logsList.get(2).getMessage());
    }
}