package com.jounaidr.jrc.server.peers.peer.services;

import com.jounaidr.jrc.server.blockchain.Block;
import com.jounaidr.jrc.server.blockchain.Blockchain;
import com.jounaidr.jrc.server.peers.Peers;
import com.jounaidr.jrc.server.peers.peer.Peer;
import com.jounaidr.jrc.server.peers.peer.PeerClient;
import com.jounaidr.jrc.server.peers.peer.util.Status;
import lombok.extern.slf4j.Slf4j;

import java.io.InvalidObjectException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.*;

@Slf4j
public class PollingService implements Runnable{
    private static final long POLLING_FREQUENCY = 5000; //TODO: SET TO 15 secs when deploying only 1sec for testing

    private final Blockchain blockchain;
    private final ScheduledThreadPoolExecutor executor;

    private final Peer peer;
    private final Peers peers;
    private final PeerClient peerClient;

    private String cashedPeerSocketsList;

    public PollingService(Blockchain blockchain, Peer peer, Peers peers) {
        this.blockchain = blockchain;
        this.executor = peers.getExecutor();

        this.peer = peer;
        this.peers = peers;
        this.peerClient = new PeerClient(peer.getPeerSocket());
    }

    public void start() {
        executor.scheduleAtFixedRate(this, this.getRandomInitialDelay(), POLLING_FREQUENCY, TimeUnit.MILLISECONDS);
    }

    private long getRandomInitialDelay(){
        double delay = ThreadLocalRandom.current().nextDouble() * POLLING_FREQUENCY;

        return Double.valueOf(Math.ceil(delay)).longValue();
    }

    @Override
    public void run() {
        log.debug("Attempting to poll peer: [{}]",peer.getPeerSocket());
        try {
            //First check the peers health status
            if(peerClient.getPeerHealth().equals("UP")){
                peer.setPeerStatus(Status.UP);
                log.info("Connection reestablished with peer: [{}] ! Setting peer status to UP", peer.getPeerSocket());
            }
            else{
                peer.setPeerStatus(Status.UNKNOWN);
                log.info("Peer health check returned invalid response: {}. Setting peer [{}] status to UNKNOWN", peerClient.getPeerHealth(), peer.getPeerSocket());
            }

            if(peer.getPeerStatus() == Status.UP){
                // Get the peers peer list
                String peerSocketsList = peerClient.getHealthySocketsList();

                if(!peerSocketsList.equals(cashedPeerSocketsList)){ // The peers peer list has changed, update this nodes peer list...
                    log.info("New peers have been detected from the following peer: [{}] !",peer.getPeerSocket());
                    peers.addSocketsList(peerSocketsList);
                    this.cashedPeerSocketsList = peerSocketsList;
                }

                // Compare the peers blockchain size against this nodes blockchain
                int chainSizeDiff = peerClient.getPeerBlockchainSize() - this.blockchain.getChain().size();

                if(chainSizeDiff == 0){ // The peers are in sync
                    log.debug("Peer [{}] is in sync with this node",peer.getPeerSocket());
                }
                if(chainSizeDiff == 1){ // The peers chain has the newest block, request it...
                    log.info("A new block was detected in the following peer: [{}] !",peer.getPeerSocket());
                    try {
                        this.blockchain.addBlock(peerClient.getPeerLastBlock());
                    } catch (InvalidObjectException e) { // A block in the blockchain is invalid
                        log.error("Could not add the new block from peer: [{}]. Reason: {}", peer.getPeerSocket(), e.getMessage());
                    }
                }
                if(chainSizeDiff > 1){ // This nodes blockchain is behind the peer, attempt to synchronise...
                    log.info("Attempting to synchronize with the following peer: [{}]", peer.getPeerSocket());
                    ArrayList<Block> chainResponse = peerClient.getPeerBlockchain();

                    try {
                        Blockchain incomingBlockchain = new Blockchain(chainResponse);
                        this.blockchain.replaceChain(incomingBlockchain);

                        log.info("Successfully synchronized blockchain with the following peer: [{}] !", peer.getPeerSocket());
                    } catch (InvalidObjectException e) {
                        // A block in the blockchain is invalid
                        log.error("Could not synchronize with the following peer: [{}]. Reason: {}", peer.getPeerSocket(), e.getMessage());
                    }
                }
                if(chainSizeDiff < 0){ // The peers blockchain is behind this nodes
                    log.debug("Peer [{}] is behind this node",peer.getPeerSocket());
                }
            }
        } catch (SocketTimeoutException | ConnectException e) {
            if(peer.getPeerStatus() != Status.DOWN){
                peer.setPeerStatus(Status.DOWN);
                log.info("Could not poll the following peer: [{}]. Reason: {}. Setting peer status to DOWN", peer.getPeerSocket(), e.getMessage());
            }
            log.debug("Could not poll the following peer: [{}]. Reason: {}. Peer status is: {}", peer.getPeerSocket(), e.getMessage(), peer.getPeerStatus());
        } catch (Exception e) {
            //TODO: test around this, different exceptions, what happens if non block json is returned, what if different response code
            e.printStackTrace();
        }
    }
}
