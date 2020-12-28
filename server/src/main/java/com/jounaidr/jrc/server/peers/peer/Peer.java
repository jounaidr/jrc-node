package com.jounaidr.jrc.server.peers.peer;

import com.jounaidr.jrc.server.blockchain.Blockchain;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

public class Peer {
    private Blockchain blockchain;
    private String peerUrl;
    private OkHttpClient client = new OkHttpClient.Builder().readTimeout(3, TimeUnit.SECONDS).build();

    public Peer(Blockchain blockchain, String peerUrl) {
        this.blockchain = blockchain;
        this.peerUrl = peerUrl;
        this.synchronizePeer();
    }

    public void synchronizePeer() {
        Thread peerSynchronizer = new Thread(new PeerThreadedSynchronizer(blockchain, peerUrl, client));
        peerSynchronizer.start();
    }
}
