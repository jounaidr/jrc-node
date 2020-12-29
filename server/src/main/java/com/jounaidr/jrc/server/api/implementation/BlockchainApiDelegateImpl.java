package com.jounaidr.jrc.server.api.implementation;

import com.jounaidr.jrc.server.api.generated.BlockchainApiDelegate;
import com.jounaidr.jrc.server.api.generated.model.Block;
import com.jounaidr.jrc.server.api.implementation.helpers.BlockModelHelper;
import com.jounaidr.jrc.server.blockchain.Blockchain;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.InvalidObjectException;
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
        Block response = BlockModelHelper.getBlockAsModel(this.blockchain.getLastBlock());

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Object> addBlock(Block newBlock){
        try {
            this.blockchain.addBlock(BlockModelHelper.getBlockFromModel(newBlock));
        } catch (InvalidObjectException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok("Block added successfully!");
    }

    @Override
    public ResponseEntity<Integer> getBlockchainSize() {
        Integer response = this.blockchain.getChain().size();

        return ResponseEntity.ok(response);
    }
}
