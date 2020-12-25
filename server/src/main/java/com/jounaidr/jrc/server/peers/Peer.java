package com.jounaidr.jrc.server.peers;

import com.jounaidr.jrc.server.blockchain.Block;
import com.jounaidr.jrc.server.blockchain.Blockchain;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Peer {
    private Blockchain blockchain;
    private String peerUrl;
    private OkHttpClient client = new OkHttpClient.Builder().readTimeout(3, TimeUnit.SECONDS).build();

    public Peer(Blockchain blockchain, String peerUrl) throws IOException, JSONException {
        this.blockchain = blockchain;
        this.peerUrl = peerUrl;
        this.synchronisePeer();
    }

    public void synchronisePeer() throws JSONException {
        Request blockchainRequest = new Request.Builder().url(peerUrl + "/blockchain").build();
        Response response = null;
        JSONArray jsonResponse = null;
        try {
            response = client.newCall(blockchainRequest).execute();
            jsonResponse = new JSONArray(response.body().string());
        } catch (IOException e) {

        }


        ArrayList<Block> chainResponse = new ArrayList<>();

        for (int i = 0; i < jsonResponse.length(); i++) {
            chainResponse.add(new Block(
                    jsonResponse.getJSONObject(i).getString("hash"),
                    jsonResponse.getJSONObject(i).getString("previousHash"),
                    jsonResponse.getJSONObject(i).getString("data"),
                    jsonResponse.getJSONObject(i).getString("timeStamp"),
                    jsonResponse.getJSONObject(i).getString("nonce"),
                    jsonResponse.getJSONObject(i).getString("difficulty"),
                    jsonResponse.getJSONObject(i).getString("proofOfWork")
            ));
        }

        Blockchain blockchainResponse = new Blockchain(chainResponse);
        this.blockchain.replaceChain(blockchainResponse);
    }
}
