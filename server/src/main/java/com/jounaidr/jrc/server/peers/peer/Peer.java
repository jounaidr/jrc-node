package com.jounaidr.jrc.server.peers.peer;

import com.jounaidr.jrc.server.blockchain.Blockchain;
import com.jounaidr.jrc.server.peers.peer.helpers.Status;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Peer {
    private ReadWriteLock rwLock = new ReentrantReadWriteLock();

    private String peerSocket;
    private Status peerStatus;

    private final PeerThreadedPoller peerPoller;

    public Peer(Blockchain blockchain, String peerSocket) {
        this.peerSocket = peerSocket;
        this.peerStatus = Status.UNKNOWN;

        this.peerPoller = new PeerThreadedPoller(blockchain, this);


        this.startPeerPolling();
    }

    public void startPeerPolling(){
        this.peerPoller.start();
    }

    public void stopPeerPolling(){
        this.peerPoller.stop();
    }

    public String getPeerSocket() {
        return this.peerSocket;
    }

    public Status getPeerStatus() {
        //Read lock whilst getting peer status as multiple peer threads can read/write to this
        Lock readLock = rwLock.readLock();
        readLock.lock();

        try {
            return this.peerStatus;
        } finally {
            readLock.unlock();
        }
    }

    public void setPeerStatus(Status peerStatus) {
        //Write lock whilst getting peer status as multiple peer threads can read/write to this
        Lock writeLock = rwLock.writeLock();
        writeLock.lock();

        try {
            this.peerStatus = peerStatus;
        } finally {
            writeLock.unlock();
        }
    }
}
