package com.jounaidr.jrc.server.peers.peer;

import com.jounaidr.jrc.server.blockchain.Block;
import com.jounaidr.jrc.server.blockchain.Blockchain;
import com.jounaidr.jrc.server.peers.peer.helpers.JsonBlockResponseHelper;
import com.jounaidr.jrc.server.peers.peer.helpers.Status;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PeerThreadedPoller implements Runnable{
    private Blockchain blockchain;
    private Peer peer;

    private Request blockchainSizeRequest;
    private Request blockchainLastBlockRequest;
    private Request peerHealthRequest;

    private final int POLLING_FREQUENCY = 1000; //TODO: SET TO 15 secs when deploying only 1sec for testing

    private ScheduledExecutorService executor;

    public PeerThreadedPoller(Blockchain blockchain, Peer peer) {
        this.blockchain = blockchain;
        this.peer = peer;

        this.blockchainSizeRequest = new Request.Builder().url(peer.getPeerUrl() + "/blockchain/size").build();
        this.blockchainLastBlockRequest = new Request.Builder().url(peer.getPeerUrl() + "/blockchain/lastblock").build();
        this.peerHealthRequest = new Request.Builder().url(peer.getPeerUrl() + "/actuator/health").build();

        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void run() {
        log.debug("Attempting to poll peer: [{}]",peer.getPeerUrl());
        try {
            this.peerHealthCheck(); //First check the peers health status
            if(peer.getPeerStatus() == Status.UP){
                int chainSizeDiff = this.getPeerBlockchainSize() - this.blockchain.getChain().size();

                if(chainSizeDiff == 0){
                    // The peers are in sync
                    log.debug("Peer [{}] is in sync with this node",peer.getPeerUrl());
                }
                if(chainSizeDiff == 1){
                    // The peers chain has the newest block, request it...
                    log.info("A new block was detected in the following peer: [{}] !",peer.getPeerUrl());
                    try {
                        this.blockchain.addBlock(this.getPeerLastBlock());
                    } catch (InvalidObjectException e) {
                        // A block in the blockchain is invalid
                        log.error("Could not add the new block from peer: [{}]. Reason: {}", peer.getPeerUrl(), e.getMessage());
                    }
                }
                if(chainSizeDiff > 1 && peer.isSynchronized()){
                    // This nodes blockchain is behind the peer, attempt to synchronise...
                    log.info("The blockchain is more than one blocks behind the following peer: [{}]",peer.getPeerUrl());
                    peer.setSynchronized(false);
                    peer.synchronizePeer();
                }
                if(chainSizeDiff < 0){
                    // The peers blockchain is behind this nodes
                    log.debug("Peer [{}] is behind this node",peer.getPeerUrl());
                }
            }
        } catch (SocketTimeoutException e) {
            peer.setPeerStatus(Status.DOWN);
            log.debug("Could not poll the following peer: [{}]. Reason: {}. Setting peer status to DOWN", peer.getPeerUrl(), e.getMessage());
        } catch (Exception e) {
            //TODO: test around this, different exceptions, what happens if non block json is returned, what if different response code
            e.printStackTrace();
        }
    }

    private void peerHealthCheck() throws IOException, JSONException {
        Response peerHealthResponse = peer.getClient().newCall(peerHealthRequest).execute();
        String peerHealth = new JSONObject(peerHealthResponse.body().string()).getString("status");

        if(peer.getPeerStatus() != Status.UP){
            if(peerHealth.equals("UP")){
                peer.setPeerStatus(Status.UP);
                log.info("Connection reestablished with peer: [{}] ! Setting peer status to UP", peer.getPeerUrl());
            }
            else{
                peer.setPeerStatus(Status.UNKNOWN);
                log.debug("Setting peer [{}] status to UNKNOWN",peer.getPeerUrl());
            }
        }
    }

    private int getPeerBlockchainSize() throws IOException {
        Response blockchainSizeResponse = peer.getClient().newCall(blockchainSizeRequest).execute();

        return Integer.parseInt(blockchainSizeResponse.body().string());
    }

    private Block getPeerLastBlock() throws IOException, JSONException {
        Response blockchainLastBlockResponse = peer.getClient().newCall(blockchainLastBlockRequest).execute();
        Block blockResponse = JsonBlockResponseHelper.getBlockFromJsonObject(new JSONObject(blockchainLastBlockResponse.body().string()));

        return blockResponse;
    }

    public void start() {
        //TODO: set initial delay to 15 secs for deploy only 50 for testing!!!!!
        executor.scheduleAtFixedRate(this, 50, POLLING_FREQUENCY, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        executor.shutdown();
    }
}
