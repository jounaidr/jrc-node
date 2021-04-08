package com.jounaidr.jrc.server.peers.peer;

import com.jounaidr.jrc.server.blockchain.Block;
import com.jounaidr.jrc.server.blockchain.Blockchain;
import com.jounaidr.jrc.server.peers.Peers;
import com.jounaidr.jrc.server.peers.peer.services.PeerBroadcastingService;
import com.jounaidr.jrc.server.peers.peer.util.Status;
import com.jounaidr.jrc.server.peers.peer.services.PeerPollingService;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Peer {
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    private final String PEER_SOCKET;
    private Status peerStatus;

    private final PeerPollingService peerPoller;
    private final PeerBroadcastingService peerBroadcaster;

    /**
     * Instantiates a new Peer object given a peer socket
     * and initially sets its status to unknown.
     *
     * @param peerSocket    the socket to initialise the Peer with
     * @param peersExecutor the peers thread pool executor
     */
    public Peer(String peerSocket, ScheduledThreadPoolExecutor peersExecutor, Blockchain blockchain, Peers peers) {
        this.PEER_SOCKET = peerSocket; //The peers socket, value will not change
        this.peerStatus = Status.UNKNOWN; //peer status is unknown until first polling cycle

        //Initialise PollingService and BroadcastingService runnable for this peer
        //Using the peers pool thread executor which will have one thread for each peer available
        this.peerPoller = new PeerPollingService(this, peersExecutor, blockchain, peers);
        this.peerBroadcaster = new PeerBroadcastingService(this, peersExecutor);
    }

    /**
     * Start the polling service for this peer.
     */
    public void startPolling(){
        this.peerPoller.start();
    }

    /**
     * Submit a block broadcasting task to the peers pool executor
     * given a block to broadcast to the peer
     *
     * @param block the block to be broadcasted
     */
    public void broadcastBlock(Block block){
        this.peerBroadcaster.broadcastBlock(block);
    }

    /**
     * Gets the peers socket.
     *
     * @return the peers socket
     */
    public String getPeerSocket() {
        //Value is set on initialisation and will not change so no locking required
        return this.PEER_SOCKET;
    }

    /**
     * Getter for the peers status with
     * a read lock for thread safety
     *
     * @return Status this peers status
     */
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

    /**
     * Setter for the peers status with
     * a write lock for thread safety
     *
     * @param peerStatus the status to set this peer to
     */
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
