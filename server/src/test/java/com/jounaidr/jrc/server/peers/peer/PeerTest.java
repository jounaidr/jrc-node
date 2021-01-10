package com.jounaidr.jrc.server.peers.peer;

import com.jounaidr.jrc.server.blockchain.Block;
import com.jounaidr.jrc.server.peers.peer.services.PeerBroadcastingService;
import com.jounaidr.jrc.server.peers.peer.services.PeerPollingService;
import com.jounaidr.jrc.server.peers.peer.util.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;

class PeerTest {
    @Mock
    PeerPollingService mockPeerPollingService;

    @Mock
    PeerBroadcastingService mockPeerBroadcastingService;

    @BeforeEach
    void setUp() {
        //Given
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testPeerInitialisation(){
        //Given
        Peer testPeer;

        //When
        testPeer = new Peer("10.100.156.55:7090", new ScheduledThreadPoolExecutor(1));

        //Then
        assertEquals(Status.UNKNOWN, testPeer.getPeerStatus());
        assertEquals("10.100.156.55:7090", testPeer.getPeerSocket());
    }

    @Test
    void testPeerPolling(){
        //Given
        Peer testPeer = new Peer("10.100.156.55:7090", new ScheduledThreadPoolExecutor(1));
        ReflectionTestUtils.setField(testPeer, "peerPoller", mockPeerPollingService);

        //When
        testPeer.startPolling();

        //Then
        Mockito.verify(mockPeerPollingService, times(1)).start();
    }

    @Test
    void testPeerBroadcasting(){
        //Given
        Peer testPeer = new Peer("10.100.156.55:7090", new ScheduledThreadPoolExecutor(1));
        ReflectionTestUtils.setField(testPeer, "peerBroadcaster", mockPeerBroadcastingService);

        Block testBlock = new Block("this","is","a","test","block","lol","yeet");;

        //When
        testPeer.broadcastBlock(testBlock);

        //Then
        Mockito.verify(mockPeerBroadcastingService, times(1)).broadcastBlock(testBlock);
    }

    @Test
    void testPeerStatusGetterAndSetter(){
        //Given
        Peer testPeer = new Peer("10.100.156.55:7090", new ScheduledThreadPoolExecutor(1));
        ReflectionTestUtils.setField(testPeer, "peerBroadcaster", mockPeerBroadcastingService);

        //When
        testPeer.setPeerStatus(Status.DOWN);

        //Then
        assertEquals(Status.DOWN, testPeer.getPeerStatus());
    }
}