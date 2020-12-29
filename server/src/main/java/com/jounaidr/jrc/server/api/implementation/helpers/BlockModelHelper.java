package com.jounaidr.jrc.server.api.implementation.helpers;

public class BlockModelHelper {
    public static com.jounaidr.jrc.server.api.generated.model.Block getBlockAsModel(com.jounaidr.jrc.server.blockchain.Block block){
        com.jounaidr.jrc.server.api.generated.model.Block blockModel = new com.jounaidr.jrc.server.api.generated.model.Block();

        blockModel.setHash(block.getHash());
        blockModel.setPreviousHash(block.getPreviousHash());
        blockModel.setData(block.getData());
        blockModel.setTimeStamp(block.getTimeStamp());
        blockModel.setNonce(block.getNonce());
        blockModel.setDifficulty(block.getDifficulty());
        blockModel.setProofOfWork(block.getProofOfWork());

        return blockModel;
    }

    public static com.jounaidr.jrc.server.blockchain.Block getBlockFromModel(com.jounaidr.jrc.server.api.generated.model.Block blockModel){
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
