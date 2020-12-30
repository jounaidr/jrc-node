package com.jounaidr.jrc.server.peers.peer;

import com.jounaidr.jrc.server.blockchain.Block;
import com.jounaidr.jrc.server.blockchain.Blockchain;
import com.jounaidr.jrc.server.peers.peer.helpers.JsonBlockResponseHelper;
import com.jounaidr.jrc.server.peers.peer.helpers.Status;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j //TODO: THIS WILL GO IN THE POLLER AS ONLY POLLER WILL SYNC!!!!!!!!
public class PeerThreadedSynchronizer implements Runnable{
    private Blockchain blockchain;
    private Peer peer;

    private Request blockchainRequest;

    private ExecutorService executor;

    public PeerThreadedSynchronizer(Blockchain blockchain, Peer peer) {
        this.blockchain = blockchain;
        this.peer = peer;

        this.blockchainRequest = new Request.Builder().url(peer.getPeerUrl() + "/blockchain").build();

        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void run() {
        if(peer.getPeerStatus() != Status.DOWN){ // Only attempt synchronization if peer is not down
            try {
                log.info("Attempting to synchronize with the following peer: [{}]", peer.getPeerUrl());
                ArrayList<Block> chainResponse = this.getPeerBlockchain();

                if(chainResponse.size() > this.blockchain.getChain().size()){
                    try {
                        Blockchain incomingBlockchain = new Blockchain(chainResponse);
                        this.blockchain.replaceChain(incomingBlockchain);

                        peer.setSynchronized(true);
                        log.info("Successfully synchronized blockchain with the following peer: [{}] !", peer.getPeerUrl());
                    } catch (InvalidObjectException e) {
                        // A block in the blockchain is invalid
                        log.error("Could not synchronize with the following peer: [{}]. Reason: {}", peer.getPeerUrl(), e.getMessage());
                    }
                } else{
                    log.info("Could not synchronize with the following peer: [{}]. Reason: The peers blockchain is smaller than the current blockchain", peer.getPeerUrl());
                }
            } catch (SocketTimeoutException e) {
                peer.setPeerStatus(Status.DOWN); // The peer is unreachable, set its status to down
                log.error("Could not synchronize with the following peer: [{}]. Reason: {}. Setting peer status to DOWN", peer.getPeerUrl(), e.getMessage());
            } catch (Exception e) {
                //TODO: test around this, different exceptions, what happens if non block json is returned, what if different response code
                e.printStackTrace();
            }
        }
        log.info("Could not synchronize with the following peer: [{}]. Reason: peer status is {}", peer.getPeerUrl(), peer.getPeerStatus());
    }

    private ArrayList<Block> getPeerBlockchain() throws IOException, JSONException {
        ArrayList<Block> chainResponse = new ArrayList<>();

        Response blockchainResponse = peer.getClient().newCall(blockchainRequest).execute();
        JSONArray jsonResponse = new JSONArray(blockchainResponse.body().string());

        for (int i = 0; i < jsonResponse.length(); i++) {
            chainResponse.add(JsonBlockResponseHelper.getBlockFromJsonObject(jsonResponse.getJSONObject(i)));
        }

        return chainResponse;
    }

    public void start() {
        executor.submit(this);
    }
}
