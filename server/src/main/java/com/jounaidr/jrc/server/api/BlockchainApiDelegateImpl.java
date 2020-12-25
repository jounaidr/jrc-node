package com.jounaidr.jrc.server.api;

import com.jounaidr.jrc.server.blockchain.Blockchain;
import com.jounaidr.jrc.server.model.Block;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BlockchainApiDelegateImpl implements BlockchainApiDelegate {
    @Autowired
    Blockchain blockchain;

    @Override
    public ResponseEntity<List<Block>> getBlockchain() {
        ArrayList<Block> response = new ArrayList<>();

        for(int i=0; i < this.blockchain.getChain().size(); i++){
            Block block = getBlockResponse(i);
            response.add(block);
        }

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Block> getLastBlock() {
        int lastBlockIndex = this.blockchain.getChain().size() - 1;
        Block response = getBlockResponse(lastBlockIndex);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Integer> getBlockchainLength() {
        Integer response = this.blockchain.getChain().size();

        return ResponseEntity.ok(response);
    }

    private Block getBlockResponse(int blockIndex) {
        Block block = new Block();

        block.setHash(this.blockchain.getChain().get(blockIndex).getHash());
        block.setPreviousHash(this.blockchain.getChain().get(blockIndex).getPreviousHash());
        block.setData(this.blockchain.getChain().get(blockIndex).getData());
        block.setTimeStamp(this.blockchain.getChain().get(blockIndex).getTimeStamp());
        block.setNonce(this.blockchain.getChain().get(blockIndex).getNonce());
        block.setDifficulty(this.blockchain.getChain().get(blockIndex).getDifficulty());
        block.setProofOfWork(this.blockchain.getChain().get(blockIndex).getProofOfWork());

        return block;
    }
}
