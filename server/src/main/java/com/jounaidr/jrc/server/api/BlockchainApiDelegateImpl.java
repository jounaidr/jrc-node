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
            Block block = new Block();

            block.setHash(this.blockchain.getChain().get(i).getHash());
            block.setPreviousHash(this.blockchain.getChain().get(i).getPreviousHash());
            block.setData(this.blockchain.getChain().get(i).getData());
            block.setTimeStamp(this.blockchain.getChain().get(i).getTimeStamp());
            block.setNonce(this.blockchain.getChain().get(i).getNonce());
            block.setDifficulty(this.blockchain.getChain().get(i).getDifficulty());
            block.setProofOfWork(this.blockchain.getChain().get(i).getProofOfWork());

            response.add(block);
        }

        return ResponseEntity.ok(response);
    }
}
