//package com.jounaidr.jrc.server.api.implementation.util;
//
//import com.jounaidr.jrc.server.api.generated.model.PeerModel;
//import com.jounaidr.jrc.server.peers.peer.Peer;
//import org.junit.jupiter.api.Test;
//
//import static org.junit.Assert.assertEquals;
//
//class PeerModelUtilTest {
//
//    @Test
//    void getPeerAsModel() {
//        //Given
//        //Initialise test peer, executor isn't needed as peer functionality isn't tested in this unit test
//        Peer testPeer = new Peer("10.10.10.10:8080", null);
//
//        //When
//        PeerModel testPeerModel = PeerModelUtil.getPeerAsModel(testPeer);
//
//        //Then
//        assertEquals("10.10.10.10:8080", testPeerModel.getPeerSocket());
//        assertEquals("UNKNOWN", testPeerModel.getPeerStatus());
//    }
//}