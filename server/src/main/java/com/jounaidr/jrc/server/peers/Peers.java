package com.jounaidr.jrc.server.peers;

import com.jounaidr.jrc.server.blockchain.Blockchain;
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

    private final Blockchain blockchain;
    private final String nodeSocket;
    private final int maxPeers;

    private int poolSize;
    private final ScheduledThreadPoolExecutor executor;

    private final ArrayList<Peer> peerList;

    public Peers(Blockchain blockchain, String nodeSocket, int maxPeers, String socketsList) {
        this.blockchain = blockchain;
        this.nodeSocket = nodeSocket;
        this.maxPeers = maxPeers;

        if(maxPeers > Runtime.getRuntime().availableProcessors()){
            log.warn("It is recommended to set the maximum peers to less than {} for your system, performance may be impacted...", Runtime.getRuntime().availableProcessors());
        }
        executor = new ScheduledThreadPoolExecutor(poolSize);

        this.peerList = new ArrayList<>();
        this.addSocketsList(socketsList);
    }

    public void addSocketsList(String socketsList){
        log.info("Attempting to add the following sockets [{}] to the peer list", socketsList);
        if(!StringUtils.isEmpty(socketsList)){
            // Split the socket list and add a peer for each individual socket
            for(String peerSocket : socketsList.split(",")){
                this.addPeer(peerSocket);
            }
        }
    }

    private void addPeer(String peerSocket){
        if(this.getPeerList().size() > maxPeers){
            log.error("Unable to add new peer [{}] as max peer size of {} has been reached", peerSocket, maxPeers);
            return;
        }
        if(peerSocket.equals(nodeSocket)){
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

        // Increase the thread pool size by one for the new peer
        // Since .getExecutor() is read locked, poolSize++ will also happen sequentially for each thread calling addPeer()
        this.getExecutor().setCorePoolSize(poolSize++);

        this.getPeerList().add(new Peer(blockchain, this, peerSocket));
        this.getPeerList().get(this.getPeerList().size() - 1).startPolling();
    }

    private boolean isSocketValid(String peerSocket){
        // TODO: Replace this with https://stackoverflow.com/questions/3114595/java-regex-for-accepting-a-valid-hostname-ipv4-or-ipv6-address
        // TODO: Split it into IP and Port sections and validate separately
        // TODO: Cus regex uglyyyy
        String ipV4Pattern = "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}):(\\d+)";
        String ipV6Pattern = "\\[([a-zA-Z0-9:]+)\\]:(\\d+)";

        Pattern validSocket = Pattern.compile( ipV4Pattern + "|" + ipV6Pattern);

        return validSocket.matcher(peerSocket).matches();
    }

    private boolean isPeerKnown(String peerSocket){
        for(Peer peer : this.getPeerList()){
            if(peer.getPeerSocket().equals(peerSocket)){
                return true;
            }
        }
        return false;
    }

    public ScheduledThreadPoolExecutor getExecutor() {
        //Read lock whilst getting executor as multiple peer threads can adjust the core pool size
        Lock readLock = rwLock.readLock();
        readLock.lock();

        try {
            return this.executor;
        } finally {
            readLock.unlock();
        }
    }

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
