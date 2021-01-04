package com.jounaidr.jrc.server.api.implementation;

import com.jounaidr.jrc.server.api.generated.BlockchainApiDelegate;
import com.jounaidr.jrc.server.api.generated.model.BlockModel;
import com.jounaidr.jrc.server.api.implementation.util.BlockModelUtil;
import com.jounaidr.jrc.server.blockchain.Block;
import com.jounaidr.jrc.server.blockchain.Blockchain;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.List;

public class BlockchainApiDelegateImpl implements BlockchainApiDelegate {
    private final Blockchain blockchain;

    public BlockchainApiDelegateImpl(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    @Override
    public ResponseEntity<List<BlockModel>> getBlockchain() {
        ArrayList<BlockModel> response = new ArrayList<>();

        for(Block block : blockchain.getChain()){
            response.add(BlockModelUtil.getBlockAsModel(block));
        }

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<BlockModel> getLastBlock() {
        BlockModel response = BlockModelUtil.getBlockAsModel(blockchain.getLastBlock());

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Object> addBlock(BlockModel newBlock){
        try {
            blockchain.addBlock(BlockModelUtil.getBlockFromModel(newBlock));
        } catch (InvalidObjectException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok("Block added successfully!");
    }

    @Override
    public ResponseEntity<Integer> getBlockchainSize() {
        Integer response = blockchain.getChain().size();

        return ResponseEntity.ok(response);
    }
}
