package com.jounaidr.jrc.server.api.implementation;

import com.jounaidr.jrc.server.api.generated.BlockchainApiDelegate;
import com.jounaidr.jrc.server.api.generated.model.BlockModel;
import com.jounaidr.jrc.server.api.implementation.util.BlockModelUtil;
import com.jounaidr.jrc.server.blockchain.Block;
import com.jounaidr.jrc.server.blockchain.Blockchain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.List;

//TODO: Try catch all methods like the addBlock method but with generic Exception, and respond with it (see addBlock())
public class BlockchainApiDelegateImpl implements BlockchainApiDelegate {
    @Autowired
    private Blockchain blockchain; //The blockchain bean instance for this node, injected through spring

    /**
     * Generate a list of BlockModel data objects based on this nodes
     * blockchain and return it.
     *
     * @return the BlockModel list as a response entity with status code 200
     */
    @Override
    public ResponseEntity<List<BlockModel>> getBlockchain() {
        ArrayList<BlockModel> response = new ArrayList<>();

        for(Block block : blockchain.getChain()){
            response.add(BlockModelUtil.getBlockAsModel(block));
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Gets this nodes blockchain size and returns it
     *
     * @return the integer blockchain size as a response entity with status code 200
     */
    @Override
    public ResponseEntity<Integer> getBlockchainSize() {
        Integer response = blockchain.getChain().size();

        return ResponseEntity.ok(response);
    }

    /**
     * Generate a BlockModel data object from this nodes blockchain last block
     *
     * @return the BlockModel list as a response entity with status code 200
     */
    @Override
    public ResponseEntity<BlockModel> getLastBlock() {
        BlockModel response = BlockModelUtil.getBlockAsModel(blockchain.getLastBlock());

        return ResponseEntity.ok(response);
    }

    /**
     * Calls this blockchains addBlock method for a new incoming block
     *
     * @param newBlock the new incoming block to be added
     * @return if the block fails to add return the error message with status code 400
     *         if the block is added, return a success message with status code 200
     */
    @Override
    public ResponseEntity<Object> addBlock(BlockModel newBlock){
        try {
            blockchain.addBlock(BlockModelUtil.getBlockFromModel(newBlock));
        } catch (InvalidObjectException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok("Block added successfully!");
    }
}
