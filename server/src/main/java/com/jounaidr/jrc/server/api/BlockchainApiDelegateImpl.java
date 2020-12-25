package com.jounaidr.jrc.server.api;

import com.jounaidr.jrc.server.blockchain.Blockchain;
import com.jounaidr.jrc.server.model.Block;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
        Block blockResponse = new Block();

        blockResponse.setHash(this.blockchain.getChain().get(blockIndex).getHash());
        blockResponse.setPreviousHash(this.blockchain.getChain().get(blockIndex).getPreviousHash());
        blockResponse.setData(this.blockchain.getChain().get(blockIndex).getData());
        blockResponse.setTimeStamp(this.blockchain.getChain().get(blockIndex).getTimeStamp());
        blockResponse.setNonce(this.blockchain.getChain().get(blockIndex).getNonce());
        blockResponse.setDifficulty(this.blockchain.getChain().get(blockIndex).getDifficulty());
        blockResponse.setProofOfWork(this.blockchain.getChain().get(blockIndex).getProofOfWork());

        return blockResponse;
    }
}
