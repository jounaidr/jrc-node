package com.jounaidr.jrc.server.api.implementation.util;

import com.jounaidr.jrc.server.api.generated.model.BlockModel;
import com.jounaidr.jrc.server.blockchain.Block;

public class BlockModelUtil {
    public static BlockModel getBlockAsModel(Block block){
        BlockModel blockModel = new BlockModel();

        blockModel.setHash(block.getHash());
        blockModel.setPreviousHash(block.getPreviousHash());
        blockModel.setData(block.getData());
        blockModel.setTimeStamp(block.getTimeStamp());
        blockModel.setNonce(block.getNonce());
        blockModel.setDifficulty(block.getDifficulty());
        blockModel.setProofOfWork(block.getProofOfWork());

        return blockModel;
    }

    public static Block getBlockFromModel(BlockModel blockModel){
        return new com.jounaidr.jrc.server.blockchain.Block(
                blockModel.getHash(),
                blockModel.getPreviousHash(),
                blockModel.getData(),
                blockModel.getTimeStamp(),
                blockModel.getNonce(),
                blockModel.getDifficulty(),
                blockModel.getProofOfWork()
        );
    }
}
