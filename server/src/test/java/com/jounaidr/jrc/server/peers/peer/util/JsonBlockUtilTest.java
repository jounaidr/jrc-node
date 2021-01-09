package com.jounaidr.jrc.server.peers.peer.util;

import com.jounaidr.jrc.server.blockchain.Block;
import org.junit.jupiter.api.Test;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import static org.junit.Assert.assertEquals;

class JsonBlockUtilTest {

    @Test
    void getBlockFromJsonObject() throws JSONException {
        //Given
        JSONObject jsonBlock = new JSONObject();

        jsonBlock.put("hash", "this");
        jsonBlock.put("previousHash", "is");
        jsonBlock.put("data", "a");
        jsonBlock.put("timeStamp", "test");
        jsonBlock.put("nonce", "json");
        jsonBlock.put("difficulty", "block");
        jsonBlock.put("proofOfWork", "=DDD");

        //When
        Block testBlock = JsonBlockUtil.getBlockFromJsonObject(jsonBlock);

        //Then
        assertEquals("this", testBlock.getHash());
        assertEquals("is", testBlock.getPreviousHash());
        assertEquals("a", testBlock.getData());
        assertEquals("test", testBlock.getTimeStamp());
        assertEquals("json", testBlock.getNonce());
        assertEquals("block", testBlock.getDifficulty());
        assertEquals("=DDD", testBlock.getProofOfWork());

        assertEquals("Block{hash='this', previousHash='is', data='a', timeStamp='test', nonce='json', difficulty='block', proofOfWork='=DDD'}", testBlock.toString());
    }

    @Test
    void getJsonObjectFromBlock() throws JSONException {
        //Given
        Block testBlock = new Block("this","is","a","test","block","lol","yeet");

        //When
        JSONObject jsonBlock = JsonBlockUtil.getJsonObjectFromBlock(testBlock);

        //Then
        assertEquals("this", jsonBlock.getString("hash"));
        assertEquals("is", jsonBlock.getString("previousHash"));
        assertEquals("a", jsonBlock.getString("data"));
        assertEquals("test", jsonBlock.getString("timeStamp"));
        assertEquals("block", jsonBlock.getString("nonce"));
        assertEquals("lol", jsonBlock.getString("difficulty"));
        assertEquals("yeet", jsonBlock.getString("proofOfWork"));

        assertEquals("{\"hash\":\"this\",\"previousHash\":\"is\",\"data\":\"a\",\"timeStamp\":\"test\",\"nonce\":\"block\",\"difficulty\":\"lol\",\"proofOfWork\":\"yeet\"}", jsonBlock.toString());
    }
}