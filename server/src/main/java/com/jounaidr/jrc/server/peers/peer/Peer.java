package com.jounaidr.jrc.server.peers.peer;

import com.jounaidr.jrc.server.blockchain.Blockchain;
import com.jounaidr.jrc.server.peers.peer.helpers.Status;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Peer {
    private ReadWriteLock rwLock = new ReentrantReadWriteLock(); //this might become irellivant as only 1 thread per peer

    private String peerUrl;
    private Status peerStatus = Status.UNKNOWN;
    private boolean isSynchronied = false; //this will go aswell since syncroniser will be in poller

    private final OkHttpClient client;

    private final PeerThreadedSynchronizer peerSynchronizer;
    private final PeerThreadedPoller peerPoller;

    public Peer(Blockchain blockchain, String peerUrl) {
        this.peerUrl = peerUrl;

        this.client = new OkHttpClient.Builder().readTimeout(3, TimeUnit.SECONDS).build(); //TODO: all this okhttp shit into a different class!!
        this.peerSynchronizer = new PeerThreadedSynchronizer(blockchain, this);
        this.peerPoller = new PeerThreadedPoller(blockchain, this);


        this.startPeerPolling();
    }

    public void synchronizePeer() {
        if(!isSynchronied) {
            this.peerSynchronizer.start();
        }
    }

    public void startPeerPolling(){
        this.peerPoller.start();
    }

    public void stopPeerPolling(){
        this.peerPoller.stop();
    }

    public String getPeerUrl() {
        return this.peerUrl;
    }

    public Status getPeerStatus() {
        Lock readLock = rwLock.readLock();
        readLock.lock();

        try {
            return this.peerStatus;
        } finally {
            readLock.unlock();
        }
    }

    public void setPeerStatus(Status peerStatus) {
        Lock writeLock = rwLock.writeLock();
        writeLock.lock();

        try {
            this.peerStatus = peerStatus;
        } finally {
            writeLock.unlock();
        }
    }

    public boolean isSynchronized() {
        Lock readLock = rwLock.readLock();
        readLock.lock();

        try {
            return isSynchronied;
        } finally {
            readLock.unlock();
        }
    }

    public void setSynchronized(boolean synchronied) {
        Lock writeLock = rwLock.writeLock();
        writeLock.lock();

        try {
            isSynchronied = synchronied;
        } finally {
            writeLock.unlock();
        }
    }

    public OkHttpClient getClient() {
        return this.client;
    }
}
