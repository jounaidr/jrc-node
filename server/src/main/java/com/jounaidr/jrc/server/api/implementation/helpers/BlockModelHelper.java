package com.jounaidr.jrc.server.api.implementation.helpers;

import com.jounaidr.jrc.server.api.generated.model.Block;

public class BlockModelHelper {
    public static Block getBlockAsModel(com.jounaidr.jrc.server.blockchain.Block block){
        Block blockModel = new Block();

        blockModel.setHash(block.getHash());
        blockModel.setPreviousHash(block.getPreviousHash());
        blockModel.setData(block.getData());
        blockModel.setTimeStamp(block.getTimeStamp());
        blockModel.setNonce(block.getNonce());
        blockModel.setDifficulty(block.getDifficulty());
        blockModel.setProofOfWork(block.getProofOfWork());

        return blockModel;
    }
}
