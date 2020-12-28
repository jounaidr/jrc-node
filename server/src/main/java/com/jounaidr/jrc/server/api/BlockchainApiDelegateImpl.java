package com.jounaidr.jrc.server.api;

import com.jounaidr.jrc.server.blockchain.Blockchain;
import com.jounaidr.jrc.server.model.Block;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

public class BlockchainApiDelegateImpl implements BlockchainApiDelegate {
    private Blockchain blockchain;

    public BlockchainApiDelegateImpl(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    @Override
    public ResponseEntity<List<Block>> getBlockchain() {
        ArrayList<Block> response = new ArrayList<>();

        for(int i=0; i < this.blockchain.getChain().size(); i++){
            Block block = getBlockModel(i);
            response.add(block);
        }

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Block> getLastBlock() {
        int lastBlockIndex = this.blockchain.getChain().size() - 1;
        Block response = getBlockModel(lastBlockIndex);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Integer> getBlockchainLength() {
        Integer response = this.blockchain.getChain().size();

        return ResponseEntity.ok(response);
    }

    private Block getBlockModel(int blockIndex) {
        Block blockModel = new Block();

        blockModel.setHash(this.blockchain.getChain().get(blockIndex).getHash());
        blockModel.setPreviousHash(this.blockchain.getChain().get(blockIndex).getPreviousHash());
        blockModel.setData(this.blockchain.getChain().get(blockIndex).getData());
        blockModel.setTimeStamp(this.blockchain.getChain().get(blockIndex).getTimeStamp());
        blockModel.setNonce(this.blockchain.getChain().get(blockIndex).getNonce());
        blockModel.setDifficulty(this.blockchain.getChain().get(blockIndex).getDifficulty());
        blockModel.setProofOfWork(this.blockchain.getChain().get(blockIndex).getProofOfWork());

        return blockModel;
    }
}
