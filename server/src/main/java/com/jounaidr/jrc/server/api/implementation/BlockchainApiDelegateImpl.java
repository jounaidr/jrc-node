package com.jounaidr.jrc.server.api.implementation;

import com.jounaidr.jrc.server.api.generated.BlockchainApiDelegate;
import com.jounaidr.jrc.server.api.generated.model.Block;
import com.jounaidr.jrc.server.api.implementation.helpers.BlockModelHelper;
import com.jounaidr.jrc.server.blockchain.Blockchain;
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
            Block block = BlockModelHelper.getBlockAsModel(this.blockchain.getChain().get(i));
            response.add(block);
        }

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Block> getLastBlock() {
        int lastBlockIndex = this.blockchain.getChain().size() - 1;
        Block response = BlockModelHelper.getBlockAsModel(this.blockchain.getChain().get(lastBlockIndex));

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Integer> getBlockchainSize() {
        Integer response = this.blockchain.getChain().size();

        return ResponseEntity.ok(response);
    }
}
