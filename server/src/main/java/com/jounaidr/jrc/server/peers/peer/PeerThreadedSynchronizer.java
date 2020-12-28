package com.jounaidr.jrc.server.peers.peer;

import com.jounaidr.jrc.server.blockchain.Block;
import com.jounaidr.jrc.server.blockchain.Blockchain;
import com.jounaidr.jrc.server.peers.peer.helpers.JsonBlockResponseHelper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.boot.configurationprocessor.json.JSONArray;

import java.util.ArrayList;

public class PeerThreadedSynchronizer implements Runnable{
    private Blockchain blockchain;
    private OkHttpClient client;

    private Request blockchainRequest;

    public PeerThreadedSynchronizer(Blockchain blockchain, String peerUrl, OkHttpClient client) {
        this.blockchain = blockchain;
        this.client = client;

        this.blockchainRequest = new Request.Builder().url(peerUrl + "/blockchain").build();
    }

    @Override
    public void run() {
        ArrayList<Block> chainResponse = new ArrayList<>();

        try {
            Response response = client.newCall(blockchainRequest).execute();
            JSONArray jsonResponse = new JSONArray(response.body().string());

            for (int i = 0; i < jsonResponse.length(); i++) {
                chainResponse.add(JsonBlockResponseHelper.getBlockFromJsonObject(jsonResponse.getJSONObject(i)));
            }
        } catch (Exception e) {
            //TODO: test around this, different exceptions, what happens if non block json is returned, what if different response code
            e.printStackTrace();
        }

        if(chainResponse.size() > this.blockchain.getChain().size()){
            Blockchain blockchainResponse = new Blockchain(chainResponse);
            this.blockchain.replaceChain(blockchainResponse);
        }
    }
}
