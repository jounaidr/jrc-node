package com.jounaidr.jrc.server.peers.peer.util;

import com.jounaidr.jrc.server.blockchain.Block;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

public class JsonBlockUtil {

    /**
     * Converts a json block data object into a block object
     *
     * @param jsonBlock the json block object to be converted
     * @return the converted block object
     * @throws JSONException json conversion exceptions
     */
    public static Block getBlockFromJsonObject(JSONObject jsonBlock) throws JSONException {
        return new Block(
                jsonBlock.getString("hash"),
                jsonBlock.getString("previousHash"),
                jsonBlock.getString("data"),
                jsonBlock.getString("timeStamp"),
                jsonBlock.getString("nonce"),
                jsonBlock.getString("difficulty"),
                jsonBlock.getString("proofOfWork")
        );
    }

    /**
     * Converts a block object's data into json format
     *
     * @param block the block object to be converted
     * @return the json object from generated from the block
     * @throws JSONException json conversion exceptions
     */
    public static JSONObject getJsonObjectFromBlock(Block block) throws JSONException {
        JSONObject jsonBlock = new JSONObject();

        jsonBlock.put("hash", block.getHash());
        jsonBlock.put("previousHash", block.getPreviousHash());
        jsonBlock.put("data", block.getData());
        jsonBlock.put("timeStamp", block.getTimeStamp());
        jsonBlock.put("nonce", block.getNonce());
        jsonBlock.put("difficulty", block.getDifficulty());
        jsonBlock.put("proofOfWork", block.getProofOfWork());

        return jsonBlock;
    }
}
