package com.jounaidr.jrc.server.peers.peer.services;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.jounaidr.jrc.server.blockchain.Block;
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
import java.net.ConnectException;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;

class PeerBroadcastingServiceTest {
    Logger logger = (Logger) LoggerFactory.getLogger(PeerBroadcastingService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

    PeerBroadcastingService testPeerBroadcastingService;

    @Mock
    Peer mockPeer;

    @Mock
    ScheduledThreadPoolExecutor mockPeersExecutor;

    @Mock
    PeerClient mockPeerClient;

    @BeforeEach
    void setUp() {
        //Given
        MockitoAnnotations.initMocks(this);
        Mockito.when(mockPeer.getPeerStatus()).thenReturn(Status.UP);

        testPeerBroadcastingService = new PeerBroadcastingService(mockPeer, mockPeersExecutor);
        ReflectionTestUtils.setField(testPeerBroadcastingService, "peerClient", mockPeerClient);
    }

    @Test
    public void testBroadcastBlock(){
        //Given
        Block testBlock = new Block("this","is","a","test","block","lol","yeet");

        //When
        testPeerBroadcastingService.broadcastBlock(testBlock);

        //Then
        //Check blockToBroadcast is set correctly
        assertEquals(testBlock.toString(), ReflectionTestUtils.getField(testPeerBroadcastingService, "blockToBroadcast").toString());
        //Verify executor submits the runnable task once
        Mockito.verify(mockPeersExecutor, times(1)).submit(testPeerBroadcastingService);
    }

    @Test
    public void testRunSuccess() throws IOException, JSONException {
        //Given
        Block testBlock = new Block("this","is","a","test","block","lol","yeet");;
        ReflectionTestUtils.setField(testPeerBroadcastingService, "blockToBroadcast", testBlock);

        //When
        testPeerBroadcastingService.run();

        //Then
        //Verify the peer client addBlockToPeer method is called with the testBlock
        Mockito.verify(mockPeerClient, times(1)).addBlockToPeer(testBlock);
    }

    @Test
    public void testRunConnectionException() throws IOException, JSONException {
        //Given
        Block testBlock = new Block("this","is","a","test","block","lol","yeet");;
        ReflectionTestUtils.setField(testPeerBroadcastingService, "blockToBroadcast", testBlock);

        Mockito.when(mockPeerClient.addBlockToPeer(testBlock)).thenThrow(new ConnectException());

        //When
        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        testPeerBroadcastingService.run();

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        //Verify status is set to DOWN
        Mockito.verify(mockPeer, times(1)).setPeerStatus(Status.DOWN);
        //Verify log message
        assertEquals("failure - incorrect logging message displayed","Could not broadcast to the following peer: [{}]. Reason: {}. Setting peer status to DOWN", logsList.get(1).getMessage());
    }

    @Test
    public void testRunUnknownException() throws IOException, JSONException {
        //Given
        Block testBlock = new Block("this","is","a","test","block","lol","yeet");;
        ReflectionTestUtils.setField(testPeerBroadcastingService, "blockToBroadcast", testBlock);

        Mockito.when(mockPeerClient.addBlockToPeer(testBlock)).thenThrow(new NullPointerException());

        //When
        listAppender.start();
        logger.addAppender(listAppender); //start log capture...

        testPeerBroadcastingService.run();

        //Then
        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs

        //Verify status is set to DOWN
        Mockito.verify(mockPeer, times(1)).setPeerStatus(Status.UNKNOWN);
        //Verify log message
        assertEquals("failure - incorrect logging message displayed","Could not broadcast to the following peer: [{}]. Reason: {}. Setting peer status to UNKNOWN", logsList.get(1).getMessage());
    }
}