package com.jounaidr.jrc.server.api.implementation.util;

import com.jounaidr.jrc.server.api.generated.model.BlockModel;
import com.jounaidr.jrc.server.blockchain.Block;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

class BlockModelUtilTest {

    @Test
    void getBlockAsModel() {
        //Given
        Block testBlock = new Block("this","is","a","test","block","lol","yeet");

        //When
        BlockModel testBlockModel = BlockModelUtil.getBlockAsModel(testBlock);

        //Then
        assertEquals("this", testBlockModel.getHash());
        assertEquals("is", testBlockModel.getPreviousHash());
        assertEquals("a", testBlockModel.getData());
        assertEquals("test", testBlockModel.getTimeStamp());
        assertEquals("block", testBlockModel.getNonce());
        assertEquals("lol", testBlockModel.getDifficulty());
        assertEquals("yeet", testBlockModel.getProofOfWork());

        assertEquals("class BlockModel {\n" +
                "    hash: this\n" +
                "    previousHash: is\n" +
                "    data: a\n" +
                "    timeStamp: test\n" +
                "    nonce: block\n" +
                "    difficulty: lol\n" +
                "    proofOfWork: yeet\n" +
                "}", testBlockModel.toString());
    }

    @Test
    void getBlockFromModel() {
        //Given
        BlockModel testBlockModel = new BlockModel();

        testBlockModel.setHash("this");
        testBlockModel.setPreviousHash("is");
        testBlockModel.setData("a");
        testBlockModel.setTimeStamp("test");
        testBlockModel.setNonce("block");
        testBlockModel.setDifficulty("model");
        testBlockModel.setProofOfWork("ay");

        //When
        Block testBlock = BlockModelUtil.getBlockFromModel(testBlockModel);

        //Then
        assertEquals("this", testBlock.getHash());
        assertEquals("is", testBlock.getPreviousHash());
        assertEquals("a", testBlock.getData());
        assertEquals("test", testBlock.getTimeStamp());
        assertEquals("block", testBlock.getNonce());
        assertEquals("model", testBlock.getDifficulty());
        assertEquals("ay", testBlock.getProofOfWork());

        assertEquals("Block{hash='this', previousHash='is', data='a', timeStamp='test', nonce='block', difficulty='model', proofOfWork='ay'}", testBlock.toString());
    }
}