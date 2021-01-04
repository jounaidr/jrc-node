package com.jounaidr.jrc.server.peers.peer;

import com.jounaidr.jrc.server.blockchain.Block;
import com.jounaidr.jrc.server.peers.peer.util.JsonBlockResponseUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class PeerClient {
    private final OkHttpClient client;

    private final Request blockchainRequest;
    private final Request blockchainSizeRequest;
    private final Request blockchainLastBlockRequest;

    private final Request peersRequest;

    private final Request healthRequest;

    public PeerClient(String peerSocket) {
        this.client = new OkHttpClient.Builder().readTimeout(3, TimeUnit.SECONDS).build();

        this.blockchainRequest = new Request.Builder().url(String.format("http://%s/blockchain", peerSocket)).build();
        this.blockchainSizeRequest = new Request.Builder().url(String.format("http://%s/blockchain/size", peerSocket)).build();
        this.blockchainLastBlockRequest = new Request.Builder().url(String.format("http://%s/blockchain/lastblock", peerSocket)).build();

        this.peersRequest = new Request.Builder().url(String.format("http://%s/peers", peerSocket)).build();

        this.healthRequest = new Request.Builder().url(String.format("http://%s/blockchain/actuator/health", peerSocket)).build();
    }

    public ArrayList<Block> getPeerBlockchain() throws IOException, JSONException {
        ArrayList<Block> chainResponse = new ArrayList<>();

        Response blockchainResponse = client.newCall(blockchainRequest).execute();
        JSONArray jsonResponse = new JSONArray(blockchainResponse.body().string());

        for (int i = 0; i < jsonResponse.length(); i++) {
            chainResponse.add(JsonBlockResponseUtil.getBlockFromJsonObject(jsonResponse.getJSONObject(i)));
        }

        return chainResponse;
    }

    public int getPeerBlockchainSize() throws IOException {
        Response blockchainSizeResponse = client.newCall(blockchainSizeRequest).execute();

        return Integer.parseInt(blockchainSizeResponse.body().string());
    }

    public Block getPeerLastBlock() throws IOException, JSONException {
        Response blockchainLastBlockResponse = client.newCall(blockchainLastBlockRequest).execute();

        return JsonBlockResponseUtil.getBlockFromJsonObject(new JSONObject(blockchainLastBlockResponse.body().string()));
    }

    public String getHealthySocketsList() throws IOException, JSONException {
        StringBuilder socketsListResponse = new StringBuilder();
        //Only get the sockets with that have status 'UP'
        Response peersResponse = client.newCall(peersRequest).execute();
        JSONArray jsonResponse = new JSONArray(peersResponse.body().string());

        for (int i = 0; i < jsonResponse.length(); i++) {
            if(jsonResponse.getJSONObject(i).getString("peerStatus").equals("UP")){
                socketsListResponse.append(jsonResponse.getJSONObject(i).getString("peerSocket"));
                socketsListResponse.append(",");
            }
        }

        return socketsListResponse.substring(0, socketsListResponse.length() - 1);
    }

    public String getPeerHealth() throws IOException, JSONException {
        Response peerHealthResponse = client.newCall(healthRequest).execute();

        return new JSONObject(peerHealthResponse.body().string()).getString("status");
    }
}
