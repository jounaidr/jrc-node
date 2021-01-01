package com.jounaidr.jrc.server.peers.peer.util;

import com.jounaidr.jrc.server.blockchain.Block;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

public class JsonBlockResponseUtil {

    public static Block getBlockFromJsonObject(JSONObject jsonBlock) throws JSONException {
        Block block = new Block(
                jsonBlock.getString("hash"),
                jsonBlock.getString("previousHash"),
                jsonBlock.getString("data"),
                jsonBlock.getString("timeStamp"),
                jsonBlock.getString("nonce"),
                jsonBlock.getString("difficulty"),
                jsonBlock.getString("proofOfWork")
        );

        return block;
    }
}
