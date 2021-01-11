package com.jounaidr.jrc.server.peers.peer;

import com.jounaidr.jrc.server.blockchain.Block;
import com.jounaidr.jrc.server.blockchain.Blockchain;
import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

class PeerClientTest {
    PeerClient testPeerClient;

    @BeforeEach
    void setUp() {
        testPeerClient = new PeerClient("test:7090");
    }

    @Test
    void getPeerBlockchain() throws IOException, JSONException {
        //Given
        OkHttpClient mockOkHttpClient = mockHttpClient("[{\"hash\":\"89b76d274d54b62e56aea14299ea6feb282e5ba573cd378a42ecdfb00a772c22\",\"previousHash\":\"dummyhash\",\"data\":\"dummydata\",\"timeStamp\":\"2020-11-07T19:40:57.585581100Z\",\"nonce\":\"dummydata\",\"difficulty\":\"3\",\"proofOfWork\":\"1101011101110100010010011001011010101001000010001011100011111011000110111000010010111111000100000000000011100011110011000000001101011000011110011010111110001000101010111000000100001100010101001100101011001110011110010000011110001011001010000001010011011000\"},{\"hash\":\"e1f6c84e1d746cbe7d20347ed8c233bb94d35f39728c1dc7109813a3184df3c2\",\"previousHash\":\"89b76d274d54b62e56aea14299ea6feb282e5ba573cd378a42ecdfb00a772c22\",\"data\":\"poop\",\"timeStamp\":\"2021-01-11T17:20:45.196868Z\",\"nonce\":\"6\",\"difficulty\":\"2\",\"proofOfWork\":\"0011111101100101100001110100111111110010111111000000010010001100101001001001010000001110000010100101000111001010000111001000010000111110000110101000000111110001010111111110101110010111011000110010000010111010001011000011110011010011010111110000111011001011\"},{\"hash\":\"e8b4c47993c2b6aa71caee141f1dfb0f07c394f6cc963df53ee3ab13214d71e1\",\"previousHash\":\"e1f6c84e1d746cbe7d20347ed8c233bb94d35f39728c1dc7109813a3184df3c2\",\"data\":\"poodsadp\",\"timeStamp\":\"2021-01-11T17:20:45.587369900Z\",\"nonce\":\"23\",\"difficulty\":\"3\",\"proofOfWork\":\"0001000011110010101111111101110100100101010011111101011001111101000011101111001100001101111100001010000001011000101010111101100111101110000011010000100100010011100011110001000011010001101100101001111010110110010110000010110000100101110100100001101010010110\"},{\"hash\":\"596b27130c0af3539cc52dcb64561f29617a86e4db0890f020047eefa4917ed4\",\"previousHash\":\"e8b4c47993c2b6aa71caee141f1dfb0f07c394f6cc963df53ee3ab13214d71e1\",\"data\":\"pooasdasp\",\"timeStamp\":\"2021-01-11T17:20:45.665476200Z\",\"nonce\":\"3\",\"difficulty\":\"4\",\"proofOfWork\":\"0000110100110010001111110100111111101011000110000110001100100011011111010001000110101010100100111010101000011100100011101011100001110000010000001011001100000011001010100110101010100110010010001000011111110001110100011111100011111000100001100101100000001101\"}]");
        ReflectionTestUtils.setField(testPeerClient, "client", mockOkHttpClient); //Create a mock client with a json blockchain response and inject into the test client

        //When
        ArrayList<Block> testResponse = testPeerClient.getPeerBlockchain();

        //Then
        assertTrue(new Blockchain(testResponse).isChainValid()); //Check the response is valid

        assertEquals(4, testResponse.size()); //Check its size

        assertEquals("Block{hash='89b76d274d54b62e56aea14299ea6feb282e5ba573cd378a42ecdfb00a772c22', previousHash='dummyhash', data='dummydata', timeStamp='2020-11-07T19:40:57.585581100Z', nonce='dummydata', difficulty='3', proofOfWork='1101011101110100010010011001011010101001000010001011100011111011000110111000010010111111000100000000000011100011110011000000001101011000011110011010111110001000101010111000000100001100010101001100101011001110011110010000011110001011001010000001010011011000'}",
                testResponse.get(0).toString());
        assertEquals("Block{hash='e1f6c84e1d746cbe7d20347ed8c233bb94d35f39728c1dc7109813a3184df3c2', previousHash='89b76d274d54b62e56aea14299ea6feb282e5ba573cd378a42ecdfb00a772c22', data='poop', timeStamp='2021-01-11T17:20:45.196868Z', nonce='6', difficulty='2', proofOfWork='0011111101100101100001110100111111110010111111000000010010001100101001001001010000001110000010100101000111001010000111001000010000111110000110101000000111110001010111111110101110010111011000110010000010111010001011000011110011010011010111110000111011001011'}",
                testResponse.get(1).toString());
        assertEquals("Block{hash='e8b4c47993c2b6aa71caee141f1dfb0f07c394f6cc963df53ee3ab13214d71e1', previousHash='e1f6c84e1d746cbe7d20347ed8c233bb94d35f39728c1dc7109813a3184df3c2', data='poodsadp', timeStamp='2021-01-11T17:20:45.587369900Z', nonce='23', difficulty='3', proofOfWork='0001000011110010101111111101110100100101010011111101011001111101000011101111001100001101111100001010000001011000101010111101100111101110000011010000100100010011100011110001000011010001101100101001111010110110010110000010110000100101110100100001101010010110'}",
                testResponse.get(2).toString());
        assertEquals("Block{hash='596b27130c0af3539cc52dcb64561f29617a86e4db0890f020047eefa4917ed4', previousHash='e8b4c47993c2b6aa71caee141f1dfb0f07c394f6cc963df53ee3ab13214d71e1', data='pooasdasp', timeStamp='2021-01-11T17:20:45.665476200Z', nonce='3', difficulty='4', proofOfWork='0000110100110010001111110100111111101011000110000110001100100011011111010001000110101010100100111010101000011100100011101011100001110000010000001011001100000011001010100110101010100110010010001000011111110001110100011111100011111000100001100101100000001101'}",
                testResponse.get(3).toString());
    }

    @Test
    void getPeerBlockchainSize() throws IOException {
        //Given
        OkHttpClient mockOkHttpClient = mockHttpClient("69");
        ReflectionTestUtils.setField(testPeerClient, "client", mockOkHttpClient); //Create a mock client with a json blockchain size response and inject into the test client

        //When
        int testBlockchainSizeResponse = testPeerClient.getPeerBlockchainSize();

        //Then
        assertEquals(69, testBlockchainSizeResponse); //Check the correct value was returned
    }

    @Test
    void getPeerLastBlock() throws IOException, JSONException {
        //Given
        OkHttpClient mockOkHttpClient = mockHttpClient("{\"hash\":\"596b27130c0af3539cc52dcb64561f29617a86e4db0890f020047eefa4917ed4\",\"previousHash\":\"e8b4c47993c2b6aa71caee141f1dfb0f07c394f6cc963df53ee3ab13214d71e1\",\"data\":\"pooasdasp\",\"timeStamp\":\"2021-01-11T17:20:45.665476200Z\",\"nonce\":\"3\",\"difficulty\":\"4\",\"proofOfWork\":\"0000110100110010001111110100111111101011000110000110001100100011011111010001000110101010100100111010101000011100100011101011100001110000010000001011001100000011001010100110101010100110010010001000011111110001110100011111100011111000100001100101100000001101\"}");
        ReflectionTestUtils.setField(testPeerClient, "client", mockOkHttpClient); //Create a mock client with a json block response and inject into the test client

        //When
        Block testBlockchainLastBlockResponse = testPeerClient.getPeerLastBlock();

        //Then
        assertEquals("Block{hash='596b27130c0af3539cc52dcb64561f29617a86e4db0890f020047eefa4917ed4', previousHash='e8b4c47993c2b6aa71caee141f1dfb0f07c394f6cc963df53ee3ab13214d71e1', data='pooasdasp', timeStamp='2021-01-11T17:20:45.665476200Z', nonce='3', difficulty='4', proofOfWork='0000110100110010001111110100111111101011000110000110001100100011011111010001000110101010100100111010101000011100100011101011100001110000010000001011001100000011001010100110101010100110010010001000011111110001110100011111100011111000100001100101100000001101'}",
                testBlockchainLastBlockResponse.toString());
    }

    @Test
    void getPeerHealth() throws IOException, JSONException {
        //Given
        OkHttpClient mockOkHttpClient = mockHttpClient("{\"status\":\"UP\"}");
        ReflectionTestUtils.setField(testPeerClient, "client", mockOkHttpClient); //Create a mock client with a json health status and inject into the test client

        //When
        String testPeerStatusResponse = testPeerClient.getPeerHealth();

        //Then
        assertEquals("UP", testPeerStatusResponse);
    }

    @Test
    void addBlockToPeer() throws IOException, JSONException {
        //Given
        OkHttpClient mockOkHttpClient = mockHttpClient("Block added successfully!");
        ReflectionTestUtils.setField(testPeerClient, "client", mockOkHttpClient); //The response for this call will either be success, or invalid object exception message

        //When
        String testAddBlockResponse = testPeerClient.addBlockToPeer(new Block().genesis());

        //Then
        assertEquals("Block added successfully!", testAddBlockResponse);
    }

    @Test
    void getHealthySocketsList() throws IOException, JSONException {
        //Given
        OkHttpClient mockOkHttpClient = mockHttpClient("[{\"peerSocket\":\"54.90.44.155:8080\",\"peerStatus\":\"UNKNOWN\"},{\"peerSocket\":\"85.123.44.55:8080\",\"peerStatus\":\"UNKNOWN\"},{\"peerSocket\":\"33.44.55.66:6666\",\"peerStatus\":\"UP\"},{\"peerSocket\":\"123.24.53.42:8080\",\"peerStatus\":\"UP\"},{\"peerSocket\":\"69.42.0.88:3636\",\"peerStatus\":\"UNKNOWN\"}]");
        ReflectionTestUtils.setField(testPeerClient, "client", mockOkHttpClient); //Create a mock client with a json health status and inject into the test client

        //When
        String testHealthySocketsListReponse = testPeerClient.getHealthySocketsList();

        //Then
        assertEquals("33.44.55.66:6666,123.24.53.42:8080", testHealthySocketsListReponse);
    }

    private static OkHttpClient mockHttpClient(final String serializedBody) throws IOException {
        final OkHttpClient okHttpClient = Mockito.mock(OkHttpClient.class);

        final Call remoteCall = Mockito.mock(Call.class);

        final Response response = new Response.Builder()
                .request(new Request.Builder().url("http://url.com").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200).message("").body(
                        ResponseBody.create(
                                MediaType.parse("application/json"),
                                serializedBody
                        ))
                .build();

        Mockito.when(remoteCall.execute()).thenReturn(response);
        Mockito.when(okHttpClient.newCall(any())).thenReturn(remoteCall);

        return okHttpClient;
    }
}