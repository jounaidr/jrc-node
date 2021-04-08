//package com.jounaidr.jrc.server.peers;
//
//import ch.qos.logback.classic.Logger;
//import ch.qos.logback.classic.spi.ILoggingEvent;
//import ch.qos.logback.core.read.ListAppender;
//import com.jounaidr.jrc.server.blockchain.Block;
//import com.jounaidr.jrc.server.peers.peer.Peer;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.MockitoAnnotations;
//import org.slf4j.LoggerFactory;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.Assert.assertEquals;
//import static org.mockito.Mockito.times;
//
//class PeersTest {
//    Logger logger = (Logger) LoggerFactory.getLogger(Peers.class);
//    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
//
//    @Mock
//    Peer mockPeer;
//
//    @BeforeEach
//    void setUp() {
//        //Given
//        MockitoAnnotations.initMocks(this);
//    }
//
//    @Test
//    void testPeerInitialisationAndGetPeerList() {
//        //Given
//        Peers testPeers;
//
//        //When
//        listAppender.start();
//        logger.addAppender(listAppender); //start log capture...
//
//        testPeers = new Peers("10.10.10.10:5000", 1000, "20.20.20.20:3000,30.30.30.30:8000");
//
//        //Then
//        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs
//
//        assertEquals(2, testPeers.getPeerList().size());
//        assertEquals("20.20.20.20:3000", testPeers.getPeerList().get(0).getPeerSocket());
//        assertEquals("30.30.30.30:8000", testPeers.getPeerList().get(1).getPeerSocket());
//
//        assertEquals("10.10.10.10:5000", ReflectionTestUtils.getField(testPeers, "NODE_SOCKET"));
//        assertEquals(1000, ReflectionTestUtils.getField(testPeers, "MAX_PEERS"));
//
//        //NOTE: the below assertion could fail if the system running this test has more than 1000 logical cores, highly unlikely...
//        assertEquals("failure - incorrect logging message displayed","It is recommended to set the max peers to less than {} for your system, performance may be impacted...", logsList.get(0).getMessage());
//    }
//
//    @Test
//    void testAddSocketMaxPeerExceeded(){
//        //Given
//        Peers testPeers = new Peers("10.10.10.10:5000", 2, "20.20.20.20:3000");
//
//        //When
//        listAppender.start();
//        logger.addAppender(listAppender); //start log capture...
//
//        testPeers.addSocketsList("30.30.30.30:8000,58.23.15.55:9898");
//
//        //Then
//        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs
//
//        assertEquals("failure - incorrect logging message displayed","Unable to add new peer [{}] as max peer size of {} has been reached", logsList.get(2).getMessage());
//    }
//
//    @Test
//    void testAddSocketSameNodeSocket(){
//        //Given
//        Peers testPeers = new Peers("10.10.10.10:5000", 2, "20.20.20.20:3000");
//
//        //When
//        listAppender.start();
//        logger.addAppender(listAppender); //start log capture...
//
//        testPeers.addSocketsList("10.10.10.10:5000");
//
//        //Then
//        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs
//
//        assertEquals("failure - incorrect logging message displayed","Unable to add new peer [{}] as its socket refers to this node!", logsList.get(1).getMessage());
//    }
//
//    @Test
//    void testAddSocketInvalidSocket(){
//        //Given
//        Peers testPeers = new Peers("10.10.10.10:5000", 2, "20.20.20.20:3000");
//
//        //When
//        listAppender.start();
//        logger.addAppender(listAppender); //start log capture...
//
//        testPeers.addSocketsList("yeet:5000");
//
//        //Then
//        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs
//
//        assertEquals("failure - incorrect logging message displayed","Unable to add new peer [{}] as its socket is of invalid format", logsList.get(1).getMessage());
//    }
//
//    @Test
//    void testAddSocketKnownSocket(){
//        //Given
//        Peers testPeers = new Peers("10.10.10.10:5000", 2, "20.20.20.20:3000");
//
//        //When
//        listAppender.start();
//        logger.addAppender(listAppender); //start log capture...
//
//        testPeers.addSocketsList("20.20.20.20:3000");
//
//        //Then
//        List<ILoggingEvent> logsList = listAppender.list; //...store captured logs
//
//        assertEquals("failure - incorrect logging message displayed","Unable to add new peer [{}] as its already known", logsList.get(1).getMessage());
//    }
//
//    @Test
//    void testBroadcastBlockToPeers() {
//        //Given
//        Peers testPeers = new Peers("", 2, "");
//
//        ArrayList<Peer> testPeerList = new ArrayList<>();
//        testPeerList.add(mockPeer); //Create a dummy peerList with the mock peer...
//        ReflectionTestUtils.setField(testPeers, "peerList", testPeerList); //...And inject it into testPeers
//
//        Block testBlock = new Block("this","is","a","test","block","lol","yeet");;
//
//        //When
//        testPeers.broadcastBlockToPeers(testBlock);
//
//        //Then
//        Mockito.verify(mockPeer, times(1)).broadcastBlock(testBlock);
//    }
//}