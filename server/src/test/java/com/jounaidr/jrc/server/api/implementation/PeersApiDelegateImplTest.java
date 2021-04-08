//package com.jounaidr.jrc.server.api.implementation;
//
//import com.jounaidr.jrc.server.peers.Peers;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import static org.junit.Assert.assertEquals;
//
//class PeersApiDelegateImplTest {
//    private Peers peers;
//    private PeersApiDelegateImpl testApiDelegate;
//
//    @BeforeEach
//    void setUp() {
//        //Initialise peers with some dummy values
//        peers = new Peers("127.0.0.1:2323", 200, "54.90.44.155:8080,23.444.23.145:2020");
//
//        //Initialise the api delegate and inject the test peers
//        testApiDelegate = new PeersApiDelegateImpl();
//        ReflectionTestUtils.setField(testApiDelegate, "peers", peers);
//    }
//
//    @Test
//    void testPeersApiDelegateImplGetEndpoints() {
//        //When
//        String peersResponse = testApiDelegate.getSocketsList().toString();
//
//        //Then
//        assertEquals("<200 OK OK,[class PeerModel {\n" +
//                "    peerSocket: 54.90.44.155:8080\n" +
//                "    peerStatus: UNKNOWN\n" +
//                "}, class PeerModel {\n" +
//                "    peerSocket: 23.444.23.145:2020\n" +
//                "    peerStatus: UNKNOWN\n" +
//                "}],[]>", peersResponse);
//    }
//}