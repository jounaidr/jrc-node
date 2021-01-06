package com.jounaidr.jrc.server.peers.peer.services;

import com.jounaidr.jrc.server.blockchain.Block;
import com.jounaidr.jrc.server.peers.peer.Peer;
import com.jounaidr.jrc.server.peers.peer.PeerClient;
import com.jounaidr.jrc.server.peers.peer.util.Status;
import lombok.extern.slf4j.Slf4j;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@Slf4j
public class PeerBroadcastingService implements Runnable{
    private final Peer peer;
    private final PeerClient peerClient;

    private final ScheduledThreadPoolExecutor peersExecutor;

    private Block blockToBroadcast;

    /**
     * Instantiates a new peer broadcasting service for a given peer.
     *
     * @param peer          the peer to broadcast to
     * @param peersExecutor the the peers thread pool executor
     */
    public PeerBroadcastingService(Peer peer, ScheduledThreadPoolExecutor peersExecutor) {
        this.peer = peer;
        this.peerClient = new PeerClient(peer.getPeerSocket()); //Instantiate a new peer client from the peers socket

        this.peersExecutor = peersExecutor;
    }

    /**
     * Set the blockToBroadcast to the provided block
     * Then submit the broadcasting task to the peers thread pool executor
     *
     * @param block the to be broadcast
     */
    public void broadcastBlock(Block block) {
        this.blockToBroadcast = block;
        peersExecutor.submit(this);
    }

    /**
     * The BlockBroadcasting run task.
     *
     * Calls the addBlockToPeer() method for this peerClient given
     * the blockToBroadcast set in the task submit method, only if
     * the peer status is UP
     */
    @Override
    public void run() {
        if(peer.getPeerStatus() == Status.UP){ //Only broadcast if the peer is UP
            try {
                log.debug("Attempting to broadcast block: {}, to the following peer [{}]...", blockToBroadcast.toString(), peer.getPeerSocket());
                peerClient.addBlockToPeer(blockToBroadcast);
                log.debug("...Block was broadcasted successfully!");
            } catch (SocketTimeoutException | ConnectException e) {
                if(peer.getPeerStatus() != Status.DOWN){
                    peer.setPeerStatus(Status.DOWN);
                    log.info("Could not broadcast to the following peer: [{}]. Reason: {}. Setting peer status to DOWN", peer.getPeerSocket(), e.getMessage());
                }
                log.debug("Could not broadcast to the following peer: [{}]. Reason: {}. Peer status is: {}", peer.getPeerSocket(), e.getMessage(), peer.getPeerStatus());
            } catch (Exception e) {
                //TODO: test around this, different exceptions, what happens if non block json is returned, what if different response code
                peer.setPeerStatus(Status.UNKNOWN);
                log.debug("Could not broadcast to the following peer: [{}]. Reason: {}. Setting peer status to UNKNOWN", peer.getPeerSocket(), e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
