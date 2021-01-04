package com.jounaidr.jrc.server.peers.peer;

import com.jounaidr.jrc.server.blockchain.Blockchain;
import com.jounaidr.jrc.server.peers.Peers;
import com.jounaidr.jrc.server.peers.peer.util.Status;
import com.jounaidr.jrc.server.peers.peer.services.PollingService;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Peer {
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    private final String peerSocket;
    private Status peerStatus;

    private final PollingService peerPoller;

    public Peer(Blockchain blockchain, Peers peers, String peerSocket) {
        this.peerSocket = peerSocket;
        this.peerStatus = Status.UNKNOWN;

        this.peerPoller = new PollingService(blockchain, this, peers);
    }

    public void startPolling(){
        this.peerPoller.start();
    }

    public String getPeerSocket() {
        //Value is set on initialisation and will not change so no locking required
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
