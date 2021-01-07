package com.jounaidr.jrc.server.peers;

import com.jounaidr.jrc.server.blockchain.Block;
import com.jounaidr.jrc.server.peers.peer.Peer;
import org.apache.commons.lang3.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

@Slf4j
public class Peers {
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    private final String NODE_SOCKET;
    private final int MAX_PEERS;

    private final ScheduledThreadPoolExecutor peersExecutor;
    private int poolSize;

    private final ArrayList<Peer> peerList;

    /**
     * Instantiates the peers service which contains a list of
     * all the nodes known peers, and methods to interact with
     * the peers
     *
     * @param nodeSocket  this nodes socket (used for validation checks)
     * @param maxPeers    the maximum peers for the system
     * @param socketsList initial list of peer sockets from properties
     */
    public Peers(String nodeSocket, int maxPeers, String socketsList) {
        this.NODE_SOCKET = nodeSocket; //This nodes socket
        this.MAX_PEERS = maxPeers; //Max number of peers from properties

        if(maxPeers > Runtime.getRuntime().availableProcessors()){
            //Check the amount of logical cores available to the JVM, since each peer will generate a new thread,
            //If there are more peers than logical cores available the peer thread scheduling will no longer be handled
            //By the peersExecutor and system performance could be impacted, however this would be minimal as peer threads
            //Are not very expensive, so only log a warning...
            log.warn("It is recommended to set the max peers to less than {} for your system, performance may be impacted...", Runtime.getRuntime().availableProcessors());
        }
        //Initialise the executor with a pool size of 1
        peersExecutor = new ScheduledThreadPoolExecutor(poolSize++);

        //Initialise the array of Peer.java objects, and add Peer's to the list for each socket provided in properties
        this.peerList = new ArrayList<>();
        this.addSocketsList(socketsList);
    }

    /**
     * Split a comma separated string of sockets
     * and instantiate and add a peer object for each socket
     * to the peerList
     *
     * @param socketsList the sockets list to be added
     */
    public void addSocketsList(String socketsList){
        log.info("Attempting to add the following sockets [{}] to the peer list", socketsList);
        if(!StringUtils.isEmpty(socketsList)){
            // Split the socket list and add a peer for each individual socket
            for(String peerSocket : socketsList.split(",")){
                this.addPeer(peerSocket);
            }
        }
    }

    /**
     * Run validation checks against the incoming peerSocket,
     * before instantiating and adding a Peer object to the peerList from it,
     * whilst also starting polling on that Peer object. The peers thread exectutors
     * pool size is also incremented for the newly added peer
     *
     * @param peerSocket the peer socket to add
     */
    private void addPeer(String peerSocket){
        //First do max peers and node socket checks as these are less expensive,
        //Than do valid socket and known peer checks...
        if(this.getPeerList().size() > MAX_PEERS){
            log.error("Unable to add new peer [{}] as max peer size of {} has been reached", peerSocket, MAX_PEERS);
            return;
        }
        if(peerSocket.equals(NODE_SOCKET)){
            log.error("Unable to add new peer [{}] as its socket refers to this node!", peerSocket);
            return;
        }
        if(!isSocketValid(peerSocket)){
            log.error("Unable to add new peer [{}] as its socket is of invalid format", peerSocket);
            return;
        }
        if(isPeerKnown(peerSocket)){
            log.info("Unable to add new peer [{}] as its already known", peerSocket);
            return;
        }

        //Increase the thread pool size by one for the new peer
        //Since .getExecutor() is read locked, poolSize++ will also happen sequentially for each thread calling addPeer()
        this.peersExecutor.setCorePoolSize(poolSize++);

        //Instantiate a new Peer object for the peerSocket and pass it the pool thread executor
        //Then start polling the peer
        this.getPeerList().add(new Peer(peerSocket, peersExecutor));
        log.debug("Starting polling with new peer [{}] ...", peerSocket);
        this.getPeerList().get(this.getPeerList().size() - 1).startPolling();
    }

    /**
     * Validate that the socket provided contains a valid IPV4
     * or IPV6 address and port. Note that hostnames are currently
     * not valid
     *
     * @param peerSocket the peer socket to validate
     */
    private boolean isSocketValid(String peerSocket){
        // TODO: Replace this with https://stackoverflow.com/questions/3114595/java-regex-for-accepting-a-valid-hostname-ipv4-or-ipv6-address
        // TODO: Split it into IP and Port sections and validate separately
        String ipV4Pattern = "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}):(\\d+)";
        String ipV6Pattern = "\\[([a-zA-Z0-9:]+)\\]:(\\d+)";

        Pattern validSocket = Pattern.compile( ipV4Pattern + "|" + ipV6Pattern);

        return validSocket.matcher(peerSocket).matches();
    }

    /**
     * Checks that a provided peerSocket does not already exist
     * in the peerList
     *
     * @param peerSocket the peer socket to check
     */
    private boolean isPeerKnown(String peerSocket){
        for(Peer peer : this.getPeerList()){
            if(peer.getPeerSocket().equals(peerSocket)){
                return true;
            }
        }
        return false;
    }

    /**
     * Broadcast a block to each peer in the peerList
     *
     * @param block the block to broadcast
     */
    public void broadcastBlockToPeers(Block block){
        for(Peer peer : this.getPeerList()){
            peer.broadcastBlock(block);
        }
    }

    /**
     * Getter for the nodes peerList with
     * a read lock for thread safety
     *
     * @return ArrayList<Peer> this nodes peerList
     */
    public ArrayList<Peer> getPeerList() {
        //Read lock whilst getting peerList as multiple peer threads can add a new peer
        Lock readLock = rwLock.readLock();
        readLock.lock();

        try {
            return peerList;
        } finally {
            readLock.unlock();
        }
    }
}
