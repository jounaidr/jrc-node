package com.jounaidr.jrc.server.peers.peer;

import com.jounaidr.jrc.server.blockchain.Block;
import com.jounaidr.jrc.server.peers.peer.util.JsonBlockUtil;
import okhttp3.*;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class PeerClient {
    private final String peerSocket;
    private final OkHttpClient client;

    private final Request blockchainGetRequest;
    private final Request blockchainSizeGetRequest;
    private final Request blockchainLastBlockGetRequest;

    private final Request peersGetRequest;

    private final Request healthGetRequest;

    /**
     * Instantiates a new Peer client for a given peerSocket,
     * and initialises the get endpoint requests for said peerSocket.
     *
     * The peer client is used to interface with the peer, and a new
     * peer client is instantiated per thread, so thread safety is not
     * necessary...
     *
     * @param peerSocket the socket for which the peer client will be initialised for
     */
    public PeerClient(String peerSocket) {
        this.peerSocket = peerSocket;
        this.client = new OkHttpClient.Builder().readTimeout(3, TimeUnit.SECONDS).build();

        this.blockchainGetRequest = new Request.Builder().url(String.format("http://%s/blockchain", peerSocket)).build();
        this.blockchainSizeGetRequest = new Request.Builder().url(String.format("http://%s/blockchain/size", peerSocket)).build();
        this.blockchainLastBlockGetRequest = new Request.Builder().url(String.format("http://%s/blockchain/lastblock", peerSocket)).build();

        this.peersGetRequest = new Request.Builder().url(String.format("http://%s/peers", peerSocket)).build();

        this.healthGetRequest = new Request.Builder().url(String.format("http://%s/blockchain/actuator/health", peerSocket)).build();
    }

    /**
     * Gets the peers blockchain.
     *
     * Each json object returned from the peers blockchain
     * is converted into a Block object using ...peer.util.JsonBlockUtil
     *
     * @return the peers blockchain
     * @throws IOException   connection exceptions
     * @throws JSONException json conversion exceptions
     */
    public ArrayList<Block> getPeerBlockchain() throws IOException, JSONException {
        ArrayList<Block> chainResponse = new ArrayList<>();

        Response blockchainResponse = client.newCall(blockchainGetRequest).execute();
        JSONArray jsonResponse = new JSONArray(blockchainResponse.body().string());

        for (int i = 0; i < jsonResponse.length(); i++) {
            chainResponse.add(JsonBlockUtil.getBlockFromJsonObject(jsonResponse.getJSONObject(i)));
        }

        return chainResponse;
    }

    /**
     * Gets the peers blockchain size.
     *
     * @return the peers blockchain size
     * @throws IOException connection exceptions
     */
    public int getPeerBlockchainSize() throws IOException {
        Response blockchainSizeResponse = client.newCall(blockchainSizeGetRequest).execute();

        return Integer.parseInt(blockchainSizeResponse.body().string());
    }

    /**
     * Gets the peers blockchains last block.
     *
     * The json object returned is converted into a Block object
     * using ...peer.util.JsonBlockUtil
     *
     * @return the peers blockchains last block
     * @throws IOException   connection exceptions
     * @throws JSONException json conversion exceptions
     */
    public Block getPeerLastBlock() throws IOException, JSONException {
        Response blockchainLastBlockResponse = client.newCall(blockchainLastBlockGetRequest).execute();

        return JsonBlockUtil.getBlockFromJsonObject(new JSONObject(blockchainLastBlockResponse.body().string()));
    }

    /**
     * Gets the peers health status.
     *
     * @return the peers health status
     * @throws IOException   connection exceptions
     * @throws JSONException json conversion exceptions
     */
    public String getPeerHealth() throws IOException, JSONException {
        Response peerHealthResponse = client.newCall(healthGetRequest).execute();

        return new JSONObject(peerHealthResponse.body().string()).getString("status");
    }

    /**
     * Submits the provided Block object to the peer.
     *
     * The block object parameter supplied is converted into json
     * format using ...peer.util.JsonBlockUtil
     *
     * @param block the block to be submitted
     * @return the return message, will return a success or error message
     * @throws IOException   connection exceptions
     * @throws JSONException json conversion exceptions
     */
    public String addBlockToPeer(Block block) throws JSONException, IOException {
        JSONObject jsonBlock = JsonBlockUtil.getJsonObjectFromBlock(block);
        RequestBody body = RequestBody.create(jsonBlock.toString(), MediaType.parse("application/json; charset=utf-8"));

        Request blockchainAddBlockRequest = new Request.Builder()
                .url(String.format("http://%s/blockchain/addblock", peerSocket))
                .post(body)
                .build();

        Response addBlockResponse = client.newCall(blockchainAddBlockRequest).execute();

        return addBlockResponse.body().string();
    }

    /**
     * Gets the peers socket list and builds a comma separated
     * string containing only peers with health status UP
     *
     * @return the healthy sockets list
     * @throws IOException   connection exceptions
     * @throws JSONException json conversion exceptions
     */
    public String getHealthySocketsList() throws IOException, JSONException {
        StringBuilder socketsListResponse = new StringBuilder();

        Response peersResponse = client.newCall(peersGetRequest).execute();
        JSONArray jsonResponse = new JSONArray(peersResponse.body().string());

        for (int i = 0; i < jsonResponse.length(); i++) {
            //Only get the sockets with that have status UP
            if(jsonResponse.getJSONObject(i).getString("peerStatus").equals("UP")){
                socketsListResponse.append(jsonResponse.getJSONObject(i).getString("peerSocket"));
                socketsListResponse.append(",");
            }
        }

        //Remove last char from string before return as it will be a comma...
        return socketsListResponse.substring(0, socketsListResponse.length() - 1);
    }
}
