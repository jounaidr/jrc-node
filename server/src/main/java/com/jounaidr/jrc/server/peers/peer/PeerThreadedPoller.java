package com.jounaidr.jrc.server.peers.peer;

import com.jounaidr.jrc.server.blockchain.Blockchain;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PeerThreadedPoller implements Runnable{
    private Blockchain blockchain;
    private String peerUrl;
    private OkHttpClient client;

    private Request blockchainSizeRequest;
    private Request blockchainLastBlockRequest;

    private final int POLLING_FREQUENCY = 5000;

    private ScheduledExecutorService executor;

    public PeerThreadedPoller(Blockchain blockchain, String peerUrl, OkHttpClient client) {
        this.blockchain = blockchain;
        this.peerUrl = peerUrl;
        this.client = client;

        this.blockchainSizeRequest = new Request.Builder().url(peerUrl + "/blockchain/size").build();
        this.blockchainLastBlockRequest = new Request.Builder().url(peerUrl + "/blockchain/lastblock").build();

        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void run() {


    }

    public void start() {
        executor.scheduleAtFixedRate(this, 50, POLLING_FREQUENCY, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        executor.shutdown();
    }
}
