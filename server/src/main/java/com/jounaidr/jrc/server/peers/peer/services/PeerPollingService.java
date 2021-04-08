package com.jounaidr.jrc.server.peers.peer.services;

import com.jounaidr.jrc.server.blockchain.Block;
import com.jounaidr.jrc.server.blockchain.Blockchain;
import com.jounaidr.jrc.server.peers.Peers;
import com.jounaidr.jrc.server.peers.peer.Peer;
import com.jounaidr.jrc.server.peers.peer.PeerClient;
import com.jounaidr.jrc.server.peers.peer.util.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.*;

@Slf4j
public class PeerPollingService implements Runnable{
    @Autowired
    private Blockchain blockchain; //The blockchain bean instance for this node, injected through spring
    @Autowired
    private Peers peers; //The peers bean instance for this node, injected through spring

    private final Peer peer;
    private final PeerClient peerClient;

    private final ScheduledThreadPoolExecutor peersExecutor;

    //TODO: SET TO 15 secs when deploying only 1sec for testing
    private static final long POLLING_FREQUENCY = 5000; //The delay between each poll in ms
    private String cashedPeerSocketsList;

    /**
     * Instantiates a new peer polling service for a given peer
     *
     * @param peer          the peer that this polling service will poll
     * @param peersExecutor the the peers thread pool executor
     */
    public PeerPollingService(Peer peer, ScheduledThreadPoolExecutor peersExecutor, Blockchain blockchain, Peers peers) {
        this.blockchain = blockchain;
        this.peers = peers;

        this.peer = peer;
        this.peerClient = new PeerClient(peer.getPeerSocket()); //Instantiate a new peer client from the peers socket

        this.peersExecutor = peersExecutor;
    }

    /**
     * Returns a random initial delay to be used when scheduling this runnable
     *
     * @return the random initial delay
     */
    private long getRandomInitialDelay(){
        //Random delay is calculated using a random value with the range specified by the polling frequency
        double delay = ThreadLocalRandom.current().nextDouble() * POLLING_FREQUENCY;

        return Double.valueOf(Math.ceil(delay)).longValue();
    }

    /**
     * Submit the polling task as a scheduled thread to the peers thread pool executor
     */
    public void start() {
        //TODO: add a static delay to the initial delay aswel so that beans are deffo initialised
        //Schedule the task specified in the run() method to run consecutively
        //With an initially random delay, and subsequent fixed delay defined in POLLING_FREQUENCY
        peersExecutor.scheduleAtFixedRate(this, this.getRandomInitialDelay(), POLLING_FREQUENCY, TimeUnit.MILLISECONDS);
    }

    /**
     * Gets the peers health status from its actuator endpoint
     * and sets its status variable for this polling services respective peer
     *
     * @throws IOException   connection exceptions
     * @throws JSONException json conversion exceptions
     */
    private void peerHealthCheck() throws IOException, JSONException {
        String peerHealth = peerClient.getPeerHealth();

        if(peer.getPeerStatus() != Status.UP){
            if(peerHealth.equals("UP")){
                peer.setPeerStatus(Status.UP);
                log.info("Connection reestablished with peer: [{}] ! Setting peer status to UP", peer.getPeerSocket());
            }
            else{
                peer.setPeerStatus(Status.UNKNOWN);
                log.info("Peer health check returned invalid response: {}. Setting peer [{}] status to UNKNOWN", peerClient.getPeerHealth(), peer.getPeerSocket());
            }
        }
    }

    /**
     * The PeerPollingService run task.
     *
     * First the peers health status is checked, and if healthy
     * proceed to get the peers sockets list if it has changed. Then compare
     * the peers blockchain size again this nodes local blockchain instance,
     * and depending on the difference either get the peers last block or
     * attempt to synchronise the whole blockchain if this node is behind the peer
     */
    @Override
    public void run() {
        log.debug("Attempting to poll peer: [{}]",peer.getPeerSocket());
        try {
            this.peerHealthCheck(); //First check the peers health status
            if(peer.getPeerStatus() == Status.UP){
                // Get the peers healthy peer list
                String peerSocketsList = peerClient.getHealthySocketsList();

                if(!peerSocketsList.equals(cashedPeerSocketsList)){ // The peers peer list has changed, update this nodes peer list...
                    log.info("New peers have been detected from the following peer: [{}] !",peer.getPeerSocket());
                    peers.addSocketsList(peerSocketsList);
                    this.cashedPeerSocketsList = peerSocketsList;
                }

                // Compare the peers blockchain size against this nodes blockchain size...
                int chainSizeDiff = peerClient.getPeerBlockchainSize() - this.blockchain.getChain().size();

                if(chainSizeDiff == 0){ // The peers are in sync as there is no difference in chain size
                    log.debug("Peer [{}] is in sync with this node",peer.getPeerSocket());
                }
                if(chainSizeDiff == 1){ // The peers chain is ahead of this node and has the newest block, request and add it...
                    log.info("A new block was detected in the following peer: [{}] !",peer.getPeerSocket());
                    try {
                        this.blockchain.addBlock(peerClient.getPeerLastBlock());
                    } catch (InvalidObjectException e) { // The new block is invalid...
                        log.error("Could not add the new block from peer: [{}]. Reason: {}", peer.getPeerSocket(), e.getMessage());
                    }
                }
                if(chainSizeDiff > 1){ // This nodes blockchain is behind the peer by more than one block, attempt to synchronise the blockchain...
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
        } catch (SocketTimeoutException | ConnectException e) { // Currently tested exceptions caused by a lack of connection
            if(peer.getPeerStatus() != Status.DOWN){
                peer.setPeerStatus(Status.DOWN);
                log.info("Could not poll the following peer: [{}]. Reason: {}. Setting peer status to DOWN", peer.getPeerSocket(), e.getMessage());
            }
            log.debug("Could not poll the following peer: [{}]. Reason: {}. Peer status is: {}", peer.getPeerSocket(), e.getMessage(), peer.getPeerStatus());
        } catch (Exception e) {
            //TODO: test around this, different exceptions, what happens if non block json is returned, what if different response code
            peer.setPeerStatus(Status.UNKNOWN);
            log.debug("Could not poll the following peer: [{}]. Reason: {}. Setting peer status to UNKNOWN", peer.getPeerSocket(), e.getMessage());
            e.printStackTrace();
        }
    }
}
